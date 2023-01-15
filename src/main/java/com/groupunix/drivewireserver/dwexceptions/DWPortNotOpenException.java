package com.groupunix.drivewireserver.dwexceptions;

public class DWPortNotOpenException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Port not open exception constructor.
   * @param msg
   */
  public DWPortNotOpenException(final String msg) {
    super(msg);
  }

}
