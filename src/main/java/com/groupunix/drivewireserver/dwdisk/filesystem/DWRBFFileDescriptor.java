package com.groupunix.drivewireserver.dwdisk.filesystem;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWRBFFileDescriptor {
  /**
   * R bit mask.
   */
  public static final int READ_BIT_MASK = 0x01;
  /**
   * W bit mask.
   */
  public static final int WRITE_BIT_MASK = 0x02;
  /**
   * E bit mask.
   */
  public static final int EXECUTE_BIT_MASK = 0x04;
  /**
   * PR bit mask.
   */
  public static final int PUBLIC_READ_BIT_MASK = 0x08;
  /**
   * PW bit mask.
   */
  public static final int PUBLIC_WRITE_BIT_MASK = 0x10;
  /**
   * PE bit mask.
   */
  public static final int PUBLIC_EXECUTE_BIT_MASK = 0x20;
  /**
   * D bit mask.
   */
  public static final int DIRECTORY_BIT_MASK = 0x80;
  /**
   * S bit mask.
   */
  public static final int SHARE_BIT_MASK = 0x40;
  /**
   * Segments count.
   */
  public static final int SEGMENTS_LENGTH = 48;
  /**
   * Segments offset.
   */
  public static final int SEGMENTS_OFFSET = 16;
  /**
   * Segment length (bytes).
   */
  public static final int SEGMENT_LENGTH = 5;
  /**
   * modified date offset.
   */
  public static final int MODIFIED_OFFSET = 3;
  /**
   * created date offset.
   */
  public static final int CREATED_OFFSET = 13;
  /**
   * Bytes length for date.
   */
  public static final int DATE_LENGTH = 3;
  /**
   * Bytes length for date time.
   */
  public static final int DATETIME_LENGTH = 5;
  /**
   * Attribute offset.
   */
  public static final int ATTRIBUTES_OFFSET = 0;
  /**
   * Link count offset.
   */
  public static final int LINKS_OFFSET = 8;
  /**
   * Owner MSB offset.
   */
  public static final int OWNER_MSB_OFFSET = 1;
  /**
   * Owner LSB offset.
   */
  public static final int OWNER_LSB_OFFSET = 2;
  /**
   * Filesize MSB offset.
   */
  public static final int FILESIZE_MSB_OFFSET = 9;
  /**
   * Filesize LSB offset.
   */
  public static final int FILESIZE_LSB_OFFSET = 12;
  /**
   * Date time file modified.
   */
  private final byte[] dateModified = new byte[DATETIME_LENGTH];
  /**
   * Date file creator.
   */
  private final byte[] dateCreated = new byte[DATE_LENGTH];
  /**
   * File segment array.
   */
  private final DWRBFFileSegment[] segmentList
      = new DWRBFFileSegment[SEGMENTS_LENGTH];
  /**
   * Attributes.
   */
  private int attributes;
  /**
   * Owner id.
   */
  private int owner;
  /**
   * Link count.
   */
  private int linkCount;
  /**
   * File size.
   */
  private int filesize;

  /**
   * RBF file descriptor.
   *
   * @param data file data
   */
  public DWRBFFileDescriptor(final byte[] data) {
    this.setAttributes(data[ATTRIBUTES_OFFSET] & BYTE_MASK);
    this.setOwner(
        (data[OWNER_MSB_OFFSET] & BYTE_MASK) * BYTE_SHIFT
            + (data[OWNER_LSB_OFFSET] & BYTE_MASK)
    );
    System.arraycopy(
        data, MODIFIED_OFFSET, this.dateModified, 0, DATETIME_LENGTH
    );
    this.setLinkCount(BYTE_MASK & data[LINKS_OFFSET]);
    this.setFilesize(
        (data[FILESIZE_MSB_OFFSET] & BYTE_MASK)
            * BYTE_SHIFT * BYTE_SHIFT * BYTE_SHIFT
            + (data[FILESIZE_MSB_OFFSET + 1] & BYTE_MASK)
            * BYTE_SHIFT * BYTE_SHIFT
            + (data[FILESIZE_MSB_OFFSET + 2] & BYTE_MASK)
            * BYTE_SHIFT
            + (data[FILESIZE_LSB_OFFSET] & BYTE_MASK)
    );
    System.arraycopy(
        data, CREATED_OFFSET, this.dateCreated, 0, DATE_LENGTH
    );
    for (int i = 0; i < SEGMENTS_LENGTH; i++) {
      this.segmentList[i] = new DWRBFFileSegment(
          data,
          SEGMENTS_OFFSET + (i * SEGMENT_LENGTH)
      );
    }
  }

  /**
   * Get attributes byte.
   *
   * @return attributes
   */
  public int getAttributes() {
    return attributes;
  }

  /**
   * Set attributes byte.
   *
   * @param attr attributes
   */
  public void setAttributes(final int attr) {
    this.attributes = attr;
  }

  /**
   * Get owner.
   *
   * @return owner
   */
  @SuppressWarnings("unused")
  public int getOwner() {
    return owner;
  }

  /**
   * Set owner.
   *
   * @param ownerId owner
   */
  public void setOwner(final int ownerId) {
    this.owner = ownerId;
  }

  /**
   * Get link count.
   *
   * @return link count
   */
  @SuppressWarnings("unused")
  public int getLinkCount() {
    return linkCount;
  }

  /**
   * Set link count.
   *
   * @param links link count
   */
  public void setLinkCount(final int links) {
    this.linkCount = links;
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
   * Set file size.
   *
   * @param size file size (bytes)
   */
  public void setFilesize(final int size) {
    this.filesize = size;
  }

  /**
   * Get segment array.
   *
   * @return array of RBF file segment
   */
  public DWRBFFileSegment[] getSegmentList() {
    return this.segmentList;
  }

  /**
   * Get date modified.
   *
   * @return modified date
   */
  public byte[] getDateModified() {
    return this.dateModified;
  }

  /**
   * Get date created.
   *
   * @return created date
   */
  public byte[] getDateCreated() {
    return this.dateCreated;
  }

  /**
   * Test if directory bit set.
   *
   * @return directory set
   */
  @SuppressWarnings("unused")
  public boolean isAttrD() {
    return (this.attributes & DIRECTORY_BIT_MASK) == DIRECTORY_BIT_MASK;
  }

  /**
   * Test if share bit set.
   *
   * @return share set
   */
  @SuppressWarnings("unused")
  public boolean isAttrS() {
    return (this.attributes & SHARE_BIT_MASK) == SHARE_BIT_MASK;
  }

  /**
   * Test if public execute bit set.
   *
   * @return public execute set
   */
  @SuppressWarnings("unused")
  public boolean isAttrPE() {
    return (this.attributes & PUBLIC_EXECUTE_BIT_MASK)
        == PUBLIC_EXECUTE_BIT_MASK;
  }

  /**
   * Test if public write bit set.
   *
   * @return public write set
   */
  @SuppressWarnings("unused")
  public boolean isAttrPW() {
    return (this.attributes & PUBLIC_WRITE_BIT_MASK) == PUBLIC_WRITE_BIT_MASK;
  }

  /**
   * Test if public read bit set.
   *
   * @return public read set
   */
  @SuppressWarnings("unused")
  public boolean isAttrPR() {
    return (this.attributes & PUBLIC_READ_BIT_MASK) == PUBLIC_READ_BIT_MASK;
  }

  /**
   * Test if execute bit set.
   *
   * @return execute set
   */
  @SuppressWarnings("unused")
  public boolean isAttrE() {
    return (this.attributes & EXECUTE_BIT_MASK) == EXECUTE_BIT_MASK;
  }

  /**
   * Test if write attribute set.
   *
   * @return write set
   */
  @SuppressWarnings("unused")
  public boolean isAttrW() {
    return (this.attributes & WRITE_BIT_MASK) == WRITE_BIT_MASK;
  }

  /**
   * Test if read attribute set.
   *
   * @return read set
   */
  @SuppressWarnings("unused")
  public boolean isAttrR() {
    return (this.attributes & READ_BIT_MASK) == READ_BIT_MASK;
  }
}
