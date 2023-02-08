package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWJVCDisk extends DWDisk {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWJVCDisk");
  /**
   * Expected JVC sector size.
   */
  public static final int SECTOR_SIZE = 256;
  /**
   * Disk image header.
   */
  private DWJVCDiskHeader header;

  /**
   * JVC Disk constructor.
   *
   * @param fileObject source file object
   * @throws IOException
   * @throws DWImageFormatException
   */
  public DWJVCDisk(final FileObject fileObject)
      throws IOException, DWImageFormatException {
    super(fileObject);
    this.setParam("_format", "jvc");
    load();
    LOGGER.debug("New JVC disk for " + fileObject.getName().getURI());
  }

  /**
   * Evaluate file image header to see if it is a disk image.
   *
   * @param hdr      header byte array
   * @param fObjSize file object size
   * @return decision on identification
   */
  public static int considerImage(final byte[] hdr, final long fObjSize) {
    DWJVCDiskHeader header = new DWJVCDiskHeader();
    int headerLen = (int) (fObjSize % BYTE_SHIFT);
    // only consider jvc with header
    if (headerLen > 0) {
      byte[] buf = new byte[headerLen];
      System.arraycopy(hdr, 0, buf, 0, headerLen);
      header.setData(buf);
      if (
          (header.getSectorAttributes() == 0)
          && (header.getSectorSize() == SECTOR_SIZE)
          && (fObjSize >= header.getSectorSize())
          && (header.getSides() > 0)
          && (fObjSize - headerLen > 0)
          && (header.getSectorsPerTrack() > 0)
          && (header.getSectorSize() > 0)
          && (fObjSize - headerLen) % header.getSectorSize() == 0
      ) {
        return DWDefs.DISK_CONSIDER_MAYBE;
      }
    }
    return DWDefs.DISK_CONSIDER_NO;
  }

  /**
   * Get disk format.
   *
   * @return disk format (JVC)
   */
  public int getDiskFormat() {
    return DWDefs.DISK_FORMAT_JVC;
  }

  /**
   * Synchronise disk.
   */
  @Override
  void sync() {
    // no operation
  }

  /**
   * Load file into sector array.
   *
   * @throws IOException
   * @throws DWImageFormatException
   */
  public void load() throws IOException, DWImageFormatException {
    InputStream fis;
    fis = this.getFileObject().getContent().getInputStream();
    this.header = new DWJVCDiskHeader();
    int filelen = (int) this.getFileObject().getContent().getSize();
    int headerlen = (filelen % BYTE_SHIFT);
    if (headerlen > 0) {
      int readres = 0;
      byte[] buf = new byte[headerlen];
      while (readres < headerlen) {
        readres += fis.read(buf, readres, headerlen - readres);
      }
      this.header.setData(buf);
    }
    if (this.header.getSectorAttributes() > 0) {
      throw new DWImageFormatException(
          "JVC with sector attributes not supported"
      );
    }
    this.setParam("_secpertrack", header.getSectorsPerTrack());
    this.setParam("_sides", header.getSides());
    this.setParam("_sectorsize", header.getSectorSize());
    this.setParam("_firstsector", header.getFirstSector());
    int tracks = (filelen - headerlen)
        / (header.getSectorsPerTrack() * (header.getSectorSize()))
        / header.getSides();
    this.setParam("_tracks", tracks);
    this.setParam(
        "_sectors",
        tracks * header.getSides() * header.getSectorsPerTrack()
    );
    this.getSectors().setSize(this.getParams().getInt("_sectors"));
    byte[] buf = new byte[header.getSectorSize()];
    int readres;
    for (int i = 0; i < this.getParams().getInt("_sectors"); i++) {
      readres = 0;
      while (readres < header.getSectorSize()) {
        readres += fis.read(buf, readres, header.getSectorSize() - readres);
      }
      this.getSectors().set(i, new DWDiskSector(
          this,
          i,
          header.getSectorSize(),
          false
      ));
      this.getSectors().get(i).setData(buf, false);
    }
    fis.close();
    this.setParam(
        "_filesystem",
        DWUtils.prettyFileSystem(DWDiskDrives.getDiskFSType(this.getSectors()))
    );
  }

  /**
   * Seek sector by LSN.
   *
   * @param newLSN logical sector number
   * @throws DWInvalidSectorException
   * @throws DWSeekPastEndOfDeviceException
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
   * Write sector.
   *
   * @param data byte array
   * @throws DWDriveWriteProtectedException
   * @throws IOException
   */
  public void writeSector(final byte[] data)
      throws DWDriveWriteProtectedException, IOException {
    if (this.getWriteProtect()) {
      throw new DWDriveWriteProtectedException("Disk is write protected");
    } else {
      this.getSectors().get(this.getLSN()).setData(data);
      this.incParam("_writes");
    }
  }

  /**
   * Read disk sector.
   *
   * @return sector data byte array
   * @throws IOException
   */
  public byte[] readSector() throws IOException {
    this.incParam("_reads");
    return this.getSectors().get(this.getLSN()).getData();
  }
}
