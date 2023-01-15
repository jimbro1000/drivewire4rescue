package com.groupunix.drivewireserver.dwexceptions;

public class DWPrinterNotDefinedException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Printer not defined exception constructor.
   * @param msg
   */
  public DWPrinterNotDefinedException(final String msg) {
    super(msg);
  }
}
