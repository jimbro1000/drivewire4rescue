package com.groupunix.drivewireserver.dwexceptions;

public class DWCommTimeOutException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Communication Timeout exception constructor.
   * @param msg
   */
  public DWCommTimeOutException(final String msg) {
    super(msg);
  }
}
