package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;


public class DWRBFFileSystemDirEntry extends DWFileSystemDirEntry {
  /**
   * Directory type bit mask.
   */
  private static final int DIRECTORY_MASK = 0x80;
  /**
   * File name.
   */
  private final String filename;
  /**
   * File descriptor logical sector number.
   */
  private int fdLsn;
  /**
   * File descriptor.
   */
  private DWRBFFileDescriptor fileDescriptor;

  /**
   * RBF file system directory entry.
   *
   * @param file       file name
   * @param lsn        file descriptor logical sector number
   * @param descriptor file descriptor
   */
  public DWRBFFileSystemDirEntry(
      final String file, final int lsn, final DWRBFFileDescriptor descriptor
  ) {
    super(null);
    this.filename = file;
    this.setFdLsn(lsn);
    this.setFd(descriptor);
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
    String res = "";
    final int dot = this.filename.lastIndexOf(".");
    if (dot > 0 && dot < this.filename.length() - 1) {
      res = this.filename.substring(dot + 1);
    }
    return res;
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
   * @return parent
   */
  @Override
  public DWFileSystemDirEntry getParentEntry() {
    return null;
  }

  /**
   * Is directory.
   *
   * @return true if directory
   */
  @Override
  public boolean isDirectory() {
    return (this.fileDescriptor.getAttributes() & DIRECTORY_MASK)
        == DIRECTORY_MASK;
  }

  /**
   * Is file type ascii.
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
   * Get file descriptor.
   *
   * @return file descriptor
   */
  public DWRBFFileDescriptor getFD() {
    return fileDescriptor;
  }

  /**
   * Set file descriptor.
   *
   * @param descriptor file descriptor
   */
  public void setFd(final DWRBFFileDescriptor descriptor) {
    this.fileDescriptor = descriptor;
  }

  /**
   * Get file descriptor logical sector number.
   *
   * @return logical sector number
   */
  @SuppressWarnings("unused")
  public int getFdLsn() {
    return fdLsn;
  }

  /**
   * Set file descriptor logical sector number.
   *
   * @param lsn logical sector number
   */
  public void setFdLsn(final int lsn) {
    this.fdLsn = lsn;
  }

  /**
   * Get pretty date modified.
   *
   * @return date modified
   */
  @SuppressWarnings("unused")
  public String getPrettyDateModified() {
    return DWUtils.pretty5ByteDateTime(this.fileDescriptor.getDateModified());
  }

  /**
   * get pretty date created.
   *
   * @return date created
   */
  @SuppressWarnings("unused")
  public String getPrettyDateCreated() {
    return DWUtils.pretty3ByteDate(this.fileDescriptor.getDateCreated());
  }
}
