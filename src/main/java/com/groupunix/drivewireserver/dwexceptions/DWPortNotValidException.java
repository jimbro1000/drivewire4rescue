package com.groupunix.drivewireserver.dwexceptions;

public class DWPortNotValidException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Port not valid exception constructor.
   * @param msg
   */
  public DWPortNotValidException(final String msg) {
    super(msg);
  }
}
