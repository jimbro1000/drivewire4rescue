package com.groupunix.drivewireserver.dwdisk;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;
import static com.groupunix.drivewireserver.DWDefs.LOW_SIX_BITS;

public class DWDMKDiskTrack {
  /**
   * Maximum permitted number of IDAM records.
   */
  public static final int MAXIMUM_IDAM = 64;
  /**
   * ID address mark record size.
   */
  public static final int IDAM_SIZE = 6;
  /**
   * Track data.
   */
  private final byte[] trackData;

  /**
   * DMK Disk Track constructor.
   *
   * @param data track data byte array
   */
  public DWDMKDiskTrack(final byte[] data) {
    this.trackData = new byte[data.length];
    System.arraycopy(data, 0, this.trackData, 0, data.length);
  }

  /**
   * Get ID Address Mark by index.
   *
   * @param index IDAM index
   * @return IDAM record
   */
  public DWDMKDiskIDAM getIDAM(final int index) {
    byte msb = this.trackData[(index * 2) + 1];
    byte lsb = this.trackData[(index * 2)];
    byte[] iBuf = new byte[IDAM_SIZE];
    System.arraycopy(
        this.trackData, getIDAMPtr(msb, lsb) + 1, iBuf, 0, IDAM_SIZE
    );
    return new DWDMKDiskIDAM(msb, lsb, iBuf);
  }

  /**
   * Get ID Address Mark pointer value.
   *
   * @param msb ptr most-significant byte
   * @param lsb ptr least-significant byte
   * @return masked ptr value
   */
  public int getIDAMPtr(final byte msb, final byte lsb) {
    return ((LOW_SIX_BITS & msb) * BYTE_SHIFT + (BYTE_MASK & lsb));
  }

  /**
   * Get track data.
   *
   * @return track data byte array
   */
  public byte[] getData() {
    return this.trackData;
  }


  /**
   * Get number of sectors.
   *
   * @return sectors
   */
  public int getNumSectors() {
    int ns = 0;
    for (int i = 0; i < MAXIMUM_IDAM; i++) {
      if (getIDAM(i).getPtr() != 0) {
        ns++;
      }
    }
    return ns;
  }
}
