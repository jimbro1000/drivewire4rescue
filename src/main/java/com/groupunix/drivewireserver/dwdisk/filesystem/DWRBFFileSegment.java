package com.groupunix.drivewireserver.dwdisk.filesystem;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWRBFFileSegment {
  /**
   * Offset to size bytes.
   */
  public static final int SIZE_OFFSET = 3;
  /**
   * Segment sector number.
   */
  private int sector;
  /**
   * Segment size.
   */
  private int segmentSize;

  /**
   * RBF File Segment.
   *
   * @param data content data
   * @param i segment offset
   */
  public DWRBFFileSegment(final byte[] data, final int i) {
    this.setLsn((data[i] & BYTE_MASK) * BYTE_SHIFT * BYTE_SHIFT
        + (data[i + 1] & BYTE_MASK) * BYTE_SHIFT
        + (data[i + 2] & BYTE_MASK));
    this.setSize((data[i + SIZE_OFFSET] & BYTE_MASK) * BYTE_SHIFT
        + (data[i + SIZE_OFFSET + 1] & BYTE_MASK));
  }

  /**
   * Get logical sector number.
   *
   * @return logical sector number
   */
  public int getLsn() {
    return sector;
  }

  /**
   * Set logical sector number.
   *
   * @param lsn logical sector number
   */
  public void setLsn(final int lsn) {
    this.sector = lsn;
  }

  /**
   * Get size.
   *
   * @return size (bytes)
   */
  public int getSize() {
    return segmentSize;
  }

  /**
   * Set size.
   *
   * @param size size (bytes)
   */
  public void setSize(final int size) {
    this.segmentSize = size;
  }

  /**
   * Is segment used.
   *
   * @return bool
   */
  public boolean isUsed() {
    return this.sector + this.segmentSize > 0;
  }
}
