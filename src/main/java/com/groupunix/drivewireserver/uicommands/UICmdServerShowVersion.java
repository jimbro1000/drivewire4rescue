package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowVersion extends DWCommand {
  /**
   * UI Command Server Show Version.
   */
  public UICmdServerShowVersion() {
    setCommand("version");
    setShortHelp("show server version");
    setUsage("ui server show version");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    return new DWCommandResponse(
        "DriveWire version "
            + DriveWireServer.DW_SERVER_VERSION
            + " ("
            + DriveWireServer.DW_SERVER_VERSION_DATE
            + ")"
    );
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
