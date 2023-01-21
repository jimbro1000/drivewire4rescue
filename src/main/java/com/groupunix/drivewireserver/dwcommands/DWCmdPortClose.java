package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class DWCmdPortClose extends DWCommand {

  /**
   * Port serial protocol.
   */
  private DWVSerialProtocol dwProtocol;

  /**
   * Port close command constructor.
   * @param protocol
   * @param parent
   */
  public DWCmdPortClose(
      final DWVSerialProtocol protocol,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("close");
    this.setShortHelp("Close port #");
    this.setUsage("dw port close #");
  }

  /**
   * Parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw port close requires a port # as an argument"
      );
    }
    return (doPortClose(cmdline));
  }

  private DWCommandResponse doPortClose(final String port) {
    try {
      final int portNumber = Integer.parseInt(port);
      dwProtocol.getVPorts().closePort(portNumber);
      return new DWCommandResponse(
          "Port #" + portNumber + " closed."
      );
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: non numeric port #"
      );
    } catch (DWPortNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_INVALID_PORT,
          e.getMessage()
      );
    }
  }

  /**
   * Validate command.
   * @param cmdline
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
