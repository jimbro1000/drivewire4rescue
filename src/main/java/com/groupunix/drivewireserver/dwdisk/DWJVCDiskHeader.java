package com.groupunix.drivewireserver.dwdisk;

public class DWJVCDiskHeader {
  /**
   * Byte mask.
   */
  private static final int BYTE_MASK = 0xFF;
  /**
   * Default sector size (bytes).
   */
  public static final int DEFAULT_SECTOR_SIZE = 256;
  /**
   * Minimum sector size (bytes).
   */
  public static final int MINIMUM_SECTOR_SIZE = 128;
  /**
   * Default sectors per track.
   */
  public static final int DEFAULT_SECTORS_PER_TRACK = 18;
  /**
   * Default sides to disk.
   */
  public static final int DEFAULT_DISK_SIDES = 1;
  /**
   * Default first sector number.
   */
  public static final int DEFAULT_FIRST_SECTOR = 1;
  /**
   * Byte offset to attributes byte.
   */
  public static final int ATTRIBUTES_OFFSET = 4;
  /**
   * Byte offset to first sector byte.
   */
  public static final int FIRST_SECTOR_OFFSET = 3;
  /**
   * Byte offset to sector size byte.
   */
  public static final int SECTOR_SIZE_OFFSET = 2;
  /**
   * Byte offset to disk sides byte.
   */
  public static final int DISK_SIDES_OFFSET = 1;
  /**
   * Header data.
   */
  private byte[] headerData;

  /**
   * Set header data.
   *
   * @param data header data bytes
   */
  public void setData(final byte[] data) {
    this.headerData = new byte[data.length];
    System.arraycopy(data, 0, this.headerData, 0, data.length);
  }

  /**
   * Get sectors per track.
   *
   * @return sectors per track
   */
  public int getSectorsPerTrack() {
    if ((headerData == null) || (headerData.length < 1)) {
      return DEFAULT_SECTORS_PER_TRACK;
    }
    return (BYTE_MASK & headerData[0]);
  }

  /**
   * Get disk sides.
   *
   * @return number of disk sides
   */
  public int getSides() {
    if ((headerData == null) || (headerData.length <= DISK_SIDES_OFFSET)) {
      return DEFAULT_DISK_SIDES;
    }
    return (BYTE_MASK & headerData[DISK_SIDES_OFFSET]);
  }

  /**
   * Get sector size (bytes).
   *
   * @return sector size
   */
  public int getSectorSize() {
    if ((headerData == null) || (headerData.length <= SECTOR_SIZE_OFFSET)) {
      return DEFAULT_SECTOR_SIZE;
    }
    return (MINIMUM_SECTOR_SIZE << this.headerData[SECTOR_SIZE_OFFSET]);
  }

  /**
   * Get first sector.
   *
   * @return first sector byte
   */
  public int getFirstSector() {
    if ((headerData == null) || (headerData.length <= FIRST_SECTOR_OFFSET)) {
      return DEFAULT_FIRST_SECTOR;
    }
    return (BYTE_MASK & headerData[FIRST_SECTOR_OFFSET]);
  }

  /**
   * Get sector attributes.
   *
   * @return attributes byte
   */
  public int getSectorAttributes() {
    if ((headerData == null) || (headerData.length <= ATTRIBUTES_OFFSET)) {
      return 0;
    }
    return (BYTE_MASK & headerData[ATTRIBUTES_OFFSET]);
  }
}
