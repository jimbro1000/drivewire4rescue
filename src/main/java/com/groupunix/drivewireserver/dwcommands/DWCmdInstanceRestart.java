package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdInstanceRestart extends DWCommand {
  /**
   * Protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * Command Instance Restart constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdInstanceRestart(
      final DWProtocol protocol,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("restart");
    this.setShortHelp("Restart instance #");
    this.setUsage("dw instance restart #");
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
          "Syntax error: "
              + "dw instance restart requires an instance # as an argument"
      );
    }
    return doRestart(cmdline);
  }

  private DWCommandResponse doRestart(final String instance) {
    try {
      final int instanceNumber = Integer.parseInt(instance);
      final DWCommandResponse testInstance
          = DWCmdInstance.guardInstance(instanceNumber);
      if (testInstance != null) {
        return testInstance;
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
   *
   * @param cmdline command string
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
