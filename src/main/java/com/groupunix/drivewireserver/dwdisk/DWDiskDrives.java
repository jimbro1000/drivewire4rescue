package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWDECBFileSystem;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWImageHasNoSourceException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;


public class DWDiskDrives {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWDiskDrives");
  /**
   * Sectors per drive for HBDDOS.
   */
  private static final int SECTORS_PER_DRIVE = 630;
  /**
   * OS9 FS check offset 1.
   */
  private static final int OS9_OFFSET1 = 3;
  /**
   * OS9 FS check offset 2.
   */
  private static final int OS9_OFFSET2 = 73;
  /**
   * OS9 FS check offset 3.
   */
  private static final int OS9_OFFSET3 = 75;
  /**
   * OS9 data match at offset1-3.
   */
  private static final int OS9_DATA_MATCH = 18;
  /**
   * Byte array size for matching LWFS.
   */
  private static final int LWFS_ARRAY_SIZE = 4;
  /**
   * Disk drives.
   */
  private final DWDiskDrive[] diskDrives;
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;
  /**
   * Disk Drive serial.
   */
  private int diskDriveSerial = -1;
  /**
   * File system manager.
   */
  private FileSystemManager fsManager;
  /**
   * HBD Dos Drive.
   */
  private int hdbdosdrive = 0;

  /**
   * Disk Drives constructor.
   *
   * @param protocolHandler protocol handler
   */
  public DWDiskDrives(final DWProtocolHandler protocolHandler) {
    LOGGER.debug(
        "disk drives init for handler #" + protocolHandler.getHandlerNo()
    );
    this.dwProtocolHandler = protocolHandler;
    this.diskDrives = new DWDiskDrive[getMaxDrives()];

    for (int i = 0; i < getMaxDrives(); i++) {
      this.diskDrives[i] = new DWDiskDrive(this, i);

      if (!DriveWireServer.isNoMount()
          && protocolHandler.getConfig()
          .getBoolean("RestoreDrivePaths", true)
          && protocolHandler.getConfig()
          .getString("Drive" + i + "Path", null) != null
      ) {
        try {
          LOGGER.debug(
              "Restoring drive " + i + " from "
                  + protocolHandler.getConfig().getString("Drive" + i + "Path")
          );
          this.loadDiskFromFile(
              i, protocolHandler.getConfig().getString("Drive" + i + "Path")
          );
        } catch (DWDriveNotValidException
                 | DWImageFormatException
                 | IOException
                 | DWDriveAlreadyLoadedException e
        ) {
          LOGGER.warn("Restoring drive " + i + ": " + e.getMessage());
        }
      }
    }

  }

  /**
   * Generate disk from file object.
   *
   * @param fileobj file object
   * @return disk object
   * @throws DWImageFormatException
   * @throws IOException
   */
  public static DWDisk diskFromFile(final FileObject fileobj)
      throws DWImageFormatException, IOException {
    return diskFromFile(fileobj, false);
  }

  /**
   * Generate disk from file object.
   * <p>
   * Attempts to auto-identify disk
   * type from file object contents
   * </p>
   *
   * @param fileobj    file object
   * @param forcecache force cache
   * @return disk object
   * @throws DWImageFormatException invalid disk image
   * @throws IOException            read failure
   */
  public static DWDisk diskFromFile(
      final FileObject fileobj, final boolean forcecache
  )
      throws DWImageFormatException, IOException {
    if (fileobj.getType() != FileType.FILE) {
      throw new DWImageFormatException("Attempt to load image from non file");
    }

    final FileContent content = fileobj.getContent();
    final long fObjSize = content.getSize();

    // size check
    if (fObjSize > Integer.MAX_VALUE) {
      throw new DWImageFormatException(
          "Image too big, maximum size is " + Integer.MAX_VALUE + " bytes."
      );
    }

    // get header
    final int hdrSize
        = (int) Math.min(DWDefs.DISK_IMAGE_HEADER_SIZE, fObjSize);

    final byte[] header = new byte[hdrSize];

    if (hdrSize > 0) {
      int readres = 0;
      final InputStream inputStream = content.getInputStream();
      while (readres < hdrSize) {
        readres += inputStream.read(header, readres, hdrSize - readres);
      }
      inputStream.close();
    }
    // collect votes
    final Hashtable<Integer, Integer> votes = new Hashtable<>();
    votes.put(DWDefs.DISK_FORMAT_DMK,
        DWDMKDisk.considerImage(header, fObjSize));
    votes.put(DWDefs.DISK_FORMAT_RAW,
        DWRawDisk.considerImage(header, fObjSize));
    votes.put(DWDefs.DISK_FORMAT_VDK,
        DWVDKDisk.considerImage(header, fObjSize));
    votes.put(DWDefs.DISK_FORMAT_JVC,
        DWJVCDisk.considerImage(header, fObjSize));
    votes.put(DWDefs.DISK_FORMAT_CCB,
        DWCCBDisk.considerImage(header, fObjSize));
    final int format = getBestFormat(votes);
    return switch (format) {
      case DWDefs.DISK_FORMAT_DMK -> new DWDMKDisk(fileobj);
      case DWDefs.DISK_FORMAT_VDK -> new DWVDKDisk(fileobj);
      case DWDefs.DISK_FORMAT_JVC -> new DWJVCDisk(fileobj);
      case DWDefs.DISK_FORMAT_CCB -> new DWCCBDisk(fileobj);
      case DWDefs.DISK_FORMAT_RAW -> new DWRawDisk(fileobj,
          DWDefs.DISK_SECTORSIZE,
          DWDefs.DISK_MAXSECTORS,
          forcecache);
      default -> throw new DWImageFormatException("Unsupported image format");
    };
  }

  /**
   * Get best guess disk format.
   *
   * @param votes votes
   * @return disk format
   * @throws DWImageFormatException invalid image format
   */
  private static int getBestFormat(final Hashtable<Integer, Integer> votes)
      throws DWImageFormatException {
    // who wants it... lets get silly playing with java collections
    // yes
    if (votes.containsValue(DWDefs.DISK_CONSIDER_YES)) {
      if (Collections.frequency(votes.values(), DWDefs.DISK_CONSIDER_YES) > 1) {
        throw new DWImageFormatException("Multiple formats claim this image?");
      } else {
        // a single yes vote.. we are good to go
        for (final Entry<Integer, Integer> entry : votes.entrySet()) {
          if (entry.getValue().equals(DWDefs.DISK_CONSIDER_YES)) {
            return entry.getKey();
          }
        }
      }
    } else if (votes.containsValue(DWDefs.DISK_CONSIDER_MAYBE)) {
      // maybe
      if (Collections
          .frequency(votes.values(), DWDefs.DISK_CONSIDER_MAYBE) > 1
      ) {
        throw new DWImageFormatException(
            "Multiple formats might read this image?"
        );
      } else {
        // a single maybe vote.. we are good to go
        for (final Entry<Integer, Integer> entry : votes.entrySet()) {
          if (entry.getValue().equals(DWDefs.DISK_CONSIDER_MAYBE)) {
            return entry.getKey();
          }
        }
      }
    }
    return DWDefs.DISK_FORMAT_NONE;
  }

  /**
   * Get filesystem type of disk.
   *
   * @param sectors disk sectors
   * @return file system
   */
  public static int getDiskFSType(final Vector<DWDiskSector> sectors) {
    try {
      if (!sectors.isEmpty()) {
        // OS9 ?
        if (
            sectors.get(0).getData()[OS9_OFFSET1] == OS9_DATA_MATCH
                && sectors.get(0).getData()[OS9_OFFSET2] == OS9_DATA_MATCH
                && sectors.get(0).getData()[OS9_OFFSET3] == OS9_DATA_MATCH
        ) {
          return DWDefs.DISK_FILESYSTEM_OS9;
        }
        // LWFS
        final byte[] lwfs = new byte[LWFS_ARRAY_SIZE];
        System.arraycopy(sectors.get(0).getData(), 0, lwfs, 0, lwfs.length);
        if (new String(lwfs, DWDefs.ENCODING).equals("LWFS")
            || new String(lwfs, DWDefs.ENCODING).equals("LW16")) {
          return DWDefs.DISK_FILESYSTEM_LWFS;
        }
        // TODO - outdated? cocoboot isave
        if (sectors.get(0).getData()[0]
            == (byte) 'f' && sectors.get(0).getData()[1] == (byte) 'c') {
          return DWDefs.DISK_FILESYSTEM_CCB;
        }
        // DECB? no 100% sure way that i know of
        final DWDECBFileSystem fileSystem =
            new DWDECBFileSystem(new DWRawDisk(sectors));
        if (fileSystem.isValidFS()) {
          return DWDefs.DISK_FILESYSTEM_DECB;
        }
      }
    } catch (IOException e) {
      LOGGER.debug("While checking FS type: " + e.getMessage());
    }
    return DWDefs.DISK_FILESYSTEM_UNKNOWN;
  }

  /**
   * Get disk in drive.
   *
   * @param driveNumber drive number
   * @return disk object
   * @throws DWDriveNotLoadedException
   * @throws DWDriveNotValidException
   */
  public DWDisk getDisk(final int driveNumber)
      throws DWDriveNotLoadedException, DWDriveNotValidException {
    // validate drive number
    if (!isDriveNo(driveNumber)) {
      throw new DWDriveNotValidException(
          "There is no drive " + driveNumber
              + ". Valid drives numbers are 0 - "
              + (dwProtocolHandler.getConfig().getInt(
              "DiskMaxDrives", DWDefs.DISK_MAXDRIVES
          ) - 1)
      );
    }
    return diskDrives[driveNumber].getDisk();
  }

  /**
   * Write sector for given drive.
   *
   * @param driveNumber drive number
   * @param data        sector byte array
   * @throws DWDriveNotLoadedException
   * @throws DWDriveNotValidException
   * @throws DWDriveWriteProtectedException
   * @throws IOException
   */
  public void writeSector(final int driveNumber, final byte[] data)
      throws DWDriveNotLoadedException,
      DWDriveNotValidException,
      DWDriveWriteProtectedException,
      IOException {
    int driveNo = driveNumber;
    if (dwProtocolHandler.getConfig().getBoolean("HDBDOSMode", false)) {
      driveNo = this.hdbdosdrive;
    }
    this.diskDrives[driveNo].writeSector(data);
  }

  /**
   * Read current sector for given drive.
   *
   * @param driveNumber drive number
   * @return sector byte array
   * @throws DWDriveNotLoadedException drive not loaded
   * @throws DWDriveNotValidException invalid drive
   * @throws IOException read failure
   * @throws DWImageFormatException invalid disk image format
   */
  public byte[] readSector(final int driveNumber)
      throws DWDriveNotLoadedException,
      DWDriveNotValidException,
      IOException,
      DWImageFormatException {
    int driveNo = driveNumber;
    if (dwProtocolHandler.getConfig().getBoolean("HDBDOSMode", false)) {
      driveNo = this.hdbdosdrive;
    }
    return this.diskDrives[driveNo].readSector();
  }

  /**
   * Seek sector for given drive.
   *
   * @param driveNumber drive number
   * @param lsn         logical sector number
   * @throws DWDriveNotLoadedException drive not loaded
   * @throws DWDriveNotValidException invalid drive
   * @throws DWInvalidSectorException invalid sector
   * @throws DWSeekPastEndOfDeviceException attempt to read past end of disk
   */
  public void seekSector(final int driveNumber, final int lsn)
      throws DWDriveNotLoadedException,
      DWDriveNotValidException,
      DWInvalidSectorException,
      DWSeekPastEndOfDeviceException {
    int newdriveno = driveNumber;
    int newlsn = lsn;
    if (dwProtocolHandler.getConfig().getBoolean("HDBDOSMode", false)) {
      // every 630 sectors is drive, lsn to remainder
      newdriveno = lsn / SECTORS_PER_DRIVE;
      newlsn = lsn % SECTORS_PER_DRIVE;
      if (lsn != newlsn || driveNumber != newdriveno) {
        LOGGER.debug(
            "HDBDOSMode maps seek from drv " + driveNumber
                + " sector " + lsn
                + " to drv " + newdriveno
                + " sector " + newlsn
        );
      }
      this.hdbdosdrive = newdriveno;
    }
    this.diskDrives[newdriveno].seekSector(newlsn);
  }

  /**
   * Is drive number valid.
   *
   * @param driveNumber drive number
   * @return true if valid
   */
  public boolean isDriveNo(final int driveNumber) {
    return driveNumber >= 0 && driveNumber < getMaxDrives();
  }

  /**
   * Is given drive loaded.
   *
   * @param driveNumber drive number
   * @return loaded status of drive
   */
  public boolean isLoaded(final int driveNumber) {
    return this.diskDrives[driveNumber].isLoaded();
  }

  /**
   * Reload disk in given drive.
   *
   * @param driveNumber drive number
   * @throws DWDriveNotLoadedException
   * @throws IOException
   * @throws DWDriveNotValidException
   * @throws DWImageFormatException
   */
  public void reLoadDisk(final int driveNumber)
      throws DWDriveNotLoadedException,
      IOException,
      DWDriveNotValidException,
      DWImageFormatException {
    getDisk(driveNumber).reload();
  }

  /**
   * Reload disks in drives.
   *
   * @throws IOException
   * @throws DWImageFormatException
   */
  public void reLoadAllDisks() throws IOException, DWImageFormatException {
    for (int i = 0; i < getMaxDrives(); i++) {
      try {
        if (isLoaded(i)) {
          getDisk(i).reload();
        }
      } catch (DWDriveNotLoadedException | DWDriveNotValidException e) {
        LOGGER.warn(e.getMessage());
      }
    }
  }

  /**
   * Eject disk from given drive.
   *
   * @param driveNumber drive number
   * @throws DWDriveNotValidException
   * @throws DWDriveNotLoadedException
   */
  public void ejectDisk(final int driveNumber)
      throws DWDriveNotValidException, DWDriveNotLoadedException {
    diskDrives[driveNumber].eject();
    if (dwProtocolHandler
        .getConfig()
        .getBoolean("SaveDrivePaths", true)
    ) {
      dwProtocolHandler
          .getConfig()
          .setProperty("Drive" + driveNumber + "Path", null);
    }
    LOGGER.info("ejected disk from drive " + driveNumber);
    incDiskDriveSerial();
  }

  /**
   * Eject all disks.
   */
  public void ejectAllDisks() {
    for (int i = 0; i < getMaxDrives(); i++) {
      if (isLoaded(i)) {
        try {
          ejectDisk(i);
        } catch (DWDriveNotValidException | DWDriveNotLoadedException e) {
          LOGGER.warn(e.getMessage());
        }
      }
    }
  }

  /**
   * Write disk in given drive.
   *
   * @param driveNumber drive number
   * @throws IOException
   * @throws DWDriveNotLoadedException
   * @throws DWDriveNotValidException
   * @throws DWImageHasNoSourceException
   */
  public void writeDisk(final int driveNumber)
      throws IOException,
      DWDriveNotLoadedException,
      DWDriveNotValidException,
      DWImageHasNoSourceException {
    getDisk(driveNumber).write();
  }

  /**
   * write disk image to path.
   *
   * @param driveNumber
   * @param path
   * @throws IOException
   * @throws DWDriveNotLoadedException
   * @throws DWDriveNotValidException
   */
  public void writeDisk(final int driveNumber, final String path)
      throws IOException, DWDriveNotLoadedException, DWDriveNotValidException {
    getDisk(driveNumber).writeTo(path);
  }

  /**
   * Load disk from file image.
   *
   * @param driveNumber drive number
   * @param path        file object path
   * @throws DWDriveNotValidException
   * @throws DWDriveAlreadyLoadedException
   * @throws IOException
   * @throws DWImageFormatException
   */
  public void loadDiskFromFile(final int driveNumber, final String path)
      throws DWDriveNotValidException,
      DWDriveAlreadyLoadedException,
      IOException,
      DWImageFormatException {
    // Determine what kind of disk we have
    this.fsManager = VFS.getManager();
    try {
      final FileObject fileobj = fsManager.resolveFile(path);
      if (fileobj.exists() && fileobj.isReadable()) {
        this.loadDisk(driveNumber, DWDiskDrives.diskFromFile(fileobj));
      } else {
        LOGGER.error("Unreadable path '" + path + "'");
        throw new IOException("Unreadable path");
      }
    } catch (org.apache.commons.vfs2.FileSystemException e) {
      LOGGER.error("FileSystemException: " + e.getMessage());
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Load disk in given drive.
   *
   * @param driveno drive number
   * @param disk    disk object
   * @throws DWDriveNotValidException
   * @throws DWDriveAlreadyLoadedException
   */
  public void loadDisk(final int driveno, final DWDisk disk)
      throws DWDriveNotValidException, DWDriveAlreadyLoadedException {
    // eject existing disk if necessary
    if (this.isLoaded(driveno)) {
      try {
        this.diskDrives[driveno].eject();
      } catch (DWDriveNotLoadedException e) {
        LOGGER.warn("Loaded but not loaded.. well what is this about then?");
      }
    }
    // put into use
    diskDrives[driveno].insert(disk);
    if (dwProtocolHandler.getConfig().getBoolean(
        "SaveDrivePaths", true
    )) {
      dwProtocolHandler.getConfig().setProperty(
          "Drive" + driveno + "Path", disk.getFilePath()
      );
    }
    LOGGER.info(
        "loaded disk '" + disk.getFilePath() + "' in drive " + driveno
    );
    incDiskDriveSerial();
  }

  /**
   * Generate an empty sector byte array.
   *
   * @return empty sector
   */
  public byte[] nullSector() {
    byte[] tmp = new byte[dwProtocolHandler
        .getConfig()
        .getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)];
    for (int i = 0;
         i < dwProtocolHandler.getConfig().getInt(
             "DiskSectorSize", DWDefs.DISK_SECTORSIZE
         );
         i++
    ) {
      tmp[i] = (byte) 0;
    }
    return tmp;
  }

  /**
   * Graceful shutdown.
   */
  public void shutdown() {
    LOGGER.debug("shutting down");
    // sync all disks
    sync();
  }

  /**
   * Sync all loaded drives.
   */
  public void sync() {
    for (int driveno = 0; driveno < getMaxDrives(); driveno++) {
      if (isLoaded(driveno)) {
        try {
          getDisk(driveno).sync();
        } catch (DWDriveNotLoadedException
                 | IOException
                 | DWDriveNotValidException e
        ) {
          LOGGER.warn(e.getMessage());
        }
      }
    }
  }

  /**
   * Get maximum drive count.
   *
   * @return maximum drives
   */
  public int getMaxDrives() {
    return dwProtocolHandler
        .getConfig()
        .getInt("DiskMaxDrives", DWDefs.DISK_MAXDRIVES);
  }

  /**
   * Get last free drive number.
   *
   * @return drive number
   */
  public int getFreeDriveNo() {
    int res = getMaxDrives() - 1;
    while (isLoaded(res) && res > 0) {
      res--;
    }
    return res;
  }

  /**
   * Increment disk drive serial.
   */
  public void incDiskDriveSerial() {
    this.diskDriveSerial++;
  }

  /**
   * Get disk drive serial.
   *
   * @return disk drive serial object
   */
  public int getDiskDriveSerial() {
    return diskDriveSerial;
  }

  /**
   * Get drive number from string.
   *
   * @param driveNumber drive number as string
   * @return drive number
   * @throws DWDriveNotValidException
   */
  public int getDriveNoFromString(final String driveNumber)
      throws DWDriveNotValidException {
    int res = -1;

    try {
      res = Integer.parseInt(driveNumber);
    } catch (NumberFormatException e) {
      throw new DWDriveNotValidException("Drive numbers must be numeric");
    }
    this.isDriveNo(res);
    return res;
  }

  /**
   * Submit disk event.
   *
   * @param driveNumber drive number
   * @param key         event key
   * @param val         value
   */
  public void submitEvent(
      final int driveNumber, final String key, final String val
  ) {
    DriveWireServer.submitDiskEvent(
        this.dwProtocolHandler.getHandlerNo(), driveNumber, key, val
    );
  }

  /**
   * Get configuration.
   *
   * @return configuration
   */
  public HierarchicalConfiguration getConfig() {
    return this.dwProtocolHandler.getConfig();

  }

  /**
   * Create raw disk.
   * <p>
   * Fails if drive is already loaded
   * </p>
   *
   * @param driveNumber target drive number
   * @throws DWDriveAlreadyLoadedException
   */
  public void createDisk(final int driveNumber)
      throws DWDriveAlreadyLoadedException {
    if (this.isLoaded(driveNumber)) {
      throw new DWDriveAlreadyLoadedException(
          "Already a disk in drive " + driveNumber
      );
    }
    this.diskDrives[driveNumber].insert(
        new DWRawDisk(DWDefs.DISK_SECTORSIZE, DWDefs.DISK_MAXSECTORS)
    );
  }

  /**
   * Format Disk in given drive.
   *
   * @param driveNumber drive number
   * @throws DWDriveNotLoadedException
   * @throws DWDriveNotValidException
   * @throws DWInvalidSectorException
   * @throws DWSeekPastEndOfDeviceException
   * @throws DWDriveWriteProtectedException
   * @throws IOException
   */
  public void formatDOSFS(final int driveNumber)
      throws DWDriveNotLoadedException,
      DWDriveNotValidException,
      DWInvalidSectorException,
      DWSeekPastEndOfDeviceException,
      DWDriveWriteProtectedException,
      IOException {
    if (!this.isLoaded(driveNumber)) {
      throw new DWDriveNotLoadedException("No disk in drive " + driveNumber);
    }
    new DWDECBFileSystem(this.getDisk(driveNumber)).format();
  }

  /**
   * Get protocol handler.
   *
   * @return protocol handler
   */
  public DWProtocolHandler getDWProtocolHandler() {
    return this.dwProtocolHandler;
  }

  /**
   * Get object mount name/id.
   *
   * @param objname object name
   * @return drive id
   */
  public int nameObjMount(final String objname) {
    // turn objname into path
    final String objPath = getObjPath(objname);
    try {
      final FileObject fileobj = fsManager.resolveFile(objPath);
      // look for already mounted
      for (int i = 0; i < getMaxDrives(); i++) {
        try {
          if (
              this.diskDrives[i] != null
                  && this.diskDrives[i].isLoaded()
                  && (this.diskDrives[i].getDisk().getFilePath().equals(objPath)
                  || this.diskDrives[i].getDisk()
                  .getFilePath().equals(fileobj.getName().getFriendlyURI())
                  || this.diskDrives[i].getDisk()
                  .getFilePath().equals(fileobj.getName().getURI()))
          ) {
            return i;
          }
        } catch (DWDriveNotLoadedException ignored) {
        }
      }
      final int drv = this.getFreeDriveNo();
      this.loadDiskFromFile(drv, objPath);
      return drv;
    } catch (DWDriveNotValidException
             | DWImageFormatException
             | IOException
             | DWDriveAlreadyLoadedException e) {
      LOGGER.debug(
          "namedobjmount of '" + objPath + "' failed with: " + e.getMessage()
      );
    }
    return 0;
  }

  /**
   * Get object path.
   *
   * @param objName named object
   * @return path parameter
   */
  public String getObjPath(final String objName) {
    @SuppressWarnings("unchecked") final List<HierarchicalConfiguration> objs = this.dwProtocolHandler
        .getConfig()
        .configurationsAt("NamedObject");
    for (final HierarchicalConfiguration obj : objs) {
      if (
          obj.containsKey("[@name]")
              && obj.containsKey("[@path]")
              && obj.getString("[@name]").equals(objName)) {
        return obj.getString("[@path]");
      }
    }
    // namedobjdir
    return dwProtocolHandler.getConfig().getString(
        "NamedObjectDir",
        System.getProperty("user.dir")) + "/" + objName;
  }
}
