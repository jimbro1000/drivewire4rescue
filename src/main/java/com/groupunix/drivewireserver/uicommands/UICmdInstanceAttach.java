package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdInstanceAttach extends DWCommand {
  /**
   * Client thread ref.
   */
  private final DWUIClientThread clientRef;

  /**
   * UI Command Instance Attach.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceAttach(final DWUIClientThread clientThread) {
    super();
    clientRef = clientThread;
    setCommand("attach");
    setShortHelp("attach to instance #");
    setUsage("ui instance attach #");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    try {
      final int handler = Integer.parseInt(cmdline);
      if (DriveWireServer.isValidHandlerNo(handler)) {
        // set this connection's instance
        clientRef.setInstance(handler);
        return new DWCommandResponse("Attached to instance " + handler);
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_HANDLER,
            "Invalid handler number"
        );
      }
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: non numeric instance #"
      );
    }
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
