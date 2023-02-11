package com.groupunix.drivewireserver.dwdisk.filesystem;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWLW16FileSystemInode {
  /**
   * Change time offset.
   */
  private static final int CHANGE_TIME_OFFSET = 20;
  /**
   * Modified time offset.
   */
  private static final int MODIFY_TIME_OFFSET = 16;
  /**
   * Accessed time offset.
   */
  private static final int ACCESS_TIME_OFFSET = 12;
  /**
   * File size offset.
   */
  private static final int FILE_SIZE_OFFSET = 8;
  /**
   * Gid offset.
   */
  private static final int GID_OFFSET = 6;
  /**
   * Uid offset.
   */
  private static final int USERID_OFFSET = 4;
  /**
   * Link count offset.
   */
  private static final int LINKS_OFFSET = 2;
  /**
   * Mode offset.
   */
  private static final int MODE_OFFSET = 0;
  /**
   * Number of bytes in double word.
   */
  public static final int DOUBLE_WORD = 4;
  /**
   * File type property.
   */
  private int fileType;
  /**
   * Permissions property.
   */
  private int filePermissions;
  /**
   * Mode property.
   */
  private int fileMode;
  /**
   * Number of links.
   */
  private int linksCount;
  /**
   * User id of owner.
   */
  private int userId;
  /**
   * gId of the owner.
   */
  private int gid;
  /**
   * File size (bytes).
   */
  private int fileSize;
  /**
   * Last access timestamp.
   */
  private int accessTime;
  /**
   * Last modified timestamp.
   */
  private int modifyTime;
  /**
   * Last status change timestamp.
   */
  private int changeTime;
//  /**
//   * direct blocks.
//   */
//  private final int[] directBlocks = new int[14];
//  /**
//   * indirect blocks.
//   */
//  private final int[] indirectBlocks = new int[2];
//  /**
//   * double indirection blocks.
//   */
//  private final int[] doubleIndirectBlocks = new int[4];
  /**
   * iNodeNum.
   */
  private int iNodeNum;

  /**
   * LWL16 File System iNode constructor.
   *
   * @param iNode iNode number
   * @param data byte array
   */
  public DWLW16FileSystemInode(final int iNode, final byte[] data) {
    this.setInodeNum(iNode);
    this.setFileMode(get2(MODE_OFFSET, data));
    this.setLinksCount(get2(LINKS_OFFSET, data));
    this.setUserId(get2(USERID_OFFSET, data));
    this.setGid(get2(GID_OFFSET, data));
    this.setFileSize(get4(FILE_SIZE_OFFSET, data));
    this.setAccessTime(get4(ACCESS_TIME_OFFSET, data));
    this.setModifyTime(get4(MODIFY_TIME_OFFSET, data));
    this.setChangeTime(get4(CHANGE_TIME_OFFSET, data));
//    for (int i = 0; i < 14; i++) {
//      directBlocks[i] = get2(24 + i * 2, data);
//    }
//    for (int i = 0; i < 2; i++) {
//      indirectBlocks[i] = get2(52 + i * 2, data);
//    }
//    for (int i = 0; i < 4; i++) {
//      doubleIndirectBlocks[i] = get2(56 + i * 2, data);
//    }
  }

  /**
   * toString implementation.
   *
   * @return readable string of content
   */
  @Override
  public String toString() {
    String res = "";
    res += "inode " + this.getInodeNum()
        + System.getProperty("line.separator");
    res += "mode  " + this.getFileMode()
        + System.getProperty("line.separator");
    res += "links " + this.getLinksCount()
        + System.getProperty("line.separator");
    res += "uid   " + this.getUserId()
        + System.getProperty("line.separator");
    res += "gid   " + this.getGid()
        + System.getProperty("line.separator");
    res += "size  " + this.getFileSize()
        + System.getProperty("line.separator");
    res += "atime " + this.getAccessTime()
        + System.getProperty("line.separator");
    res += "mtime " + this.getModifyTime()
        + System.getProperty("line.separator");
    res += "ctime " + this.getChangeTime()
        + System.getProperty("line.separator");
    return res;
  }

  /**
   * Get 4 bytes (double word) from array at index.
   * <p>
   *   Assumes big endian representation.
   *   Only extracts bytes in range, otherwise assumes a 0
   *   for each out of range byte
   * </p>
   * @param index start index
   * @param data byte array
   * @return double word value
   */
  private int get4(final int index, final byte[] data) {
    int result = 0;
    for (int i = 0; i < DOUBLE_WORD; ++i) {
      result *= BYTE_SHIFT;
      if (index + i >= 0 && index + i < data.length) {
        result += BYTE_MASK & data[index + i];
      }
    }
    return result;
  }

  /**
   * Get 2 bytes (word) from array at index.
   * <p>
   *   Assumes big endian representation
   * </p>
   * @param index index
   * @param data byte array
   * @return word value
   */
  private int get2(final int index, final byte[] data) {
    if (index == data.length - 1) {
      return (BYTE_MASK & data[index]) * BYTE_SHIFT;
    }
    return (BYTE_MASK & data[index]) * BYTE_SHIFT
        + (BYTE_MASK & data[index + 1]);
  }

  /**
   * Get filetype.
   *
   * @return file type
   */
  @SuppressWarnings("unused")
  public int getFileType() {
    return fileType;
  }

  /**
   * Set filetype.
   *
   * @param filetype filetype
   */
  @SuppressWarnings("unused")
  public void setFileType(final int filetype) {
    this.fileType = filetype;
  }

  /**
   * Get permissions.
   *
   * @return permissions
   */
  @SuppressWarnings("unused")
  public int getFilePermissions() {
    return filePermissions;
  }

  /**
   * set permissions.
   *
   * @param permissions permissions
   */
  @SuppressWarnings("unused")
  public void setFilePermissions(final int permissions) {
    this.filePermissions = permissions;
  }

  /**
   * Get mode.
   *
   * @return mode
   */
  public int getFileMode() {
    return fileMode;
  }

  /**
   * Set mode.
   *
   * @param mode mode
   */
  public void setFileMode(final int mode) {
    this.fileMode = mode;
  }

  /**
   * Get links.
   *
   * @return number of links
   */
  public int getLinksCount() {
    return linksCount;
  }

  /**
   * Set links.
   *
   * @param links number of links
   */
  public void setLinksCount(final int links) {
    this.linksCount = links;
  }

  /**
   * Get uId.
   *
   * @return user id
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Set uId.
   *
   * @param uid user id
   */
  public void setUserId(final int uid) {
    this.userId = uid;
  }

  /**
   * get gId.
   *
   * @return gId
   */
  public int getGid() {
    return gid;
  }

  /**
   * Set gId.
   *
   * @param identifier gId
   */
  public void setGid(final int identifier) {
    this.gid = identifier;
  }

  /**
   * Get file size.
   *
   * @return file size (bytes)
   */
  public int getFileSize() {
    return fileSize;
  }

  /**
   * Set File size.
   *
   * @param filesize file size (bytes)
   */
  public void setFileSize(final int filesize) {
    this.fileSize = filesize;
  }

  /**
   * Get aTime.
   *
   * @return aTime
   */
  public int getAccessTime() {
    return accessTime;
  }

  /**
   * Set aTime.
   *
   * @param atime aTime
   */
  public void setAccessTime(final int atime) {
    this.accessTime = atime;
  }

  /**
   * Get mTime.
   *
   * @return mTime
   */
  public int getModifyTime() {
    return modifyTime;
  }

  /**
   * Set mTime.
   *
   * @param mtime mTime
   */
  public void setModifyTime(final int mtime) {
    this.modifyTime = mtime;
  }

  /**
   * Get cTime.
   *
   * @return cTime
   */
  public int getChangeTime() {
    return changeTime;
  }

  /**
   * Set cTime.
   *
   * @param ctime cTime
   */
  public void setChangeTime(final int ctime) {
    this.changeTime = ctime;
  }

  /**
   * Get iNode number.
   *
   * @return iNode number
   */
  public int getInodeNum() {
    return iNodeNum;
  }

  /**
   * Set iNode number.
   *
   * @param iNode iNode number
   */
  public void setInodeNum(final int iNode) {
    this.iNodeNum = iNode;
  }
}
