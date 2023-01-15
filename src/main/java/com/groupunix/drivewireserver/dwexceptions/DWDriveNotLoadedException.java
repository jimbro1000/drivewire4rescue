package com.groupunix.drivewireserver.dwexceptions;

public class DWDriveNotLoadedException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Drive not loaded exception constructor.
   * @param msg
   */
  public DWDriveNotLoadedException(final String msg) {
    super(msg);
  }
}
