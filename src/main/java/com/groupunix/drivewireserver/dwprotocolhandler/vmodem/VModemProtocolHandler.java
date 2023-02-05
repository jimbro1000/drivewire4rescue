package com.groupunix.drivewireserver.dwprotocolhandler.vmodem;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TooManyListenersException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotOpenException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwhelp.DWHelp;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolDevice;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolTimers;
import com.groupunix.drivewireserver.dwprotocolhandler.DWSerialDevice;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

public class VModemProtocolHandler implements Runnable, DWVSerialProtocol {
  /**
   * Buffer size for serial read/write.
   */
  private static final int SERIAL_BUFFER_SIZE = 256;
  /**
   * Log appender.
   */
  private final Logger logger
      = Logger.getLogger("DWServer.VModemProtocolHandler");
  /**
   * Creation/init time.
   */
  private final GregorianCalendar inittime = new GregorianCalendar();
  /**
   * Handler number.
   */
  private final int handlerNo;
  /**
   * Configuration.
   */
  private final HierarchicalConfiguration config;
  /**
   * Timers.
   */
  private final DWProtocolTimers timers = new DWProtocolTimers();
  /**
   * Serial ports.
   */
  private final DWVSerialPorts vSerialPorts;
  /**
   * Log device bytes.
   */
  private final boolean logdevbytes;
  /**
   * Protocol device.
   */
  private DWProtocolDevice dwProtocolDevice = null;
  /**
   * Started.
   */
  private boolean started = false;
  /**
   * Ready.
   */
  private boolean ready = false;
  /**
   * Connected status.
   */
  private boolean connected = false;
  /**
   * Shutdown flag.
   */
  private boolean wanttodie = false;
  /**
   * Help topics.
   */
  private DWHelp dwhelp = new DWHelp(this);

  /**
   * Virtual Modem Protocol Handler.
   *
   * @param handlerno handler
   * @param hconf     configuration
   */
  public VModemProtocolHandler(final int handlerno,
                               final HierarchicalConfiguration hconf) {
    this.handlerNo = handlerno;
    this.config = hconf;
    this.logdevbytes = config.getBoolean("LogDeviceBytes", false);
    this.vSerialPorts = new DWVSerialPorts(this);
    this.vSerialPorts.resetAllPorts();
    if (config.containsKey("HelpFile")) {
      this.dwhelp = new DWHelp(config.getString("HelpFile"));
    }
  }

  /**
   * Start running thread.
   */
  @Override
  public void run() {
    Thread.currentThread().setName("vmodemproto-" + handlerNo + "-"
        + Thread.currentThread().getId());
    logger.info("VModemHandler #" + this.handlerNo + " starting");
    this.started = true;
    this.timers.resetTimer(DWDefs.TIMER_START);
    if (this.dwProtocolDevice == null) {
      setupProtocolDevice();
    }
    try {
      this.vSerialPorts.openPort(0);
      this.vSerialPorts.getPortVModem(0).setEcho(true);
      this.vSerialPorts.getPortVModem(0).setVerbose(true);
    } catch (DWPortNotValidException e1) {
      logger.error(e1.getMessage());
      wanttodie = true;
    }

    Thread vModemToSerialT = new Thread(new Runnable() {
      private boolean wanttodie = false;

      @Override
      public void run() {
        while (!wanttodie) {
          try {
            int bread = dwProtocolDevice.comRead1(false);
            vSerialPorts.serWrite(0, bread);
            if (logdevbytes) {
              logger.debug("read byte from serial device: " + bread);
            }
          } catch (IOException | NullPointerException | DWCommTimeOutException
                   | DWPortNotValidException | DWPortNotOpenException e) {
            wanttodie = true;
          }
        }
      }
    });
    vModemToSerialT.start();
    if (!wanttodie && (this.dwProtocolDevice != null)) {
      this.ready = true;
      logger.debug("handler #" + handlerNo + " is ready");
    } else {
      logger.warn("handler #" + handlerNo + " failed to get ready");
    }
    byte[] buffer = new byte[SERIAL_BUFFER_SIZE];
    while (!wanttodie && (this.dwProtocolDevice != null)) {
      try {
        int bread = vSerialPorts.getPortOutput(0).read(buffer);
        this.dwProtocolDevice.comWrite(buffer, bread, false);
        if (logdevbytes) {
          logger.debug("read " + bread + " bytes from vmodem: "
              + DWUtils.byteArrayToHexString(buffer, bread));
        }
      } catch (IOException | DWPortNotValidException e) {
        logger.error(e.getMessage());
      }
    }
    logger.debug("handler #" + handlerNo + " is exiting");
    this.vSerialPorts.shutdown();
  }

  private void setupProtocolDevice() {
    if ((dwProtocolDevice != null)) {
      dwProtocolDevice.shutdown();
    }
    // create serial device
    if ((config.containsKey("SerialDevice")
        && config.containsKey("SerialRate"))) {
      try {
        dwProtocolDevice = new DWSerialDevice(this);
      } catch (NoSuchPortException e1) {
        logger.error("handler #" + handlerNo + ": Serial device '"
            + config.getString("SerialDevice") + "' not found");
      } catch (PortInUseException e2) {
        logger.error("handler #" + handlerNo + ": Serial device '"
            + config.getString("SerialDevice") + "' in use");
      } catch (UnsupportedCommOperationException e3) {
        logger.error("handler #" + handlerNo
            + ": Unsupported comm operation while opening serial port '"
            + config.getString("SerialDevice") + "'");
      } catch (IOException e) {
        logger.error("handler #" + handlerNo
            + ": IO exception while opening serial port '"
            + config.getString("SerialDevice") + "'");
      } catch (TooManyListenersException e) {
        logger.error("handler #" + handlerNo
            + ": Too many listeneres while opening serial port '"
            + config.getString("SerialDevice") + "'");
      }
    } else {
      logger.error("VModem requires both SerialDevice and SerialRate "
          + "to be set, please configure this instance.");
    }
  }

  /**
   * Stop handler.
   */
  @Override
  public void shutdown() {
    logger.debug("vmodem handler #" + handlerNo + ": shutdown requested");
    this.wanttodie = true;
    if (this.dwProtocolDevice != null) {
      this.dwProtocolDevice.shutdown();
    }
  }

  /**
   * Is stopping.
   *
   * @return stopping
   */
  @Override
  public boolean isDying() {
    return wanttodie;
  }

  /**
   * Is started.
   *
   * @return started
   */
  @Override
  public boolean isStarted() {
    return this.started;
  }

  /**
   * Is ready.
   *
   * @return ready status
   */
  @Override
  public boolean isReady() {
    return this.ready;
  }

  /**
   * Get configuration.
   *
   * @return config
   */
  @Override
  public HierarchicalConfiguration getConfig() {
    return this.config;
  }

  /**
   * Get protocol device.
   *
   * @return device
   */
  @Override
  public DWProtocolDevice getProtoDev() {
    return this.dwProtocolDevice;
  }

  /**
   * Get init time.
   *
   * @return init time
   */
  @Override
  public GregorianCalendar getInitTime() {
    return this.inittime;
  }

  /**
   * Get status text.
   *
   * @return status
   */
  @Override
  public String getStatusText() {
    return "VModem status TODO";
  }

  /**
   * Reset device.
   */
  @Override
  public void resetProtocolDevice() {
    logger.debug("resetting serial port");
    if (this.dwProtocolDevice != null) {
      this.dwProtocolDevice.shutdown();
      this.dwProtocolDevice = null;
    }
    setupProtocolDevice();
  }

  /**
   * Synchronise storage.
   */
  @Override
  public void syncStorage() {
    // noop
  }

  /**
   * Get handler number.
   *
   * @return handler
   */
  @Override
  public int getHandlerNo() {
    return this.handlerNo;
  }

  /**
   * Get log appender.
   *
   * @return logger
   */
  @Override
  public Logger getLogger() {
    return this.logger;
  }

  /**
   * Get cmd columns.
   *
   * @return 0
   */
  @Override
  public int getCMDCols() {
    return 0;
  }

  /**
   * Get help.
   *
   * @return help
   */
  @Override
  public DWHelp getHelp() {
    return this.dwhelp;
  }

  /**
   * Submit configuration event.
   * <p>
   * no-op
   * </p>
   *
   * @param propertyName property
   * @param string       value
   */
  @Override
  public void submitConfigEvent(final String propertyName,
                                final String string) {
    // noop
  }

  /**
   * Get total ops counter.
   *
   * @return total ops
   */
  @Override
  public long getNumOps() {
    return 0;
  }

  /**
   * Get counter of disk ops.
   *
   * @return disk ops
   */
  @Override
  public long getNumDiskOps() {
    return 0;
  }

  /**
   * Get counter of serial ops.
   *
   * @return serial ops
   */
  @Override
  public long getNumVSerialOps() {
    return 0;
  }

  /**
   * Get protocol timers.
   *
   * @return timers
   */
  @Override
  public DWProtocolTimers getTimers() {
    return this.timers;
  }

  /**
   * Is connected.
   *
   * @return connected status
   */
  @Override
  public boolean isConnected() {
    return this.connected;
  }

  /**
   * Has printers.
   *
   * @return false
   */
  @Override
  public boolean hasPrinters() {
    return false;
  }

  /**
   * Has disks.
   *
   * @return false
   */
  @Override
  public boolean hasDisks() {
    return false;
  }

  /**
   * Has midi capability.
   *
   * @return false
   */
  @Override
  public boolean hasMIDI() {
    return false;
  }

  /**
   * Has serial capability.
   *
   * @return true
   */
  @Override
  public boolean hasVSerial() {
    return true;
  }

  /**
   * Get virtual serial ports.
   *
   * @return serial ports
   */
  @Override
  public DWVSerialPorts getVPorts() {
    return this.vSerialPorts;
  }
}
