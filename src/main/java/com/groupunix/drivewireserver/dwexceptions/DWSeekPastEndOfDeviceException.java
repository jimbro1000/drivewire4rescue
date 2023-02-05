package com.groupunix.drivewireserver.dwexceptions;

public class DWSeekPastEndOfDeviceException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Seek past end of device exception constructor.
   * @param msg
   */
  public DWSeekPastEndOfDeviceException(final String msg) {
    super(msg);
  }
}
