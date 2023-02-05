package com.groupunix.drivewireserver.dwexceptions;

public class DWConnectionNotValidException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Connection not valid exception constructor.
   * @param msg
   */
  public DWConnectionNotValidException(final String msg) {
    super(msg);
  }
}
