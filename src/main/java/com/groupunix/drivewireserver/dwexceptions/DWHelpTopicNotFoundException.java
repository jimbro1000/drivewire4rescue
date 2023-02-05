package com.groupunix.drivewireserver.dwexceptions;

public class DWHelpTopicNotFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Help topic not found exception constructor.
   * @param msg
   */
  public DWHelpTopicNotFoundException(final String msg) {
    super(msg);
  }
}
