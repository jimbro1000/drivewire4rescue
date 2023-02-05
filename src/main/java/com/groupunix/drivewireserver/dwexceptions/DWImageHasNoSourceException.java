package com.groupunix.drivewireserver.dwexceptions;

public class DWImageHasNoSourceException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Image has no source exception constructor.
   * @param msg
   */
  public DWImageHasNoSourceException(final String msg) {
    super(msg);
  }
}
