package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TooManyListenersException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.MCXDefs;
import com.groupunix.drivewireserver.dwdisk.DWDiskDrives;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwhelp.DWHelp;
import com.groupunix.drivewireserver.virtualprinter.DWVPrinter;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class MCXProtocolHandler implements Runnable, DWProtocol {
  /**
   * Default device cols.
   */
  public static final int DEFAULT_COLS = 32;
  /**
   * Sector size.
   */
  public static final int SECTOR_SIZE = 256;
  /**
   * Byte overhead to sector data.
   */
  public static final int SECTOR_META = 6;
  /**
   * Sector data offset in long sector record.
   */
  public static final int SECTOR_OFFSET = 4;
  /**
   * Checksum offset in long sector record.
   */
  public static final int CHECKSUM_OFFSET = 260;
  /**
   * Bytes in LSN.
   */
  public static final int LSN_LENGTH = 3;
  /**
   * Retry failed wait time.
   */
  public static final int RETRY_FAILED_PORT_MILLIS = 1000;
  /**
   * Bytes in argument data.
   */
  public static final int ARG_BYTES = 4;
  /**
   * Log appender.
   */
  private final Logger logger
      = Logger.getLogger("DWServer.MCXProtocolHandler");

  // record keeping portion of dwTransferData
  /**
   * Creation/Init time.
   */
  private final GregorianCalendar dwinitTime = new GregorianCalendar();
  /**
   * handler number.
   */
  private final int handlerNo;
  /**
   * configuration.
   */
  private final HierarchicalConfiguration config;
  /**
   * Last drive accessed.
   */
  private byte lastDrive = 0;
  /**
   * Read retry counter.
   */
  private int readRetries = 0;
  /**
   * Write retry counter.
   */
  private int writeRetries = 0;
  /**
   * Sectors read counter.
   */
  private int sectorsRead = 0;
  /**
   * Sectors written counter.
   */
  private int sectorsWritten = 0;
  /**
   * Last op code handled.
   */
  private byte lastOpcode = DWDefs.OP_RESET1;
  /**
   * Last checksum calculated.
   */
  private int lastChecksum = 0;

  // serial port instance
  /**
   * Last error encountered.
   */
  private int lastError = 0;
  /**
   * Last LSN used.
   */
  private byte[] lastLSN = new byte[LSN_LENGTH];
  /**
   * Protocol device.
   */
  private DWProtocolDevice dwProtocolDevice;
  /**
   * Printer.
   */
  private DWVPrinter vprinter;
  // private static Thread readerthread;
  /**
   * Disk drives.
   */
  private DWDiskDrives diskDrives;
  /**
   * Protocol stopping.
   */
  private boolean wanttodie = false;
  /**
   * Protocol started.
   */
  private boolean started = false;

  /**
   * MCX Protocol Handler.
   *
   * @param handlerno handler number
   * @param hconf     configuration
   */
  public MCXProtocolHandler(final int handlerno,
                            final HierarchicalConfiguration hconf) {
    this.handlerNo = handlerno;
    this.config = hconf;
    //config.addConfigurationListener(new DWProtocolConfigListener());
  }

  /**
   * Get configuration.
   *
   * @return config
   */
  public HierarchicalConfiguration getConfig() {
    return (this.config);
  }

  /**
   * Perform reset.
   */
  public void reset() {
    doOpReset();
  }

  /**
   * Get connected state.
   *
   * @return connected
   */
  public boolean connected() {
    return (dwProtocolDevice.connected());
  }

  /**
   * Gracefully halt handler.
   */
  public void shutdown() {
    logger.info("handler #" + handlerNo + ": shutdown requested");
    this.wanttodie = true;
    this.dwProtocolDevice.shutdown();
  }

  /**
   * Start running protocol handler.
   */
  public void run() {
    int opcodeint;
    int alertcodeint;
    this.started = true;

    Thread.currentThread().setName("mcxproto-" + handlerNo + "-"
        + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    setupProtocolDevice();

    // setup environment and get started
    if (!wanttodie) {
      logger.info("handler #" + handlerNo + ": starting...");
      // setup printer
      vprinter = new DWVPrinter(this);
    }
    logger.info("handler #" + handlerNo + ": ready");
    // protocol loop
    while (!wanttodie) {
      // try to get an opcode
      if (dwProtocolDevice != null) {
        try {
          alertcodeint = dwProtocolDevice.comRead1(false);
          opcodeint = dwProtocolDevice.comRead1(false);
          if (alertcodeint == MCXDefs.ALERT) {
            switch (opcodeint) {
              case MCXDefs.OP_DIRFILEREQUEST -> doOpDirFileRequest();
              case MCXDefs.OP_DIRNAMEREQUEST -> doOpDirNameRequest();
              case MCXDefs.OP_GETDATABLOCK -> doOpGetDataBlock();
              case MCXDefs.OP_LOADFILE -> doOpLoadFile();
              case MCXDefs.OP_OPENDATAFILE -> doOpOpenDataFile();
              case MCXDefs.OP_PREPARENEXTBLOCK -> doOpPrepareNextBlock();
              case MCXDefs.OP_RETRIEVENAME -> doOpRetrieveName();
              case MCXDefs.OP_SAVEFILE -> doOpSaveFile();
              case MCXDefs.OP_SETCURRENTDIR -> doOpSetCurrentDir();
              case MCXDefs.OP_WRITEBLOCK -> doOpWriteBlock();
              default -> logger.warn("UNKNOWN OPCODE: " + opcodeint);
            }
          } else {
            logger.warn("Got non alert code when expected alert code: "
                + alertcodeint);
          }
        } catch (IOException e) {
          // this should not actually ever get thrown,
          // since we call comRead1 with timeout = false..
          logger.error(e.getMessage());
          opcodeint = -1;
        } catch (DWCommTimeOutException e) {
          e.printStackTrace();
        }
      } else {
        logger.debug("cannot access the device.. maybe it has not been "
            + "configured or maybe it does not exist");

        // take a break, reset, hope things work themselves out
        try {
          Thread.sleep(
              config.getInt("FailedPortRetryTime", RETRY_FAILED_PORT_MILLIS)
          );
          resetProtocolDevice();

        } catch (InterruptedException e) {
          logger.error("Interrupted during failed port delay.. "
              + "giving up on this situation");
          wanttodie = true;
        }
      }
    }

    logger.info("handler #" + handlerNo + ": exiting");

    if (this.diskDrives != null) {
      this.diskDrives.shutdown();
    }

    if (dwProtocolDevice != null) {
      dwProtocolDevice.shutdown();
    }
  }

  // MCX OP methods

  private void doOpLoadFile() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_LOADFILE");
    }
  }

  private void doOpGetDataBlock() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_GETDATABLOCK");
    }
  }

  private void doOpPrepareNextBlock() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_PREPARENEXTBLOCK");
    }
  }

  private void doOpSaveFile() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_SAVEFILE");
    }
  }

  private void doOpWriteBlock() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_WRITEBLOCK");
    }
  }

  private void doOpOpenDataFile() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_OPENDATAFILE");
    }
  }

  private void doOpDirFileRequest() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_DIRFILEREQUEST");
    }
    try {
      // read flag byte
      int flag = dwProtocolDevice.comRead1(true);
      // read arg length
      int argLen = dwProtocolDevice.comRead1(true);
      // read arg
      byte[] buf = dwProtocolDevice.comRead(argLen);
      logger.debug("DIRFILEREQUEST fl: " + flag + "  arg: "
          + new String(buf, DWDefs.ENCODING));
      //respond
      dwProtocolDevice.comWrite1(0, false);
      if (flag == 0) {
        dwProtocolDevice.comWrite1(ARG_BYTES, false);
      } else {
        dwProtocolDevice.comWrite1(0, false);
      }
    } catch (IOException e) {
      logger.warn(e.getMessage());
    } catch (DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  private void doOpRetrieveName() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_RETRIEVENAME");
    }
    try {
      int arglen = dwProtocolDevice.comRead1(true);
      if (arglen == ARG_BYTES) {
        dwProtocolDevice.comWrite1('T', false);
        dwProtocolDevice.comWrite1('e', false);
        dwProtocolDevice.comWrite1('s', false);
        dwProtocolDevice.comWrite1('2', false);
      }
    } catch (IOException e) {
      logger.warn(e.getMessage());
    } catch (DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  private void doOpDirNameRequest() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_DIRNAMEREQUEST");
    }
  }

  private void doOpSetCurrentDir() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_SETCURRENTDIR");
    }
  }

  private void doOpReset() {
    // coco has been reset/turned on

    // reset stats
    lastDrive = 0;
    readRetries = 0;
    writeRetries = 0;
    sectorsRead = 0;
    sectorsWritten = 0;
    lastOpcode = DWDefs.OP_RESET1;
    lastChecksum = 0;
    lastError = 0;
    lastLSN = new byte[LSN_LENGTH];

    // Sync disks??
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_RESET");
    }
  }


  @SuppressWarnings("unused")
  private void doOpWrite(final byte opcode) {
    byte[] cocosum = new byte[2];
    byte[] responsebuf;
    byte response;
    byte[] sector = new byte[SECTOR_SIZE];

    try {
      // read rest of packet
      responsebuf = dwProtocolDevice.comRead(SECTOR_SIZE + SECTOR_META);
      lastDrive = responsebuf[0];
      System.arraycopy(responsebuf, 1, lastLSN, 0, LSN_LENGTH);
      System.arraycopy(responsebuf, SECTOR_OFFSET, sector, 0, SECTOR_SIZE);
      System.arraycopy(responsebuf, CHECKSUM_OFFSET, cocosum, 0, 2);
      // Compute Checksum on sector received - NOTE: no V1 version checksum
      lastChecksum = computeChecksum(sector, SECTOR_SIZE);
    } catch (IOException e1) {
      // Timed out reading data from Coco
      logger.error("DoOP_WRITE error: " + e1.getMessage());
      // reset, abort
      return;
    } catch (DWCommTimeOutException e) {
      e.printStackTrace();
    }
    // Compare checksums
    if (lastChecksum != DWUtils.int2(cocosum)) {
      // checksums do not match, tell Coco
      dwProtocolDevice.comWrite1(DWDefs.DWERROR_CRC, false);
      logger.warn("DoOP_WRITE: Bad checksum, drive: " + lastDrive + " LSN: "
          + DWUtils.int3(lastLSN) + " CocoSum: " + DWUtils.int2(cocosum)
          + " ServerSum: " + lastChecksum);
      return;
    }
    // do the write
    response = DWDefs.DWOK;
    try {
      // Seek to LSN in DSK image
      diskDrives.seekSector(lastDrive, DWUtils.int3(lastLSN));
      // Write sector to DSK image
      diskDrives.writeSector(lastDrive, sector);
    } catch (DWDriveNotLoadedException e1) {
      // send drive not ready response
      response = DWDefs.DWERROR_NOTREADY;
      logger.warn(e1.getMessage());
    } catch (DWDriveNotValidException e2) {
      // basically the same as not ready
      response = DWDefs.DWERROR_NOTREADY;
      logger.warn(e2.getMessage());
    } catch (DWDriveWriteProtectedException e3) {
      // hopefully this is appropriate
      response = DWDefs.DWERROR_WP;
      logger.warn(e3.getMessage());
    } catch (IOException e4) {
      // error on our end doing the write
      response = DWDefs.DWERROR_WRITE;
      logger.error(e4.getMessage());
    } catch (DWInvalidSectorException | DWSeekPastEndOfDeviceException e5) {
      response = DWDefs.DWERROR_WRITE;
      logger.warn(e5.getMessage());
    }
    // record error
    if (response != DWDefs.DWOK) {
      lastError = response;
    }
    // send response
    dwProtocolDevice.comWrite1(response, false);
    // Increment sectorsWritten count
    if (response == DWDefs.DWOK) {
      sectorsWritten++;
    }
    if (opcode == DWDefs.OP_REWRITE) {
      writeRetries++;
      if (config.getBoolean("LogOpCode", false)) {
        logger.info("DoOP_REWRITE lastDrive: " + (int) lastDrive + " LSN: "
            + DWUtils.int3(lastLSN));
      }
    } else {
      if (config.getBoolean("LogOpCode", false)) {
        logger.info("DoOP_WRITE lastDrive: " + (int) lastDrive + " LSN: "
            + DWUtils.int3(lastLSN));
      }
    }
  }

  // printing
  @SuppressWarnings("unused")
  private void doOpPrint() {
    int tmpint;
    try {
      tmpint = dwProtocolDevice.comRead1(true);
      if (config.getBoolean("LogOpCode", false)) {
        logger.info("DoOP_PRINT: byte " + tmpint);
      }
      vprinter.addByte((byte) tmpint);
    } catch (IOException e) {
      logger.error("IO exception reading print byte: " + e.getMessage());
    } catch (DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  private void doOpPrintFlush() {
    if (config.getBoolean("LogOpCode", false)) {
      logger.info("DoOP_PRINTFLUSH");
    }
    vprinter.flush();
  }

  /**
   * Calculate checkum.
   *
   * @param data     source data
   * @param numbytes length of data
   * @return checksum
   */
  private int computeChecksum(final byte[] data, final int numbytes) {
    lastChecksum = 0;
    int bytes = numbytes;
    /* Check to see if numbytes is odd or even */
    while (bytes > 0) {
      bytes--;
      lastChecksum += data[bytes] & BYTE_MASK;
    }
    return lastChecksum;
  }

  /**
   * Get last drive.
   *
   * @return last drive
   */
  public byte getLastDrive() {
    return lastDrive;
  }

  /**
   * Get count of read retries.
   *
   * @return read retries
   */
  @SuppressWarnings("unused")
  public int getReadRetries() {
    return readRetries;
  }

  /**
   * Get count of write retries.
   *
   * @return write retries
   */
  @SuppressWarnings("unused")
  public int getWriteRetries() {
    return writeRetries;
  }

  /**
   * Get count of sectors read.
   *
   * @return sectors read
   */
  @SuppressWarnings("unused")
  public int getSectorsRead() {
    return sectorsRead;
  }

  /**
   * Get count of sectors written.
   *
   * @return sectors written
   */
  @SuppressWarnings("unused")
  public int getSectorsWritten() {
    return sectorsWritten;
  }

  /**
   * Get last op code.
   *
   * @return op code
   */
  public byte getLastOpcode() {
    return lastOpcode;
  }

  /**
   * Get last checksum.
   *
   * @return checksum
   */
  @SuppressWarnings("unused")
  public int getLastChecksum() {
    return lastChecksum;
  }

  /**
   * Get last error.
   *
   * @return last error
   */
  public int getLastError() {
    return lastError;
  }

  /**
   * Get last LSN set.
   *
   * @return LSN
   */
  public byte[] getLastLSN() {
    return lastLSN;
  }

  /**
   * Get initial time.
   *
   * @return init time
   */
  public GregorianCalendar getInitTime() {
    return (dwinitTime);
  }

  /**
   * Is waiting to die.
   *
   * @return bool
   */
  public boolean isDying() {
    return this.wanttodie;
  }

  /**
   * Get protocol device.
   *
   * @return device
   */
  public DWProtocolDevice getProtoDev() {
    return (this.dwProtocolDevice);
  }

  /**
   * Reset protocol device.
   */
  public void resetProtocolDevice() {
    if (!this.wanttodie) {
      logger.warn("resetting protocol device");
      // do we need to do anything else here?
      setupProtocolDevice();
    }
  }

  /**
   * Setup protocol device.
   */
  private void setupProtocolDevice() {
    if (dwProtocolDevice != null) {
      dwProtocolDevice.shutdown();
    }
    if (config.getString("DeviceType", "serial")
        .equalsIgnoreCase("serial")) {
      // create serial device
      if (config.containsKey("SerialDevice")) {
        try {
          dwProtocolDevice = new DWSerialDevice(this);
        } catch (NoSuchPortException e1) {
          //wanttodie = true; lets keep on living and see how that goes
          logger.error("handler #" + handlerNo + ": Serial device '"
              + config.getString("SerialDevice") + "' not found");
        } catch (PortInUseException e2) {
          //wanttodie = true;
          logger.error("handler #" + handlerNo + ": Serial device '"
              + config.getString("SerialDevice") + "' in use");
        } catch (UnsupportedCommOperationException e3) {
          //wanttodie = true;
          logger.error("handler #" + handlerNo
              + ": Unsupported comm operation while opening serial port '"
              + config.getString("SerialDevice") + "'");
        } catch (IOException | TooManyListenersException e) {
          e.printStackTrace();
        }
      } else {
        logger.error("Serial mode requires SerialDevice to be set, "
            + "cannot use this configuration");
        //wanttodie = true;
      }
    } else if (config.getString("DeviceType")
        .equalsIgnoreCase("tcp")) {
      // create TCP device
      if (config.containsKey("TCPDevicePort")) {
        try {
          dwProtocolDevice = new DWTCPDevice(
              this.handlerNo, config.getInt("TCPDevicePort")
          );
        } catch (IOException e) {
          //wanttodie = true;
          logger.error("handler #" + handlerNo + ": " + e.getMessage());
        }
      } else {
        logger.error("TCP mode requires TCPDevicePort to be set, "
            + "cannot use this configuration");
        //wanttodie = true;
      }
    } else if (config.getString("DeviceType")
        .equalsIgnoreCase("tcp-client")) {
      // create TCP device
      if (config.containsKey("TCPClientPort")
          && config.containsKey("TCPClientHost")) {
        try {
          dwProtocolDevice = new DWTCPClientDevice(
              this.handlerNo,
              config.getString("TCPClientHost"),
              config.getInt("TCPClientPort")
          );
        } catch (IOException e) {
          //wanttodie = true;
          logger.error("handler #" + handlerNo + ": " + e.getMessage());
        }
      } else {
        logger.error("TCP mode requires TCPClientPort and TCPClientHost "
            + "to be set, cannot use this configuration");
        //wanttodie = true;
      }
    }
  }

  /**
   * Get prettified status.
   *
   * @return status text
   */
  @Override
  public String getStatusText() {
    String text = "";
    text += "Last OpCode:   " + DWUtils.prettyOP(getLastOpcode()) + "\r\n";
    text += "Last Drive:    " + getLastDrive() + "\r\n";
    text += "Last LSN:      " + getLastLSN() + "\r\n";
    text += "Last Error:    " + (getLastError() & BYTE_MASK) + "\r\n";
    return text;
  }

  /**
   * Synchronise storage.
   */
  @Override
  public void syncStorage() {
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
   * Get command columns.
   *
   * @return columns
   */
  @Override
  public int getCMDCols() {
    return DEFAULT_COLS;
  }


  /**
   * Get help.
   *
   * @return help
   */
  @Override
  public DWHelp getHelp() {
    return null;
  }

  /**
   * Is ready.
   *
   * @return true
   */
  @Override
  public boolean isReady() {
    return true;
  }

  /**
   * Submit configuration event.
   *
   * @param propertyName property
   * @param string       value
   */
  @Override
  public void submitConfigEvent(final String propertyName,
                                final String string) {
  }

  /**
   * Get counter of all operations.
   *
   * @return op counter
   */
  @Override
  public long getNumOps() {
    return 0;
  }

  /**
   * Get counter of disk operations.
   *
   * @return op counter
   */
  @Override
  public long getNumDiskOps() {
    return 0;
  }

  /**
   * Get count of v.serial operations.
   *
   * @return op counter
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
    return null;
  }

  /**
   * Is started.
   *
   * @return bool
   */
  @Override
  public boolean isStarted() {
    return this.started;
  }

  /**
   * Is connected.
   *
   * @return false
   */
  @Override
  public boolean isConnected() {
    return false;
  }

  /**
   * Has virtual printer capability.
   *
   * @return true
   */
  @Override
  public boolean hasPrinters() {
    return true;
  }

  /**
   * Has disk capability.
   *
   * @return true
   */
  @Override
  public boolean hasDisks() {
    return true;
  }

  /**
   * Has Midi capability.
   *
   * @return false
   */
  @Override
  public boolean hasMIDI() {
    return false;
  }

  /**
   * Has virtual serial capability.
   *
   * @return false
   */
  @Override
  public boolean hasVSerial() {
    return false;
  }
}
