package com.groupunix.drivewireserver.dwdisk;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWDMKDiskHeader {
  /**
   * Header offset for write protected status.
   */
  public static final int WRITE_PROTECTED = 0;
  /**
   * Header offset for number of tracks.
   */
  public static final int NUMBER_OF_TRACKS = 1;
  /**
   * Header offset for lsb track length.
   */
  public static final int TRACK_LENGTH_LSB = 2;
  /**
   * Header offset for msb track length.
   */
  public static final int TRACK_LENGTH_MSB = 3;
  /**
   * Header offset for disk options.
   */
  public static final int DISK_OPTIONS = 4;
  /**
   * Length of header byte array.
   */
  public static final int HEADER_SIZE = 16;
  /**
   * Write protected byte value.
   */
  public static final int WP_VALUE = 0xFF;
  /**
   * Disk signature offset start.
   */
  private static final int DISK_SIGNATURE_OFFSET = 12;
  /**
   * Disk signature offset end.
   */
  private static final int DISK_SIGNATURE_OFFSET_END = 15;
  /**
   * Signature byte 1.
   */
  private static final int SIGNATURE_1 = 0x12;
  /**
   * Signature byte 2.
   */
  private static final int SIGNATURE_2 = 0x34;
  /**
   * Signature byte 3.
   */
  private static final int SIGNATURE_3 = 0x56;
  /**
   * Signature byte 4.
   */
  private static final int SIGNATURE_4 = 0x78;
  /**
   * Bit mask for bit 6 (64).
   */
  private static final int BIT_MASK_6 = 0x40;
  /**
   * Bit mask for bit 4 (16).
   */
  private static final int BIT_MASK_4 = 0x10;
  /**
   * Disk header data.
   */
  private final byte[] header = new byte[HEADER_SIZE];

  /**
   * DMK Disk Header constructor.
   *
   * @param hdr header byte array
   */
  public DWDMKDiskHeader(final byte[] hdr) {
    System.arraycopy(hdr, 0, this.header, 0, HEADER_SIZE);
  }

  /**
   * Is disk write protected.
   *
   * @return true if write protected
   */
  public boolean isWriteProtected() {
    return (BYTE_MASK & this.header[WRITE_PROTECTED]) == WP_VALUE;
  }

  /**
   * Get number of tracks.
   *
   * @return number of tracks on disk
   */
  public int getTracks() {
    return BYTE_MASK & this.header[NUMBER_OF_TRACKS];
  }

  /**
   * Get disk track length.
   *
   * @return track length (bytes)
   */
  public int getTrackLength() {
    return (BYTE_MASK & this.header[TRACK_LENGTH_MSB]) * BYTE_SHIFT
        + (BYTE_MASK & this.header[TRACK_LENGTH_LSB]);
  }

  /**
   * Get disk options.
   *
   * @return disk options byte
   */
  @SuppressWarnings("unused")
  public int getOptions() {
    return BYTE_MASK & this.header[DISK_OPTIONS];
  }

  /**
   * Is disk single sided.
   *
   * @return true if single sided
   */
  public boolean isSingleSided() {
    return ((byte) BIT_MASK_4
        & this.header[DISK_OPTIONS]) == (byte) BIT_MASK_4;
  }

  /**
   * Is disk single density.
   *
   * @return true if single density
   */
  public boolean isSingleDensity() {
    return ((byte) BIT_MASK_6
        & this.header[DISK_OPTIONS]) == (byte) BIT_MASK_6;
  }

  // bytes 5-11 reserved

  /**
   * Is this a real disk.
   *
   * @return reality
   */
  @SuppressWarnings("unused")
  public boolean isRealDisk() {
    // bytes 12-15 for real disk or not...
    // not sure i've got this right, or if it even matters
    return (BYTE_MASK & header[DISK_SIGNATURE_OFFSET]) == SIGNATURE_1
        && (BYTE_MASK & header[DISK_SIGNATURE_OFFSET + 1]) == SIGNATURE_2
        && (BYTE_MASK & header[DISK_SIGNATURE_OFFSET + 2]) == SIGNATURE_3
        && (BYTE_MASK & header[DISK_SIGNATURE_OFFSET_END]) == SIGNATURE_4;
  }

  /**
   * Get sides of disk.
   *
   * @return sides
   */
  public int getSides() {
    if (isSingleSided()) {
      return 1;
    }
    return 2;
  }

  /**
   * Get disk density.
   *
   * @return 1 (single density) or 2 (double density)
   */
  public Object getDensity() {
    if (isSingleDensity()) {
      return 1;
    }
    return 2;
  }
}
