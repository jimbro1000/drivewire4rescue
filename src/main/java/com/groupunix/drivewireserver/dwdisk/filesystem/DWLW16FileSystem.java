package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.dwdisk.*;
import com.groupunix.drivewireserver.dwexceptions.*;

import java.io.*;
import java.util.*;

import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWLW16FileSystem extends DWFileSystem {


  private static final String FSNAME = "LW16";
  DWLW16FileSystemSuperBlock superblock;
  private List<String> fserrors = new ArrayList<String>();


  public DWLW16FileSystem(DWDisk disk) throws IOException, DWDiskInvalidSectorNumber {
    super(disk);

    this.superblock = new DWLW16FileSystemSuperBlock(this.disk.getSector(0));
  }


  @Override
  public List<String> getFSErrors() {
    return this.fserrors;
  }


  @Override
  public List<DWFileSystemDirEntry> getDirectory(String path)
      throws IOException, DWFileSystemInvalidDirectoryException, DWDiskInvalidSectorNumber {

    List<DWFileSystemDirEntry> res = new ArrayList<DWFileSystemDirEntry>();

    if (path == null) {
      for (DWLW16FileSystemDirEntry entry : this.getRootDirectory()) {
        res.add(entry);
      }
    } else {
      System.out.println("req dir: ");
    }

    return res;
  }


  public List<DWLW16FileSystemDirEntry> getRootDirectory() throws DWDiskInvalidSectorNumber, IOException {
    List<DWLW16FileSystemDirEntry> res = new ArrayList<DWLW16FileSystemDirEntry>();

    // get inode 0

    System.out.println("first inode: " + this.superblock.getFirstinodeblock());
    System.out.println("first data: " + this.superblock.getFirstdatablock());
    System.out.println("data blocks: " + this.superblock.getDatablocks());
    System.out.println("data bmps: " + this.superblock.getDatabmpblocks());
    System.out.println("tot inodes: " + this.superblock.getInodes());
    System.out.println();


    DWLW16FileSystemInode in0 = new DWLW16FileSystemInode(0, this.disk.getSector(this.superblock.getFirstinodeblock() + 1).getData());

    System.out.println(in0.toString());

    return res;
  }


  @Override
  public boolean hasFile(String filename) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Get file sectors list.
   * <p>
   *   Not implemented
   * </p>
   * @param filename file name
   * @return file sectors
   */
  @Override
  public ArrayList<DWDiskSector> getFileSectors(final String filename) {
    return null;
  }

  /**
   * Get directory entry.
   * <p>
   *   Not implemented
   * </p>
   * @param filename file name
   * @return directory entry
   */
  @Override
  public DWFileSystemDirEntry getDirEntry(final String filename) {
    return null;
  }

  /**
   * Get file contents.
   * <p>
   *   Not implemented
   * </p>
   * @param filename source file name
   * @return null
   */
  @Override
  public byte[] getFileContents(final String filename) {
    return null;
  }

  /**
   * Add file to filesystem.
   * <p>
   *   Not implemented
   * </p>
   * @param filename file name
   * @param fileContents byte array of contents
   */
  @Override
  public void addFile(final String filename, final byte[] fileContents) {
  }

  /**
   * Format filesystem.
   * <p>
   *   Not implemented
   * </p>
   * @throws DWInvalidSectorException
   * @throws DWSeekPastEndOfDeviceException
   * @throws DWDriveWriteProtectedException
   * @throws IOException
   */
  @Override
  public void format() throws DWInvalidSectorException,
      DWSeekPastEndOfDeviceException, DWDriveWriteProtectedException,
      IOException {
  }

  /**
   * Get filesystem name.
   *
   * @return name
   */
  @Override
  public String getFSName() {
    return DWLW16FileSystem.FSNAME;
  }

  /**
   * Is filesystem valid.
   *
   * @return true if valid
   */
  @Override
  public boolean isValidFS() {
    // valid superblock?
    if (this.superblock.isValid()) {
      // image size checks
      return (this.disk.getSectors().size() < (BYTE_SHIFT * BYTE_SHIFT))
          && (this.superblock.getFirstdatablock()
            < this.disk.getSectors().size())
          && (this.superblock.getFirstinodeblock()
            < this.disk.getSectors().size());
    }
    return false;
  }
}
