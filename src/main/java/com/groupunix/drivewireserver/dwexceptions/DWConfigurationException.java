package com.groupunix.drivewireserver.dwexceptions;

public class DWConfigurationException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Configuration exception constructor.
   * @param msg
   */
  public DWConfigurationException(final String msg) {
    super(msg);
  }
}
