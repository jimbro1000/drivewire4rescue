package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWSIDEImageDisk extends DWDisk {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWSIDEImageDisk");
  /**
   * Sector size (bytes).
   */
  private static final int SECTOR_SIZE = 256;
  /**
   * Start position.
   */
  private final long startPos;
  /**
   * End position.
   */
  private final long endPos;
  /**
   * Use half sectors flag.
   */
  @SuppressWarnings("unused")
  private final boolean halfsector;

  /**
   * SIDE Image disk constructor.
   *
   * @param fileObject source file object
   * @param start start position
   * @param end end position
   * @param halfSector half sector flag
   * @throws IOException
   * @throws DWImageFormatException
   */
  public DWSIDEImageDisk(
      final FileObject fileObject,
      final long start,
      final long end,
      final boolean halfSector
  ) throws IOException, DWImageFormatException {
    super(fileObject);
    this.setParam("_format", "side");
    this.startPos = start;
    this.endPos = end;
    this.halfsector = halfSector;
    load();
    LOGGER.debug(
        "New SuperIDE image disk for " + fileObject.getName().getURI()
            + " (start: " + start
            + " end: " + end
            + " halfsectors: " + halfSector + ")"
    );
  }

  /**
   * Seek sector by LSN.
   * <p>
   *   Not implemented
   * </p>
   * @param lsn logical sector number
   * @throws DWInvalidSectorException
   * @throws DWSeekPastEndOfDeviceException
   */
  @Override
  public void seekSector(final int lsn)
      throws DWInvalidSectorException, DWSeekPastEndOfDeviceException {
  }

  /**
   * Write data to sector.
   * <p>
   *   not implemented
   * </p>
   * @param data byte array
   * @throws DWDriveWriteProtectedException
   * @throws IOException
   */
  @Override
  public void writeSector(final byte[] data)
      throws DWDriveWriteProtectedException, IOException {
  }

  /**
   * Read sector.
   *
   * @return null (not implemented)
   * @throws IOException
   * @throws DWImageFormatException
   */
  @Override
  public byte[] readSector() throws IOException, DWImageFormatException {
    return null;
  }

  /**
   * Load sector array from file.
   *
   * @throws IOException
   * @throws DWImageFormatException
   */
  @Override
  protected void load() throws IOException, DWImageFormatException {
    // load file into sector array
    int sector = 0;
    long filesize = this.endPos - this.startPos;
    if ((filesize > Integer.MAX_VALUE)) {
      throw new DWImageFormatException("Image file is too large");
    }
    int sz = 0;
    this.getSectors().setSize((int) (filesize / SECTOR_SIZE));
    while (sz < filesize) {
      this.getSectors().set(
          sector,
          new DWDiskSector(this, sector, SECTOR_SIZE, true)
      );
      sector++;
      sz += SECTOR_SIZE;
    }
    long lastmodtime = -1;
    try {
      lastmodtime = this.getFileObject().getContent().getLastModifiedTime();
    } catch (FileSystemException e) {
      LOGGER.warn(e.getMessage());
    }
    this.setLastModifiedTime(lastmodtime);
    this.setParam("_sectors", sector);
    this.setParam("_filesystem", DWUtils.prettyFileSystem(
        DWDiskDrives.getDiskFSType(this.getSectors()))
    );
  }

  /**
   * Get disk format.
   *
   * @return disk format (SIDE)
   */
  @Override
  public int getDiskFormat() {
    return DWDefs.DISK_FORMAT_SIDE;
  }
}
