package com.groupunix.drivewireserver.dwexceptions;

public class DWFileSystemInvalidFilenameException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * File system invalid filename exception constructor.
   * @param msg
   */
  public DWFileSystemInvalidFilenameException(final String msg) {
    super(msg);
  }
}
