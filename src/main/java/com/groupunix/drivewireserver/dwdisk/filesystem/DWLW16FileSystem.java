package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwdisk.DWDiskSector;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

@SuppressWarnings("unused")
public class DWLW16FileSystem extends DWFileSystem {
  /**
   * File system name.
   */
  private static final String FS_NAME = "LW16";
  /**
   * LWL16 Super block.
   */
  private final DWLW16FileSystemSuperBlock superBlock;
  /**
   * File system errors.
   */
  private final List<String> fsErrors = new ArrayList<>();

  /**
   * LWL16 filesystem.
   *
   * @param disk source disk
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   */
  @SuppressWarnings("unused")
  public DWLW16FileSystem(final DWDisk disk)
      throws IOException, DWDiskInvalidSectorNumber {
    super(disk);
    this.superBlock = new DWLW16FileSystemSuperBlock(
        this.getDisk().getSector(0)
    );
  }

  /**
   * Get filesystem errors list.
   *
   * @return error list
   */
  @Override
  public List<String> getFSErrors() {
    return this.fsErrors;
  }

  /**
   * Get directory at file path.
   *
   * @param path filepath
   * @return directory entry list
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   */
  @Override
  public List<DWFileSystemDirEntry> getDirectory(final String path)
      throws IOException, DWDiskInvalidSectorNumber {
    List<DWFileSystemDirEntry> res = new ArrayList<>();
    if (path == null) {
      res.addAll(this.getRootDirectory());
    } else {
      System.out.println("req dir: ");
    }
    return res;
  }

  /**
   * Get root directory.
   *
   * @return root directory entry list
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws IOException failed to read from source
   */
  public List<DWLW16FileSystemDirEntry> getRootDirectory()
      throws DWDiskInvalidSectorNumber, IOException {
    List<DWLW16FileSystemDirEntry> res = new ArrayList<>();

    // get inode 0
    System.out.println("first inode: " + this.superBlock.getFirstInodeBlock());
    System.out.println("first data: " + this.superBlock.getFirstDataBlock());
    System.out.println("data blocks: " + this.superBlock.getDataBlocks());
    System.out.println("data bmps: " + this.superBlock.getDataBmpBlocks());
    System.out.println("tot inodes: " + this.superBlock.getInodes());
    System.out.println();

    DWLW16FileSystemInode in0 = new DWLW16FileSystemInode(
        0,
        this.getDisk()
            .getSector(this.superBlock.getFirstInodeBlock() + 1)
            .getData()
    );
    System.out.println(in0);
    return res;
  }

  /**
   * Test if file exists in file system.
   * <p>
   *   Not implemented
   * </p>
   * @param filename filename
   * @return false
   */
  @Override
  public boolean hasFile(final String filename) {
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
   * @throws DWInvalidSectorException invalid sector
   * @throws DWSeekPastEndOfDeviceException attempt to read past end of disk
   * @throws DWDriveWriteProtectedException write protected
   * @throws IOException failed to read from source
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
    return DWLW16FileSystem.FS_NAME;
  }

  /**
   * Is filesystem valid.
   *
   * @return true if valid
   */
  @Override
  public boolean isValidFS() {
    // valid super block?
    if (this.superBlock.isValid()) {
      // image size checks
      return (this.getDisk().getSectors().size() < (BYTE_SHIFT * BYTE_SHIFT))
          && (this.superBlock.getFirstDataBlock()
            < this.getDisk().getSectors().size())
          && (this.superBlock.getFirstInodeBlock()
            < this.getDisk().getSectors().size());
    }
    return false;
  }
}
