package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import com.groupunix.drivewireserver.virtualserial.DWVPortTCPConnectionThread;

public class DWCmdPortOpen extends DWCommand {
  /**
   * Drivewire serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;

  /**
   * Port open command constructor.
   *
   * @param protocol serial protocol
   * @param parent parent command
   */
  public DWCmdPortOpen(
      final DWVSerialProtocol protocol,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwvSerialProtocol = protocol;
    this.setCommand("open");
    this.setShortHelp("Connect port # to tcp host:port");
    this.setUsage("dw port open port# host:port");
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    String[] args = cmdline.split(" ");
    if (args.length < 2) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw port open requires a port # and tcphost:port as an argument"
      );
    } else {
      return doPortOpen(args[0], args[1]);
    }
  }

  private DWCommandResponse doPortOpen(
      final String port,
      final String hostPort
  ) {
    int portNumber;
    int tcpPort;
    String tcpHostName;

    try {
      portNumber = Integer.parseInt(port);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: non numeric port #"
      );
    }
    String[] tcpArguments = hostPort.split(":");
    try {
      tcpPort = Integer.parseInt(tcpArguments[1]);
      tcpHostName = tcpArguments[0];
      dwvSerialProtocol.getVPorts().openPort(portNumber);
      Thread cthread = new Thread(
          new DWVPortTCPConnectionThread(
              this.dwvSerialProtocol,
              portNumber,
              tcpHostName,
              tcpPort,
              false
          )
      );
      cthread.start();
      return new DWCommandResponse(
          "Port #" + portNumber + " open."
      );
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: non numeric tcp port"
      );
    } catch (DWPortNotValidException e) {
      return new DWCommandResponse(
          false, DWDefs.RC_INVALID_PORT, e.getMessage()
      );
    }
  }

  /**
   * Validate command.
   *
   * @param cmdline command string
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
