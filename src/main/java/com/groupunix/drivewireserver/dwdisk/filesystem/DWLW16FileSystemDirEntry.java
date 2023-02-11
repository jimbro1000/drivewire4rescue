package com.groupunix.drivewireserver.dwdisk.filesystem;

public class DWLW16FileSystemDirEntry extends DWFileSystemDirEntry {
  /**
   * File name.
   */
  private final String filename;
  /**
   * File system iNode.
   */
  private DWLW16FileSystemInode iNode;

  /**
   * LW16 File system directory entry.
   *
   * @param file filename
   * @param node iNode
   */
  public DWLW16FileSystemDirEntry(
      final String file, final DWLW16FileSystemInode node
  ) {
    super(null);
    this.setInode(node);
    this.filename = file;
  }

  /**
   * Get file name.
   *
   * @return file name
   */
  @Override
  public String getFileName() {
    return this.filename;
  }

  /**
   * Get file extension.
   *
   * @return file extension
   */
  @Override
  public String getFileExt() {
    final int dot = this.filename.lastIndexOf(".");
    if (dot > 0 && dot < this.filename.length() - 1) {
      return this.filename.substring(dot + 1);
    }
    return "";
  }

  /**
   * Get file path.
   * <p>
   *   Not implemented
   * </p>
   * @return null
   */
  @Override
  public String getFilePath() {
    return null;
  }

  /**
   * Get pretty file type.
   * <p>
   *   Not implemented
   * </p>
   * @return null
   */
  @Override
  public String getPrettyFileType() {
    return null;
  }

  /**
   * Get file type.
   * <p>
   *   Not implemented
   * </p>
   * @return 0
   */
  @Override
  public int getFileType() {
    return 0;
  }

  /**
   * Get parent directory entry.
   * <p>
   *   Not implemented
   * </p>
   * @return null
   */
  @Override
  public DWFileSystemDirEntry getParentEntry() {
    return null;
  }

  /**
   * is directory.
   * <p>
   *   Not implemented
   * </p>
   * @return false
   */
  @Override
  public boolean isDirectory() {
    return false;
  }

  /**
   * Is ascii file type.
   * <p>
   *   Not implemented
   * </p>
   * @return false
   */
  @Override
  public boolean isAscii() {
    return false;
  }

  /**
   * Get iNode.
   *
   * @return iNode
   */
  @SuppressWarnings("unused")
  public DWLW16FileSystemInode getInode() {
    return iNode;
  }

  /**
   * Set iNode.
   *
   * @param node iNode
   */
  public void setInode(final DWLW16FileSystemInode node) {
    this.iNode = node;
  }
}
