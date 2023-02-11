package com.groupunix.drivewireserver.dwdisk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWImageHasNoSourceException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;
import static com.groupunix.drivewireserver.DWDefs.KILOBYTE;

public class DWRawDisk extends DWDisk {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWRawDisk");
  /**
   * Minimum file object size.
   */
  public static final int MIN_OBJECT_SIZE = 3;

  /**
   * Direct flag.
   */
  private boolean direct = false;

  /**
   * Raw Disk Constructor.
   *
   * @param fileObject file object
   * @param sectorSize sector size
   * @param maxSectors max sectors
   * @throws IOException
   * @throws DWImageFormatException
   */
  public DWRawDisk(
      final FileObject fileObject,
      final int sectorSize,
      final int maxSectors
  ) throws IOException, DWImageFormatException {
    super(fileObject);
    commonConstructor(false, sectorSize, maxSectors);
  }

  /**
   * Raw disk constructor.
   *
   * @param fileObject file object
   * @param sectorSize sector size
   * @param maxSectors max sectors
   * @param forceCache force cache
   * @throws IOException
   * @throws DWImageFormatException
   */
  public DWRawDisk(
      final FileObject fileObject,
      final int sectorSize,
      final int maxSectors,
      final boolean forceCache
  ) throws IOException, DWImageFormatException {
    super(fileObject);
    commonConstructor(forceCache, sectorSize, maxSectors);
  }

  /**
   * Raw disk constructor.
   *
   * @param sectorSize sector size
   * @param maxSectors max sectors
   */
  public DWRawDisk(final int sectorSize, final int maxSectors) {
    super();
    setDefaultOptions(sectorSize, maxSectors);
    LOGGER.debug("New DWRawDisk (in memory only)");
  }

  /**
   * Raw disk constructor.
   *
   * @param sectors disk sector vectors
   */
  public DWRawDisk(final Vector<DWDiskSector> sectors) {
    // used only for temp objs...
    super();
    this.setSectors(sectors);
  }

  /**
   * Consider disk image.
   *
   * @param header header bytes
   * @param fObjSize file object size
   * @return decision on disk image
   */
  public static int considerImage(final byte[] header, final long fObjSize) {
    // is it right size for raw sectors
    if (fObjSize % DWDefs.DISK_SECTORSIZE == 0) {
      // is it an os9 filesystem
      if (fObjSize > MIN_OBJECT_SIZE
          && (fObjSize == ((BYTE_MASK & header[0]) * BYTE_SHIFT * BYTE_SHIFT
            + (BYTE_MASK & header[1]) * BYTE_SHIFT
            + (BYTE_MASK & header[2])) * BYTE_SHIFT)
      ) {
        // exact match, lets claim it
        return DWDefs.DISK_CONSIDER_YES;
      }
      // not os9 so can't be sure?
      return DWDefs.DISK_CONSIDER_MAYBE;
    }
    // not /256
    return DWDefs.DISK_CONSIDER_NO;
  }

  private void commonConstructor(
      final boolean forceCache,
      final int sectorSize,
      final int maxSectors
  ) throws IOException, DWImageFormatException {
    // expose user options
    this.setParam("syncfrom", DWDefs.DISK_DEFAULT_SYNCFROM);
    this.setParam("syncto", DWDefs.DISK_DEFAULT_SYNCTO);
    setDefaultOptions(sectorSize, maxSectors);
    load(forceCache);
    LOGGER.debug("New DWRawDisk for '" + this.getFilePath() + "'");
  }

  /**
   * Set default options.
   *
   * @param sectorSize default sector size
   * @param maxSectors default maximum sectors
   */
  private void setDefaultOptions(final int sectorSize, final int maxSectors) {
    // set internal info
    this.setParam("_sectorsize", sectorSize);
    this.setParam("_maxsectors", maxSectors);
    this.setParam("_format", "raw");

    // expose user options
    this.setParam("offset", DWDefs.DISK_DEFAULT_OFFSET);
    this.setParam("offsetdrv", 0);
    this.setParam("sizelimit", DWDefs.DISK_DEFAULT_SIZELIMIT);
    this.setParam("expand", DWDefs.DISK_DEFAULT_EXPAND);
  }

  /**
   * Get disk format.
   *
   * @return disk format (raw)
   */
  public int getDiskFormat() {
    return DWDefs.DISK_FORMAT_RAW;
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
    if (newLSN < 0 || newLSN > this.getMaxSectors()) {
      throw new DWInvalidSectorException("Sector " + newLSN + " is not valid");
    } else if (
        newLSN >= this.getDiskSectors()
            && !this.getParams().
            getBoolean("expand", DWDefs.DISK_DEFAULT_EXPAND)
    ) {
      throw new DWSeekPastEndOfDeviceException(
          "Sector " + newLSN
              + " is beyond end of file, and expansion is not allowed"
      );
    } else if (this.getSizeLimit() > -1 && newLSN >= this.getSizeLimit()) {
      throw new DWSeekPastEndOfDeviceException(
          "Sector " + newLSN + " is beyond specified sector size limit"
      );
    } else {
      this.setParam("_lsn", newLSN);
    }
  }

  /**
   * Load file into sector array.
   *
   * @throws IOException
   * @throws DWImageFormatException
   */
  public void load() throws IOException, DWImageFormatException {
    this.load(false);
  }

  /**
   * Load file into sector array.
   *
   * @param forceCache force cache flag
   * @throws IOException
   * @throws DWImageFormatException
   */
  public void load(final boolean forceCache)
      throws IOException, DWImageFormatException {
    // load file into sector array
    final long filesize = this.getFileObject().getContent().getSize();
    if (filesize > Integer.MAX_VALUE
        || filesize / this.getSectorSize() > DWDefs.DISK_MAXSECTORS) {
      throw new DWImageFormatException("Image file is too large");
    }
    if (!forceCache
        && this.getFileObject().getName().toString().startsWith("file://")) {
      this.direct = true;
    }
    final int sectorsize = this.getSectorSize();
    int sector = 0;
    if (direct) {
      int size = 0;
      this.getSectors().setSize((int) (filesize / sectorsize));
      while (size < filesize) {
        this.getSectors().set(
            sector,
            new DWDiskSector(this, sector, sectorsize, true)
        );
        sector++;
        size += sectorsize;
      }
    } else {
      LOGGER.debug("Caching " + this.getFileObject().getName() + " in memory");
      final long memFree = Runtime.getRuntime().maxMemory()
          - (Runtime.getRuntime().totalMemory()
          - Runtime.getRuntime().freeMemory());
      if (filesize > memFree) {
        throw new DWImageFormatException(
            "Image file will not fit in memory ("
                + (memFree / KILOBYTE) + " Kbytes free)"
        );
      }

      final BufferedInputStream inputStream = new BufferedInputStream(
          this.getFileObject().getContent().getInputStream()
      );

      int readres = 0;
      int bytesRead = 0;
      final byte[] buffer = new byte[sectorsize];
      this.getSectors().setSize((int) (filesize / sectorsize));
      readres = inputStream.read(buffer, 0, sectorsize);
      while (readres > -1) {
        bytesRead += readres;
        if (bytesRead == sectorsize) {
          this.getSectors().set(sector, new DWDiskSector(
              this, sector, sectorsize, false)
          );
          this.getSectors().get(sector).setData(buffer, false);
          sector++;
          bytesRead = 0;
        }
        readres = inputStream.read(buffer, bytesRead, sectorsize - bytesRead);
      }
      if (bytesRead > 0) {
        throw new DWImageFormatException(
            "Incomplete sector data on sector " + sector
        );
      }
      LOGGER.debug(
          "read " + sector
              + " sectors from '" + this.getFileObject().getName() + "'"
      );
      inputStream.close();
    }
    long lastmodtime = -1;
    try {
      lastmodtime = this.getFileObject().getContent().getLastModifiedTime();
    } catch (FileSystemException e) {
      LOGGER.warn(e.getMessage());
    }
    this.setLastModifiedTime(lastmodtime);
    this.setParam("_sectors", sector);
    this.setParam(
        "_filesystem",
        DWUtils.prettyFileSystem(DWDiskDrives.getDiskFSType(this.getSectors()))
    );
  }

  /**
   * Read sector to byte array.
   *
   * @return sector bytes
   * @throws IOException read failure
   * @throws DWImageFormatException image format exception
   */
  public byte[] readSector() throws IOException, DWImageFormatException {
    this.incParam("_reads");
    // check source for changes...
    if (this.isSyncFrom()
        && this.getFileObject() != null
        && this.getFileObject()
        .getContent()
        .getLastModifiedTime() != this.getLastModifiedTime()
        && this.getDirtySectors() > 0
    ) {
      // doh
      LOGGER.warn(
          "Sync conflict on " + getFilePath()
              + ", both the source and our cached image have changed.  "
              + "Source will be overwritten!"
      );
      try {
        this.write();
      } catch (DWImageHasNoSourceException ignored) {
      }
    } else {
      LOGGER.info(
          "Disk source " + getFilePath() + " has changed, reloading"
      );
      this.reload();
    }
    final int effLSN = this.getLSN() + this.getOffset();
    // we can read beyond the current size of the image
    if (
        effLSN >= this.getSectors().size()
            || this.getSectors().get(effLSN) == null
    ) {
      LOGGER.debug(
          "request for undefined sector, effLSN: " + effLSN
              + "  rawLSN: " + this.getLSN()
              + "  curSize: " + (this.getSectors().size() - 1)
      );
      // no need to expand disk on read, give a blank sector
      return new byte[this.getSectorSize()];
    }
    return this.getSectors().get(effLSN).getData();
  }

  /**
   * Expand disk to target.
   *
   * @param target sector count
   */
  private void expandDisk(final int target) {
    this.getSectors().setSize(target);
    this.setParam("_sectors", target + 1);
  }

  /**
   * Write sector data.
   *
   * @param data byte array
   * @throws DWDriveWriteProtectedException write attempt to protected disk
   * @throws IOException write failure
   */
  public void writeSector(final byte[] data)
      throws DWDriveWriteProtectedException, IOException {

    if (this.isWriteProtect()) {
      throw new DWDriveWriteProtectedException("Disk is write protected");
    } else {
      final int effLSN = this.getLSN() + this.getOffset();

      // we can write beyond our current size
      if (effLSN >= this.getSectors().size()) {
        // expand disk / add sector
        expandDisk(effLSN);
        this.getSectors().add(
            effLSN, new DWDiskSector(this, effLSN, this.getSectorSize(), false)
        );
      }

      // jit sector maker
      if (this.getSectors().get(effLSN) == null) {
        this.getSectors().set(
            effLSN, new DWDiskSector(this, effLSN, this.getSectorSize(), false)
        );
      }
      this.getSectors().get(effLSN).setData(data);
      this.incParam("_writes");
    }
  }

  /**
   * Write disk to source image.
   *
   * @throws IOException
   * @throws DWImageHasNoSourceException
   */
  public void write() throws IOException, DWImageHasNoSourceException {
    if (this.getFileObject() == null) {
      throw
          new DWImageHasNoSourceException(
              "The image has no source object, must specify write path."
          );
    }
    if (this.getFileObject().isWriteable()) {
      if (
          this.getFileObject()
              .getFileSystem()
              .hasCapability(Capability.RANDOM_ACCESS_WRITE)
      ) {
        // we can sync individual sectors
        syncSectors();
      } else if (
          this.getFileObject()
              .getFileSystem()
              .hasCapability(Capability.WRITE_CONTENT)
      ) {
        // we must rewrite the entire object
        writeSectors(this.getFileObject());
      } else {
        // no way to write to this filesystem
        throw new FileSystemException("Filesystem is unwriteable");
      }
    } else {
      throw new FileSystemException("File is unwriteable");
    }
  }

  /**
   * Sync sectors to source.
   */
  private void syncSectors() {
    long sectorswritten = 0;
    final long starttime = System.currentTimeMillis();
    long sleeptime = 0;

    try {
      final RandomAccessContent raf = getFileObject()
          .getContent()
          .getRandomAccessContent(RandomAccessMode.READWRITE);

      for (int i = 0; i < this.getSectors().size(); i++) {
        if (getSector(i) != null && getSector(i).isDirty()) {
          if (
              this.getDrive().getDiskDrives().getDWProtocolHandler().isInOp()
          ) {
            try {
              final long sleepStart = System.currentTimeMillis();
              Thread.sleep(DWDefs.DISK_SYNC_INOP_PAUSE);
              sleeptime += System.currentTimeMillis() - sleepStart;
            } catch (InterruptedException e) {
              //  this would be weird..
              e.printStackTrace();
            }
          }
          final long pos = (long) i * this.getSectorSize();
          raf.seek(pos);
          raf.write(getSector(i).getData());
          sectorswritten++;
          getSector(i).makeClean();
        }
      }
      raf.close();
      this.getFileObject().close();
      this.setLastModifiedTime(
          this.getFileObject().getContent().getLastModifiedTime()
      );
    } catch (IOException | DWDiskInvalidSectorNumber e) {
      LOGGER.error(
          "Error writing sectors in "
              + this.getFilePath() + ": " + e.getMessage()
      );
    }

    if (sectorswritten > 0) {
      LOGGER.debug(
          "wrote " + sectorswritten
              + " sectors in " + (System.currentTimeMillis() - starttime)
              + " ms (" + sleeptime + "ms sleep), to " + getFilePath()
      );
    }
  }

  /**
   * Get maximum sector.
   *
   * @return max sectors
   */
  private int getMaxSectors() {
    return this.getParams().getInt("_maxsectors", DWDefs.DISK_MAXSECTORS);
  }

  /**
   * Get sector size.
   *
   * @return disk sector size
   */
  private int getSectorSize() {
    return this.getParams()
        .getInt("_sectorsize", DWDefs.DISK_SECTORSIZE);
  }

  /**
   * Get SyncFrom parameter.
   *
   * @return SyncFrom
   */
  private boolean isSyncFrom() {
    return this.getParams()
        .getBoolean("syncfrom", DWDefs.DISK_DEFAULT_SYNCFROM);
  }

  /**
   * Get SyncTo parameter.
   *
   * @return SyncTo
   */
  private boolean isSyncTo() {
    return this.getParams()
        .getBoolean("syncto", DWDefs.DISK_DEFAULT_SYNCTO);
  }

  /**
   * Get disk offset.
   *
   * @return offset
   */
  private int getOffset() {
    return this.getParams().getInt("offset", DWDefs.DISK_DEFAULT_OFFSET)
        + this.getParams().getInt("offsetdrv", 0)
        * DWDefs.DISK_HDBDOS_DISKSIZE;
  }

  /**
   * Get disk size limit.
   *
   * @return size limit
   */
  private int getSizeLimit() {
    return this.getParams().getInt("sizelimit", DWDefs.DISK_DEFAULT_SIZELIMIT);
  }

  /**
   * Sync disk.
   *
   * @throws IOException
   */
  public void sync() throws IOException {
    if (
        this.isSyncTo()
            && this.getFileObject() != null
            && this.getDirtySectors() > 0
    ) {
      try {
        this.write();
      } catch (DWImageHasNoSourceException ignored) {
      }
    }
  }

  /**
   * Get direct flag.
   *
   * @return direct flag
   */
  @Override
  public boolean isDirect() {
    return this.direct;
  }
}
