package com.groupunix.drivewireserver.dwexceptions;

public class DWDriveNotValidException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Drive not valid exception constructor.
   * @param msg
   */
  public DWDriveNotValidException(final String msg) {
    super(msg);
  }
}







