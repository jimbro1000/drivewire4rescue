package com.groupunix.drivewireserver.dwexceptions;

public class DWFileSystemInvalidFATException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * File system invalid FAT exception constructor.
   * @param msg
   */
  public DWFileSystemInvalidFATException(final String msg) {
    super(msg);
  }
}
