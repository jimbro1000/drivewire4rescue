package com.groupunix.drivewireserver.virtualprinter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWPrinterFileError;
import com.groupunix.drivewireserver.dwexceptions.DWPrinterNotDefinedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWVPrinter {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPrinter");
  /**
   * Available printer drivers.
   */
  private final ArrayList<DWVPrinterDriver> drivers = new ArrayList<>();
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Virtual printer constructor.
   *
   * @param protocol protocol
   */
  public DWVPrinter(final DWProtocol protocol) {
    this.dwProtocol = protocol;
    LOGGER.debug("dwprinter init for handler #" + protocol.getHandlerNo());
    // load drivers
    @SuppressWarnings("unchecked")
    final List<HierarchicalConfiguration> printers = protocol
        .getConfig().configurationsAt("Printer");
    for (final HierarchicalConfiguration printer : printers) {
      if (printer.containsKey("[@name]") && printer.containsKey("Driver")) {
        // definition appears valid
        // now can we instantiate the requested driver...
        try {
          @SuppressWarnings("unchecked")
          final Constructor<DWVPrinterDriver> pconst
              = (Constructor<DWVPrinterDriver>) Class.forName(
                  "com.groupunix.drivewireserver.virtualprinter.DWVPrinter"
                      + printer.getString("Driver"),
              true,
              this.getClass()
                  .getClassLoader()
          ).getConstructor(Class.forName(
              "org.apache.commons.configuration.HierarchicalConfiguration"
          ));
          this.drivers.add(pconst.newInstance(printer));
          // yes we can
          LOGGER.debug(
              "init printer '" + printer.getString("[@name]")
                  + "' using driver '" + printer.getString("Driver") + "'"
          );
        } catch (ClassNotFoundException e) {
          LOGGER.warn(
              "Invalid printer definition '" + printer.getString("[@name]")
                  + "' in config, '" + printer.getString("Driver")
                  + "' is not a known driver."
          );
        } catch (InstantiationException e) {
          LOGGER.warn(
              "InstantiationException on printer '"
                  + printer.getString("[@name]") + "': " + e.getMessage()
          );
        } catch (IllegalAccessException e) {
          LOGGER.warn(
              "IllegalAccessException on printer '"
                  + printer.getString("[@name]") + "': " + e.getMessage()
          );
        } catch (SecurityException e) {
          LOGGER.warn(
              "SecurityException on printer '"
                  + printer.getString("[@name]") + "': " + e.getMessage()
          );
        } catch (NoSuchMethodException e) {
          LOGGER.warn(
              "NoSuchMethodException on printer '"
                  + printer.getString("[@name]") + "': " + e.getMessage()
          );
        } catch (IllegalArgumentException e) {
          LOGGER.warn(
              "IllegalArgumentException on printer '"
                  + printer.getString("[@name]") + "': " + e.getMessage()
          );
        } catch (InvocationTargetException e) {
          LOGGER.warn(
              "InvocationTargetException on printer '"
                  + printer.getString("[@name]") + "': " + e.getMessage()
          );
        }
      } else {
        LOGGER.warn(
            "Invalid printer definition in config, name "
                + printer.getString("[@name]", "not defined")
                + " driver " + printer.getString("Driver", "not defined")
        );
      }
    }
  }

  /**
   * Add byte data to buffer.
   *
   * @param data byte data
   */
  public void addByte(final byte data) {
    try {
      getCurrentDriver().addByte(data);
    } catch (IOException | DWPrinterNotDefinedException e) {
      LOGGER.warn("error writing to print buffer: " + e.getMessage());
    }
  }

  private DWVPrinterDriver getCurrentDriver()
      throws DWPrinterNotDefinedException {
    final String currentPrinter = this.dwProtocol
        .getConfig()
        .getString("CurrentPrinter", null);
    if (currentPrinter == null) {
      throw new DWPrinterNotDefinedException(
          "No current printer is set in the configuration"
      );
    }
    for (final DWVPrinterDriver drv : this.drivers) {
      if (drv.getPrinterName().equals(currentPrinter)) {
        return drv;
      }
    }
    throw new DWPrinterNotDefinedException(
        "Cannot find printer named '" + currentPrinter + "'"
    );
  }

  /**
   * Flush printer buffer.
   */
  public void flush() {
    LOGGER.debug("Printer flush");
    // get out of main thread for flush...
    final Thread flusher = new Thread(new Runnable() {
      @Override
      public void run() {
        Thread.currentThread().setName("printflush-"
            + Thread.currentThread().getId());
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        LOGGER.debug("flush thread run");
        try {
          getCurrentDriver().flush();
        } catch (
            DWPrinterNotDefinedException | IOException | DWPrinterFileError e
        ) {
          LOGGER.warn("error flushing print buffer: " + e.getMessage());
        }
      }
    });
    flusher.start();
  }

  /**
   * Get printer configuration.
   *
   * @return configuration
   */
  public HierarchicalConfiguration getConfig() {
    return dwProtocol.getConfig();
  }

  /**
   * Get log appender.
   *
   * @return logger
   */
  public Logger getLogger() {
    return dwProtocol.getLogger();
  }
}
