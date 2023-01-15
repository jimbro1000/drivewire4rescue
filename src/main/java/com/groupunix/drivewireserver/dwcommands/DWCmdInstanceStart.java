package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdInstanceStart extends DWCommand {
  /**
   * Command Instance Start constructor.
   * @param dwProto2 protocol
   * @param parent parent
   */
  public DWCmdInstanceStart(final DWProtocol dwProto2, final DWCommand parent) {
    setParentCmd(parent);
  }

  /**
   * get command.
   * @return command name
   */
  public String getCommand() {
    return "start";
  }

  /**
   * Short help for class.
   * @return short help text
   */
  public String getShortHelp() {
    return "Start instance #";
  }

  /**
   * get usage instruction for class.
   * @return instruction
   */
  public String getUsage() {
    return "dw instance start #";
  }

  /**
   * parse command.
   * Checks that the provided command is not empty and starts the command
   * @param cmdline
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
   * @param instr
   * @return command response
   */
  private DWCommandResponse doStart(final String instr) {
    try {
      int intno = Integer.parseInt(instr);
      if (!DriveWireServer.isValidHandlerNo(intno)) {
        return (
            new DWCommandResponse(
                false,
                DWDefs.RC_INVALID_HANDLER,
                "Invalid instance number."
            )
        );
      }
      if (DriveWireServer.getHandler(intno) == null) {
        return (
            new DWCommandResponse(
                false,
                DWDefs.RC_INVALID_HANDLER,
                "Instance " + intno + " is not defined."
            )
        );
      }
      if (DriveWireServer.getHandler(intno).isReady()) {
        return (
            new DWCommandResponse(
                false,
                DWDefs.RC_INSTANCE_ALREADY_STARTED,
                "Instance " + intno + " is already started."
            )
        );
      }
      if (DriveWireServer.getHandler(intno).isDying()) {
        return (
            new DWCommandResponse(
                false,
                DWDefs.RC_INSTANCE_NOT_READY,
                "Instance " + intno + " is in the process of shutting down."
            )
        );
      }
      DriveWireServer.startHandler(intno);
      return (new DWCommandResponse("Starting instance # " + intno));
    } catch (NumberFormatException e) {
      return (
          new DWCommandResponse(
              false,
              DWDefs.RC_SYNTAX_ERROR,
              "dw instance start requires a numeric instance # as an argument"
          )
      );
    }
  }

  /**
   * validate provided command line instruction.
   * @param cmdline
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return (true);
  }
}
