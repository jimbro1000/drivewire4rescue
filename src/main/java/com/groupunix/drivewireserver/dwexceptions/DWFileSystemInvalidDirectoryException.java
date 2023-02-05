package com.groupunix.drivewireserver.dwexceptions;

public class DWFileSystemInvalidDirectoryException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * File system invalid directory exception constructor.
   * @param msg
   */
  public DWFileSystemInvalidDirectoryException(final String msg) {
    super(msg);
  }
}
