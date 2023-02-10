package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdInstance extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Command instance constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdInstance(final DWProtocol protocol, final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    final DWCommandList commands = new DWCommandList(
        this.dwProtocol, this.dwProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdInstanceShow(protocol, this));
    commands.addCommand(new DWCmdInstanceStart(protocol, this));
    commands.addCommand(new DWCmdInstanceStop(protocol, this));
    commands.addCommand(new DWCmdInstanceRestart(protocol, this));
    this.setCommand("instance");
    this.setShortHelp("Commands to control instances");
    this.setUsage("dw instance [command]");
  }

  /**
   * Verify instance number is safe to operate on.
   *
   * @param instanceNumber instance id
   * @return command response or null
   */
  public static DWCommandResponse guardInstance(final int instanceNumber) {
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
    return null;
  }

  /**
   * Parse command if present.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(this.getCommandList().getShortHelp());
    }
    return this.getCommandList().parse(cmdline);
  }

  /**
   * Validate command for start, stop, show and restart.
   *
   * @param cmdline command string
   * @return true if valid for all actions
   */
  public boolean validate(final String cmdline) {
    return this.getCommandList().validate(cmdline);
  }
}
