package com.groupunix.drivewireserver.dwexceptions;

public class DWFileSystemFileNotFoundException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * File system file not found exception constructor.
   * @param msg
   */
  public DWFileSystemFileNotFoundException(final String msg) {
    super(msg);
  }
}
