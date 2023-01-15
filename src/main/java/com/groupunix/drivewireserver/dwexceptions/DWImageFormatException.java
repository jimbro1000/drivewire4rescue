package com.groupunix.drivewireserver.dwexceptions;

public class DWImageFormatException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Image format exception constructor.
   * @param msg
   */
  public DWImageFormatException(final String msg) {
    super(msg);
  }
}
