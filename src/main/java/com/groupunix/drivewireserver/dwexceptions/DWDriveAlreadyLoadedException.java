package com.groupunix.drivewireserver.dwexceptions;

public class DWDriveAlreadyLoadedException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Drive already loaded exception constructor.
   * @param msg
   */
  public DWDriveAlreadyLoadedException(final String msg) {
    super(msg);
  }
}
