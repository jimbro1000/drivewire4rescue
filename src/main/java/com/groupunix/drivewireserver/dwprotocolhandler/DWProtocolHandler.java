package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TooManyListenersException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwdisk.DWDiskDrives;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotOpenException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwhelp.DWHelp;
import com.groupunix.drivewireserver.virtualprinter.DWVPrinter;
import com.groupunix.drivewireserver.virtualserial.DWVPortTermThread;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

import static com.groupunix.drivewireserver.DWDefs.BYTE_BITS;
import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.PD_INT_OFFSET;
import static com.groupunix.drivewireserver.DWDefs.PD_QUT_OFFSET;
import static com.groupunix.drivewireserver.OS9Defs.SS_COMST;
import static com.groupunix.drivewireserver.OS9Defs.SS_KY_SNS;
import static com.groupunix.drivewireserver.OS9Defs.SS_S_CLOSE;
import static com.groupunix.drivewireserver.OS9Defs.SS_S_OPEN;

public class DWProtocolHandler implements Runnable, DWVSerialProtocol {
  /**
   * Device description array length.
   */
  public static final int DEVICE_DESCRIPTION_LENGTH = 26;
  /**
   * Timer byte array length.
   */
  public static final int TIMER_LEN = 4;
  /**
   * Full time, date and day of week array length.
   */
  public static final int FULL_TIME_AND_DOW = 7;
  /**
   * Full time and date array length.
   */
  public static final int FULL_TIME = 6;
  /**
   * Offset to Gregorian years to standard.
   */
  public static final int GREGORIAN_YEAR_OFFSET = 108;
  /**
   * Read packet length.
   */
  public static final int READ_PACKET_LEN = 4;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWProtocolHandler");
  /**
   * Sector data overhead.
   * <p>
   *   prefix and suffix meta data
   * </p>
   */
  public static final int SECTOR_OVERHEAD = 6;
  /**
   * Offset to start of sector data.
   */
  public static final int SECTOR_START_OFFSET = 4;
  /**
   * Offset to LSN.
   */
  public static final int LSN_OFFSET = 3;
  /**
   * Default last stat value.
   */
  public static final int DEFAULT_NON_STAT = 255;
  /**
   * Device retry time.
   */
  public static final int DEVICE_RETRY_MILLIS = 6000;
  /**
   * Bytes needed for LSN.
   */
  public static final int LSN_SIZE = 3;
  /**
   * Version number max for DW3.
   */
  public static final int VERSION_DW3_MAX = 0x3F;
  /**
   * Version number max for nitros9.
   */
  public static final int VERSION_NITROS_MAX = 0x4F;
  /**
   * Version number min for nitros9.
   */
  public static final int VERSION_NITROS_MIN = 0x40;
  /**
   * Version number min for LWOS/LWBASIC.
   */
  public static final int VERSION_LWOS_MIN = 0x60;
  /**
   * Version number max for LWOS/LWBASIC.
   */
  public static final int VERSION_LWOS_MAX = 0x6F;
  /**
   * Version number max.
   */
  public static final int VERSION_MAX = 0x80;

  // record keeping portion of dwTransferData
  /**
   * Last drive accessed.
   */
  private int lastDrive;
  /**
   * Read retry count.
   */
  private int readRetries;
  /**
   * Write retry count.
   */
  private int writeRetries;
  /**
   * Sectors read count.
   */
  private int sectorsRead;
  /**
   * Sectors written count.
   */
  private int sectorsWritten;
  /**
   * Last op code handled.
   * <p>
   *   This is the wrong data-type, it needs to be an int not a byte
   * </p>
   */
  private byte lastOpcode;
  /**
   * Last get stat.
   */
  private byte lastGetStat;
  /**
   * Last set stat.
   */
  private byte lastSetStat;
  /**
   * Last checksum calculated.
   */
  private int lastChecksum;
  /**
   * Last error handled.
   */
  private int lastError;
  /**
   * Last LSN.
   */
  private byte[] lastLSN;
  /**
   * Total ops counter.
   */
  private long totalOps = 0;
  /**
   * Disk ops counter.
   */
  private long diskOps = 0;
  /**
   * Serial ops counter.
   */
  private long vserialOps = 0;
  /**
   * In Op flag.
   */
  private boolean inOp = false;
  /**
   * Sync skipped.
   */
  private int syncSkipped = 0;
  /**
   * Creation time.
   */
  private GregorianCalendar dwinitTime = new GregorianCalendar();
  /**
   * Serial port instance.
   */
  private DWProtocolDevice protodev = null;
  /**
   * Printer instance.
   */
  private DWVPrinter vprinter;
  /**
   * Drive instances.
   */
  private DWDiskDrives diskDrives;
  /**
   * Waiting to shut down.
   */
  private boolean wanttodie = false;
  /**
   * RFM handler.
   */
  private DWRFMHandler rfmhandler;
  /**
   * Handler id.
   */
  private final int handlerno;
  /**
   * Configuration.
   */
  private final HierarchicalConfiguration config;
  /**
   * Terminal thread.
   */
  private Thread termT;
  /**
   * Serial Ports.
   */
  private DWVSerialPorts dwVSerialPorts;
  /**
   * Terminal handler.
   */
  private DWVPortTermThread termHandler;
  /**
   * Help instance.
   */
  private DWHelp dwhelp;
  /**
   * Timers.
   */
  private DWProtocolTimers timers;
  /**
   * Am I ready.
   */
  private boolean ready = false;
  /**
   * Am I started.
   */
  private boolean started = false;
  /**
   * Am I waiting to reset.
   */
  private boolean resetPending = false;

  /**
   * Protocol Handler.
   *
   * @param handlerId handlerId
   * @param hconf configuration
   */
  public DWProtocolHandler(
      final int handlerId, final HierarchicalConfiguration hconf
  ) {
    this.handlerno = handlerId;
    this.config = hconf;
    initData();
    config.addConfigurationListener(new DWProtocolConfigListener(this));
  }

  private void initData() {
    lastDrive = 0;
    readRetries = 0;
    writeRetries = 0;
    sectorsRead = 0;
    sectorsWritten = 0;
    lastOpcode = DWDefs.OP_RESET1;
    lastGetStat = (byte) DEFAULT_NON_STAT;
    lastSetStat = (byte) DEFAULT_NON_STAT;
    lastChecksum = 0;
    lastError = 0;
    lastLSN = new byte[LSN_SIZE];
  }

  /**
   * Get configuration.
   *
   * @return config
   */
  public HierarchicalConfiguration getConfig() {
    return this.config;
  }

  /**
   * Reset protocol handler.
   */
  public void reset() {
    doOpReset();
  }

  /**
   * Is device connected.
   *
   * @return true if connected
   */
  public boolean isConnected() {
    if (protodev != null) {
      return (protodev.connected());
    }
    return false;
  }

  /**
   * Shutdown thread gracefully.
   */
  public void shutdown() {
    LOGGER.debug("handler #" + handlerno + ": shutdown requested");
    this.wanttodie = true;
    if (this.protodev != null) {
      this.protodev.shutdown();
    }
  }

  /**
   * Start protocol handler.
   * <p>
   *   This code is horrible, really horrible and needs
   *   sorting out.
   *   Maximum permitted lines is 150, currently on 287...
   * </p>
   * <p>
   *   Thread needs to run frequently to avoid dropped data
   * </p>
   */
  public void run() {
    this.started = true;
    this.timers = new DWProtocolTimers();
    this.timers.resetTimer(DWDefs.TIMER_START);
    Thread.currentThread().setName("dwproto-" + handlerno + "-"
        + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    if (this.protodev == null) {
      setupProtocolDevice();
    }
    if (!wanttodie) {
      setupHandlerOnRun();
    }
    this.ready = true;
    LOGGER.debug("handler #" + handlerno + " is ready");
    // protocol loop
    try {
      runLoop();
      LOGGER.debug("handler #" + handlerno + ": shutting down");
      shutdownAfterRun();
    } catch (Exception e) {
      System.out.println("\n\n");
      e.printStackTrace();
      System.out.println("\n\n");
      LOGGER.error(e.getMessage());
    } finally {
      if (protodev != null) {
        protodev.shutdown();
      }
    }
    LOGGER.debug("handler #" + handlerno + ": exiting");
  }

  private void runLoop() {
    int opcodeint;
    long optime;
    long optook;
    while (!wanttodie) {
      opcodeint = -1;
      // try to get an opcode
      if (!(protodev == null) && !resetPending) {
        try {
          opcodeint = protodev.comRead1(false) & BYTE_MASK;
        } catch (IOException e) {
          LOGGER.error("Strange result in proto read loop: "
              + e.getMessage());
        } catch (DWCommTimeOutException e) {
          // this should not actually ever get thrown, since we call
          // comRead1 with timeout = false..
          LOGGER.error("Timeout in proto read loop: " + e.getMessage());
        }
      }
      if ((opcodeint > -1) && (this.protodev != null)) {
        if (this.protodev.getClass().getCanonicalName()
            .equals("com.groupunix.drivewireserver."
                + "dwprotocolhandler.DWSerialDevice")
        ) {
          ((DWSerialDevice) this.protodev).resetReadtime();
        }
        optime = System.currentTimeMillis();
        this.inOp = true;
        lastOpcode = (byte) opcodeint;
        totalOps++;
        try {
          // fast writes
          // *BIG BUG* handling opcode with byte means everything is "greater"
          // than OP_FASTWRITE_BASE (-128) - need to handle bytes as ints
          // to cope with the sign-bit being msb...
          if ((lastOpcode >= DWDefs.OP_FASTWRITE_BASE)
//            if ((lastOpcode < 0)
              && (lastOpcode <= (
              DWDefs.OP_FASTWRITE_BASE
                  + this.dwVSerialPorts.getMaxPorts() - 1)
          )
          ) {
            doOpFastSerialWrite(lastOpcode);
            this.timers.resetTimer(DWDefs.TIMER_NP_OP, optime);
            vserialOps++;
          } else {
            // regular OP decode
            decodeOp(lastOpcode, optime, opcodeint);
          }
        } catch (IOException e) {
          LOGGER.error("IOError in proto op: " + e.getMessage());
        } catch (DWCommTimeOutException e) {
          LOGGER.warn("Timed out reading from CoCo in "
              + DWUtils.prettyOP(lastOpcode));
        } catch (DWPortNotValidException e) {
          LOGGER.warn("Invalid port # from CoCo in "
              + DWUtils.prettyOP(lastOpcode) + ": " + e.getMessage());
        }
        this.inOp = false;
        optook = System.currentTimeMillis() - optime;
        if (optook > DWDefs.SERVER_SLOW_OP) {
          LOGGER.warn(DWUtils.prettyOP(lastOpcode) + " took " + optook
              + "ms.");
        } else if (config.getBoolean("LogTiming", false)) {
          LOGGER.debug(DWUtils.prettyOP(lastOpcode) + " took " + optook
              + "ms, serial read delay was "
              + ((DWSerialDevice) this.protodev).getReadtime());
        }
      } else {
        if (!this.wanttodie) {
          if (this.resetPending) {
            LOGGER.debug("device is resetting...");
            if (protodev != null) {
              this.protodev.shutdown();
            }
            this.resetPending = false;
          } else if (
              !config.getString("DeviceType", "").equals("dummy")
          ) {
            LOGGER.debug("device unavailable, will retry in "
                + config.getInt("DeviceFailRetryTime", DEVICE_RETRY_MILLIS)
                + "ms");
          }
          try {
            Thread.sleep(
                config.getInt("DeviceFailRetryTime", DEVICE_RETRY_MILLIS)
            );
            setupProtocolDevice();
          } catch (InterruptedException e) {
            LOGGER.error("Interrupted during failed port delay.. "
                + "giving up on this crazy situation");
            wanttodie = true;
          }
        }
      }
    }
  }

  private void shutdownAfterRun() {
    if (this.dwVSerialPorts != null) {
      this.dwVSerialPorts.shutdown();
    }
    if (this.diskDrives != null) {
      this.diskDrives.shutdown();
    }
    if (this.termT != null) {
      termHandler.shutdown();
      termT.interrupt();
    }
  }

  private void setupHandlerOnRun() {
    diskDrives = new DWDiskDrives(this);
    this.dwVSerialPorts = new DWVSerialPorts(this);
    dwVSerialPorts.resetAllPorts();
    if (this.config.getBoolean("RestartClientsOnOpen", false)) {
      dwVSerialPorts.setRebootRequested(true);
    }
    vprinter = new DWVPrinter(this);
    rfmhandler = new DWRFMHandler(handlerno);
    if (config.containsKey("TermPort")) {
      LOGGER.debug("handler #" + handlerno
          + ": starting term device listener thread");
      this.termHandler = new DWVPortTermThread(
          this, config.getInt("TermPort")
      );
      this.termT = new Thread(termHandler);
      this.termT.setDaemon(true);
      this.termT.start();
    }
    if (config.containsKey("HelpFile")) {
      this.dwhelp = new DWHelp(config.getString("HelpFile"));
    } else {
      this.dwhelp = new DWHelp(this);
    }
  }

  /**
   * Route execution according to op code.
   *
   * @param opCode op code
   * @param opTime timestamp
   * @param opCodeInt original integer version of op code
   * @throws DWCommTimeOutException port timeout
   * @throws IOException read/write failure
   * @throws DWPortNotValidException invalid port
   */
  private void decodeOp(
      final byte opCode, final long opTime, final int opCodeInt
  ) throws DWCommTimeOutException, IOException, DWPortNotValidException {
    switch (opCode) {
      case DWDefs.OP_RESET1, DWDefs.OP_RESET2, DWDefs.OP_RESET3 -> {
        this.timers.resetTimer(DWDefs.TIMER_RESET, opTime);
        doOpReset();
      }
      case DWDefs.OP_DWINIT -> {
        this.timers.resetTimer(DWDefs.TIMER_DWINIT, opTime);
        doOpDwInit();
      }
      case DWDefs.OP_INIT -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpInit();
      }
      case DWDefs.OP_TERM -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpTerm();
      }
      case DWDefs.OP_REREAD, DWDefs.OP_READ -> {
        this.timers.resetTimer(DWDefs.TIMER_READ, opTime);
        doOpRead(opCode);
        diskOps++;
      }
      case DWDefs.OP_REREADEX, DWDefs.OP_READEX -> {
        this.timers.resetTimer(DWDefs.TIMER_READ, opTime);
        doOpReadEx(opCode);
        diskOps++;
      }
      case DWDefs.OP_WRITE, DWDefs.OP_REWRITE -> {
        this.timers.resetTimer(DWDefs.TIMER_WRITE, opTime);
        doOpWrite(opCode);
        diskOps++;
      }
      case DWDefs.OP_GETSTAT, DWDefs.OP_SETSTAT -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpStat(opCode);
        diskOps++;
      }
      case DWDefs.OP_TIME -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpTime();
      }
      case DWDefs.OP_SETTIME -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSetTime();
      }
      case DWDefs.OP_PRINT -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpPrint();
      }
      case DWDefs.OP_PRINTFLUSH -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpPrintFlush();
      }
      case DWDefs.OP_SERREADM -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSerialReadM();
        vserialOps++;
      }
      case DWDefs.OP_SERREAD -> {
        this.timers.resetTimer(DWDefs.TIMER_POLL, opTime);
        doOpSerialRead();
        vserialOps++;
      }
      case DWDefs.OP_SERWRITE -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSerialWrite();
        vserialOps++;
      }
      case DWDefs.OP_SERWRITEM -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSerialWriteM();
        vserialOps++;
      }
      case DWDefs.OP_SERSETSTAT -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSetSerialStat();
        vserialOps++;
      }
      case DWDefs.OP_SERGETSTAT -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSerialGetStat();
        vserialOps++;
      }
      case DWDefs.OP_SERINIT -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSerialInit();
        vserialOps++;
      }
      case DWDefs.OP_SERTERM -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpSerialTerm();
        vserialOps++;
      }
      case DWDefs.OP_NOP, DWDefs.OP_230K230K -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpNop();
      }
      case DWDefs.OP_RFM -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpRfm();
      }
      case DWDefs.OP_230K115K -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOp230K115K();
      }
      case DWDefs.OP_NAMEOBJ_MOUNT -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpNameObjMount();
      }
      case DWDefs.OP_TIMER -> doOpTimer();
      case DWDefs.OP_RESET_TIMER -> doOpResetTimer();
      case DWDefs.OP_AARON -> {
        this.timers.resetTimer(DWDefs.TIMER_NP_OP, opTime);
        doOpAaron();
      }
      default -> {
        this.timers.resetTimer(DWDefs.TIMER_BAD_DATA, opTime);
        LOGGER.warn("UNKNOWN OPCODE: " + opCodeInt + " "
            + ((char) opCodeInt));
      }
    }
  }

  /**
   * Aaron.
   */
  private void doOpAaron() {
    LOGGER.warn("DriveWire " + DriveWireServer.DW_SERVER_VERSION
        + " (" + DriveWireServer.DW_SERVER_VERSION_DATE + ") by Aaron Wolfe");
  }

  /**
   * Mount named object.
   *
   * @throws DWCommTimeOutException port timeout
   * @throws IOException read/write failure
   */
  private void doOpNameObjMount()
      throws DWCommTimeOutException, IOException {
    long startTime = System.currentTimeMillis();
    int nameSize = protodev.comRead1(true);
    byte[] nameBuf = protodev.comRead(nameSize);
    String objName = new String(nameBuf);
    int result = diskDrives.nameObjMount(objName);
    // artificial delay test
    if (config.containsKey("NameObjMountDelay")) {
      try {
        LOGGER.debug("named object mount delay "
            + config.getLong("NameObjMountDelay") + " ms...");
        Thread.sleep(config.getLong("NameObjMountDelay"));
      } catch (InterruptedException e) {
        LOGGER.warn("Interrupted during mount delay");
      }
    }
    protodev.comWrite1(result, false);
    if (config.getBoolean("LogOpCode", false)) {
      long delay = System.currentTimeMillis() - startTime;
      LOGGER.info("DoOP_NAMEOBJ_MOUNT for '" + objName + "' result: " + result
          + ", call took " + delay + "ms");
    }
  }

  /**
   * Switch serial baud to 230k.
   */
  private void doOp230K115K() {
    if (config.getBoolean("DetectDATurbo", false)) {
      try {
        ((DWSerialDevice) protodev).enableDATurbo();
        LOGGER.info("Detected switch to 230k mode");
      } catch (UnsupportedCommOperationException e) {
        LOGGER.error("comm port did not make the switch to 230k mode: "
            + e.getMessage());
        LOGGER.error("bail out!");
        this.wanttodie = true;
      }
    }
  }

  /**
   * Fast serial write.
   *
   * @param opcode op code
   * @throws DWCommTimeOutException port timeout
   * @throws IOException read/write failure
   */
  private void doOpFastSerialWrite(final byte opcode)
      throws DWCommTimeOutException, IOException {
    int dataByte;
    int port = opcode - DWDefs.OP_FASTWRITE_BASE;
    try {
      dataByte = protodev.comRead1(true);
      dwVSerialPorts.serWrite(port, dataByte);
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.info("DoOP_FASTSERWRITE to port " + port + ": " + dataByte);
      }
    } catch (DWPortNotOpenException | DWPortNotValidException e1) {
      LOGGER.error(e1.getMessage());
    }
  }

  // DW OP methods
  /**
   * Drivewire init.
   *
   * @throws DWCommTimeOutException port timeout
   * @throws IOException read/write failure
   */
  private void doOpDwInit() throws DWCommTimeOutException, IOException {
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_DWINIT");
    }
    int drvVersion = protodev.comRead1(true);
    // are we limited to dw3?
    if (!config.getBoolean("DW3Only", false)) {
      // send response
      protodev.comWrite1(DWDefs.DW_PROTOCOL_VERSION, true);
      if (drvVersion <= VERSION_DW3_MAX) {
        LOGGER.debug("DWINIT from NitrOS9! Implementation variety type # "
            + drvVersion);
      } else if ((drvVersion <= VERSION_NITROS_MAX)) {
        LOGGER.debug("DWINIT from CoCoBoot! Implementation variety type # "
            + (drvVersion - VERSION_NITROS_MIN));
      } else if ((drvVersion >= VERSION_LWOS_MIN)
          && (drvVersion <= VERSION_LWOS_MAX)) {
        LOGGER.debug("DWINIT from LWOS/LWBASIC! Implementation variety type # "
            + (drvVersion - VERSION_LWOS_MIN));
      } else {
        LOGGER.info("DWINIT got unknown driver version # " + drvVersion);
      }
      // possibly extend this to all DWINITs..
      if (drvVersion < VERSION_MAX) {
        if (this.config.getBoolean("HDBDOSMode", false)) {
          LOGGER.warn("Disabling HDBDOS mode due to non HDBDOS DWINIT");
          this.config.setProperty("HDBDOSMode", false);
        }
      }
      // coco has just booted an os..
      dwinitTime = new GregorianCalendar();
      // reset all ports
      dwVSerialPorts.resetAllPorts();
    } else {
      LOGGER.info("DWINIT received, ignoring due to DW3Only setting");
    }
  }

  /**
   * Do nothing.
   */
  private void doOpNop() {
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_NOP");
    }
  }

  /**
   * RFM.
   *
   * @throws DWCommTimeOutException port timeout
   * @throws IOException read failure
   */
  private void doOpRfm() throws DWCommTimeOutException, IOException {
    int rfmOp = protodev.comRead1(true);
    LOGGER.info("DoOP_RFM call " + rfmOp);
    rfmhandler.DoRFMOP(protodev, rfmOp);
  }

  /**
   * Terminate.
   */
  private void doOpTerm() {
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_TERM");
    }
  }

  /**
   * Initialise.
   */
  private void doOpInit() {
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_INIT");
    }
  }

  /**
   * Reset.
   */
  private void doOpReset() {
    // coco has been reset/turned on
    // reset stats
    initData();
    // Sync disks??
    // reset all ports
    dwVSerialPorts.resetAllPorts();
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_RESET");
    }
  }

  /**
   * Write.
   *
   * @param opcode op code
   * @throws DWCommTimeOutException port timeout
   * @throws IOException read/write failure
   */
  private void doOpWrite(final byte opcode)
      throws DWCommTimeOutException, IOException {
    byte[] cocoSum = new byte[2];
    byte[] responseBuf;
    byte response = DWDefs.DWOK;
    byte[] sector = new byte[
        getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)
        ];
    // read rest of packet
    responseBuf = protodev.comRead(
        getConfig()
            .getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)
                + SECTOR_OVERHEAD
    );
    lastDrive = responseBuf[0] & BYTE_MASK;
    System.arraycopy(responseBuf, 1, lastLSN, 0, LSN_OFFSET);
    System.arraycopy(
        responseBuf,
        SECTOR_START_OFFSET,
        sector,
        0,
        getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)
    );
    System.arraycopy(
        responseBuf,
        getConfig()
            .getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)
                + SECTOR_START_OFFSET,
        cocoSum,
        0,
        2
    );

    // Compute Checksum on sector received - NOTE: no V1 version checksum
    lastChecksum = computeChecksum(
        sector,
        getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)
    );
    // Compare checksums
    if (lastChecksum != DWUtils.int2(cocoSum)) {
      // checksums do not match, tell Coco
      protodev.comWrite1(DWDefs.DWERROR_CRC, true);
      LOGGER.warn("DoOP_WRITE: Bad checksum, drive: " + lastDrive + " LSN: "
          + DWUtils.int3(lastLSN) + " CocoSum: " + DWUtils.int2(cocoSum)
          + " ServerSum: " + lastChecksum);
      return;
    }
    // do the write
    try {
      // Seek to LSN in DSK image
      diskDrives.seekSector(lastDrive, DWUtils.int3(lastLSN));
      // Write sector to DSK image
      diskDrives.writeSector(lastDrive, sector);
    } catch (DWDriveNotLoadedException e1) {
      // send drive not ready response
      response = DWDefs.DWERROR_NOTREADY;
      LOGGER.warn(e1.getMessage());
    } catch (DWDriveNotValidException e2) {
      // basically the same as not ready
      response = DWDefs.DWERROR_NOTREADY;
      LOGGER.warn(e2.getMessage());
    } catch (DWDriveWriteProtectedException e3) {
      // hopefully this is appropriate
      response = DWDefs.DWERROR_WP;
      LOGGER.warn(e3.getMessage());
    } catch (DWInvalidSectorException | DWSeekPastEndOfDeviceException e5) {
      response = DWDefs.DWERROR_WRITE;
      LOGGER.warn(e5.getMessage());
    }
    // record error
    if (response != DWDefs.DWOK) {
      lastError = response;
    }
    // send response
    protodev.comWrite1(response, true);
    // Increment sectorsWritten count
    if (response == DWDefs.DWOK) {
      sectorsWritten++;
    }
    if (opcode == DWDefs.OP_REWRITE) {
      writeRetries++;
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.warn("DoOP_REWRITE lastDrive: " + lastDrive + " LSN: "
            + DWUtils.int3(lastLSN));
      }
    } else {
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.info("DoOP_WRITE lastDrive: " + lastDrive + " LSN: "
            + DWUtils.int3(lastLSN));
      }
    }
  }

  /**
   * Read.
   *
   * @param opcode op code
   * @throws IOException failed to read
   * @throws DWCommTimeOutException port timeout
   */
  private void doOpRead(final int opcode)
      throws IOException, DWCommTimeOutException {
    byte[] mySum = new byte[2];
    byte[] responseBuf;
    byte[] sector = new byte[getConfig().getInt(
        "DiskSectorSize",
        DWDefs.DISK_SECTORSIZE
    )];
    byte result = DWDefs.DWOK;

    try {
      // read rest of packet - drive # and 3 byte LSN
      responseBuf = protodev.comRead(READ_PACKET_LEN);
      // store that..
      lastDrive = responseBuf[0] & BYTE_MASK;
      System.arraycopy(
          responseBuf, 1, lastLSN, 0, READ_PACKET_LEN - 1
      );
      // attempt seek to requested LSN (will throw one of the many exceptions
      // caught below if it cannot)
      diskDrives.seekSector(lastDrive, DWUtils.int3(lastLSN));
      // we didn't throw an exception in our seek, so load a buffer with
      // the sector's data
      sector = diskDrives.readSector(lastDrive);
      // deal with all kinds of things that could have gone wrong as we did
      // the seek and read above...
    } catch (DWDriveNotLoadedException
             | DWDriveNotValidException e1) {
      LOGGER.warn("DoOP_READ: " + e1.getMessage());
      result = DWDefs.DWERROR_NOTREADY;
    } catch (DWInvalidSectorException
             | DWImageFormatException
             | DWSeekPastEndOfDeviceException e5) {
      LOGGER.error("DoOP_READ: " + e5.getMessage());
      result = DWDefs.DWERROR_READ;
    }

    // send ultimate result to coco in a response byte
    protodev.comWrite1(result, true);

    if (result == DWDefs.DWOK) {
      // if our response was OK, next we send the sector data
      // write out response sector
      protodev.comWrite(
          sector,
          getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE),
          true
      );
      // calc a checksum
      lastChecksum = computeChecksum(
          sector,
          getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)
      );
      mySum[0] = (byte) ((lastChecksum >> BYTE_BITS) & BYTE_MASK);
      mySum[1] = (byte) (lastChecksum & BYTE_MASK);
      // send checksum to coco
      protodev.comWrite(mySum, 2, true);
      // we're done.. do housekeeping stuff
      sectorsRead++;
      if (opcode == DWDefs.OP_REREAD) {
        readRetries++;
        LOGGER.warn("DoOP_REREAD lastDrive: " + lastDrive + " LSN: "
            + DWUtils.int3(lastLSN));
      } else {
        if (config.getBoolean("LogOpCode", false)) {
          LOGGER.info("DoOP_READ lastDrive: " + lastDrive + " LSN: "
              + DWUtils.int3(lastLSN));
        }
      }
    }
  }

  /**
   * Read extended.
   * <p>
   *   This is a hideous monolith of code and
   *   needs breaking down...
   * </p>
   * @param opcode op code
   * @throws IOException failed to read/write
   * @throws DWCommTimeOutException port timeout
   */
  private void doOpReadEx(final int opcode)
      throws IOException, DWCommTimeOutException {
    byte[] cocoSum;
    byte[] mySum = new byte[2];
    byte[] responseBuf;
    byte[] sector;
    //= new byte[getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)];
    byte result = DWDefs.DWOK;

    try {
      // read rest of packet
      responseBuf = protodev.comRead(READ_PACKET_LEN);
      lastDrive = responseBuf[0] & BYTE_MASK;
      System.arraycopy(
          responseBuf, 1, lastLSN, 0, READ_PACKET_LEN - 1
      );
      // seek to requested LSN
      diskDrives.seekSector(lastDrive, DWUtils.int3(lastLSN));
      // load lastSector with bytes from file
      sector = diskDrives.readSector(lastDrive);
    } catch (DWDriveNotLoadedException
             | DWDriveNotValidException e1) {
      // zero sector
      sector = diskDrives.nullSector();
      LOGGER.warn("DoOP_READEX: " + e1.getMessage());
      result = DWDefs.DWERROR_NOTREADY;
    } catch (DWInvalidSectorException
             | DWImageFormatException
             | DWSeekPastEndOfDeviceException e5) {
      sector = diskDrives.nullSector();
      LOGGER.error("DoOP_READEX: " + e5.getMessage());
      result = DWDefs.DWERROR_READ;
    }
    // artificial delay test
    if (config.containsKey("ReadDelay")) {
      try {
        LOGGER.debug("read delay "
            + config.getLong("ReadDelay") + " ms...");
        Thread.sleep(config.getLong("ReadDelay"));
      } catch (InterruptedException e) {
        LOGGER.warn("Interrupted during read delay");
      }
    }
    // write out response sector
    protodev.comWrite(
        sector,
        getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE),
        true
    );
    if (!config.getBoolean("ProtocolDisableReadChecksum", false)) {
      // calc checksum
      lastChecksum = computeChecksum(
          sector,
          getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)
      );
      mySum[0] = (byte) ((lastChecksum >> BYTE_BITS) & BYTE_MASK);
      mySum[1] = (byte) (lastChecksum & BYTE_MASK);
      // logger.debug("looking for checksum " + mySum[0] + ":" + mySum[1]);
      cocoSum = protodev.comRead(2);
      if (((mySum[0] == cocoSum[0]) && (mySum[1] == cocoSum[1]))
          || config.getBoolean("ProtocolLieAboutCRC", false)) {
        // Good checksum, all is well
        sectorsRead++;
        if (opcode == DWDefs.OP_REREADEX) {
          readRetries++;
          LOGGER.warn("DoOP_REREADEX lastDrive: " + lastDrive + " LSN: "
              + DWUtils.int3(lastLSN));
        } else {
          if (config.getBoolean("LogOpCode", false)) {
            LOGGER.info("DoOP_READEX lastDrive: " + lastDrive + " LSN: "
                + DWUtils.int3(lastLSN));
          }
        }
      } else {
        // checksum mismatch
        // sectorsRead++;  should we increment this?
        result = DWDefs.DWERROR_CRC;
        if (opcode == DWDefs.OP_REREADEX) {
          readRetries++;
          LOGGER.warn("DoOP_REREADEX CRC check failed, lastDrive: "
              + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
        } else {
          LOGGER.warn("DoOP_READEX CRC check failed, lastDrive: "
              + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
          try {
            this.diskDrives.getDisk(lastDrive).incParam("_read_errors");
          } catch (DWDriveNotLoadedException | DWDriveNotValidException e) {
            LOGGER.warn(e.getMessage());
          }
        }
      }
    }
    // send result byte
    protodev.comWrite1(result, true);
  }

  /**
   * Perform stat operation.
   * <p>
   *   Routes op to get or set
   * </p>
   * @param opcode op code
   * @throws IOException failed read/write
   * @throws DWCommTimeOutException serial port timeout
   */
  private void doOpStat(final byte opcode)
      throws IOException, DWCommTimeOutException {
    byte[] responseBuf;
    // get packet args
    // drive # and stat
    responseBuf = protodev.comRead(2);
    lastDrive = responseBuf[0] & BYTE_MASK;
    if (opcode == DWDefs.OP_GETSTAT) {
      lastGetStat = responseBuf[1];
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.info("DoOP_GETSTAT: " + DWUtils.prettySS(responseBuf[1])
            + " lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
      }
    } else {
      lastSetStat = responseBuf[1];
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.info("DoOP_SETSTAT " + DWUtils.prettySS(responseBuf[1])
            + " lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
      }
    }
  }

  /**
   * Set time.
   *
   * @throws IOException failed to write
   * @throws DWCommTimeOutException port timeout
   */
  private void doOpSetTime() throws IOException, DWCommTimeOutException {
    byte[] responseBuf;
    responseBuf = protodev.comRead(FULL_TIME);
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_SETTIME data: "
          + DWUtils.byteArrayToHexString(responseBuf));
    }
  }

  /**
   * Fetch current time.
   */
  private void doOpTime() {
    GregorianCalendar c = (GregorianCalendar) Calendar.getInstance();
    byte[] buf = new byte[FULL_TIME_AND_DOW];
    int index = 0;
    buf[index++] = (byte) (c.get(Calendar.YEAR) - GREGORIAN_YEAR_OFFSET);
    buf[index++] = (byte) (c.get(Calendar.MONTH) + 1);
    buf[index++] = (byte) c.get(Calendar.DAY_OF_MONTH);
    buf[index++] = (byte) c.get(Calendar.HOUR_OF_DAY);
    buf[index++] = (byte) c.get(Calendar.MINUTE);
    buf[index++] = (byte) c.get(Calendar.SECOND);
    buf[index] = (byte) c.get(Calendar.DAY_OF_WEEK);
    if (config.getBoolean("OpTimeSendsDOW", false)) {
      protodev.comWrite(buf, FULL_TIME_AND_DOW, true);
    } else {
      protodev.comWrite(buf, FULL_TIME, true);
    }
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_TIME");
    }
  }

  /**
   * Set timer.
   *
   * @throws IOException failed to read/write
   * @throws DWCommTimeOutException serial timeout
   */
  private void doOpTimer() throws IOException, DWCommTimeOutException {
    // read rest of packet - timer #
    byte tno = (byte) protodev.comRead1(true);
    protodev.comWrite(timers.getTimerBytes(tno), TIMER_LEN, true);
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_TIMER # " + (tno & BYTE_MASK) + " val ~"
          + timers.getTimer(tno));
    }
  }

  /**
   * Reset timer.
   *
   * @throws IOException failed to read from port
   * @throws DWCommTimeOutException serial port timeout
   */
  private void doOpResetTimer() throws IOException, DWCommTimeOutException {
    // read rest of packet - timer #
    byte tno = (byte) protodev.comRead1(true);
    timers.resetTimer(tno);
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_RESET_TIMER # " + (tno & BYTE_MASK));
    }
  }

  // serial ports

  /**
   * Get serial stat.
   *
   * @throws IOException failed to read/write
   * @throws DWCommTimeOutException serial port timeout
   */
  private void doOpSerialGetStat() throws IOException, DWCommTimeOutException {
    byte[] responseBuffer;
    // get packet args
    // port # and stat
    responseBuffer = protodev.comRead(2);
    // Z
    if (
        (responseBuffer[0] >= this.dwVSerialPorts.getMaxNPorts())
            && (responseBuffer[0] < this.dwVSerialPorts.getMaxZPorts())
            && (responseBuffer[1] == SS_KY_SNS)
    ) {
      protodev.comWrite1(0, true);
    }

    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_SERGETSTAT: " + DWUtils.prettySS(responseBuffer[1])
          + " port: " + responseBuffer[0] + "("
          + dwVSerialPorts.prettyPort(responseBuffer[0]) + ")");
    }
  }

  /**
   * Set serial stat.
   *
   * @throws IOException failed to read/write
   * @throws DWCommTimeOutException serial port timeout
   */
  private void doOpSetSerialStat() throws IOException, DWCommTimeOutException {
    byte[] responseBuffer;
    try {
      // get packet args
      // port # and stat
      responseBuffer = protodev.comRead(2);
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.info("DoOP_SERSETSTAT: " + DWUtils.prettySS(responseBuffer[1])
            + " port: " + responseBuffer[0] + "("
            + dwVSerialPorts.prettyPort(responseBuffer[0]) + ")");
      }
      switch (responseBuffer[1]) {
        // SS.ComSt
        case SS_COMST -> {
          byte[] devDescr;
          devDescr = protodev.comRead(DEVICE_DESCRIPTION_LENGTH);
          LOGGER.debug("COMST on port " + responseBuffer[0] + ": "
              + DWUtils.byteArrayToHexString(devDescr));
          // should move into DWVSerialPorts
          // store it
          dwVSerialPorts.setDD(responseBuffer[0], devDescr);
          // set PD.INT offset 16 and PD.QUT offset 17
          if (dwVSerialPorts.getPD_INT(responseBuffer[0])
              != devDescr[PD_INT_OFFSET]) {
            dwVSerialPorts.setPD_INT(
                responseBuffer[0], devDescr[PD_INT_OFFSET]
            );
          }
          if (dwVSerialPorts.getPD_QUT(responseBuffer[0])
              != devDescr[PD_QUT_OFFSET]) {
            dwVSerialPorts.setPD_QUT(
                responseBuffer[0], devDescr[PD_QUT_OFFSET]
            );
          }
        }
        case SS_S_OPEN -> dwVSerialPorts.openPort(responseBuffer[0]);
        case SS_S_CLOSE -> dwVSerialPorts.closePort(responseBuffer[0]);
        default -> { }
      }
    } catch (DWPortNotValidException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Initialise serial port.
   * <p>
   *   Functionality is removed. Just
   *   fetches the port number and
   *   exits
   * </p>
   * @throws IOException failed to read
   * @throws DWCommTimeOutException serial port timeout
   * @throws DWPortNotValidException invalid port
   */
  private void doOpSerialInit()
      throws IOException, DWCommTimeOutException, DWPortNotValidException {
    byte[] responseBuffer;
    // get packet args
    // port # (mode no longer sent)
    responseBuffer = protodev.comRead(1);
    int portNumber = responseBuffer[0];
    //dwVSerialPorts.openPort(portNumber);
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_SERINIT for port "
          + dwVSerialPorts.prettyPort(portNumber));
    }
  }

  /**
   * Terminate serial port.
   * <p>
   *   Functionality is removed. Just
   *   fetches the port number and
   *   exits
   * </p>
   * @throws IOException failed to read from port
   * @throws DWCommTimeOutException serial timeout
   * @throws DWPortNotValidException invalid port
   */
  private void doOpSerialTerm()
      throws IOException, DWCommTimeOutException, DWPortNotValidException {
    int portNumber;

    // get packet args
    // just port #
    portNumber = protodev.comRead1(true);
    //dwVSerialPorts.closePort(portNumber);
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_SERTERM for port " + portNumber);
    }
  }

  /**
   * Read a single byte from serial.
   */
  private void doOpSerialRead() {
    byte[] result;
    result = dwVSerialPorts.serRead();
    protodev.comWrite(result, 2, true);
    //if (result[0] != 0)
    if (config.getBoolean("LogOpCodePolls", false)) {
      LOGGER.info("DoOP_SERREAD response " + (int) (result[0] & BYTE_MASK)
          + ":" + (int) (result[1] & BYTE_MASK));
    }
  }

  /**
   * Write a single byte to serial.
   *
   * @throws IOException failed to read/write
   * @throws DWCommTimeOutException comm port timeout
   */
  private void doOpSerialWrite() throws IOException, DWCommTimeOutException {
    byte[] cmdPacket;
    try {
      cmdPacket = protodev.comRead(2);
      dwVSerialPorts.serWrite(cmdPacket[0], cmdPacket[1]);
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.debug("DoOP_SERWRITE to port " + cmdPacket[0]);
      }
    } catch (DWPortNotOpenException | DWPortNotValidException e1) {
      LOGGER.error(e1.getMessage());
    }
  }

  /**
   * Read multiple bytes from serial port.
   *
   * @throws IOException failed read/write
   * @throws DWCommTimeOutException comm port time out
   */
  private void doOpSerialReadM()
      throws IOException, DWCommTimeOutException {
    byte[] cmdPacket;
    byte[] data;
    try {
      cmdPacket = protodev.comRead(2);
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.info("DoOP_SERREADM for " + (cmdPacket[1] & BYTE_MASK)
            + " bytes on port " + cmdPacket[0]);
      }
      data = dwVSerialPorts.serReadM(cmdPacket[0], (cmdPacket[1] & BYTE_MASK));
      protodev.comWrite(data, cmdPacket[1] & BYTE_MASK, true);
    } catch (DWPortNotOpenException | DWPortNotValidException e1) {
      LOGGER.error(e1.getMessage());
    }
  }

  /**
   * Write multiple bytes  to serial port.
   *
   * @throws IOException failed read/write
   * @throws DWCommTimeOutException port time out
   */
  private void doOpSerialWriteM()
      throws IOException, DWCommTimeOutException {
    byte[] cmdPacket;
    try {
      cmdPacket = protodev.comRead(2);
      byte[] data;
      data = protodev.comRead(BYTE_MASK & cmdPacket[1]);
      dwVSerialPorts.serWriteM(cmdPacket[0], data);
      if (config.getBoolean("LogOpCode", false)) {
        LOGGER.debug("DoOP_SERWRITEM to port " + cmdPacket[0] + ", "
            + (BYTE_MASK & cmdPacket[1]) + " bytes");
      }
    } catch (DWPortNotOpenException | DWPortNotValidException e1) {
      LOGGER.error(e1.getMessage());
    }
  }

  // printing
  /**
   * Print single byte.
   *
   * @throws IOException failed to write to print stream
   * @throws DWCommTimeOutException printer time out
   */
  private void doOpPrint()
      throws IOException, DWCommTimeOutException {
    int printByte = protodev.comRead1(true);
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_PRINT: byte " + printByte);
    }
    vprinter.addByte((byte) printByte);
  }

  /**
   * Flush printer buffer.
   */
  private void doOpPrintFlush() {
    if (config.getBoolean("LogOpCode", false)) {
      LOGGER.info("DoOP_PRINTFLUSH");
    }
    vprinter.flush();
  }

  /**
   * Calculate checksum for supplied byte array.
   * <p>
   *   Operates on the first N bytes of the
   *   array, not the full length. No check
   *   for N being greater than the array length
   * </p>
   * @param data byte array
   * @param numBytes number of bytes to work on
   * @return checksum
   */
  private int computeChecksum(final byte[] data, final int numBytes) {
    lastChecksum = 0;
    int counter = numBytes;
    /* Check to see if numBytes is odd or even */
    while (counter > 0) {
      counter--;
      lastChecksum += data[counter] & BYTE_MASK;
    }
    return lastChecksum;
  }

  /**
   * Get last drive used.
   *
   * @return last drive
   */
  public int getLastDrive() {
    return lastDrive;
  }

  /**
   * Get read retry count.
   *
   * @return read retries
   */
  @SuppressWarnings("unused")
  public int getReadRetries() {
    return readRetries;
  }

  /**
   * Get write retry count.
   *
   * @return write retries
   */
  @SuppressWarnings("unused")
  public int getWriteRetries() {
    return writeRetries;
  }

  /**
   * Get sectors read count.
   *
   * @return sectors read
   */
  @SuppressWarnings("unused")
  public int getSectorsRead() {
    return sectorsRead;
  }

  /**
   * Get sectors written count.
   *
   * @return sectors written
   */
  @SuppressWarnings("unused")
  public int getSectorsWritten() {
    return sectorsWritten;
  }

  /**
   * Get last operation code.
   *
   * @return last op code
   */
  public byte getLastOpcode() {
    return lastOpcode;
  }

  /**
   * Get last get statistic.
   *
   * @return last get stat.
   */
  public byte getLastGetStat() {
    return lastGetStat;
  }

  /**
   * Get last set statistic.
   *
   * @return last set stat.
   */
  public byte getLastSetStat() {
    return lastSetStat;
  }

  /**
   * Get last checksum.
   *
   * @return checksum
   */
  public int getLastChecksum() {
    return lastChecksum;
  }

  /**
   * Get last error recorded.
   *
   * @return last error id
   */
  public int getLastError() {
    return lastError;
  }

  /**
   * Get last logical sector number.
   *
   * @return last LSN
   */
  public byte[] getLastLSN() {
    return lastLSN;
  }

  /**
   * Get initialisation time.
   *
   * @return init. time
   */
  public GregorianCalendar getInitTime() {
    return dwinitTime;
  }

  /**
   * Get disk drives.
   *
   * @return disk drives
   */
  public DWDiskDrives getDiskDrives() {
    return this.diskDrives;
  }

  /**
   * Get virt. serial ports.
   *
   * @return serial ports
   */
  public DWVSerialPorts getVPorts() {
    return this.dwVSerialPorts;
  }

  /**
   * Is device shutting down.
   *
   * @return true if shutting down
   */
  public boolean isDying() {
    return this.wanttodie;
  }

  /**
   * Get protocol device.
   *
   * @return protocol device
   */
  public DWProtocolDevice getProtoDev() {
    return this.protodev;
  }

  /**
   * Reset protocol device.
   */
  public void resetProtocolDevice() {
    if (!this.wanttodie) {
      LOGGER.info("requesting protocol device reset");
      // flag that we want a reset
      this.resetPending = true;
      if (this.protodev != null) {
        this.protodev.close();
      }
    }
  }

  /**
   * Setup protocol device.
   */
  private void setupProtocolDevice() {
    if ((protodev != null) && (!resetPending)) {
      protodev.shutdown();
    }
    if (config.getString("DeviceType", "dummy")
        .equalsIgnoreCase("dummy")
    ) {
      this.resetPending = false;
    } else if (config.getString("DeviceType")
        .equalsIgnoreCase("serial")
    ) {
      // create serial device
      if ((config.containsKey("SerialDevice")
          && config.containsKey("SerialRate"))
      ) {
        try {
          protodev = new DWSerialDevice(this);
          this.resetPending = false;
        } catch (NoSuchPortException e1) {
          //wanttodie = true; lets keep on living and see how that goes
          LOGGER.error("handler #" + handlerno + ": Serial device '"
              + config.getString("SerialDevice") + "' not found");
        } catch (PortInUseException e2) {
          //wanttodie = true;
          LOGGER.error("handler #" + handlerno + ": Serial device '"
              + config.getString("SerialDevice") + "' in use");
        } catch (UnsupportedCommOperationException e3) {
          //wanttodie = true;
          LOGGER.error("handler #" + handlerno + ": Unsupported comm "
              + "operation while opening serial port '"
              + config.getString("SerialDevice") + "'");
        } catch (IOException e) {
          //wanttodie = true;
          LOGGER.error("handler #" + handlerno + ": IO exception while "
              + "opening serial port '"
              + config.getString("SerialDevice") + "'");
        } catch (TooManyListenersException e) {
          //wanttodie = true;
          LOGGER.error("handler #" + handlerno + ": Too many listeners "
              + "while opening serial port '"
              + config.getString("SerialDevice") + "'");
        }
      } else {
        LOGGER.error("Serial mode requires both SerialDevice and SerialRate "
            + "to be set, please configure this instance.");
        //wanttodie = true;
      }
    } else if (config.getString("DeviceType").equalsIgnoreCase("tcp")
        || config.getString("DeviceType").equalsIgnoreCase("tcp-server")) {
      // create TCP device
      if (config.containsKey("TCPServerPort")) {
        try {
          protodev = new DWTCPDevice(
              this.handlerno, config.getInt("TCPServerPort")
          );
        } catch (IOException e) {
          //wanttodie = true;
          LOGGER.error("handler #" + handlerno + ": " + e.getMessage());
        }
      } else {
        LOGGER.error("TCP server mode requires TCPServerPort "
            + "to be set, cannot use this configuration");
        //wanttodie = true;
      }
    } else if (config.getString("DeviceType")
        .equalsIgnoreCase("tcp-client")) {
      // create TCP device
      if (config.containsKey("TCPClientPort")
          && config.containsKey("TCPClientHost")) {
        try {
          protodev = new DWTCPClientDevice(
              this.handlerno,
              config.getString("TCPClientHost"),
              config.getInt("TCPClientPort")
          );
        } catch (IOException e) {
          //wanttodie = true;
          LOGGER.error("handler #" + handlerno + ": " + e.getMessage());
        }
      } else {
        LOGGER.error("TCP client mode requires TCPClientPort and "
            + "TCPClientHost to be set, cannot use this configuration");
        //wanttodie = true;
      }
    }
  }

  /**
   * Get status text (pretty).
   *
   * @return status text
   */
  @Override
  public String getStatusText() {
    String text = "";
    text += "Last OpCode:   " + DWUtils.prettyOP(getLastOpcode()) + "\r\n";
    text += "Last GetStat:  " + DWUtils.prettySS(getLastGetStat()) + "\r\n";
    text += "Last SetStat:  " + DWUtils.prettySS(getLastSetStat()) + "\r\n";
    text += "Last Drive:    " + getLastDrive() + "\r\n";
    text += "Last LSN:      " + DWUtils.int3(getLastLSN()) + "\r\n";
    text += "Last Error:    " + ((int) getLastError() & BYTE_MASK) + "\r\n";
    text += "\r\n";
    return text;
  }

  /**
   * Get name.
   *
   * @return name
   */
  public String getName() {
    return this.config.getString("[@name]", "Unnamed #" + this.handlerno);
  }

  /**
   * Get handler id.
   *
   * @return handler id
   */
  public int getHandlerNo() {
    return this.handlerno;
  }

  /**
   * Synchronize storage.
   */
  @Override
  public void syncStorage() {
    if ((this.isInOp()) && (this.syncSkipped < DWDefs.DISK_MAX_SYNC_SKIPS)) {
      LOGGER.debug("Ignoring sync request because we are processing a "
          + "protocol operation (" + (this.syncSkipped + 1)
          + " of " + DWDefs.DISK_MAX_SYNC_SKIPS + ")");
      this.syncSkipped++;
    } else if (this.diskDrives != null) {
      this.diskDrives.sync();
      this.syncSkipped = 0;
    } else {
      LOGGER.debug("handler is alive, but disk drive object is null, "
          + "probably startup taking a while... skipping");
    }
  }

  /**
   * Get virt. printer.
   *
   * @return printer
   */
  public DWVPrinter getVPrinter() {
    return (this.vprinter);
  }

  /**
   * Get log appender.
   *
   * @return logger
   */
  public Logger getLogger() {
    return this.LOGGER;
  }

  /**
   * Get cmd columns.
   *
   * @return command output width (columns)
   */
  public int getCMDCols() {
    return getConfig()
        .getInt("DWCommandOutputWidth", DWDefs.DWCMD_DEFAULT_COLS);
  }

  /**
   * Get help.
   *
   * @return help
   */
  public DWHelp getHelp() {
    return dwhelp;
  }

  /**
   * Is handler device ready.
   *
   * @return ready
   */
  @Override
  public boolean isReady() {
    return this.ready;
  }

  /**
   * Submit config event.
   *
   * @param key property
   * @param val value
   */
  @Override
  public void submitConfigEvent(final String key, final String val) {
    DriveWireServer.submitInstanceConfigEvent(this.handlerno, key, val);
  }

  /**
   * Get total ops count.
   *
   * @return total op count
   */
  @Override
  public long getNumOps() {
    return this.totalOps;
  }

  /**
   * Get count of disk ops.
   *
   * @return disk op count
   */
  @Override
  public long getNumDiskOps() {
    return this.diskOps;
  }

  /**
   * Get count of virt. serial ops.
   *
   * @return vserial op count
   */
  @Override
  public long getNumVSerialOps() {
    return this.vserialOps;
  }

  /**
   * Is in operation.
   *
   * @return true if is in operation
   */
  public boolean isInOp() {
    return this.inOp;
  }

  /**
   * Get timers.
   *
   * @return protocol timers
   */
  @Override
  public DWProtocolTimers getTimers() {
    return this.timers;
  }

  /**
   * Is handler's device started.
   *
   * @return true if started
   */
  @Override
  public boolean isStarted() {
    return this.started;
  }

  /**
   * Handler has printers.
   *
   * @return true if printers available
   */
  @Override
  public boolean hasPrinters() {
    return true;
  }

  /**
   * Handler has disks.
   *
   * @return true if disks available
   */
  @Override
  public boolean hasDisks() {
    return true;
  }

  /**
   * Handler has MIDI.
   *
   * @return true if MIDI available
   */
  @Override
  public boolean hasMIDI() {
    return true;
  }

  /**
   * Handler has virt. serial.
   *
   * @return true if available
   */
  @Override
  public boolean hasVSerial() {
    return true;
  }
}
