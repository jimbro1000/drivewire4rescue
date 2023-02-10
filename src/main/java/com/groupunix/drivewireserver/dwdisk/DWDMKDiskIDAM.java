package com.groupunix.drivewireserver.dwdisk;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;
import static com.groupunix.drivewireserver.DWDefs.LOW_SIX_BITS;

public class DWDMKDiskIDAM {
  /**
   * Offset to sector size.
   */
  private static final int SECTOR_SIZE_OFFSET = 3;
  /**
   * Offset to sector count.
   */
  private static final int SECTOR_COUNT_OFFSET = 2;
  /**
   * Offset to disk sides.
   */
  private static final int DISK_SIDES_OFFSET = 1;
  /**
   * Offset to track count.
   */
  private static final int TRACK_COUNT_OFFSET = 0;
  /**
   * Minimum exponent for calculating sector size.
   */
  public static final int MINIMUM_SECTOR_SIZE_EXPONENT = 7;
  /**
   * IDAM data byte array.
   */
  private final byte[] idamData;
  /**
   * IDAM ptr most-significant byte.
   */
  private final byte idamPtrMsb;
  /**
   * IDAM ptr least-significant byte.
   */
  private final byte idamPtrLsb;

  /**
   * DMK Disk IDAM constructor.
   *
   * @param msb most significant ptr byte
   * @param lsb least significant ptr byte
   * @param data IDAM data
   */
  public DWDMKDiskIDAM(final byte msb, final byte lsb, final byte[] data) {
    this.idamData = data;
    this.idamPtrMsb = msb;
    this.idamPtrLsb = lsb;
  }

  /**
   * Get tracks.
   *
   * @return tracks
   */
  public int getTrack() {
    return BYTE_MASK & idamData[TRACK_COUNT_OFFSET];
  }

  /**
   * Get sides.
   *
   * @return sides
   */
  public int getSide() {
    return BYTE_MASK & idamData[DISK_SIDES_OFFSET];
  }

  /**
   * Get sectors.
   *
   * @return sectors
   */
  public int getSector() {
    return BYTE_MASK & idamData[SECTOR_COUNT_OFFSET];
  }

  /**
   * Get sector size.
   *
   * @return sector size (bytes)
   */
  public int getSectorSize() {
    return (int) Math.pow(
        2,
        MINIMUM_SECTOR_SIZE_EXPONENT + idamData[SECTOR_SIZE_OFFSET]
    );
  }

  /**
   * Is disk double density.
   *
   * @return true if double density
   */
  @SuppressWarnings("unused")
  public boolean isDoubleDensity() {
    return (LOW_SIX_BITS & this.idamPtrMsb) == LOW_SIX_BITS;
  }

  /**
   * Get IDAM ptr.
   *
   * @return ptr
   */
  public int getPtr() {
    return (LOW_SIX_BITS & this.idamPtrMsb) * BYTE_SHIFT
        + (BYTE_MASK & this.idamPtrLsb);
  }
}
