package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;

public class DWDiskDrive {
  /**
   * Log appender.
   */
  private static final Logger LOGGER = Logger.getLogger("DWServer.DWDiskDrive");
  /**
   * Drive number id.
   */
  private final int driveNo;
  /**
   * Disk drives object.
   */
  private final DWDiskDrives dwDrives;
  /**
   * Drive loaded?.
   */
  private boolean loaded = false;
  /**
   * Drivewire disk.
   */
  private DWDisk dwDisk = null;

  /**
   * Disk drives constructor.
   *
   * @param drives
   * @param driveNumber
   */
  public DWDiskDrive(final DWDiskDrives drives, final int driveNumber) {
    this.dwDrives = drives;
    this.driveNo = driveNumber;
  }

  /**
   * Get drive number.
   *
   * @return drive number
   */
  public int getDriveNo() {
    return driveNo;
  }

  /**
   * Is a disk loaded in drive.
   *
   * @return true if loaded
   */
  public boolean isLoaded() {
    return this.loaded;
  }

  /**
   * Get loaded disk.
   *
   * @return disk object
   * @throws DWDriveNotLoadedException
   */
  public DWDisk getDisk() throws DWDriveNotLoadedException {
    if (this.loaded) {
      return (this.dwDisk);
    } else {
      throw new DWDriveNotLoadedException(
          "No disk in drive " + this.getDriveNo()
      );
    }
  }

  /**
   * Eject disk from drive.
   *
   * @throws DWDriveNotLoadedException
   */
  public void eject() throws DWDriveNotLoadedException {
    if (this.dwDisk == null) {
      throw new DWDriveNotLoadedException(
          "There is no disk in drive " + this.driveNo
      );
    }
    synchronized (this.dwDisk) {
      try {
        this.dwDisk.eject();
      } catch (IOException e) {
        LOGGER.warn(
            "Ejecting from drive " + this.getDriveNo() + ": " + e.getMessage()
        );
      }
      this.loaded = false;
      this.dwDisk = null;
      this.submitEvent("*eject", "");
    }
  }

  /**
   * Insert disk in drive.
   *
   * @param disk disk object
   */
  public void insert(final DWDisk disk) {
    this.dwDisk = disk;
    this.loaded = true;
    this.submitEvent("*insert", this.dwDisk.getFilePath());
    this.dwDisk.insert(this);
  }

  /**
   * Seek sector by LSN.
   *
   * @param lsn logical sector number
   * @throws DWInvalidSectorException
   * @throws DWSeekPastEndOfDeviceException
   * @throws DWDriveNotLoadedException
   */
  public void seekSector(final int lsn)
      throws DWInvalidSectorException,
      DWSeekPastEndOfDeviceException,
      DWDriveNotLoadedException {
    if (this.dwDisk == null) {
      throw new DWDriveNotLoadedException("No disk in drive " + this.driveNo);
    }
    synchronized (this.dwDisk) {
      this.dwDisk.seekSector(lsn);
    }
  }

  /**
   * Read sector from disk.
   *
   * @return sector byte array
   * @throws IOException
   * @throws DWImageFormatException
   */
  public byte[] readSector() throws IOException, DWImageFormatException {
    if (this.dwDisk == null) {
      throw new IOException("Disk is null");
    }
    synchronized (this.dwDisk) {
      return (this.dwDisk.readSector());
    }
  }

  /**
   * Write sector to disk.
   *
   * @param data sector byte array
   * @throws DWDriveWriteProtectedException
   * @throws IOException
   */
  public void writeSector(final byte[] data)
      throws DWDriveWriteProtectedException, IOException {
    if (this.dwDisk == null) {
      throw new IOException("Disk is null");
    }
    synchronized (this.dwDisk) {
      this.dwDisk.writeSector(data);
    }
  }

  /**
   * Submit event to drives.
   *
   * @param key parameter key
   * @param val value
   */
  public void submitEvent(final String key, final String val) {
    if (this.dwDrives != null) {
      this.dwDrives.submitEvent(this.driveNo, key, val);
    }
  }

  /**
   * Get drives.
   *
   * @return disk drives object
   */
  public DWDiskDrives getDiskDrives() {
    return this.dwDrives;
  }
}
