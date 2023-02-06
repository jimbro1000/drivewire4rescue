package com.groupunix.drivewireserver.virtualserial.api;

import java.io.IOException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWAPISerial {
  /**
   * Timeout on open port.
   */
  private static final int OPEN_PORT_TIMEOUT = 2000;
  /**
   * Start of actual arguments in command line.
   */
  private static final int ARG_START = 3;
  /**
   * Arguments representation of 1.5 stop bits.
   */
  private static final int ARGS_STOP_BITS_1_5 = 5;
  /**
   * Arguments representation of 2 stop bits.
   */
  private static final int ARGS_STOP_BITS_2 = 2;
  /**
   * Arguments representation of 1 stop bit.
   */
  private static final int ARGS_STOP_BITS_1 = 1;
  /**
   * Command arguments.
   */
  private String[] command;
  /**
   * Virtual serial ports.
   */
  private DWVSerialPorts dwVSerialPorts;
  /**
   * Virtual port number.
   */
  private int virtualPort;

  /**
   * API Serial.
   *
   * @param cmd         arguments array
   * @param serialPorts serial ports
   * @param vport       virtual port
   */
  public DWAPISerial(final String[] cmd, final DWVSerialPorts serialPorts,
                     final int vport) {
    this.virtualPort = vport;
    this.dwVSerialPorts = serialPorts;
    this.setCommand(cmd);
  }

  /**
   * API Serial.
   *
   * @param cmd   arguments array
   * @param vport virtual port
   */
  @SuppressWarnings("unused")
  public DWAPISerial(final String[] cmd, final int vport) {
  }

  /**
   * Test if string is an integer.
   *
   * @param s
   * @return bool
   */
  public static boolean isInteger(final String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    // only got here if we didn't return false
    return true;
  }

  /**
   * Process command.
   *
   * @return command response
   */
  public DWCommandResponse process() {
    if ((command.length > 2)
        && ((command.length & 1) == 1)
        && command[1].equals("join")) {
      return doCommandJoin(command);
    }
    if ((command.length > 2)
        && command[1].equals("show")) {
      return doCommandShow(command[2]);
    } else if (command.length > 1
        && command[1].equals("devs")) {
      return doCommandDevs();
    }
    return new DWCommandResponse(
        false,
        DWDefs.RC_SYNTAX_ERROR,
        "Syntax Error"
    );
  }

  private int stopBits(final int valno) {
    return switch (valno) {
      case ARGS_STOP_BITS_1 -> SerialPort.STOPBITS_1;
      case ARGS_STOP_BITS_2 -> SerialPort.STOPBITS_2;
      case ARGS_STOP_BITS_1_5 -> SerialPort.STOPBITS_1_5;
      default -> -1;
    };
  }

  private int parity(final String val) {
    return switch (val) {
      case "N" -> SerialPort.PARITY_NONE;
      case "E" -> SerialPort.PARITY_EVEN;
      case "O" -> SerialPort.PARITY_ODD;
      case "M" -> SerialPort.PARITY_MARK;
      case "S" -> SerialPort.PARITY_SPACE;
      default -> -1;
    };
  }

  private int dataBits(final int valno) {
    return switch (valno) {
      case SerialPort.DATABITS_5 -> SerialPort.DATABITS_5;
      case SerialPort.DATABITS_6 -> SerialPort.DATABITS_6;
      case SerialPort.DATABITS_7 -> SerialPort.DATABITS_7;
      case SerialPort.DATABITS_8 -> SerialPort.DATABITS_8;
      default -> -1;
    };
  }

  private int flowControl(final byte fcByte) {
    return switch (fcByte) {
      case 'r' -> SerialPort.FLOWCONTROL_RTSCTS_OUT;
      case 'R' -> SerialPort.FLOWCONTROL_RTSCTS_IN;
      case 'x' -> SerialPort.FLOWCONTROL_XONXOFF_OUT;
      case 'X' -> SerialPort.FLOWCONTROL_XONXOFF_IN;
      case 'n' -> SerialPort.FLOWCONTROL_NONE;
      default -> -1;
    };
  }

  private DWCommandResponse doCommandJoin(final String[] cmd) {
    // validate
    String port = cmd[2];
    DWAPISerialPortDef spd = new DWAPISerialPortDef();

    for (int i = ARG_START; i < cmd.length; i += 2) {
      String item = cmd[i];
      String val = cmd[i + 1];
      int valno = -1;
      try {
        valno = Integer.parseInt(val);
      } catch (NumberFormatException ignored) {
      }

      // bps rate
      if (item.equalsIgnoreCase("r") && isInteger(val)) {
        spd.setRate(valno);
      } else if (item.equalsIgnoreCase("sb") && isInteger(val)) {
        int stopBits = stopBits(valno);
        if (stopBits == -1) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_SYNTAX_ERROR,
              "Syntax error on arg sb (1,2 or 5 is valid)"
          );
        }
        spd.setStopbits(stopBits);
      } else if (item.equalsIgnoreCase("p") && val.length() == 1) {
        // parity
        int parity = parity(val);
        if (parity == -1) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_SYNTAX_ERROR,
              "Syntax error on arg p (N,E,O,M or S is valid)"
          );
        }
        spd.setParity(parity);
      } else if (item.equalsIgnoreCase("db") && isInteger(val)) {
        // data bits
        int dataBits = dataBits(valno);
        if (dataBits == -1) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_SYNTAX_ERROR,
              "Syntax error on arg db (5,6,7 or 8 is valid)"
          );
        }
        spd.setDatabits(dataBits);
      } else if (item.equalsIgnoreCase("fc")) {
        int fc = 0;
        for (byte b : val.getBytes(DWDefs.ENCODING)) {
          int fcVal = flowControl(b);
          if (fcVal == -1) {
            return new DWCommandResponse(
                false,
                DWDefs.RC_SYNTAX_ERROR,
                "Syntax error on arg fc (R,r,X,x or n is valid)"
            );
          }
          fc += fcVal;
        }
        spd.setFlowcontrol(fc);
      }
    }
    CommPort commPort = null;
    try {
      CommPortIdentifier pi = CommPortIdentifier.getPortIdentifier(port);
      if (pi.isCurrentlyOwned()) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERIAL_PORTINUSE,
            "Port in use"
        );
      }
      commPort = pi.open("DriveWireServer", OPEN_PORT_TIMEOUT);
      if (!(commPort instanceof SerialPort)) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERIAL_PORTINVALID,
            "Invalid port"
        );
      }
      final SerialPort sp = (SerialPort) commPort;
      spd.setParams(sp);

      // join em
      Thread inputT = new Thread(() -> {
        boolean wanttodie = false;
        dwVSerialPorts.markConnected(virtualPort);
        while (!wanttodie) {
          int databyte = -1;
          try {
            databyte = dwVSerialPorts.getPortOutput(virtualPort).read();
            System.out.println("input: " + databyte);
          } catch (IOException | DWPortNotValidException e) {
            wanttodie = true;
          }
          if (databyte == -1) {
            wanttodie = true;
          } else {
            try {
              sp.getOutputStream().write(databyte);
            } catch (IOException e) {
              wanttodie = true;
            }
          }
        }
      });

      inputT.setDaemon(true);
      inputT.start();
      Thread outputT = new Thread(() -> {
        boolean wanttodie = false;
        while (!wanttodie) {
          int databyte = -1;
          try {
            databyte = sp.getInputStream().read();
            System.out.println("output: " + databyte);
          } catch (IOException e) {
            wanttodie = true;
          }
          if (databyte != -1) {
            try {
              dwVSerialPorts.writeToCoco(virtualPort,
                  (byte) (databyte & BYTE_MASK));
            } catch (DWPortNotValidException e) {
              wanttodie = true;
            }
          }
        }
      });
      outputT.setDaemon(true);
      outputT.start();
      return new DWCommandResponse("Connect to " + port);
    } catch (Exception e) {
      if (commPort != null) {
        commPort.close();
      }
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERIAL_PORTERROR,
          e.getClass().getSimpleName()
      );
    }
  }

  private DWCommandResponse doCommandShow(final String port) {
    String res;
    boolean ok = true;
    try {
      CommPortIdentifier pi = CommPortIdentifier.getPortIdentifier(port);

      if (pi.isCurrentlyOwned()) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERIAL_PORTINUSE,
            "In use"
        );
      } else {
        CommPort commPort = null;
        try {
          commPort = pi.open("DriveWireServer", OPEN_PORT_TIMEOUT);
          if (commPort instanceof SerialPort) {
            SerialPort sp = (SerialPort) commPort;
            res = sp.getBaudRate() + "|" + sp.getDataBits() + "|";
            if (sp.getParity() == SerialPort.PARITY_EVEN) {
              res += "E";
            } else if (sp.getParity() == SerialPort.PARITY_ODD) {
              res += "O";
            } else if (sp.getParity() == SerialPort.PARITY_NONE) {
              res += "N";
            } else if (sp.getParity() == SerialPort.PARITY_MARK) {
              res += "M";
            } else {
              res += "S";
            }

            res += "|";

            if (sp.getStopBits() == SerialPort.STOPBITS_1) {
              res += "1";
            } else if (sp.getStopBits() == SerialPort.STOPBITS_2) {
              res += "2";
            } else {
              res += "5";
            }

            res += "|";

            if ((sp.getFlowControlMode() & SerialPort.FLOWCONTROL_RTSCTS_IN)
                == SerialPort.FLOWCONTROL_RTSCTS_IN) {
              res += "R";
            }

            if ((sp.getFlowControlMode() & SerialPort.FLOWCONTROL_XONXOFF_IN)
                == SerialPort.FLOWCONTROL_XONXOFF_IN) {
              res += "X";
            }

            if ((sp.getFlowControlMode() & SerialPort.FLOWCONTROL_RTSCTS_OUT)
                == SerialPort.FLOWCONTROL_RTSCTS_OUT) {
              res += "r";
            }

            if ((sp.getFlowControlMode() & SerialPort.FLOWCONTROL_XONXOFF_OUT)
                == SerialPort.FLOWCONTROL_XONXOFF_OUT) {
              res += "x";
            }

            res += "|";

            if (sp.isCD()) {
              res += "CD ";
            }

            if (sp.isCTS()) {
              res += "CTS ";
            }

            if (sp.isDSR()) {
              res += "DSR ";
            }

            if (sp.isDTR()) {
              res += "DTR ";
            }

            if (sp.isRI()) {
              res += "RI ";
            }

            if (sp.isRTS()) {
              res += "RTS ";
            }

            res = res.trim();
          } else {
            ok = false;
            res = "Invalid port";
          }
        } catch (Exception e) {
          ok = false;
          res = e.toString();
        } finally {
          if (commPort != null) {
            commPort.close();
          }
        }
      }
    } catch (Exception e) {
      ok = false;
      res = e.getClass().getSimpleName();
    }
    if (ok) {
      return new DWCommandResponse(res);
    }
    return new DWCommandResponse(false, DWDefs.RC_SERIAL_PORTERROR, res);
  }

  private DWCommandResponse doCommandDevs() {
    StringBuilder res = new StringBuilder();
    for (String p : DriveWireServer.getAvailableSerialPorts()) {
      if (!res.toString().equals("")) {
        res.append("|");
      }
      res.append(p);
    }
    return new DWCommandResponse(res.toString());
  }

  /**
   * Get command arguments.
   *
   * @return command arguments
   */
  public String[] getCommand() {
    return command;
  }

  /**
   * Set command arguments.
   *
   * @param args arguments
   */
  public void setCommand(final String[] args) {
    this.command = args;
  }
}
