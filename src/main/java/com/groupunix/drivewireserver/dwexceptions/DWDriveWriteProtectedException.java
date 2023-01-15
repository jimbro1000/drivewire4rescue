package com.groupunix.drivewireserver.dwexceptions;

public class DWDriveWriteProtectedException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Drive write protected exception constructor.
   * @param msg
   */
  public DWDriveWriteProtectedException(final String msg) {
    super(msg);
  }
}
