package com.groupunix.drivewireserver.dwexceptions;

public class DWPlatformUnknownException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Platform unknown exception constructor.
   * @param msg
   */
  public DWPlatformUnknownException(final String msg) {
    super(msg);
  }
}
