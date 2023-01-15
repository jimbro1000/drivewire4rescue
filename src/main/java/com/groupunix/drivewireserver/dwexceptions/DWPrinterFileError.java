package com.groupunix.drivewireserver.dwexceptions;

public class DWPrinterFileError extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Printer file error exception constructor.
   * @param msg
   */
  public DWPrinterFileError(final String msg) {
    super(msg);
  }
}
