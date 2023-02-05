package com.groupunix.drivewireserver.dwdisk;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWVDKDiskHeader {
  /**
   * Header offset to flags.
   */
  public static final int FLAGS_OFFSET = 10;
  /**
   * Flags bit mask for write protect.
   */
  public static final int WRITE_PROTECT_FLAG = 0x1;
  /**
   * Header offset to sides.
   */
  public static final int SIDES_OFFSET = 9;
  /**
   * Header offset to tracks.
   */
  public static final int TRACKS_OFFSET = 8;
  /**
   * Header offset to source version.
   */
  public static final int SOURCE_VERSION_OFFSET = 7;
  /**
   * Header offset to source id.
   */
  public static final int SOURCE_ID_OFFSET = 6;
  /**
   * Header offset to version compatible.
   */
  public static final int VERSION_COMPATIBLE_OFFSET = 5;
  /**
   * Header offset to version.
   */
  public static final int VERSION_OFFSET = 4;
  /**
   * Overhead (bytes) to header data.
   */
  public static final int HEADER_OVERHEAD = 4;
  /**
   * Header data byte array.
   */
  private final byte[] data;

  /**
   * VDK Disk header constructor.
   *
   * @param hBuff header buffer
   */
  public DWVDKDiskHeader(final byte[] hBuff) {
    // leave 4 bytes at start just so docs and calls match up
    this.data = new byte[hBuff.length + HEADER_OVERHEAD];
    System.arraycopy(hBuff, 0, this.data, HEADER_OVERHEAD, hBuff.length);
  }

  /**
   * Get version.
   *
   * @return version
   */
  public int getVersion() {
    return (BYTE_MASK & data[VERSION_OFFSET]);
  }

  /**
   * Get version compatible.
   *
   * @return version compatible
   */
  @SuppressWarnings("unused")
  public int getVersionCompatible() {
    return (BYTE_MASK & data[VERSION_COMPATIBLE_OFFSET]);
  }

  /**
   * Get source id.
   *
   * @return source id
   */
  @SuppressWarnings("unused")
  public int getSourceID() {
    return (BYTE_MASK & data[SOURCE_ID_OFFSET]);
  }

  /**
   * Get source version.
   *
   * @return source version
   */
  @SuppressWarnings("unused")
  public int getSourceVersion() {
    return (BYTE_MASK & data[SOURCE_VERSION_OFFSET]);
  }

  /**
   * Get disk tracks.
   *
   * @return disk tracks
   */
  public int getTracks() {
    return (BYTE_MASK & data[TRACKS_OFFSET]);
  }

  /**
   * Get disk sides.
   *
   * @return number of disk sides
   */
  public int getSides() {
    return (BYTE_MASK & data[SIDES_OFFSET]);
  }

  /**
   * Get header flags.
   *
   * @return header flags
   */
  @SuppressWarnings("unused")
  public int getFlags() {
    return (BYTE_MASK & data[FLAGS_OFFSET]);
  }

  /**
   * Get header length.
   *
   * @return header length (bytes)
   */
  public int getHeaderLen() {
    return this.data.length;
  }

  /**
   * Is disk write protected.
   *
   * @return true if write protected
   */
  public boolean isWriteProtected() {
    return (data[FLAGS_OFFSET] & WRITE_PROTECT_FLAG) == WRITE_PROTECT_FLAG;
  }
}
