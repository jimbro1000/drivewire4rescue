package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import com.groupunix.drivewireserver.virtualserial.api.DWAPISerial;
import com.groupunix.drivewireserver.virtualserial.api.DWAPITCP;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;


public class DWVPortHandler {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPortHandler");
  /**
   * Virtual port.
   */
  private final int vport;
  /**
   * Modem.
   */
  private final DWVModem vModem;
  /**
   * Serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;
  /**
   * Port command.
   */
  private String portCommand = "";
  /**
   * Utility thread.
   */
  private Thread utilthread;

  /**
   * Virtual port handler.
   *
   * @param protocol serial protocol
   * @param port     port number
   */
  public DWVPortHandler(final DWVSerialProtocol protocol, final int port) {
    this.vport = port;
    this.vModem = new DWVModem(protocol, port);
    this.dwvSerialProtocol = protocol;
    this.dwVSerialPorts = protocol.getVPorts();
  }

  /**
   * Tale input data.
   *
   * @param databyte data
   */
  public void takeInput(final int databyte) {
    // echo character if modem echo is on
    if (this.vModem.isEcho()) {
      try {
        dwVSerialPorts.writeToCoco(this.vport, (byte) databyte);
        // send extra lf on cr, not sure if this is right
        if (databyte == this.vModem.getCR()) {
          dwVSerialPorts.writeToCoco(this.vport, (byte) this.vModem.getLF());
        }
      } catch (DWPortNotValidException e) {
        LOGGER.warn("in takeinput: " + e.getMessage());
      }
    }
    // process command if enter
    if (databyte == this.vModem.getCR()) {
      LOGGER.debug("port command '" + portCommand + "'");
      processCommand(portCommand.trim());
      this.portCommand = "";
    } else {
      // add character to command
      // handle backspace
      if ((databyte == this.vModem.getBS())
          && (this.portCommand.length() > 0)) {
        this.portCommand = this.portCommand
            .substring(0, this.portCommand.length() - 1);
      } else if (databyte > 0) {
        // is this really the easiest way to append a character to a string??
        this.portCommand += Character.toString((char) databyte);
        // check for os9 window wcreate:
        // 1b 20 + (valid screen type: ff,0,1,2,5,6,7,8)
      }
    }
  }

  /**
   * Process modem command.
   *
   * @param cmd command
   */
  private void processCommand(final String cmd) {
    // hitting enter on a blank line is ok
    if (cmd.length() == 0) {
      return;
    }
    // anything beginning with AT or A/ is a modem command
    if ((cmd.toUpperCase().startsWith("AT"))
        || (cmd.toUpperCase().startsWith("A/"))) {
      this.vModem.processCommand(cmd);
    } else {
      processAPICommand(cmd);
    }
  }

  private void processAPICommand(final String cmd) {
    // new API based implementation 1/2/10
    String[] cmdparts = cmd.split("\\s+");
    if (cmdparts.length > 0) {
      if (cmdparts[0].equalsIgnoreCase("tcp")) {
        respond(
            new DWAPITCP(cmdparts, this.dwvSerialProtocol, this.vport)
                .process()
        );
      } else if (cmdparts[0].equalsIgnoreCase("ser")) {
        respond(
            new DWAPISerial(cmdparts, this.dwVSerialPorts, this.vport)
                .process()
        );
      } else if (cmdparts[0].equalsIgnoreCase("dw")
          || cmdparts[0].equalsIgnoreCase("ui")) {
        // start DWcmd thread

        this.utilthread = new Thread(
            new DWUtilDWThread(this.dwvSerialProtocol, this.vport, cmd)
        );
        this.utilthread.start();
      } else if (cmdparts[0].equalsIgnoreCase("log")) {
        // log entry
        LOGGER.info("coco " + cmd);
      } else {
        LOGGER.warn("Unknown API command: '" + cmd + "'");
        respondFail(DWDefs.RC_SYNTAX_ERROR, "Unknown API '"
            + cmdparts[0] + "'");
      }
    } else {
      LOGGER.debug("got empty command?");
      respondFail(DWDefs.RC_SYNTAX_ERROR, "Syntax error: no command?");
    }
  }

  /**
   * Respond to command.
   *
   * @param cr command response
   */
  public void respond(final DWCommandResponse cr) {
    if (cr.isSuccess()) {
      respondOk(cr.getResponseText());
    } else {
      respondFail(cr.getResponseCode(), cr.getResponseText());
    }
  }

  /**
   * Response ok.
   *
   * @param txt response text
   */
  public void respondOk(final String txt) {
    try {
      dwVSerialPorts.writeToCoco(
          this.vport, "OK " + txt + (char) CARRIAGE_RETURN
      );
    } catch (DWPortNotValidException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Respond to command failure.
   *
   * @param errno error number
   * @param txt   error text
   */
  public void respondFail(final byte errno, final String txt) {
    String perrno = String.format("%03d", errno & BYTE_MASK);
    LOGGER.debug("command failed: " + perrno + " " + txt);
    try {
      dwVSerialPorts.writeToCoco(this.vport,
          "FAIL " + perrno + " " + txt + (char) CARRIAGE_RETURN);
    } catch (DWPortNotValidException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Broadcast connection to coco.
   *
   * @param conno     connection number
   * @param localport local port number
   * @param hostaddr  host address
   */
  public synchronized void announceConnection(final int conno,
                                              final int localport,
                                              final String hostaddr) {
    try {
      dwVSerialPorts.writeToCoco(this.vport,
          conno + " " + localport + " " + hostaddr + (char) CARRIAGE_RETURN);
    } catch (DWPortNotValidException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Get v modem.
   *
   * @return modem
   */
  public DWVModem getVModem() {
    return this.vModem;
  }
}
