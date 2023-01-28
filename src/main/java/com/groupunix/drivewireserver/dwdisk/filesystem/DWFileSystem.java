package com.groupunix.drivewireserver.dwdisk.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwdisk.DWDiskSector;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFullException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidDirectoryException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFilenameException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;

public abstract class DWFileSystem {
  /**
   * Source disk.
   */
  private final DWDisk dwDisk;

  /**
   * File system constructor.
   *
   * @param disk source disk
   */
  public DWFileSystem(final DWDisk disk) {
    this.dwDisk = disk;
  }

  /**
   * Get source disk.
   *
   * @return source disk
   */
  public DWDisk getDisk() {
    return this.dwDisk;
  }

  /**
   * Get file system directory from path.
   *
   * @param path filepath
   * @return directory entry
   * @throws IOException failed to read from source
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   * @throws DWDiskInvalidSectorNumber invalid sector number
   */
  public abstract List<DWFileSystemDirEntry> getDirectory(String path)
      throws IOException,
      DWFileSystemInvalidDirectoryException,
      DWDiskInvalidSectorNumber;

  /**
   * Test if current directory entry contains given file name.
   *
   * @param filename filename
   * @return true if file is present
   * @throws IOException failed to read from source
   */
  @SuppressWarnings("unused")
  public abstract boolean hasFile(String filename) throws IOException;

  /**
   * Get File sectors.
   *
   * @param filename file name
   * @return list of disk sectors
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws DWFileSystemInvalidFATException invalid FAT descriptor
   * @throws IOException failed to read form file
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public abstract ArrayList<DWDiskSector> getFileSectors(String filename)
      throws DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException;

  /**
   * Get directory entry.
   *
   * @param filename file name
   * @return File system directory entry
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws IOException failed to read from source
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public abstract DWFileSystemDirEntry getDirEntry(String filename)
      throws DWFileSystemFileNotFoundException,
      IOException,
      DWFileSystemInvalidDirectoryException;

  /**
   * Get file contents from source file.
   *
   * @param filename source file name
   * @return byte array of contents
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws DWFileSystemInvalidFATException invalid FAT descriptor
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public abstract byte[] getFileContents(String filename)
      throws DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException;

  /**
   * Add file.
   *
   * @param filename file name
   * @param fileContents byte array of contents
   * @throws DWFileSystemFullException file system is full
   * @throws DWFileSystemInvalidFilenameException invalid file name
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws DWFileSystemInvalidFATException invalid fat descriptor
   * @throws IOException failed to write to source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public abstract void addFile(String filename, byte[] fileContents)
      throws DWFileSystemFullException,
      DWFileSystemInvalidFilenameException,
      DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException;

  /**
   * Format filesystem.
   *
   * @throws DWInvalidSectorException invalid sector number
   * @throws DWSeekPastEndOfDeviceException attempt to read past last sector
   * @throws DWDriveWriteProtectedException write protected filesystem
   * @throws IOException failed to write to source file
   */
  public abstract void format()
      throws DWInvalidSectorException,
      DWSeekPastEndOfDeviceException,
      DWDriveWriteProtectedException,
      IOException;

  /**
   * Get filesystem name.
   *
   * @return filesystem name
   */
  @SuppressWarnings("unused")
  public abstract String getFSName();

  /**
   * Is filesystem valid.
   *
   * @return true if valid
   */
  public abstract boolean isValidFS();

  /**
   * Get file system errors.
   * @return list of errors
   */
  @SuppressWarnings("unused")
  public abstract List<String> getFSErrors();
}
