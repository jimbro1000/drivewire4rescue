package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdInstanceStart extends DWCommand {
  /**
   * Protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * Command Instance Start constructor.
   *
   * @param protocol protocol
   * @param parent parent
   */
  public DWCmdInstanceStart(final DWProtocol protocol, final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("start");
    this.setShortHelp("Start instance #");
    this.setUsage("dw instance start #");
  }

  /**
   * parse command.
   * <p>
   * Checks that the provided command is not empty and starts the command
   * </p>
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (
          new DWCommandResponse(
              false,
              DWDefs.RC_SYNTAX_ERROR,
              "Syntax error: "
                  + "dw instance start requires an instance # as an argument"
          )
      );
    }
    return (doStart(cmdline));
  }

  /**
   * start command.
   *
   * @param intId interrupt handler id
   * @return command response
   */
  private DWCommandResponse doStart(final String intId) {
    try {
      final int interruptId = Integer.parseInt(intId);
      if (!DriveWireServer.isValidHandlerNo(interruptId)) {
        return new DWCommandResponse(
                false,
                DWDefs.RC_INVALID_HANDLER,
                "Invalid instance number."
        );
      }
      if (DriveWireServer.getHandler(interruptId) == null) {
        return new DWCommandResponse(
                false,
                DWDefs.RC_INVALID_HANDLER,
                "Instance " + interruptId + " is not defined."
        );
      }
      if (DriveWireServer.getHandler(interruptId).isReady()) {
        return new DWCommandResponse(
                false,
                DWDefs.RC_INSTANCE_ALREADY_STARTED,
                "Instance " + interruptId + " is already started."
        );
      }
      if (DriveWireServer.getHandler(interruptId).isDying()) {
        return new DWCommandResponse(
                false,
                DWDefs.RC_INSTANCE_NOT_READY,
                "Instance " + interruptId
                    + " is in the process of shutting down."
        );
      }
      DriveWireServer.startHandler(interruptId);
      return new DWCommandResponse("Starting instance # " + interruptId);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
              false,
              DWDefs.RC_SYNTAX_ERROR,
              "dw instance start requires a numeric instance # as an argument"
      );
    }
  }

  /**
   * validate provided command line instruction.
   *
   * @param cmdline command string
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
