package com.groupunix.drivewireserver.dwdisk;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWImageHasNoSourceException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public abstract class DWDisk {
  /**
   * Log appender.
   */
  private static final Logger LOGGER = Logger.getLogger("DWServer.DWDisk");
  /**
   * Parameter map.
   */
  private final HierarchicalConfiguration params;
  /**
   * Disk sectors.
   */
  private Vector<DWDiskSector> sectors = new Vector<>();
  /**
   * File object.
   * <p>
   * Not used for in memory disks
   * </p>
   */
  private FileObject fileObj;
  /**
   * Configuration listener.
   */
  @SuppressWarnings("unused")
  private DWDiskConfigListener configlistener;
  /**
   * Associated drive.
   */
  private DWDiskDrive drive;

  // required for format implementation:

  // file image

  /**
   * File object disk constructor.
   *
   * @param fileObject source file object
   * @throws IOException Failed to read from file object
   * @throws DWImageFormatException Invalid file format
   */
  public DWDisk(final FileObject fileObject)
      throws IOException, DWImageFormatException {
    this.fileObj = fileObject;
    this.params = new HierarchicalConfiguration();
    // internal
    this.setParam("_path", fileObject.getName().getURI());
    long lastModifiedTime = -1;
    try {
      lastModifiedTime = this.fileObj.getContent().getLastModifiedTime();
    } catch (FileSystemException e) {
      LOGGER.warn(e.getMessage());
    }
    this.setLastModifiedTime(lastModifiedTime);
    this.setParam("_reads", 0);
    this.setParam("_writes", 0);
    this.setParam("_lsn", 0);
    // user options
    this.setParam("writeprotect", DWDefs.DISK_DEFAULT_WRITEPROTECT);
  }

  // memory image

  /**
   * In memory disk constructor.
   */
  public DWDisk() {
    this.fileObj = null;
    this.params = new HierarchicalConfiguration();
    // internal
    this.setParam("_path", "");
    this.setParam("_reads", 0);
    this.setParam("_writes", 0);
    this.setParam("_lsn", 0);
    // user options
    this.setParam("writeprotect", DWDefs.DISK_DEFAULT_WRITEPROTECT);
  }

  /**
   * Compare two byte arrays for equality of content.
   * <p>
   * Only checks the length of the shortest array,
   * any longer content in the longer array is
   * ignored
   * </p>
   *
   * @param b1 first byte array
   * @param b2 second byte array
   * @return true if contents are equal
   */
  public static boolean compareByteArray(
      final byte[] b1, final byte[] b2
  ) {
    boolean result = true;
    int index = 0;
    while (result && index < b1.length && index < b2.length) {
      result = b1[index] == b2[index];
      ++index;
    }
    return result;
  }

  /**
   * Seek sector.
   *
   * @param lsn logical sector number
   * @throws DWInvalidSectorException Invalid LSN
   * @throws DWSeekPastEndOfDeviceException Seek past end of disk
   */
  public abstract void seekSector(int lsn)
      throws DWInvalidSectorException, DWSeekPastEndOfDeviceException;

  /**
   * Write byte array to sector.
   *
   * @param data byte array
   * @throws DWDriveWriteProtectedException Write protected
   * @throws IOException Failed to write to file object
   */
  public abstract void writeSector(byte[] data)
      throws DWDriveWriteProtectedException, IOException;

  /**
   * read disk sector.
   *
   * @return byte array of sector
   * @throws IOException Failed to read from file object
   * @throws DWImageFormatException Invalid file format
   */
  public abstract byte[] readSector()
      throws IOException, DWImageFormatException;

  /**
   * load disk.
   *
   * @throws IOException Failed to read from file object
   * @throws DWImageFormatException Invalid file format
   */
  protected abstract void load()
      throws IOException, DWImageFormatException;

  /**
   * Get disk format.
   *
   * @return format id
   */
  @SuppressWarnings("unused")
  public abstract int getDiskFormat();

  /**
   * Get all parameters.
   *
   * @return map of parameters
   */
  public HierarchicalConfiguration getParams() {
    return this.params;
  }

  /**
   * Set named parameter to value.
   *
   * @param key parameter key
   * @param val value
   */
  public void setParam(final String key, final Object val) {
    if (val == null) {
      this.params.clearProperty(key);
    } else {
      this.params.setProperty(key, val);
    }
  }

  /**
   * Increment named parameter.
   *
   * @param key parameter key
   */
  public void incParam(final String key) {
    this.setParam(key, this.params.getInt(key, 0) + 1);
  }

  /**
   * Get file object path.
   *
   * @return file path
   */
  public String getFilePath() {
    if (this.fileObj != null) {
      return this.fileObj.getName().getURI();
    } else {
      return "(in memory only)";
    }
  }

  /**
   * Get file object associated with disk.
   *
   * @return file object
   */
  public FileObject getFileObject() {
    return this.fileObj;
  }

  /**
   * Get last modified time from disk.
   *
   * @return last modified timestamp
   */
  public long getLastModifiedTime() {
    return this.params.getLong("_last_modified", 0);
  }

  /**
   * Set last modified time on disk.
   *
   * @param lastModifiedTime last modified time stamp
   */
  public void setLastModifiedTime(final long lastModifiedTime) {
    this.params.setProperty("_last_modified", lastModifiedTime);
  }

  /**
   * Get logical sectors.
   *
   * @return sector count
   */
  public int getLSN() {
    return (this.params.getInt("_lsn", 0));
  }

  /**
   * Get number of disk sectors.
   *
   * @return sector count
   */
  public int getDiskSectors() {
    return this.sectors.size();
  }

  /**
   * Reload disk image from file system.
   *
   * @throws IOException Failed to read from file object
   * @throws DWImageFormatException Invalid file format
   */
  public void reload() throws IOException, DWImageFormatException {
    if (this.getFileObject() != null) {
      LOGGER.debug("reloading disk sectors from " + this.getFilePath());
      this.sectors.clear();
      // load from path
      load();
    } else {
      throw (new DWImageFormatException(
          "Image is in memory only, so cannot reload."
      ));
    }
  }

  /**
   * Eject disk.
   * <p>
   * Closes file object
   * </p>
   *
   * @throws IOException Failed to write to file object
   */
  public void eject() throws IOException {
    sync();
    this.sectors = null;
    if (this.fileObj != null) {
      this.fileObj.close();
      this.fileObj = null;
    }
  }

  /**
   * Sync disk.
   * <p>
   * Always NOP unless overridden
   * </p>
   *
   * @throws IOException Failed to write to file object
   */
  public void sync() throws IOException {
    // NOP on readonly image formats
  }

  /**
   * Write disk.
   * <p>
   * Always fails unless overridden
   * </p>
   *
   * @throws IOException Failed to read from file object
   * @throws DWImageHasNoSourceException No file object defined
   */
  public void write() throws IOException, DWImageHasNoSourceException {
    // Fail on readonly image formats
    throw new IOException("Image is read only");
  }

  /**
   * Attempt to write to a given file path.
   *
   * @param path filepath
   * @throws IOException Failed to write to file object
   */
  public void writeTo(final String path) throws IOException {
    // write in memory image to specified path (raw format)
    // using most efficient method available
    FileObject altobj = VFS.getManager().resolveFile(path);
    if (altobj.isWriteable()) {
      if (altobj.getFileSystem().hasCapability(Capability.WRITE_CONTENT)) {
        // we always rewrite the entire object
        writeSectors(altobj);
      } else {
        // no way to write to this filesystem
        LOGGER.warn(
            "Filesystem is unwritable for path '" + altobj.getName() + "'"
        );
        throw new FileSystemException("Filesystem is unwriteable");
      }
    } else {
      LOGGER.warn(
          "File is unwriteable for path '" + altobj.getName() + "'"
      );
      throw new IOException("File is unwriteable");
    }
  }

  /**
   * Write cached sectors to disk file.
   *
   * @param fobj disk file
   * @throws IOException Failed to write to file object
   */
  public void writeSectors(final FileObject fobj) throws IOException {
    // write out all sectors
    long timeGetdata = 0;
    long timeWrite = 0;
    long timeClean = 0;
    long timeInit;
    long timePoint = System.currentTimeMillis();

    LOGGER.debug(
        "Writing out all sectors from cache to " + fobj.getName()
    );
    BufferedOutputStream fos = new BufferedOutputStream(
        fobj.getContent().getOutputStream()
    );
    int ss = DWDefs.DISK_SECTORSIZE;
    if (this.getParams().containsKey("_sectorsize")) {
      try {
        ss = (Integer) this.getParam("_sectorsize");
      } catch (NumberFormatException e) {
        // how did they get a non int value in there... whatever
      }
    }
    byte[] zerofill = new byte[ss];
    timeInit = System.currentTimeMillis() - timePoint;
    for (DWDiskSector sector : this.sectors) {
      // we do have a sector obj
      if (sector != null) {
        timePoint = System.currentTimeMillis();
        byte[] tmp = sector.getData();
        timeGetdata += (System.currentTimeMillis() - timePoint);
        timePoint = System.currentTimeMillis();
        fos.write(tmp, 0, tmp.length);
        timeWrite += (System.currentTimeMillis() - timePoint);
        timePoint = System.currentTimeMillis();
        sector.makeClean();
        timeClean += (System.currentTimeMillis() - timePoint);
      } else {
        // we don't, write 0 filled
        fos.write(zerofill, 0, ss);
      }
    }
    LOGGER.debug(
        "disk write timing = init: " + timeInit
            + "  getdata: " + timeGetdata
            + "  writestream: " + timeWrite
            + "  clean: " + timeClean
    );
    fos.close();
    if (this.fileObj != null) {
      this.setLastModifiedTime(
          this.fileObj.getContent().getLastModifiedTime()
      );
    }
  }

  /**
   * Get a count of dirty sectors.
   *
   * @return total dirty sectors on disk
   */
  public int getDirtySectors() {
    int drt = 0;
    if (this.sectors != null) {
      for (DWDiskSector sector : this.sectors) {
        if (sector != null) {
          if (sector.isDirty()) {
            drt++;
          }
        }
      }
    }
    return drt;
  }

  /**
   * Get disk sector by number.
   *
   * @param sectorNumber sector number
   * @return Disk sector
   * @throws DWDiskInvalidSectorNumber Invalid LSN
   */
  public DWDiskSector getSector(final int sectorNumber)
      throws DWDiskInvalidSectorNumber {
    if (
        (sectorNumber < 0) || (sectorNumber >= this.sectors.size())
    ) {
      throw new DWDiskInvalidSectorNumber(
          "Invalid sector number: " + sectorNumber
      );
    }
    if (this.sectors.get(sectorNumber) != null) {
      return (this.sectors.get(sectorNumber));
    }
    return (null);
  }

  /**
   * Get write protect status.
   *
   * @return status
   */
  public boolean getWriteProtect() {
    return (this.params.getBoolean(
        "writeprotect", DWDefs.DISK_DEFAULT_WRITEPROTECT
    ));
  }

  /**
   * Get disk parameter by key name.
   *
   * @param key key name
   * @return parameter value
   */
  public Object getParam(final String key) {
    if (this.params.containsKey(key)) {
      return (this.params.getProperty(key));
    }
    return (null);
  }

  /**
   * Get associated drive.
   *
   * @return drive object
   */
  public DWDiskDrive getDrive() {
    return this.drive;
  }

  /**
   * Insert disk in drive.
   *
   * @param targetDrive target drive
   */
  public void insert(final DWDiskDrive targetDrive) {
    this.drive = targetDrive;
    // remove any existing listeners
    for (ConfigurationListener configurationListener : this.params
        .getConfigurationListeners()) {
      this.params.removeConfigurationListener(configurationListener);
    }
    // add for this drive
    this.params.addConfigurationListener(
        new DWDiskConfigListener(this)
    );
    // announce drive info to any event listeners
    Iterator<String> itr = this.params.getKeys();
    while (itr.hasNext()) {
      String key = itr.next();
      this.drive.submitEvent(
          key, this.params.getProperty(key).toString()
      );
    }
  }

  /**
   * Submit event to disk.
   *
   * @param key key name
   * @param val value
   */
  public void submitEvent(
      final String key, final String val
  ) {
    if (this.drive != null) {
      this.drive.submitEvent(key, val);
    }
  }

  /**
   * Get direct capability.
   *
   * @return true if direct available
   */
  public boolean getDirect() {
    return false;
  }

  /**
   * Get vectors to all sectors.
   *
   * @return disk sectors
   */
  public Vector<DWDiskSector> getSectors() {
    return this.sectors;
  }

  /**
   * Set sector vectors.
   *
   * @param diskSectors disk sectors
   */
  public void setSectors(final Vector<DWDiskSector> diskSectors) {
    this.sectors = diskSectors;
  }
}
