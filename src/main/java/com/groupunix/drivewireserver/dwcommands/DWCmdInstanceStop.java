package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdInstanceStop extends DWCommand {
  /**
   * Protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * Command Instance Stop constructor.
   *
   * @param protocol protocol
   * @param parent parent
   */
  public DWCmdInstanceStop(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("stop");
    this.setShortHelp("Stop instance #");
    this.setUsage("dw instance stop #");
  }

  /**
   * Parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: dw instance stop requires an instance # as an argument"
      );
    }
    return (doStart(cmdline));
  }

  private DWCommandResponse doStart(final String instance) {
    try {
      int instanceNumber = Integer.parseInt(instance);
      DWCommandResponse testInstance
          = DWCmdInstance.guardInstance(instanceNumber);
      if (testInstance != null) {
        return testInstance;
      }
      DriveWireServer.stopHandler(instanceNumber);
      return new DWCommandResponse(
          "Stopping instance # " + instanceNumber
      );
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw instance stop requires a numeric instance # as an argument"
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
