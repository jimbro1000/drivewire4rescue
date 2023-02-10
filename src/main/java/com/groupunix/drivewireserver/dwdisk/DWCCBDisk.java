package com.groupunix.drivewireserver.dwdisk;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.IOException;

public class DWCCBDisk extends DWDisk {
  /**
   * Log appender.
   */
  private static final Logger LOGGER = Logger.getLogger("DWServer.DWCCBDisk");
  /**
   * Minimum file size for CCB disk type.
   */
  private static final long CCB_FILE_SIZE_MIN = 3;
  /**
   * CCB Disk format identifier.
   */
  private static final byte[] CCB_INDENTIFIER
      = {(byte) 99, (byte) 99, (byte) 98};

  /**
   * CCB Disk Constructor.
   *
   * @param fileObj file object
   * @throws IOException read/write failure
   * @throws DWImageFormatException invalid image format
   */
  public DWCCBDisk(final FileObject fileObj)
      throws IOException, DWImageFormatException {
    super(fileObj);
    this.setParam("_format", "ccb");
    load();
    LOGGER.debug("New CCB disk for " + fileObj.getName().getURI());
  }

  /**
   * Examine file header to see if it could be an image.
   *
   * @param hdr            header bytes
   * @param fileObjectSize file size
   * @return examination outcome
   */
  public static int considerImage(final byte[] hdr, final long fileObjectSize) {
    // is it big enough to have a header
    if (fileObjectSize >= DWCCBDisk.CCB_FILE_SIZE_MIN
        && compareByteArray(hdr, CCB_INDENTIFIER)) {
      // has it been isaved...
      if (fileObjectSize % DWDefs.DISK_SECTORSIZE == 0) {
        return DWDefs.DISK_CONSIDER_YES;
      }
      return DWDefs.DISK_CONSIDER_MAYBE;
    }
    return DWDefs.DISK_CONSIDER_NO;
  }

  /**
   * Get disk format.
   *
   * @return disk format
   */
  public int getDiskFormat() {
    return DWDefs.DISK_FORMAT_CCB;
  }

  /**
   * Synchronise disk.
   */
  @Override
  public void sync() {
    // no operation
  }

  /**
   * Load file into sector array.
   *
   * @throws IOException read failure
   * @throws DWImageFormatException invalid image format
   */
  public void load() throws IOException, DWImageFormatException {
    final long fObjSize = this.getFileObject().getContent().getSize();
    if (fObjSize > Integer.MAX_VALUE) {
      throw new DWImageFormatException("Image is too large.");
    }
    this.getSectors().setSize((int) (fObjSize / DWDefs.DISK_SECTORSIZE) + 1);
    byte[] buf = new byte[DWDefs.DISK_SECTORSIZE];
    int readres = 0;
    int secres = 0;
    int sec = 0;
    final InputStream fis
        = this.getFileObject().getContent().getInputStream();
    while (readres > -1) {
      final int size = (int) Math.min(
          DWDefs.DISK_SECTORSIZE - secres,
          fObjSize - ((long) sec * DWDefs.DISK_SECTORSIZE)
      );
      readres = fis.read(buf, readres, size);
      // ccb scripts may not be /256
      if (readres == -1) {
        this.getSectors().set(
            sec,
            new DWDiskSector(
                this,
                sec,
                DWDefs.DISK_SECTORSIZE,
                false
            )
        );
        for (int i = secres; i < DWDefs.DISK_SECTORSIZE; i++) {
          buf[i] = 0;
        }
        this.getSectors().get(sec).setData(buf, false);
        sec++;
      } else {
        secres += readres;
        readres = 0;
        if (secres == DWDefs.DISK_SECTORSIZE) {
          this.getSectors().set(
              sec,
              new DWDiskSector(
                  this,
                  sec,
                  DWDefs.DISK_SECTORSIZE,
                  false
              )
          );
          this.getSectors().get(sec).setData(buf, false);
          secres = 0;
          sec++;
        }
      }
    }
    this.setParam("_sectors", sec);
    this.setParam(
        "_filesystem",
        DWUtils.prettyFileSystem(DWDefs.DISK_FILESYSTEM_CCB)
    );
    fis.close();
  }

  /**
   * Seek disk sector.
   *
   * @param newLSN logical sector number
   * @throws DWInvalidSectorException invalid sector
   * @throws DWSeekPastEndOfDeviceException attempt to seek past end of disk
   */
  public void seekSector(final int newLSN)
      throws DWInvalidSectorException, DWSeekPastEndOfDeviceException {
    if (newLSN < 0) {
      throw new DWInvalidSectorException("Sector " + newLSN + " is not valid");
    } else if (newLSN > (this.getSectors().size() - 1)) {
      throw new DWSeekPastEndOfDeviceException(
          "Attempt to seek beyond end of image"
      );
    } else {
      this.setParam("_lsn", newLSN);
    }
  }

  /**
   * Write disk sector.
   *
   * @param data byte array of new sector content
   * @throws DWDriveWriteProtectedException failed to write to protected disk
   * @throws IOException write failure
   */
  public void writeSector(final byte[] data)
      throws DWDriveWriteProtectedException, IOException {
    if (this.isWriteProtect()) {
      throw new DWDriveWriteProtectedException("Disk is write protected");
    } else {
      this.getSectors().get(this.getLSN()).setData(data);
      this.incParam("_writes");
    }
  }

  /**
   * Read disk sector.
   *
   * @return byte array of sector content
   * @throws IOException read failure
   */
  public byte[] readSector() throws IOException {
    this.incParam("_reads");
    return this.getSectors().get(this.getLSN()).getData();
  }
}
