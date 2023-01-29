package com.groupunix.drivewireserver.dwdisk.filesystem;

public abstract class DWFileSystemDirEntry {
  /**
   * Source data.
   */
  private byte[] data;

  /**
   * File system directory entry.
   *
   * @param buf byte data
   */
  public DWFileSystemDirEntry(final byte[] buf) {
    if (buf != null) {
      this.data = new byte[buf.length];
      System.arraycopy(buf, 0, this.data, 0, buf.length);
    }
  }

  /**
   * Get source data byte array.
   *
   * @return source data
   */
  protected byte[] getData() {
    byte[] result = new byte[data.length];
    System.arraycopy(data, 0, result, 0, data.length);
    return result;
  }

  /**
   * Get file name.
   *
   * @return file name
   */
  public abstract String getFileName();

  /**
   * Get file extension.
   *
   * @return file extension
   */
  public abstract String getFileExt();

  /**
   * Get file path.
   *
   * @return file path
   */
  @SuppressWarnings("unused")
  public abstract String getFilePath();

  /**
   * Get pretty file type.
   *
   * @return file type description
   */
  @SuppressWarnings("unused")
  public abstract String getPrettyFileType();

  /**
   * Get file type.
   *
   * @return file byte byte
   */
  @SuppressWarnings("unused")
  public abstract int getFileType();

  /**
   * Get parent directory entry.
   *
   * @return directory entry
   */
  @SuppressWarnings("unused")
  public abstract DWFileSystemDirEntry getParentEntry();

  /**
   * Test if directory flag set.
   *
   * @return true if directory
   */
  public abstract boolean isDirectory();

  /**
   * Test if file type is ascii.
   *
   * @return true if ascii
   */
  @SuppressWarnings("unused")
  public abstract boolean isAscii();
}
