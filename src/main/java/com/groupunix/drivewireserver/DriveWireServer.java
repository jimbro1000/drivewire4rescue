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
  public static final int THREAD_MAX_TIME_TO_DIE_MILLIS = 15000;
  /**
   * Thread sleep time in milliseconds.
   */
  public static final int THREAD_SLEEP_MILLIS = 100;
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
   * Server configuration.
   */
  public static XMLConfiguration serverConfiguration;
  /**
   * Serial configuration flags.
   */
  public static int configSerial = 0;
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
  private static Thread lazyWriterT;
  private static DWUIThread uiObj;
  private static Thread uiT;
  private static boolean wantToDie = false;
  private static String configFile = "config.xml";
  private static boolean ready = false;
  private static boolean useLF5 = false;
  private static LF5Appender lf5appender;
  private static boolean useBackup = false;
  private static SerialPort testSerialPort;
  private static long lastMemoryUpdate = 0;
  private static boolean useDebug = false;
  private static boolean noMIDI = false;
  private static boolean noMount = false;
  private static boolean noUI = false;
  private static boolean noServer = false;
  private static boolean configFreeze = false;

  @SuppressWarnings("unused")
  private static boolean restartLogging = false;
  @SuppressWarnings("unused")
  private static boolean restartUi = false;

  private DriveWireServer() {
    // Hidden default constructor - do not use, main process is entirely static
  }

  public static void main(final String[] args) throws ConfigurationException {

    // catch everything
    Thread.setDefaultUncaughtExceptionHandler(new DWExceptionHandler());

    init(args);

    // install clean shutdown handler
    //Runtime.getRuntime().addShutdownHook(new DWShutdownHandler());
    // hang around
    LOGGER.debug("ready...");

    DriveWireServer.ready = true;
    while (!wantToDie) {
      try {
        Thread.sleep(
            DriveWireServer
                .serverConfiguration
                .getInt(
                    "StatusInterval"
                    , THREAD_SLEEP_MILLIS * 10
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
    //System.exit(0);
  }

  private static void checkHandlerHealth() {
    for (int i = 0; i < DriveWireServer.DW_PROTOCOL_HANDLERS.size(); i++) {
      if (
          (DW_PROTOCOL_HANDLERS.get(i) != null)
              && (DW_PROTOCOL_HANDLERS.get(i).isReady())
              && (!DW_PROTOCOL_HANDLERS.get(i).isDying())
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
              List<HierarchicalConfiguration> handlerConfigurations
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

  private static void submitServerStatus() {
    long tickTime = System.currentTimeMillis();

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
      DWEvent fevt = new DWEvent(DWDefs.EVENT_TYPE_STATUS, -1);

      for (String key : EVT.getParamKeys()) {
        if (
            !STATUS_EVENT.hasParam(key)
                || (!STATUS_EVENT.getParam(key).equals(EVT.getParam(key)))
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

  public static DWEvent getServerStatusEvent() {
    return DriveWireServer.STATUS_EVENT;
  }


  private static long getTotalOps() {
    long res = 0;

    for (DWProtocol p : DW_PROTOCOL_HANDLERS) {
      if (p != null) {
        res += p.getNumOps();
      }
    }

    return res;
  }


  private static long getDiskOps() {
    long res = 0;

    for (DWProtocol p : DW_PROTOCOL_HANDLERS) {
      if (p != null) {
        res += p.getNumDiskOps();
      }
    }

    return res;
  }


  private static long getVSerialOps() {
    long res = 0;

    for (DWProtocol p : DW_PROTOCOL_HANDLERS) {
      if (p != null) {
        res += p.getNumVSerialOps();
      }
    }

    return res;
  }


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

  private static void startProtoHandlers() {
    @SuppressWarnings("unchecked")
    List<HierarchicalConfiguration> handlerConfigurations
        = serverConfiguration.configurationsAt("instance");

    DW_PROTOCOL_HANDLERS.ensureCapacity(handlerConfigurations.size());
    DW_PROTO_HANDLER_THREADS.ensureCapacity(handlerConfigurations.size());

    int handlerId = 0;

    for (
        HierarchicalConfiguration
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

  public static void stopHandler(final int handlerId) {
    LOGGER.info(
        "Stopping handler #" + handlerId + ": "
            + DW_PROTOCOL_HANDLERS.get(handlerId).getClass().getSimpleName()
    );

    HierarchicalConfiguration hc = DW_PROTOCOL_HANDLERS
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
    DW_PROTOCOL_HANDLERS.add(handlerId, new DWProtocolHandler(handlerId, hc));
    DW_PROTO_HANDLER_THREADS.remove(handlerId);
    DW_PROTO_HANDLER_THREADS.add(
        handlerId,
        new Thread(DW_PROTOCOL_HANDLERS.get(handlerId))
    );
  }

  private static boolean checkRXTXLoaded() {
    // try to load RXTX, redirect its version messages into our logs

    PrintStream ops = System.out;
    PrintStream eps = System.err;

    ByteArrayOutputStream rxtxBaos = new ByteArrayOutputStream();
    ByteArrayOutputStream rxtxBaes = new ByteArrayOutputStream();

    PrintStream rxtxOut = new PrintStream(rxtxBaos);
    PrintStream rxtxErr = new PrintStream(rxtxBaes);

    System.setOut(rxtxOut);
    System.setErr(rxtxErr);

    boolean res = DWUtils.testClassPath("gnu.io.RXTXCommDriver");

    for (String l : rxtxBaes.toString().trim().split("\n")) {
      System.out.println(l);
      if (!l.equals("")) {
        LOGGER.warn(l);
      }
    }

    for (String l : rxtxBaos.toString().trim().split("\n")) {
      System.out.println(l);
      // ignore pesky version warning that doesn't ever seem to matter
      if (!l.equals("WARNING:  RXTX Version mismatch") && !l.equals("")) {
        LOGGER.debug(l);
      }
    }

    System.setOut(ops);
    System.setErr(eps);

    return (res);
  }

  private static void loadRXTX() {
    try {
      String rxtxpath;

      if (
          !serverConfiguration
              .getString("LoadRXTXPath", "").equals("")
      ) {
        rxtxpath = serverConfiguration.getString("LoadRXTXPath");
      } else {
        // look for native/x/x in current dir
        File currentDir = new File(".");
        rxtxpath = currentDir.getCanonicalPath();

        // + native platform dir
        String[] osParts = System.getProperty("os.name").split(" ");

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
      }

      File testRXTXPath = new File(rxtxpath);
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

  private static void doCmdLineArgs(final String[] args) {
    // set options from cmdline args
    Options cmdoptions = new Options();

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

    CommandLineParser parser = new GnuParser();
    try {
      CommandLine line = parser.parse(cmdoptions, args);

      // help
      if (line.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
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

  private static void backupConfig(final String configurationFile) {
    try {
      DWUtils.copyFile(configurationFile, configurationFile + ".bak");
      LOGGER.debug("Backed up config to " + configurationFile + ".bak");
    } catch (IOException e) {
      LOGGER.error("Could not create config backup: " + e.getMessage());
    }
  }

  public static void serverShutdown() {
    LOGGER.info("server shutting down...");

    if (DW_PROTO_HANDLER_THREADS != null) {
      LOGGER.debug("stopping protocol handler(s)...");

      for (DWProtocol p : DW_PROTOCOL_HANDLERS) {
        if (p != null) {
          p.shutdown();
        }
      }

      for (Thread t : DW_PROTO_HANDLER_THREADS) {
        if (t.isAlive()) {
          try {
            t.interrupt();
            t.join();
          } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
          }
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

  private static void startLazyWriter() {
    lazyWriterT = new Thread(new DWDiskLazyWriter());
    lazyWriterT.start();
  }

  public static void applyUISettings() {
    if ((uiT != null) && (uiT.isAlive())) {
      uiObj.die();
      uiT.interrupt();
      try {
        uiT.join();
      } catch (InterruptedException e) {
        LOGGER.warn(e.getMessage());
      }
    }

    if (serverConfiguration.getBoolean("UIEnabled", false)) {
      uiObj = new DWUIThread(serverConfiguration.getInt("UIPort", 6800));
      uiT = new Thread(uiObj);
      uiT.start();
    }
  }

  public static void applyLoggingSettings() {
    // logging
    if (!serverConfiguration.getString("LogFormat", "").equals("")) {
      logLayout = new PatternLayout(serverConfiguration.getString("LogFormat"));
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
        (serverConfiguration.getBoolean("LogToFile", false))
            && (serverConfiguration.containsKey("LogFile"))
    ) {
      try {
        FileAppender fileAppender = new FileAppender(
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

  public static DWProtocol getHandler(final int handlerId) {
    if ((handlerId < DW_PROTOCOL_HANDLERS.size()) && (handlerId > -1)) {
      return (DW_PROTOCOL_HANDLERS.get(handlerId));
    }
    return null;
  }

  public static ArrayList<String> getLogEvents(final int numberOfEvents) {
    return (dwAppender.getLastEvents(numberOfEvents));
  }

  public static int getLogEventsSize() {
    return (dwAppender.getEventsSize());
  }

  public static int getNumHandlers() {
    return DW_PROTOCOL_HANDLERS.size();
  }

  public static int getNumHandlersAlive() {
    int res = 0;

    for (DWProtocol p : DW_PROTOCOL_HANDLERS) {
      if (p != null) {
        if (!p.isDying() && p.isReady()) {
          res++;
        }
      }
    }

    return res;
  }

  public static boolean isValidHandlerNo(final int handler) {
    boolean result = false;
    if ((handler < DW_PROTOCOL_HANDLERS.size()) && (handler >= 0)) {
      result = DW_PROTOCOL_HANDLERS.get(handler) != null;
    }
    return result;
  }

  public static void restartHandler(final int handler) {
    LOGGER.info("Restarting handler #" + handler);
    stopHandler(handler);
    startHandler(handler);
  }

  public static boolean handlerIsAlive(final int handlerId) {
    boolean result = false;
    if (
        (DW_PROTOCOL_HANDLERS.get(handlerId) != null)
            && (DW_PROTO_HANDLER_THREADS.get(handlerId) != null)
    ) {
      result = (!DW_PROTOCOL_HANDLERS.get(handlerId).isDying())
          && (DW_PROTO_HANDLER_THREADS.get(handlerId).isAlive());
    }
    return result;
  }

  public static String getHandlerName(final int handlerId) {
    if (isValidHandlerNo(handlerId)) {
      return DW_PROTOCOL_HANDLERS
          .get(handlerId)
          .getConfig()
          .getString("[@name]", "unnamed instance " + handlerId);
    }
    return ("null handler " + handlerId);
  }

  public static void saveServerConfig() throws ConfigurationException {
    serverConfiguration.save();
  }

  @SuppressWarnings("unchecked")
  public static ArrayList<String> getAvailableSerialPorts() {
    ArrayList<String> h = new ArrayList<String>();

    java.util.Enumeration<gnu.io.CommPortIdentifier> thePorts =
        gnu.io.CommPortIdentifier.getPortIdentifiers();
    while (thePorts.hasMoreElements()) {
      try {
        gnu.io.CommPortIdentifier com = thePorts.nextElement();
        if (com.getPortType() == gnu.io.CommPortIdentifier.PORT_SERIAL) {
          h.add(com.getName());
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

    return h;
  }

  public static String getSerialPortStatus(final String port) {
    String res = "";

    try {
      CommPortIdentifier pi = CommPortIdentifier.getPortIdentifier(port);

      if (pi.isCurrentlyOwned()) {
        res = "In use by " + pi.getCurrentOwner();
      } else {
        CommPort commPort = pi.open(
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

  public static void shutdown() {
    LOGGER.info("server shutdown requested");
    wantToDie = true;
  }

  public static void submitServerConfigEvent(
      final String propertyName,
      final String propertyValue
  ) {
    if (uiObj != null) {
      DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_SERVERCONFIG, -1);
      evt.setParam(DWDefs.EVENT_ITEM_KEY, propertyName);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, propertyValue);
      uiObj.submitEvent(evt);
    }
  }

  public static void submitInstanceConfigEvent(
      final int instance,
      final String propertyName,
      final String propertyValue
  ) {
    if (uiObj != null) {
      DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_INSTANCECONFIG, instance);

      evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
      evt.setParam(DWDefs.EVENT_ITEM_KEY, propertyName);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, propertyValue);

      uiObj.submitEvent(evt);
    }
  }

  public static void submitDiskEvent(
      final int instance,
      final int diskNumber,
      final String key,
      final String val
  ) {
    if (uiObj != null) {
      DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_DISK, instance);
      evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
      evt.setParam(DWDefs.EVENT_ITEM_DRIVE, String.valueOf(diskNumber));
      evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, val);
      uiObj.submitEvent(evt);
    }
  }

  public static void submitMIDIEvent(
      final int instance,
      final String key,
      final String value
  ) {
    if (uiObj != null) {
      DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_MIDI, instance);

      evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
      evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
      evt.setParam(DWDefs.EVENT_ITEM_VALUE, value);

      uiObj.submitEvent(evt);
    }
  }

  public static void submitLogEvent(final LoggingEvent event) {
    DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_LOG, -1);

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

  public static boolean isReady() {
    return DriveWireServer.ready;
  }

  public static boolean testSerialPortOpen(
      final String device
  ) throws Exception {
    try {
      CommPortIdentifier pi = CommPortIdentifier.getPortIdentifier(device);

      if (pi.isCurrentlyOwned()) {
        throw (new Exception("In use by " + pi.getCurrentOwner()));
      } else {
        CommPort commPort = pi.open(
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
          throw (new Exception("Not a serial port"));
        }
      }
    } catch (Exception e) {
      throw (new Exception(e.getLocalizedMessage()));
    }
  }

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
      throw (new Exception(e.getLocalizedMessage()));
    }
  }

  public static int testSerialPortRead() throws Exception {
    try {
      return (testSerialPort.getInputStream().read());
    } catch (Exception e) {
      throw (new Exception(e.getLocalizedMessage()));
    }
  }

  public static void testSerialPortClose() {
    try {
      testSerialPort.close();
    } catch (Exception e) {
      LOGGER.warn("failed to close serial port: " + e.getMessage(), e);
    }
  }

  public static ArrayList<DWEvent> getLogCache() {
    return DriveWireServer.LOG_CACHE;
  }

  public static boolean isConsoleLogging() {
    return DriveWireServer
        .serverConfiguration
        .getBoolean("LogToConsole", false);
  }

  public static boolean isDebug() {
    return (useDebug);
  }

  public static void handleUncaughtException(
      final Thread thread,
      final Throwable thrown
  ) {
    StringBuilder msg = new StringBuilder();
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

  public static String getStackTrace(final Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  public static long getMagic() {
    return (MAGIC);
  }

  public static boolean getNoMIDI() {
    return noMIDI;
  }

  public static boolean getNoMount() {
    return noMount;
  }

  public static boolean getNoUI() {
    return noUI;
  }

  public static boolean getNoServer() {
    return noServer;
  }

  public static boolean isConfigFreeze() {
    return DriveWireServer.configFreeze;
  }

  public static void setConfigFreeze(final boolean b) {
    DriveWireServer.configFreeze = b;
  }

  public static void setLoggingRestart() {
    DriveWireServer.restartLogging = true;
  }

  public static void setUIRestart() {
    DriveWireServer.restartUi = true;
  }

  public static Logger getLogger() {
    return LOGGER;
  }
}
