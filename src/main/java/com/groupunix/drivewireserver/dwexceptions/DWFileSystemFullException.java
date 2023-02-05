package com.groupunix.drivewireserver.dwexceptions;

public class DWFileSystemFullException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * File system full exception constructor.
   * @param msg
   */
  public DWFileSystemFullException(final String msg) {
    super(msg);
  }
}
