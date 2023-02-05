package com.groupunix.drivewireserver.dwexceptions;

public class DWDiskInvalidSectorNumber extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Disk invalid sector number exception constructor.
   * @param msg
   */
  public DWDiskInvalidSectorNumber(final String msg) {
    super(msg);
  }
}
