package com.groupunix.drivewireserver.virtualprinter;

import java.io.IOException;

import com.groupunix.drivewireserver.dwexceptions.DWPrinterFileError;
import com.groupunix.drivewireserver.dwexceptions.DWPrinterNotDefinedException;

public interface DWVPrinterDriver {
  /**
   * Flush printer buffer.
   *
   * @throws IOException
   * @throws DWPrinterFileError
   * @throws DWPrinterNotDefinedException
   */
  void flush()
      throws IOException, DWPrinterFileError, DWPrinterNotDefinedException;

  /**
   * Add byte to printer buffer.
   *
   * @param data byte value
   * @throws IOException
   */
  void addByte(byte data)
      throws IOException;

  /**
   * Get printer driver name.
   *
   * @return driver name
   */
  String getDriverName();

  /**
   * Get printer name.
   *
   * @return printer name
   */
  String getPrinterName();
}
