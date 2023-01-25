package com.groupunix.drivewireserver.dwdisk.filesystem;

public class DWLW16FileSystemInode {

  private int filetype;
  private int permissions;

  private int mode;
  private int links;        // number of links
  private int uid;        // uid of the owner
  private int gid;        // gid of the owner
  private int filesize;      // size of the file in bytes
  private int atime;        // last access time - updates optional
  private int mtime;        // last modify tim
  private int ctime;        // last status change time
  private int[] dblocks = new int[14];    // direct blocks
  private int[] indirblocks = new int[2];    // indirect blocks
  private int[] dblindirblocks = new int[4];  // double indirect blocks
  private int iNodeNum;

  public DWLW16FileSystemInode(int iNodeNum, byte[] data) {
    this.setInodeNum(iNodeNum);

    this.setMode(get2(0, data));
    this.setLinks(get2(2, data));
    this.setUid(get2(4, data));
    this.setGid(get2(6, data));
    this.setFilesize(get4(8, data));
    this.setAtime(get4(12, data));
    this.setMtime(get4(16, data));
    this.setCtime(get4(20, data));

    for (int i = 0; i < 14; i++)
      dblocks[i] = get2(24 + i * 2, data);

    for (int i = 0; i < 2; i++)
      indirblocks[i] = get2(52 + i * 2, data);

    for (int i = 0; i < 4; i++)
      dblindirblocks[i] = get2(56 + i * 2, data);

  }


  @Override
  public String toString() {
    String res = new String();

    res += "inode " + this.getInodeNum() + System.getProperty("line.separator");
    res += "mode  " + this.getMode() + System.getProperty("line.separator");
    res += "links " + this.getLinks() + System.getProperty("line.separator");
    res += "uid   " + this.getUid() + System.getProperty("line.separator");
    res += "gid   " + this.getGid() + System.getProperty("line.separator");

    res += "size  " + this.getFilesize() + System.getProperty("line.separator");

    res += "atime " + this.getAtime() + System.getProperty("line.separator");
    res += "mtime " + this.getMtime() + System.getProperty("line.separator");
    res += "ctime " + this.getCtime() + System.getProperty("line.separator");

    return res;
  }


  private int get4(int i, byte[] data) {
    return ((0xff & data[i]) * 256 * 256 * 256) + ((0xff & data[i + 1]) * 256 * 256) + ((0xff & data[i + 2]) * 256) + (0xff & data[i + 3]);
  }

  private int get2(int i, byte[] data) {
    return ((0xff & data[i]) * 256) + (0xff & data[i + 1]);
  }

  public int getFiletype() {
    return filetype;
  }

  public void setFiletype(int filetype) {
    this.filetype = filetype;
  }

  public int getPermissions() {
    return permissions;
  }

  public void setPermissions(int permissions) {
    this.permissions = permissions;
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int mode) {
    this.mode = mode;
  }

  public int getLinks() {
    return links;
  }

  public void setLinks(int links) {
    this.links = links;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
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
   * @param gid gId
   */
  public void setGid(final int gid) {
    this.gid = gid;
  }

  /**
   * Get file size.
   *
   * @return file size (bytes)
   */
  public int getFilesize() {
    return filesize;
  }

  /**
   * Set File size.
   *
   * @param filesize file size (bytes)
   */
  public void setFilesize(final int filesize) {
    this.filesize = filesize;
  }

  /**
   * Get aTime.
   *
   * @return aTime
   */
  public int getAtime() {
    return atime;
  }

  /**
   * Set aTime.
   *
   * @param atime aTime
   */
  public void setAtime(final int atime) {
    this.atime = atime;
  }

  /**
   * Get mTime.
   *
   * @return mTime
   */
  public int getMtime() {
    return mtime;
  }

  /**
   * Set mTime.
   *
   * @param mtime mTime
   */
  public void setMtime(final int mtime) {
    this.mtime = mtime;
  }

  /**
   * Get cTime.
   *
   * @return cTime
   */
  public int getCtime() {
    return ctime;
  }

  /**
   * Set cTime.
   *
   * @param ctime cTime
   */
  public void setCtime(final int ctime) {
    this.ctime = ctime;
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
   * @param iNodeNum iNode number
   */
  public void setInodeNum(final int iNodeNum) {
    this.iNodeNum = iNodeNum;
  }
}
