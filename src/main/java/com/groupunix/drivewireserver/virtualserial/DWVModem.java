package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

import static com.groupunix.drivewireserver.DWDefs.BACKSPACE;
import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;
import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;
import static com.groupunix.drivewireserver.DWDefs.NEWLINE;


public class DWVModem {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVModem");
  /**
   * Ok response index.
   */
  private static final int RESP_OK = 0;
  /**
   * Connect response index.
   */
  private static final int RESP_CONNECT = 1;
  /**
   * Ring response index.
   */
  private static final int RESP_RING = 2;
  /**
   * No carrier response index.
   */
  private static final int RESP_NOCARRIER = 3;
  /**
   * Error response index.
   */
  private static final int RESP_ERROR = 4;
  /**
   * No dial tone response index.
   */
  private static final int RESP_NODIALTONE = 6;
  /**
   * Busy register index.
   */
  private static final int RESP_BUSY = 7;
  /**
   * No answer response index.
   */
  private static final int RESP_NOANSWER = 8;
  // registers we use
  /**
   * Answer in ring register index.
   */
  private static final int REG_ANSWERONRING = 0;
  /**
   * Rings register index.
   */
  private static final int REG_RINGS = 1;
  /**
   * Escape char register index.
   */
  private static final int REG_ESCCHAR = 2;
  /**
   * CR register index.
   */
  private static final int REG_CR = 3;
  /**
   * LF register index.
   */
  private static final int REG_LF = 4;
  /**
   * BS register index.
   */
  private static final int REG_BS = 5;
  /**
   * Guard time register index.
   */
  private static final int REG_GUARDTIME = 12;
  /**
   * Listen port high byte register.
   */
  private static final int REG_LISTENPORTHI = 13;
  /**
   * Listen port low byte register.
   */
  private static final int REG_LISTENPORTLO = 14;
  /**
   * Echo register index.
   */
  private static final int REG_ECHO = 15;
  /**
   * Verbose mode register index.
   */
  private static final int REG_VERBOSE = 16;
  /**
   * Quiet mode register index.
   */
  private static final int REG_QUIET = 17;
  /**
   * DCD mode register index.
   */
  private static final int REG_DCDMODE = 18;
  /**
   * DSR mode register index.
   */
  private static final int REG_DSRMODE = 19;
  /**
   * DTR mode register index.
   */
  private static final int REG_DTRMODE = 20;
  /**
   * Number of S registers.
   */
  private static final int MODEM_S_REGISTERS = 38;
  /**
   * Default guard time.
   */
  private static final int DEFAULT_GUARD_TIME = 50;
  /**
   * Default esc char.
   */
  private static final int DEFAULT_ESC_CHAR = 43;
  /**
   * TCP esc char.
   */
  private static final int TCP_ESC_CHAR = 255;
  /**
   * Default TCP port.
   */
  private static final int DEFAULT_TCP_PORT = 23;
  /**
   * Info option 4.
   */
  private static final int INFO_OPT_4 = 4;
  /**
   * Info option 2.
   */
  private static final int INFO_OPT_2 = 2;
  /**
   * Info option 1.
   */
  private static final int INFO_OPT_1 = 1;
  /**
   * Info option 3.
   */
  private static final int INFO_OPT_3 = 3;
  /**
   * Info option 0.
   */
  private static final int INFO_OPT_0 = 0;
  /**
   * Number of modem registers.
   */
  private static final int NUM_REGISTERS = 256;
  /**
   * Virtual port.
   */
  private final int vport;
  // modem state
  /**
   * Modem registers.
   */
  private final int[] vmodemRegisters = new int[NUM_REGISTERS];
  /**
   * Serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;
  /**
   * Last command.
   */
  private String vmodemLastcommand = "";
  /**
   * Dial String.
   */
  private String vmodemDialstring = "";
  /**
   * TCP thread.
   */
  private Thread tcpthread;
  /**
   * Listener thread.
   */
  private Thread listenThread;

  /**
   * Virtual modem.
   *
   * @param serialProtocol protocol
   * @param port           port number
   */
  public DWVModem(final DWVSerialProtocol serialProtocol, final int port) {
    this.vport = port;
    this.dwvSerialProtocol = serialProtocol;
    this.dwVSerialPorts = serialProtocol.getVPorts();
    // logger.debug("new vmodem for port " + port);
    doCommandReset(1);
  }

  /**
   * Process command.
   *
   * @param command command string
   */
  public void processCommand(final String command) {
    // hitting enter on a blank line is ok
    if (command.length() == 0) {
      return;
    }
    int errors = 0;
    String cmd = command;
    // A/ repeats last command
    if (cmd.equalsIgnoreCase("A/")) {
      cmd = this.vmodemLastcommand;
    } else {
      this.vmodemLastcommand = cmd;
    }
    // must start with AT
    if (cmd.toUpperCase().startsWith("AT")) {
      // AT by itself is OK
      if (cmd.length() == 2) {
        sendResponse(RESP_OK);
        return;
      }

      // process the string looking for commands
      boolean registers = false;
      boolean extended = false;
      boolean extendedpart;
      boolean dialing = false;

      StringBuilder thiscmd = new StringBuilder();
      StringBuilder thisarg = new StringBuilder();
      StringBuilder thisreg = new StringBuilder();

      for (int i = 2; i < cmd.length(); i++) {
        extendedpart = false;

        if (dialing) {
          thisarg.append(cmd.charAt(i));
        } else {
          switch (cmd.toUpperCase().charAt(i)) {
            // commands
            case 'E':
            case '&':
            case 'Q':
            case 'Z':
            case 'I':
            case 'S':
            case 'B':
            case 'L':
            case 'M':
            case 'N':
            case 'X':
            case 'V':
            case 'F':
            case 'D':
            case 'C':
            case 'A':
              // handle extended mode
              if (extended) {
                extendedpart = switch (cmd.toUpperCase().charAt(i)) {
                  case 'V', 'F', 'C', 'D', 'S', 'Z', 'A' -> true;
                  default -> extendedpart;
                };
              }

              if (cmd.toUpperCase().charAt(i) == '&') {
                extended = true;
              }

              if (!extended && cmd.toUpperCase().charAt(i) == 'D') {
                dialing = true;
              }

              if (extendedpart) {
                thiscmd.append(cmd.charAt(i));
              } else {
                // terminate existing command if any
                if (!(thiscmd.length() == 0)) {
                  errors += doCommand(thiscmd.toString(),
                      thisreg.toString(),
                      thisarg.toString());
                }
                // set up for new command
                thiscmd = new StringBuilder(cmd.substring(i, i + 1));
                thisarg = new StringBuilder();
                thisreg = new StringBuilder();
                // registers
                registers = thiscmd.toString().equalsIgnoreCase("S");
              }
              break;
            // assignment
            case '=':
              registers = false;
              break;

            // query
            case '?':
              thisarg = new StringBuilder("?");
              break;

            // ignored
            case ' ':
              break;

            // digits
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              if (registers) {
                thisreg.append(cmd.charAt(i));
              } else {
                thisarg.append(cmd.charAt(i));
              }
              break;

            default:
              // unknown command/bad syntax
              errors++;
              break;
          }
        }
      }
      // last/only command in string
      if (!(thiscmd.length() == 0)) {
        errors += doCommand(
            thiscmd.toString(), thisreg.toString(), thisarg.toString()
        );
      }
    } else {
      errors++;
    }

    // send response
    if (errors >= 0) {
      if (errors > 0) {
        sendResponse(RESP_ERROR);
      } else {
        sendResponse(RESP_OK);
      }
    }
  }

  private int doCommand(final String thiscmd,
                        final String thisreg,
                        final String thisarg) {
    int errors = 0;
    int val = 0;
    int regval = 0;

    // convert arg and reg to int values
    try {
      val = Integer.parseInt(thisarg);
    } catch (NumberFormatException ignored) {
    }
    try {
      regval = Integer.parseInt(thisreg);
    } catch (NumberFormatException ignored) {
    }

    LOGGER.debug("vmodem doCommand: " + thiscmd + "  reg: " + thisreg
        + " (" + regval + ")  arg: " + thisarg + " (" + val + ")");

    switch (thiscmd.toUpperCase().charAt(0)) {
      // ignored
      case 'B':  // call negotiation, might implement if needed
      case 'L':  // speaker volume
      case 'M':  // speaker mode
      case 'N':  // handshake speed lock to s37
      case 'X':  // result code/dialing style
        break;
      // reset
      case 'Z':
        doCommandReset(val);
        break;
      // toggles
      case 'E':
        if (val > 1) {
          errors++;
        } else {
          this.vmodemRegisters[REG_ECHO] = val;
        }
        break;
      case 'Q':
        if (val > 1) {
          errors++;
        } else {
          this.vmodemRegisters[REG_QUIET] = val;
        }
        break;
      case 'V':
        if (val > 1) {
          errors++;
        } else {
          this.vmodemRegisters[REG_VERBOSE] = val;
        }
        break;

      // info
      case 'I':
        switch (val) {
          case INFO_OPT_0 -> write(getCRLF() + "DWVM "
              + dwVSerialPorts.prettyPort(this.vport) + getCRLF());
          case INFO_OPT_1, INFO_OPT_3 -> write(getCRLF() + "DriveWire "
              + DriveWireServer.DW_SERVER_VERSION
              + " Virtual Modem on port "
              + dwVSerialPorts.prettyPort(this.vport) + getCRLF());
          case INFO_OPT_2 -> {
            try {
              if (dwVSerialPorts.getConn(this.vport) > -1) {
                write(getCRLF() + "Connected to "
                    + dwVSerialPorts.getHostIP(this.vport)
                    + ":" + dwVSerialPorts.getHostPort(this.vport)
                    + getCRLF());
              } else {
                write(getCRLF() + "Not connected" + getCRLF());
              }
            } catch (DWPortNotValidException
                     | DWConnectionNotValidException e) {
              LOGGER.error(e.getMessage());
              write(e.getMessage());
            }
          }
          case INFO_OPT_4 -> doCommandShowActiveProfile();
          default -> errors++;
        }

        break;

      // extended commands
      case '&':
        if (thiscmd.length() > 1) {
          switch (thiscmd.toUpperCase().charAt(1)) {
            case 'C' -> doCommandSetDCDMode(val);
            case 'D' -> doCommandSetDTRMode(val);
            case 'S' -> doCommandSetDSRMode(val);
            case 'A' -> doCommandAnswerMode(val);
            case 'Z', 'F' -> doCommandReset(val);
            default -> errors++;
          }
        } else {
          errors++;
        }
        break;
      // registers
      case 'S':
        // valid?
        if (regval <= BYTE_MASK && val <= BYTE_MASK) {
          // display or set
          if (thisarg.equals("?")) {
            // display
            write(getCRLF() + this.vmodemRegisters[regval] + getCRLF());
          } else {
            // set
            this.vmodemRegisters[regval] = val;
          }
        } else {
          errors++;
        }
        break;

      // dial
      case 'D':
        if (thisarg.length() == 0) {
          // ATD without a number/host?
          errors++;
        } else {
          // if its ATDL, dont reset vs_dev so we redial last host
          if (!thisarg.equalsIgnoreCase("L")) {
            this.vmodemDialstring = thisarg;
          }
          if (doDial() == 0) {
            sendResponse(RESP_NOANSWER);
          }
          //don't print another response
          errors = -1;
          return errors;
        }
        break;
      default:
        // error on unknown commands?  OK might be preferable
        errors++;
        break;
    }
    return errors;
  }

  private void doCommandAnswerMode(final int val) {
    this.vmodemRegisters[REG_LISTENPORTHI] = val / BYTE_SHIFT;
    this.vmodemRegisters[REG_LISTENPORTLO] = val % BYTE_SHIFT;
    disableListener();
    if (val > 0) {
      enableListener(val);
    }
  }

  private void enableListener(final int val) {
    this.listenThread = new Thread(new DWVModemListenerThread(this));
    listenThread.start();
  }

  private void disableListener() {
  }

  private int doDial() {
    String tcphost;
    int tcpport;

    // parse dialstring
    final String[] dparts = this.vmodemDialstring.split(":");

    if (dparts.length == 1) {
      tcphost = dparts[0];
      tcpport = DEFAULT_TCP_PORT;
    } else if (dparts.length == 2) {
      tcphost = dparts[0];
      tcpport = Integer.parseInt(dparts[1]);
    } else {
      return 0;
    }
    // try to connect..
    this.tcpthread = new Thread(
        new DWVModemConnThread(this, tcphost, tcpport)
    );
    this.tcpthread.setDaemon(true);
    this.tcpthread.start();
    return 1;
  }

  private void doCommandShowActiveProfile() {
    // display current modem settings
    write("Active profile:" + getCRLF() + getCRLF());
    write("E" + onOff(this.isEcho()) + " ");
    write("Q" + onOff(this.isQuiet()) + " ");
    write("V" + onOff(this.isVerbose()) + " ");
    write("&C" + this.vmodemRegisters[REG_DCDMODE] + " ");
    write("&D" + this.vmodemRegisters[REG_DTRMODE] + " ");
    write("&S" + this.vmodemRegisters[REG_DSRMODE] + " ");
    write(getCRLF() + getCRLF());
    // show S0-S37, only 0-13 and 36-37 are well documented
    for (int i = 0; i < MODEM_S_REGISTERS; i++) {
      write(String.format("S%03d=%03d  ", i, this.vmodemRegisters[i]));
      Thread.yield();
    }
    write(getCRLF() + getCRLF());
  }

  private String onOff(final boolean val) {
    return val ? "1" : "0";
  }

  private void doCommandReset(final int val) {
    // common settings
    this.vmodemLastcommand = "";
    this.vmodemRegisters[REG_LISTENPORTHI] = 0;
    this.vmodemRegisters[REG_LISTENPORTLO] = 0;
    this.vmodemRegisters[REG_ANSWERONRING] = 0;
    this.vmodemRegisters[REG_RINGS] = 0;
    this.vmodemRegisters[REG_CR] = CARRIAGE_RETURN;
    this.vmodemRegisters[REG_LF] = NEWLINE;
    this.vmodemRegisters[REG_BS] = BACKSPACE;

    switch (val) {
      case 1:
        // settings for tcp mode/non modem use
        this.vmodemRegisters[REG_ESCCHAR] = TCP_ESC_CHAR;
        this.vmodemRegisters[REG_GUARDTIME] = 0;
        this.vmodemRegisters[REG_ECHO] = 0;
        this.vmodemRegisters[REG_VERBOSE] = 0;
        this.vmodemRegisters[REG_QUIET] = 0;
        this.vmodemRegisters[REG_DCDMODE] = 0;
        this.vmodemRegisters[REG_DSRMODE] = 0;
        this.vmodemRegisters[REG_DTRMODE] = 0;
        break;
      case 0:
      default:
        // settings better for use as a modem
        this.vmodemRegisters[REG_ESCCHAR] = DEFAULT_ESC_CHAR;
        this.vmodemRegisters[REG_GUARDTIME] = DEFAULT_GUARD_TIME;
        this.vmodemRegisters[REG_ECHO] = 1;
        this.vmodemRegisters[REG_VERBOSE] = 1;
        this.vmodemRegisters[REG_QUIET] = 0;
        this.vmodemRegisters[REG_DCDMODE] = 1;
        this.vmodemRegisters[REG_DSRMODE] = 1;
        this.vmodemRegisters[REG_DTRMODE] = 1;
        break;
    }
  }

  private void doCommandSetDCDMode(final int val) {
    this.vmodemRegisters[REG_DCDMODE] = val;
  }

  private void doCommandSetDTRMode(final int val) {
    this.vmodemRegisters[REG_DTRMODE] = val;
  }

  private void doCommandSetDSRMode(final int val) {
    this.vmodemRegisters[REG_DSRMODE] = val;
  }

  /**
   * Get crlf.
   *
   * @return end of line combo
   */
  private String getCRLF() {
    return Character.toString((char) this.vmodemRegisters[REG_CR])
        + (char) this.vmodemRegisters[REG_LF];
  }

  private void sendResponse(final int resp) {
    // quiet mode
    if (!this.isQuiet()) {
      // verbose mode
      if (this.isVerbose()) {
        write(getVerboseResponse(resp) + getCRLF());
      } else {
        write(resp + getCRLF());
      }
    }
  }

  private String getVerboseResponse(final int resp) {
    return switch (resp) {
      case RESP_OK -> "OK";
      case RESP_CONNECT -> "CONNECT";
      case RESP_RING -> "RING";
      case RESP_NOCARRIER -> "NO CARRIER";
      case RESP_ERROR -> "ERROR";
      case RESP_NODIALTONE -> "NO DIAL TONE";
      case RESP_BUSY -> "BUSY";
      case RESP_NOANSWER -> "NO ANSWER";
      default -> "UKNOWN";
    };
  }

  /**
   * Write string.
   *
   * @param str string
   */
  public void write(final String str) {
    try {
      dwVSerialPorts.writeToCoco(this.vport, str);
    } catch (DWPortNotValidException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Write byte data.
   *
   * @param data byte
   */
  public void write(final byte data) {
    try {
      dwVSerialPorts.writeToCoco(this.vport, data);
    } catch (DWPortNotValidException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Is echo flag set.
   *
   * @return echo flag
   */
  public boolean isEcho() {
    return this.vmodemRegisters[REG_ECHO] == 1;
  }

  /**
   * Set echo flag.
   *
   * @param echo echo flag
   */
  public void setEcho(final boolean echo) {
    this.vmodemRegisters[REG_ECHO] = echo ? 1 : 0;
  }

  /**
   * Is verbose flag set.
   *
   * @return verbose
   */
  public boolean isVerbose() {
    return this.vmodemRegisters[REG_VERBOSE] == 1;
  }

  /**
   * Set verbose flag.
   *
   * @param verbose verbose
   */
  public void setVerbose(final boolean verbose) {
    this.vmodemRegisters[REG_VERBOSE] = verbose ? 1 : 0;
  }

  /**
   * Is quiet flag set.
   *
   * @return quiet flag
   */
  public boolean isQuiet() {
    return this.vmodemRegisters[REG_QUIET] == 1;
  }

  /**
   * Get CR register.
   *
   * @return CR
   */
  public int getCR() {
    return this.vmodemRegisters[REG_CR];
  }

  /**
   * Get LF register.
   *
   * @return LF
   */
  public int getLF() {
    return this.vmodemRegisters[REG_LF];
  }

  /**
   * Get BS register.
   *
   * @return BS
   */
  public int getBS() {
    return this.vmodemRegisters[REG_BS];
  }

  /**
   * Get listen port.
   *
   * @return listen port
   */
  public int getListenPort() {
    return this.vmodemRegisters[REG_LISTENPORTHI] * BYTE_SHIFT
        + this.vmodemRegisters[REG_LISTENPORTLO];
  }

  /**
   * Get virtual port.
   *
   * @return port number
   */
  public int getVPort() {
    return this.vport;
  }

  /**
   * Get serial ports.
   *
   * @return serial ports
   */
  public DWVSerialPorts getVSerialPorts() {
    return this.dwVSerialPorts;
  }

  /**
   * Get modem registers.
   *
   * @return registers
   */
  public int[] getRegisters() {
    return this.vmodemRegisters;
  }

  /**
   * Get virtual serial protocol.
   *
   * @return serial protocol
   */
  public DWVSerialProtocol getVProto() {
    return this.dwvSerialProtocol;
  }
}
