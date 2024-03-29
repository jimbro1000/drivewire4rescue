package com.groupunix.drivewireserver;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.lf5.LF5Appender;
import org.apache.log4j.spi.LoggingEvent;

import com.groupunix.drivewireserver.dwdisk.DWDiskLazyWriter;
import com.groupunix.drivewireserver.dwexceptions.DWPlatformUnknownException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.dwprotocolhandler.MCXProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.vmodem.VModemProtocolHandler;

/**
 * DriveWireServer - this is the main entry point mechanism for startup.
 * <p>
 * This class is entirely static, do not attempt to use a constructor
 * Handles creation and shutdown of component threads
 * </p>
 */
public final class DriveWireServer {
  /**
   * Drivewire server version.
   */
  public static final String DW_SERVER_VERSION = "4.3.3p";
  /**
   * Drivewire server version date.
   */
  public static final String DW_SERVER_VERSION_DATE = "09/17/2013";
  /**
   * Maximum number of milliseconds allowed for a thread to die gracefully.
   */
  public static final int THREAD_MAX_TIME_TO_DIE_MILLIS = 15_000;
  /**
   * Thread sleep time in milliseconds.
   */
  public static final int THREAD_SLEEP_MILLIS = 100;
  /**
   * Thread sleep time in milliseconds when dying.
   */
  public static final int DYING_THREAD_SLEEP_MILLIS = 1000;
  /**
   * Status poll interval in milliseconds.
   */
  public static final int STATUS_POLL_INTERVAL_MILLIS = 1000;
  /**
   * Bytes in a kilobyte.
   */
  public static final int KILOBYTE_FACTOR = 1024;
  /**
   * Timeout in milliseconds for receiving data.
   */
  public static final int RECEIVE_TIMEOUT = 3000;
  /**
   * Timeout in milliseconds for opening ports.
   */
  public static final int OPEN_PORT_TIMEOUT_MILLIS = 2000;
  /**
   * Buffer size in bytes for appenders.
   */
  public static final int APPENDER_BUFFER_SIZE = 128;
  /**
   * Home source repository URL.
   */
  public static final String SOURCE_REPOSITORY
      = "https://sourceforge.net/apps/mediawiki/drivewireserver/index.php"
      + "?title=Installation";
  /**
   * Console output banner/separator.
   */
  private static final String MSG_BANNER = "-".repeat(80);
  /**
   * Log appender.
   */
  private static final Logger LOGGER = Logger.getLogger(DriveWireServer.class);
  /**
   * Someone called it magic - current time.
   */
  private static final long MAGIC = System.currentTimeMillis();
  /**
   * Vector table for protocol handler threads.
   */
  private static final Vector<Thread> DW_PROTO_HANDLER_THREADS = new Vector<>();
  /**
   * Vector table for protocol handlers.
   */
  private static final Vector<DWProtocol> DW_PROTOCOL_HANDLERS = new Vector<>();
  /**
   * Default status event.
   */
  private static final DWEvent STATUS_EVENT
      = new DWEvent(DWDefs.EVENT_TYPE_STATUS, -1);
  /**
   * Event log cache.
   */
  private static final ArrayList<DWEvent> LOG_CACHE = new ArrayList<>();
  /**
   * Another use for default status event.
   */
  private static final DWEvent EVT
      = new DWEvent(DWDefs.EVENT_TYPE_STATUS, -1);
  /**
   * Default server ui port.
   */
  public static final int DEFAULT_UI_PORT = 6800;
  /**
   * Server configuration.
   */
  private static XMLConfiguration serverConfiguration;
  /**
   * Serial configuration flags.
   */
  private static int configSerial = 0;
  /**
   * Console output appender.
   */
  private static ConsoleAppender consoleAppender;
  /**
   * Drivewire log appender.
   */
  private static DWLogAppender dwAppender;
  /**
   * log formatting pattern.
   */
  private static PatternLayout logLayout
      = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %m%n");
  /**
   * Lazy writer thread.
   */
  private static Thread lazyWriterT;
  /**
   * UI thread object.
   */
  private static DWUIThread uiObj;
  /**
   * UI thread.
   */
  private static Thread uiT;
  /**
   * Waiting to die gracefully.
   */
  private static boolean wantToDie = false;
  /**
   * Configuration file name.
   */
  private static String configFile = "config.xml";
  /**
   * Ready flag.
   */
  private static boolean ready = false;
  /**
   * Use LF5 flag.
   */
  private static boolean useLF5 = false;
  /**
   * LF5 appender object.
   */
  private static LF5Appender lf5appender;
  /**
   * Use backup flag.
   */
  private static boolean useBackup = false;
  /**
   * Test serial port.
   */
  private static SerialPort testSerialPort;
  /**
   * Last memory update.
   */
  private static long lastMemoryUpdate = 0;
  /**
   * Use debug flag.
   */
  private static boolean useDebug = false;
  /**
   * No MIDI flag.
   */
  private static boolean noMIDI = false;
  /**
   * No mount flag.
   */
  private static boolean noMount = false;
  /**
   * No UI flag.
   */
  private static boolean noUI = false;
  /**
   * No server flag.
   */
  private static boolean noServer = false;
  /**
   * Config freeze flag.
   */
  private static boolean configFreeze = false;
  /**
   * Restart logging flag.
   */
  @SuppressWarnings("unused")
  private static boolean restartLogging = false;
  /**
   * Restart UI flag.
   */
  @SuppressWarnings("unused")
  private static boolean restartUi = false;

  /**
   * Hidden default constructor.
   * <p>
   *   Do not attempt to directly instantiate the main class.
   * </p>
   */
  private DriveWireServer() {
    // Hidden default constructor
  }

  /**
   * Main process loop.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    // catch everything
    Thread.setDefaultUncaughtExceptionHandler(new DWExceptionHandler());
    init(args);

    LOGGER.debug("ready...");

    DriveWireServer.ready = true;
    while (!wantToDie) {
      try {
        Thread.sleep(
            DriveWireServer
                .serverConfiguration
                .getInt(
                    "StatusInterval",
                    DYING_THREAD_SLEEP_MILLIS
                )
        );
        checkHandlerHealth();
        submitServerStatus();
      } catch (InterruptedException e) {
        LOGGER.debug("I've been interrupted, now I want to die");
        wantToDie = true;
      }
    }
    serverShutdown();
  }

  private static void checkHandlerHealth() {
    for (int i = 0; i < DriveWireServer.DW_PROTOCOL_HANDLERS.size(); i++) {
      if (
          DW_PROTOCOL_HANDLERS.get(i) != null
              && DW_PROTOCOL_HANDLERS.get(i).isReady()
              && !DW_PROTOCOL_HANDLERS.get(i).isDying()
      ) {
        // check thread
        if (DW_PROTO_HANDLER_THREADS.get(i) == null) {
          LOGGER.error("Null thread for handler #" + i);
        } else {
          if (!DW_PROTO_HANDLER_THREADS.get(i).isAlive()) {
            LOGGER.error("Handler #" + i + " has died. RIP.");

            if (
                DW_PROTOCOL_HANDLERS
                    .get(i)
                    .getConfig()
                    .getBoolean("ZombieResurrection", true)
            ) {
              LOGGER.info(
                  "Arise chicken! Reanimating handler #" + i + ": "
                      + DW_PROTOCOL_HANDLERS
                      .get(i)
                      .getConfig()
                      .getString("[@name]", "unnamed")
              );

              @SuppressWarnings("unchecked")
              final List<HierarchicalConfiguration> handlerConfigurations
                  = serverConfiguration.configurationsAt("instance");

              DW_PROTOCOL_HANDLERS.set(
                  i,
                  new DWProtocolHandler(i, handlerConfigurations.get(i))
              );
              DW_PROTO_HANDLER_THREADS.set(
                  i,
                  new Thread(DW_PROTOCOL_HANDLERS.get(i))
              );
              DW_PROTO_HANDLER_THREADS.get(i).start();
            }
          }
        }
      }
    }
  }

  /**
   * Submit server status.
   * <p>
   *   Collects all current status
   * </p>
   */
  private static void submitServerStatus() {
    final long tickTime = System.currentTimeMillis();

    if (uiObj != null) {
      // add everything
      EVT.setParam(
          DWDefs.EVENT_ITEM_MAGIC,
          DriveWireServer.getMagic() + ""
      );
      EVT.setParam(
          DWDefs.EVENT_ITEM_INTERVAL,
          DriveWireServer
              .serverConfiguration
              .getInt("StatusInterval", STATUS_POLL_INTERVAL_MILLIS) + ""
      );
      EVT.setParam(
          DWDefs.EVENT_ITEM_INSTANCES,
          DriveWireServer.getNumHandlers() + ""
      );
      EVT.setParam(
          DWDefs.EVENT_ITEM_INSTANCESALIVE,
          DriveWireServer.getNumHandlersAlive() + ""
      );
      EVT.setParam(
          DWDefs.EVENT_ITEM_THREADS,
          DWUtils.getRootThreadGroup().activeCount() + ""
      );
      EVT.setParam(
          DWDefs.EVENT_ITEM_UICLIENTS,
          DriveWireServer.uiObj.getNumUIClients() + ""
      );

      // ops
      EVT.setParam(
          DWDefs.EVENT_ITEM_OPS,
          DriveWireServer.getTotalOps() + ""
      );
      EVT.setParam(
          DWDefs.EVENT_ITEM_DISKOPS,
          DriveWireServer.getDiskOps() + ""
      );
      EVT.setParam(
          DWDefs.EVENT_ITEM_VSERIALOPS,
          DriveWireServer.getVSerialOps() + ""
      );

      // some things should not be updated every tick...
      if (tickTime - lastMemoryUpdate > DWDefs.SERVER_MEM_UPDATE_INTERVAL) {
        //System.gc();
        EVT.setParam(
            DWDefs.EVENT_ITEM_MEMTOTAL,
            (Runtime.getRuntime().totalMemory() / KILOBYTE_FACTOR) + ""
        );
        EVT.setParam(
            DWDefs.EVENT_ITEM_MEMFREE,
            (Runtime.getRuntime().freeMemory() / KILOBYTE_FACTOR) + ""
        );
        lastMemoryUpdate = tickTime;
      }

      // only send updated values
      final DWEvent fevt = new DWEvent(DWDefs.EVENT_TYPE_STATUS, -1);

      for (final String key : EVT.getParamKeys()) {
        if (
            !STATUS_EVENT.hasParam(key)
                || !STATUS_EVENT.getParam(key).equals(EVT.getParam(key))
        ) {
          fevt.setParam(key, EVT.getParam(key));
          STATUS_EVENT.setParam(key, EVT.getParam(key));
        }
      }

      if (fevt.getParamKeys().size() > 0) {
        uiObj.submitEvent(fevt);
      }
    }
  }

  /**
   * Get server status event.
   *
   * @return status event
   */
  public static DWEvent getServerStatusEvent() {
    return DriveWireServer.STATUS_EVENT;
  }

  /**
   * Get total ops.
   *
   * @return total protocol ops
   */
  private static long getTotalOps() {
    long res = 0;

    for (final DWProtocol protocol : DW_PROTOCOL_HANDLERS) {
      if (protocol != null) {
        res += protocol.getNumOps();
      }
    }
    return res;
  }

  /**
   * Get disk operations.
   *
   * @return total disk ops
   */
  private static long getDiskOps() {
    long res = 0;
    for (final DWProtocol protocol : DW_PROTOCOL_HANDLERS) {
      if (protocol != null) {
        res += protocol.getNumDiskOps();
      }
    }
    return res;
  }

  /**
   * Get virtual serial ops.
   *
   * @return serial ops total
   */
  private static long getVSerialOps() {
    long res = 0;
    for (final DWProtocol protocol : DW_PROTOCOL_HANDLERS) {
      if (protocol != null) {
        res += protocol.getNumVSerialOps();
      }
    }
    return res;
  }

  /**
   * Initialise server.
   *
   * @param args arguments array
   */
  public static void init(final String[] args) {
    // set thread name
    Thread.currentThread()
        .setName("dwserver-" + Thread.currentThread().getId());

    // command line arguments
    doCmdLineArgs(args);

    // set up initial logging config
    initLogging();

    LOGGER.info("DriveWire Server v" + DW_SERVER_VERSION + " starting");
    LOGGER.debug(
        "Heap max: "
            + Runtime
            .getRuntime()
            .maxMemory() / KILOBYTE_FACTOR / KILOBYTE_FACTOR
            + "MB " + " cur: "
            + Runtime
            .getRuntime()
            .totalMemory() / KILOBYTE_FACTOR / KILOBYTE_FACTOR
            + "MB"
    );
    // load server settings
    try {
      // try to load/parse config
      serverConfiguration = new XMLConfiguration(configFile);

      // only backup if it loads
      if (useBackup) {
        backupConfig(configFile);
      }

    } catch (ConfigurationException e1) {
      LOGGER.fatal(e1.getMessage());
      System.exit(-1);
    }

    // apply settings to logger
    applyLoggingSettings();

    // Try to add native rxtx to lib path
    if (serverConfiguration.getBoolean("LoadRXTX", true)) {
      loadRXTX();
    }

    // test for RXTX..
    if (
        serverConfiguration.getBoolean("UseRXTX", true)
            && !checkRXTXLoaded()
    ) {
      LOGGER.fatal(
          "UseRXTX is set, but RXTX native libraries could not be loaded"
      );
      LOGGER.fatal("Please see " + SOURCE_REPOSITORY);
      System.exit(-1);
    }

    // add server config listener
    serverConfiguration.addConfigurationListener(new DWServerConfigListener());

    // apply configuration

    // auto save
    if (serverConfiguration.getBoolean("ConfigAutosave", true)) {
      LOGGER.debug("Auto save of configuration is enabled");
      serverConfiguration.setAutoSave(true);
    }

    // start protocol handler instance(s)
    startProtoHandlers();

    // start lazy writer
    startLazyWriter();

    // start UI server
    applyUISettings();
  }

  /**
   * Start protocol handlers.
   */
  private static void startProtoHandlers() {
    @SuppressWarnings("unchecked")
    final List<HierarchicalConfiguration> handlerConfigurations
        = serverConfiguration.configurationsAt("instance");

    DW_PROTOCOL_HANDLERS.ensureCapacity(handlerConfigurations.size());
    DW_PROTO_HANDLER_THREADS.ensureCapacity(handlerConfigurations.size());

    int handlerId = 0;

    for (
        final HierarchicalConfiguration
            hierarchicalConfiguration : handlerConfigurations
    ) {
      if (hierarchicalConfiguration.containsKey("Protocol")) {
        if (
            hierarchicalConfiguration
                .getString("Protocol")
                .equals("DriveWire")
        ) {
          DW_PROTOCOL_HANDLERS.add(
              new DWProtocolHandler(handlerId, hierarchicalConfiguration)
          );
        } else if (
            hierarchicalConfiguration
                .getString("Protocol")
                .equals("MCX")
        ) {
          DW_PROTOCOL_HANDLERS.add(
              new MCXProtocolHandler(handlerId, hierarchicalConfiguration)
          );
        } else if (
            hierarchicalConfiguration
                .getString("Protocol")
                .equals("VModem")
        ) {
          DW_PROTOCOL_HANDLERS.add(
              new VModemProtocolHandler(handlerId, hierarchicalConfiguration)
          );
        } else {
          LOGGER.error(
              "Unknown protocol '"
                  + hierarchicalConfiguration.getString("Protocol")
                  + "' in handler."
          );
        }
      } else {
        // default to drivewire
        DW_PROTOCOL_HANDLERS.add(
            new DWProtocolHandler(handlerId, hierarchicalConfiguration)
        );
      }

      DW_PROTO_HANDLER_THREADS.add(
          new Thread(DW_PROTOCOL_HANDLERS.get(handlerId))
      );

      if (hierarchicalConfiguration.getBoolean("AutoStart", true)) {
        startHandler(handlerId);
      }
      handlerId++;
    }
  }

  /**
   * Start new handler at id.
   *
   * @param handlerId handler id
   */
  public static void startHandler(final int handlerId) {
    if (DW_PROTO_HANDLER_THREADS.get(handlerId).isAlive()) {
      LOGGER.error("Requested start of already alive handler #" + handlerId);
    } else {
      LOGGER.info(
          "Starting handler #" + handlerId + ": "
              + DW_PROTOCOL_HANDLERS.get(handlerId).getClass().getSimpleName()
      );

      DW_PROTO_HANDLER_THREADS.get(handlerId).start();

      while (!DW_PROTOCOL_HANDLERS.get(handlerId).isReady()) {
        try {
          Thread.sleep(THREAD_SLEEP_MILLIS);
        } catch (InterruptedException e) {
          LOGGER.warn(
              "Interrupted while waiting for instance "
                  + handlerId + " to become ready."
          );
        }
      }
    }
  }

  /**
   * Stop named handler.
   *
   * @param handlerId handler id
   */
  public static void stopHandler(final int handlerId) {
    LOGGER.info(
        "Stopping handler #" + handlerId + ": "
            + DW_PROTOCOL_HANDLERS.get(handlerId).getClass().getSimpleName()
    );

    final HierarchicalConfiguration config = DW_PROTOCOL_HANDLERS
        .get(handlerId)
        .getConfig();

    DW_PROTOCOL_HANDLERS.get(handlerId).shutdown();

    try {
      DW_PROTO_HANDLER_THREADS
          .get(handlerId)
          .join(THREAD_MAX_TIME_TO_DIE_MILLIS);
    } catch (InterruptedException e) {
      LOGGER.warn(
          "Interrupted while waiting for handler " + handlerId + " to exit"
      );
    }

    DW_PROTOCOL_HANDLERS.remove(handlerId);
    DW_PROTOCOL_HANDLERS.add(
        handlerId, new DWProtocolHandler(handlerId, config)
    );
    DW_PROTO_HANDLER_THREADS.remove(handlerId);
    DW_PROTO_HANDLER_THREADS.add(
        handlerId,
        new Thread(DW_PROTOCOL_HANDLERS.get(handlerId))
    );
  }

  /**
   * Test rxtx library loaded.
   *
   * @return true if loaded
   */
  private static boolean checkRXTXLoaded() {
    // try to load RXTX, redirect its version messages into our logs
    final PrintStream ops = System.out;
    final PrintStream eps = System.err;
    final ByteArrayOutputStream rxtxBaos = new ByteArrayOutputStream();
    final ByteArrayOutputStream rxtxBaes = new ByteArrayOutputStream();
    final PrintStream rxtxOut = new PrintStream(rxtxBaos, false,
        DWDefs.ENCODING);
    final PrintStream rxtxErr = new PrintStream(rxtxBaes, false,
        DWDefs.ENCODING);
    System.setOut(rxtxOut);
    System.setErr(rxtxErr);
    final boolean res = DWUtils.testClassPath("gnu.io.RXTXCommDriver");
    for (
        final String log : rxtxBaes.toString(DWDefs.ENCODING)
        .trim()
        .split("\n")
    ) {
      System.out.println(log);
      if (!log.equals("")) {
        LOGGER.warn(log);
      }
    }
    for (
        final String log : rxtxBaos.toString(DWDefs.ENCODING)
        .trim()
        .split("\n")
    ) {
      System.out.println(log);
      // ignore pesky version warning that doesn't ever seem to matter
      if (!log.equals("WARNING:  RXTX Version mismatch") && !log.equals("")) {
        LOGGER.debug(log);
      }
    }
    System.setOut(ops);
    System.setErr(eps);
    return res;
  }

  /**
   * Load rxtx library.
   */
  private static void loadRXTX() {
    try {
      String rxtxpath;

      if (
          serverConfiguration
              .getString("LoadRXTXPath", "").equals("")
      ) {
        // look for native/x/x in current dir
        final File currentDir = new File(".");
        rxtxpath = currentDir.getCanonicalPath();
        // + native platform dir
        final String[] osParts = System.getProperty("os.name").split(" ");
        if (osParts.length < 1) {
          throw new DWPlatformUnknownException(
              "No native dir for os '"
                  + System.getProperty("os.name") + "' arch '"
                  + System.getProperty("os.arch") + "'"
          );
        }
        rxtxpath += File.separator + "native"
            + File.separator + osParts[0]
            + File.separator + System.getProperty("os.arch");
      } else {
        rxtxpath = serverConfiguration.getString("LoadRXTXPath");
      }
      final File testRXTXPath = new File(rxtxpath);
      LOGGER.debug("Using rxtx lib path: " + rxtxpath);
      if (!testRXTXPath.exists()) {
        throw new DWPlatformUnknownException(
            "No native dir for os '"
                + System.getProperty("os.name") + "' arch '"
                + System.getProperty("os.arch") + "'"
        );
      }

      // add this dir to path...
      System.setProperty(
          "java.library.path",
          System.getProperty("java.library.path")
              + File.pathSeparator + rxtxpath
      );

      //set sys_paths to null, so they will be reread by jvm
      Field sysPathsField;
      sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
      sysPathsField.setAccessible(true);
      sysPathsField.set(null, null);

    } catch (Exception e) {
      LOGGER.fatal(
          e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()
      );

      if (useDebug) {
        System.out.println(MSG_BANNER);
        e.printStackTrace();
        System.out.println(MSG_BANNER);
      }
    }
  }

  /**
   * Initialise logging.
   */
  private static void initLogging() {
    Logger.getRootLogger().removeAllAppenders();
    consoleAppender = new ConsoleAppender(logLayout);
    Logger.getRootLogger().addAppender(consoleAppender);

    if (useLF5) {
      Logger.getRootLogger().addAppender(lf5appender);
    }

    if (useDebug) {
      Logger.getRootLogger().setLevel(Level.ALL);
    } else {
      Logger.getRootLogger().setLevel(Level.INFO);
    }
  }

  /**
   * Process command line arguments.
   *
   * @param args arguments array
   */
  private static void doCmdLineArgs(final String[] args) {
    // set options from cmdline args
    final Options cmdoptions = new Options();

    cmdoptions.addOption(
        "config",
        true,
        "configuration file (defaults to config.xml)");
    cmdoptions.addOption(
        "backup",
        false,
        "make a backup of config at server start");
    cmdoptions.addOption(
        "help",
        false,
        "display command line argument help");
    cmdoptions.addOption(
        "logviewer",
        false,
        "open GUI log viewer at server start");
    cmdoptions.addOption(
        "debug",
        false,
        "log extra info to console");
    cmdoptions.addOption(
        "nomidi",
        false,
        "disable MIDI");
    cmdoptions.addOption(
        "nomount",
        false,
        "do not remount disks from last run");
    cmdoptions.addOption(
        "noui",
        false,
        "do not start user interface");
    cmdoptions.addOption(
        "noserver",
        false,
        "do not start server");
    cmdoptions.addOption(
        "liteui",
        false,
        "use lite user interface");

    final CommandLineParser parser = new GnuParser();
    try {
      final CommandLine line = parser.parse(cmdoptions, args);

      // help
      if (line.hasOption("help")) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar DriveWire.jar [OPTIONS]", cmdoptions);
        System.exit(0);
      }

      if (line.hasOption("config")) {
        configFile = line.getOptionValue("config");
      }

      if (line.hasOption("backup")) {
        useBackup = true;
      }

      if (line.hasOption("debug")) {
        useDebug = true;
      }

      if (line.hasOption("logviewer")) {
        useLF5 = true;
        lf5appender = new LF5Appender();
        lf5appender.setName("DriveWire 4 Server Log");
      }

      if (line.hasOption("nomidi")) {
        noMIDI = true;
      }

      if (line.hasOption("nomount")) {
        noMount = true;
      }

      if (line.hasOption("noui")) {
        noUI = true;
      }

      if (line.hasOption("noserver")) {
        noServer = true;
      }

    } catch (ParseException exp) {
      System.err.println("Could not parse command line: " + exp.getMessage());
      System.exit(-1);
    }
  }

  /**
   * Backup configuration.
   *
   * @param configurationFile configuration file name
   */
  private static void backupConfig(final String configurationFile) {
    try {
      DWUtils.copyFile(configurationFile, configurationFile + ".bak");
      LOGGER.debug("Backed up config to " + configurationFile + ".bak");
    } catch (IOException e) {
      LOGGER.error("Could not create config backup: " + e.getMessage());
    }
  }

  /**
   * Server shutdown.
   */
  public static void serverShutdown() {
    LOGGER.info("server shutting down...");
    LOGGER.debug("stopping protocol handler(s)...");
    for (final DWProtocol protocol : DW_PROTOCOL_HANDLERS) {
      if (protocol != null) {
        protocol.shutdown();
      }
    }
    for (final Thread thread : DW_PROTO_HANDLER_THREADS) {
      if (thread.isAlive()) {
        try {
          thread.interrupt();
          thread.join();
        } catch (InterruptedException e) {
          LOGGER.warn(e.getMessage());
        }
      }
    }
    if (lazyWriterT != null) {
      LOGGER.debug("stopping lazy writer...");
      lazyWriterT.interrupt();
      try {
        lazyWriterT.join();
      } catch (InterruptedException e) {
        LOGGER.warn(e.getMessage());
      }
    }
    if (uiObj != null) {
      LOGGER.debug("stopping UI thread...");
      uiObj.die();
      try {
        uiT.join();
      } catch (InterruptedException e) {
        LOGGER.warn(e.getMessage());
      }
    }
    LOGGER.info("server shutdown complete");
    LOGGER.removeAllAppenders();
  }

  /**
   * Start lazy writer.
   */
  private static void startLazyWriter() {
    lazyWriterT = new Thread(new DWDiskLazyWriter());
    lazyWriterT.start();
  }

  /**
   * Apply UI settings.
   */
  public static void applyUISettings() {
    if (uiT != null && uiT.isAlive()) {
      uiObj.die();
      uiT.interrupt();
      try {
        uiT.join();
      } catch (InterruptedException e) {
        LOGGER.warn(e.getMessage());
      }
    }
    if (serverConfiguration.getBoolean("UIEnabled", false)) {
      uiObj = new DWUIThread(
          serverConfiguration.getInt("UIPort", DEFAULT_UI_PORT)
      );
      uiT = new Thread(uiObj);
      uiT.start();
    }
  }

  /**
   * Apply logging settings.
   */
  public static void applyLoggingSettings() {
    // logging
    if (!serverConfiguration.getString("LogFormat", "").equals("")) {
      logLayout = new PatternLayout(
          serverConfiguration.getString("LogFormat")
      );
    }
    Logger.getRootLogger().removeAllAppenders();
    dwAppender = new DWLogAppender(logLayout);
    Logger.getRootLogger().addAppender(dwAppender);
    if (useLF5) {
      Logger.getRootLogger().addAppender(lf5appender);
    }
    if (serverConfiguration.getBoolean("LogToConsole", true) || useDebug) {
      consoleAppender = new ConsoleAppender(logLayout);
      Logger.getRootLogger().addAppender(consoleAppender);
    }
    if (
        serverConfiguration.getBoolean("LogToFile", false)
            && serverConfiguration.containsKey("LogFile")
    ) {
      try {
        final FileAppender fileAppender = new FileAppender(
            logLayout,
            serverConfiguration.getString("LogFile"),
            true,
            false,
            APPENDER_BUFFER_SIZE
        );
        Logger.getRootLogger().addAppender(fileAppender);
      } catch (IOException e) {
        LOGGER.error(
            "Cannot log to file '"
                + serverConfiguration.getString("LogFile")
                + "': " + e.getMessage()
        );
      }
    }

    if (useDebug) {
      Logger.getRootLogger().setLevel(Level.ALL);
    } else {
      Logger
          .getRootLogger()
          .setLevel(
              Level
                  .toLevel(
                      serverConfiguration
                          .getString("LogLevel", "INFO"))
          );
    }
  }

  /**
   * Get server configuration.
   *
   * @return config
   */
  public static XMLConfiguration getServerConfiguration() {
    return serverConfiguration;
  }

  /**
   * Get configuration serial.
   *
   * @return config serial
   */
  public static int getConfigSerial() {
    return configSerial;
  }

  /**
   * Increment configuration serial.
   */
  public static void incConfigSerial() {
    ++configSerial;
  }

  /**
   * Get Handler.
   *
   * @param handlerId handler id
   * @return handler protocol
   */
  public static DWProtocol getHandler(final int handlerId) {
    if (handlerId < DW_PROTOCOL_HANDLERS.size() && handlerId > -1) {
      return DW_PROTOCOL_HANDLERS.get(handlerId);
    }
    return null;
  }

  /**
   * Get most recent log events.
   *
   * @param numberOfEvents number of events to fetch
   * @return log event list
   */
  public static ArrayList<String> getLogEvents(final int numberOfEvents) {
    return dwAppender.getLastEvents(numberOfEvents);
  }

  /**
   * Get log events size.
   *
   * @return events log size
   */
  public static int getLogEventsSize() {
    return dwAppender.getEventsSize();
  }

  /**
   * Get number of handlers.
   *
   * @return total number of handlers
   */
  public static int getNumHandlers() {
    return DW_PROTOCOL_HANDLERS.size();
  }

  /**
   * Get number of live handlers.
   *
   * @return count of living handlers
   */
  public static int getNumHandlersAlive() {
    int res = 0;
    for (final DWProtocol protocol : DW_PROTOCOL_HANDLERS) {
      if (protocol != null && !protocol.isDying() && protocol.isReady()) {
        res++;
      }
    }
    return res;
  }

  /**
   * Is handler id valid.
   *
   * @param handler handler id
   * @return true if valid id
   */
  public static boolean isValidHandlerNo(final int handler) {
    boolean result = false;
    if (handler < DW_PROTOCOL_HANDLERS.size() && handler >= 0) {
      result = DW_PROTOCOL_HANDLERS.get(handler) != null;
    }
    return result;
  }

  /**
   * Restart handler.
   *
   * @param handler handler id
   */
  public static void restartHandler(final int handler) {
    LOGGER.info("Restarting handler #" + handler);
    stopHandler(handler);
    startHandler(handler);
  }

  /**
   * Handler is alive.
   *
   * @param handlerId handler id
   * @return true if alive
   */
  public static boolean handlerIsAlive(final int handlerId) {
    boolean result = false;
    if (
        DW_PROTOCOL_HANDLERS.get(handlerId) != null
            && DW_PROTO_HANDLER_THREADS.get(handlerId) != null
    ) {
      result = !DW_PROTOCOL_HANDLERS.get(handlerId).isDying()
          && DW_PROTO_HANDLER_THREADS.get(handlerId).isAlive();
    }
    return result;
  }

  /**
   * Get handler name.
   *
   * @param handlerId handler id
   * @return handler name
   */
  public static String getHandlerName(final int handlerId) {
    if (isValidHandlerNo(handlerId)) {
      return DW_PROTOCOL_HANDLERS
          .get(handlerId)
          .getConfig()
          .getString("[@name]", "unnamed instance " + handlerId);
    }
    return "null handler " + handlerId;
  }

  /**
   * Save configuration.
   *
   * @throws ConfigurationException invalid configuration
   */
  public static void saveServerConfig() throws ConfigurationException {
    serverConfiguration.save();
  }

  /**
   * Get available serial ports as array list.
   *
   * @return list of available ports
   */
  @SuppressWarnings("unchecked")
  public static ArrayList<String> getAvailableSerialPorts() {
    final ArrayList<String> ports = new ArrayList<>();
    final Enumeration<CommPortIdentifier> allPorts =
        CommPortIdentifier.getPortIdentifiers();
    while (allPorts.hasMoreElements()) {
      try {
        final CommPortIdentifier com = allPorts.nextElement();
        if (com.getPortType() == CommPortIdentifier.PORT_SERIAL) {
          ports.add(com.getName());
        }
      } catch (Exception e) {
        LOGGER.error("While detecting serial devices: " + e.getMessage());
        if (useDebug) {
          System.out.println(MSG_BANNER);
          e.printStackTrace();
          System.out.println(MSG_BANNER);
        }
      }
    }
    return ports;
  }

  /**
   * Get serial port status.
   * <p>
   *   Attempts to find status by trial opening port
   * </p>
   *
   * @param port port name
   * @return status
   */
  public static String getSerialPortStatus(final String port) {
    String res = "";

    try {
      final CommPortIdentifier identifier
          = CommPortIdentifier.getPortIdentifier(port);
      if (identifier.isCurrentlyOwned()) {
        res = "In use by " + identifier.getCurrentOwner();
      } else {
        final CommPort commPort = identifier.open(
            "DriveWireServer",
            OPEN_PORT_TIMEOUT_MILLIS
        );
        if (commPort instanceof SerialPort) {
          res = "Available";
        } else {
          res = "Not a serial port";
        }
        commPort.close();
      }
    } catch (Exception e) {
      res = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
      if (useDebug) {
        System.out.println(MSG_BANNER);
        e.printStackTrace();
        System.out.println(MSG_BANNER);
      }
    }
    return res;
  }

  /**
   * Shutdown server.
   */
  public static void shutdown() {
    LOGGER.info("server shutdown requested");
    wantToDie = true;
  }

  /**
   * Submit server configuration event.
   *
   * @param key parameter key
   * @param value parameter value
   */
  public static void submitServerConfigEvent(
      final String key,
      final String value
  ) {
    if (uiObj != null) {
      final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_SERVERCONFIG, -1);
      evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, value);
      uiObj.submitEvent(evt);
    }
  }

  /**
   * Submit instance configuration event.
   *
   * @param instance instance id
   * @param key property key
   * @param value property value
   */
  public static void submitInstanceConfigEvent(
      final int instance,
      final String key,
      final String value
  ) {
    if (uiObj != null) {
      final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_INSTANCECONFIG,
          instance);

      evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
      evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, value);

      uiObj.submitEvent(evt);
    }
  }

  /**
   * Submit disk event.
   *
   * @param instance instance id
   * @param diskNumber disk id
   * @param key parameter key
   * @param val parameter value
   */
  public static void submitDiskEvent(
      final int instance,
      final int diskNumber,
      final String key,
      final String val
  ) {
    if (uiObj != null) {
      final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_DISK, instance);
      evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
      evt.setParam(DWDefs.EVENT_ITEM_DRIVE, String.valueOf(diskNumber));
      evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, val);
      uiObj.submitEvent(evt);
    }
  }

  /**
   * Submit midi event.
   *
   * @param instance instance id
   * @param key parameter key
   * @param value parameter value
   */
  public static void submitMIDIEvent(
      final int instance,
      final String key,
      final String value
  ) {
    if (uiObj != null) {
      final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_MIDI, instance);
      evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
      evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, value);
      uiObj.submitEvent(evt);
    }
  }

  /**
   * Submit logging event.
   *
   * @param event logging event
   */
  public static void submitLogEvent(final LoggingEvent event) {
    final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_LOG, -1);

    evt.setParam(DWDefs.EVENT_ITEM_LOGLEVEL, event.getLevel().toString());
    evt.setParam(DWDefs.EVENT_ITEM_TIMESTAMP, event.timeStamp + "");
    evt.setParam(DWDefs.EVENT_ITEM_LOGMSG, event.getMessage().toString());
    evt.setParam(DWDefs.EVENT_ITEM_THREAD, event.getThreadName());
    evt.setParam(DWDefs.EVENT_ITEM_LOGSRC, event.getLoggerName());

    synchronized (LOG_CACHE) {
      LOG_CACHE.add(evt);
      if (LOG_CACHE.size() > DWDefs.LOGGING_MAX_BUFFER_EVENTS) {
        LOG_CACHE.remove(0);
      }
    }
    if (uiObj != null) {
      uiObj.submitEvent(evt);
    }
  }

  /**
   * Is server ready.
   *
   * @return true if ready
   */
  public static boolean isReady() {
    return DriveWireServer.ready;
  }

  /**
   * Test open serial port.
   *
   * @param device device name
   * @return true if successful
   * @throws Exception
   */
  public static boolean testSerialPortOpen(
      final String device
  ) throws Exception {
    try {
      final CommPortIdentifier identifier =
          CommPortIdentifier.getPortIdentifier(device);

      if (identifier.isCurrentlyOwned()) {
        throw new Exception("In use by " + identifier.getCurrentOwner());
      } else {
        final CommPort commPort = identifier.open(
            "DriveWireTest",
            OPEN_PORT_TIMEOUT_MILLIS
        );

        if (commPort instanceof SerialPort) {
          testSerialPort = (SerialPort) commPort;
          testSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
          testSerialPort.enableReceiveThreshold(1);
          testSerialPort.enableReceiveTimeout(RECEIVE_TIMEOUT);
          return true;
        } else {
          throw new Exception("Not a serial port");
        }
      }
    } catch (Exception e) {
      throw new Exception(e.getLocalizedMessage());
    }
  }

  /**
   * Test serial port set baud parameter.
   *
   * @param rate baud rate
   * @return true if successful
   * @throws Exception
   */
  public static boolean testSerialPortSetParams(
      final int rate
  ) throws Exception {
    try {
      testSerialPort.setSerialPortParams(
          rate,
          SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);
      return true;
    } catch (Exception e) {
      throw new Exception(e.getLocalizedMessage());
    }
  }

  /**
   * Test read of serial port.
   *
   * @return read result
   * @throws Exception
   */
  public static int testSerialPortRead() throws Exception {
    try {
      return testSerialPort.getInputStream().read();
    } catch (Exception e) {
      throw new Exception(e.getLocalizedMessage());
    }
  }

  /**
   * Test closing of serial port.
   */
  public static void testSerialPortClose() {
    try {
      testSerialPort.close();
    } catch (Exception e) {
      LOGGER.warn("failed to close serial port: " + e.getMessage(), e);
    }
  }

  /**
   * Get log cache.
   *
   * @return server log cache
   */
  public static ArrayList<DWEvent> getLogCache() {
    return DriveWireServer.LOG_CACHE;
  }

  /**
   * Is log to console set.
   *
   * @return true if set
   */
  public static boolean isConsoleLogging() {
    return DriveWireServer
        .serverConfiguration
        .getBoolean("LogToConsole", false);
  }

  /**
   * Is debug flag set.
   *
   * @return true if set
   */
  public static boolean isDebug() {
    return useDebug;
  }

  /**
   * Handle caught exception from thread.
   *
   * @param thread originating thread
   * @param thrown thrown exception
   */
  public static void handleUncaughtException(
      final Thread thread,
      final Throwable thrown
  ) {
    final StringBuilder msg = new StringBuilder();
    msg.append("Exception in thread ").append(thread.getName());
    msg.append(": ").append(thrown.getClass().getSimpleName());

    if (thrown.getMessage() != null) {
      msg.append(": ").append(thrown.getMessage());
    }

    if (DriveWireServer.LOGGER != null) {
      LOGGER.error(msg.toString());
      LOGGER.info(getStackTrace(thrown));
    }

    System.out.println(MSG_BANNER);
    System.out.println(msg);
    System.out.println(MSG_BANNER);
    thrown.printStackTrace();
  }

  /**
   * Get stack trace.
   *
   * @param aThrowable thrown exception
   * @return stringified stack trace
   */
  public static String getStackTrace(final Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  /**
   * Get magic.
   *
   * @return magic
   */
  public static long getMagic() {
    return MAGIC;
  }

  /**
   * Get no midi flag.
   *
   * @return no midi flag
   */
  public static boolean isNoMIDI() {
    return noMIDI;
  }

  /**
   * Get no mount flag.
   *
   * @return no mount flag
   */
  public static boolean isNoMount() {
    return noMount;
  }

  /**
   * Get no UI flag.
   *
   * @return no UI flag
   */
  @SuppressWarnings("unused")
  public static boolean isNoUI() {
    return noUI;
  }

  /**
   * Get no server flag.
   *
   * @return no server flag
   */
  @SuppressWarnings("unused")
  public static boolean isNoServer() {
    return noServer;
  }

  /**
   * Get config freeze.
   *
   * @return config freeze flag
   */
  public static boolean isConfigFreeze() {
    return DriveWireServer.configFreeze;
  }

  /**
   * Set config freeze.
   *
   * @param freeze boolean
   */
  public static void setConfigFreeze(final boolean freeze) {
    DriveWireServer.configFreeze = freeze;
  }

  /**
   * Set logging restart flag.
   */
  public static void setLoggingRestart() {
    DriveWireServer.restartLogging = true;
  }

  /**
   * Set UI restart flag.
   */
  public static void setUIRestart() {
    DriveWireServer.restartUi = true;
  }

  /**
   * Get log appender.
   *
   * @return logger
   */
  public static Logger getLogger() {
    return LOGGER;
  }
}
