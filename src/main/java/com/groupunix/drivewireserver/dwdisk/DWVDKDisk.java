package com.groupunix.drivewireserver.dwdisk;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public final class DWVDKDisk extends DWDisk {

  // per the docs
  /**
   * Minimum header size (bytes).
   */
  public static final int VDK_HEADER_SIZE_MIN = 12;
  /**
   * Maximum header size (bytes).
   */
  public static final int VDK_HEADER_SIZE_MAX = 256;
  // per my guesses based on what docs I could find, these are fixed?
  /**
   * Sector size (bytes).
   */
  public static final int VDK_SECTOR_SIZE = 256;
  /**
   * Sectors per track.
   */
  public static final int VDK_SECTORS_PER_TRACK = 18;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVDKDisk");
  /**
   * Length of buffer describing signature and header length (bytes).
   * <p>
   *   First two bytes are signature, second two are header length
   * </p>
   */
  private static final int HEADER_BUFFER_LEN = 4;
  /**
   * Byte offset of signature.
   */
  private static final int HEADER_SIG_OFFSET = 0;
  /**
   * Byte offset of header length.
   */
  private static final int HEADER_LEN_OFFSET = 2;
  /**
   * Bitmask for least significant 8 bits of int.
   */
  private static final int BYTE_MASK = 0xFF;
  /**
   * Byte multiplier for most significant byte in word.
   */
  private static final int MSB_SHIFT = 256;

  /**
   * VDK Disk constructor.
   *
   * @param fileObj source file object
   * @throws IOException read/write failure
   * @throws DWImageFormatException invalid image format
   */
  public DWVDKDisk(final FileObject fileObj)
      throws IOException, DWImageFormatException {
    super(fileObj);
    this.setParam("_format", "vdk");
    load();
    LOGGER.debug("New VDK disk for " + fileObj.getName().getURI());
  }

  private static DWVDKDiskHeader readHeader(final InputStream fis)
      throws IOException, DWImageFormatException {
    // read sig and hdr length
    int readres = 0;
    byte[] hbuff1 = new byte[HEADER_BUFFER_LEN];

    while (readres < HEADER_BUFFER_LEN) {
      readres += fis.read(hbuff1, readres, HEADER_BUFFER_LEN - readres);
    }
    // size and sanity
    int headerLen = getHeaderLen(hbuff1);
    // make complete header buffer
    byte[] hbuff2 = new byte[headerLen];
    System.arraycopy(hbuff1, 0, hbuff2, 0, HEADER_BUFFER_LEN);
    while (readres < headerLen) {
      readres += fis.read(hbuff2, readres, headerLen - readres);
    }
    return (new DWVDKDiskHeader(hbuff2));
  }

  private static int getHeaderLen(final byte[] hBuffer)
      throws DWImageFormatException {
    // check sanity
    if (hBuffer.length < HEADER_BUFFER_LEN) {
      throw new DWImageFormatException(
          "Invalid VDK header: too short to read size bytes"
      );
    }

    // check signature
    if (
        ((BYTE_MASK & hBuffer[HEADER_SIG_OFFSET]) != 'd')
            || ((BYTE_MASK & hBuffer[HEADER_SIG_OFFSET + 1]) != 'k')
    ) {
      throw new DWImageFormatException(
          "Invalid VDK header: " + hBuffer[0] + " " + hBuffer[1]
      );
    }

    // check header length
    int len = (BYTE_MASK & hBuffer[HEADER_LEN_OFFSET])
        + ((BYTE_MASK & hBuffer[HEADER_LEN_OFFSET + 1]) * MSB_SHIFT);

    if (len > DWVDKDisk.VDK_HEADER_SIZE_MAX) {
      throw new DWImageFormatException(
          "Invalid VDK header: too big for sanity"
      );
    }
    return len;
  }

  /**
   * Analyse image to determine format.
   *
   * @param hdr      image header byte array.
   * @param fObjSize file object size
   * @return analysis outcome
   */
  public static int considerImage(final byte[] hdr, final long fObjSize) {
    // is it big enough to have a header
    if (fObjSize >= DWVDKDisk.VDK_HEADER_SIZE_MIN) {
      int hdrlen;
      // getheaderlen checks for sanity
      try {
        hdrlen = getHeaderLen(hdr);
      } catch (DWImageFormatException e) {
        return (DWDefs.DISK_CONSIDER_NO);
      }
      // make proper sized buffer for hdr
      byte[] buf = new byte[hdrlen];
      System.arraycopy(hdr, 0, buf, 0, hdrlen);

      DWVDKDiskHeader header = new DWVDKDiskHeader(buf);
      // is the size right?
      if (
          fObjSize
              == header.getHeaderLen()
              + (
              (long) header.getSides()
                  * header.getTracks()
                  * DWVDKDisk.VDK_SECTOR_SIZE
                  * DWVDKDisk.VDK_SECTORS_PER_TRACK
          )
      ) {
        return DWDefs.DISK_CONSIDER_YES;
      }
    }
    return DWDefs.DISK_CONSIDER_NO;
  }

  /**
   * Get disk format.
   *
   * @return VDK Format
   */
  public int getDiskFormat() {
    return DWDefs.DISK_FORMAT_VDK;
  }

  /**
   * Load file as disk.
   *
   * @throws IOException read failure
   * @throws DWImageFormatException invalid image format
   */
  public void load() throws IOException, DWImageFormatException {
    // load file into sector array
    InputStream fis;

    fis = this.getFileObject().getContent().getInputStream();

    this.setLastModifiedTime(
        this.getFileObject().getContent().getLastModifiedTime()
    );

    // read disk header
    DWVDKDiskHeader header = readHeader(fis);
    this.setParam("writeprotect", header.isWriteProtected());
    this.setParam("_tracks", header.getTracks());
    this.setParam("_sides", header.getSides());
    this.setParam(
        "_sectors",
        header.getTracks()
            * header.getSides()
            * DWVDKDisk.VDK_SECTORS_PER_TRACK
    );

    if (
        this.getFileObject().getContent().getSize()
            != ((long) this.getParams().getInt("_sectors")
            * DWVDKDisk.VDK_SECTOR_SIZE
            + header.getHeaderLen())
    ) {
      throw new DWImageFormatException("Invalid VDK image, wrong file size");
    }

    this.getSectors().setSize(this.getParams().getInt("_sectors"));

    byte[] buf = new byte[DWVDKDisk.VDK_SECTOR_SIZE];
    int readres;

    for (int i = 0; i < this.getParams().getInt("_sectors"); i++) {
      readres = 0;
      while (readres < DWVDKDisk.VDK_SECTOR_SIZE) {
        readres += fis.read(
            buf,
            readres,
            DWVDKDisk.VDK_SECTOR_SIZE - readres
        );
      }
      this.getSectors().set(
          i,
          new DWDiskSector(this, i, DWVDKDisk.VDK_SECTOR_SIZE, false)
      );
      this.getSectors().get(i).setData(buf, false);
    }
    fis.close();
    this.setParam(
        "_filesystem",
        DWUtils.prettyFileSystem(DWDiskDrives.getDiskFSType(this.getSectors()))
    );
  }

  /**
   * Set given sector active.
   *
   * @param newLSN logical sector number
   * @throws DWInvalidSectorException invalid sector
   * @throws DWSeekPastEndOfDeviceException attempt to seek past end of disk
   */
  public void seekSector(final int newLSN)
      throws DWInvalidSectorException, DWSeekPastEndOfDeviceException {
    if (newLSN < 0) {
      throw new DWInvalidSectorException(
          "Sector " + newLSN + " is not valid"
      );
    } else if (newLSN > (this.getSectors().size() - 1)) {
      throw new DWSeekPastEndOfDeviceException(
          "Attempt to seek beyond end of image"
      );
    } else {
      this.setParam("_lsn", newLSN);
    }
  }

  /**
   * Synchronise disk.
   */
  @Override
  public void sync() {
    // no operation
  }

  /**
   * Write data to active sector.
   *
   * @param data byte array
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
   * Read active disk sector.
   *
   * @return sector data
   * @throws IOException read failure
   */
  public byte[] readSector() throws IOException {
    this.incParam("_reads");
    return this.getSectors().get(this.getLSN()).getData();
  }
}
