package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdInstanceRestart extends DWCommand {
  /**
   * Command Instance Restart constructor.
   * @param protocol
   * @param parent
   */
  public DWCmdInstanceRestart(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
  }

  /**
   * Get command.
   * @return command name
   */
  public String getCommand() {
    return "restart";
  }

  /**
   * Get short help.
   * @return short help details
   */
  public String getShortHelp() {
    return "Restart instance #";
  }

  /**
   * Get usage information.
   * @return usage
   */
  public String getUsage() {
    return "dw instance restart #";
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
          "Syntax error: "
              + "dw instance restart requires an instance # as an argument"
      );
    }
    return doStart(cmdline);
  }

  private DWCommandResponse doStart(final String instance) {
    try {
      int instanceNumber = Integer.parseInt(instance);
      if (!DriveWireServer.isValidHandlerNo(instanceNumber)) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_HANDLER,
            "Invalid instance number."
        );
      }
      if (DriveWireServer.getHandler(instanceNumber) == null) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_HANDLER,
            "Instance " + instanceNumber + " is not defined."
        );
      }
      if (!DriveWireServer.getHandler(instanceNumber).isReady()) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INSTANCE_ALREADY_STARTED,
            "Instance " + instanceNumber + " is not started."
        );
      }
      if (DriveWireServer.getHandler(instanceNumber).isDying()) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INSTANCE_NOT_READY,
            "Instance "
                + instanceNumber + " is in the process of shutting down."
        );
      }
      DriveWireServer.restartHandler(instanceNumber);
      return new DWCommandResponse(
          "Restarting instance # " + instanceNumber
      );
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw instance restart requires a numeric instance # as an argument"
      );
    }
  }

  /**
   * Validate command.
   * @param cmdline
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return (true);
  }
}
