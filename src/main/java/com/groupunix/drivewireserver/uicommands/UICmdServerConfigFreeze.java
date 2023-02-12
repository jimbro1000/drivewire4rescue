package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigFreeze extends DWCommand {

  /**
   * UI Command Server Configuration freeze.
   */
  public UICmdServerConfigFreeze() {
    super();
    setCommand("freeze");
    setShortHelp("Set server configuration item");
    setUsage("ui server config freeze [boolean]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final String[] args = cmdline.split(" ");
    if (args.length == 1) {
      return doSetFreeze(args[0]);
    } else {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Must specify freeze state"
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

  private DWCommandResponse doSetFreeze(final String state) {
    if (state.equalsIgnoreCase("true")) {
      DriveWireServer.setConfigFreeze(true);
      return new DWCommandResponse("Config freeze set.");
    } else {
      DriveWireServer.setConfigFreeze(false);
      return new DWCommandResponse("Config freeze unset.");
    }
  }
}
