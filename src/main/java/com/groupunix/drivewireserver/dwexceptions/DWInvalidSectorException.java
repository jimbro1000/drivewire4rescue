package com.groupunix.drivewireserver.dwexceptions;


public class DWInvalidSectorException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Invalid sector exception constructor.
   * @param msg
   */
  public DWInvalidSectorException(final String msg) {
    super(msg);
  }
}
