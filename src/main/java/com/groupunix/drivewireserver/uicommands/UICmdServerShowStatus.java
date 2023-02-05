package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

import static com.groupunix.drivewireserver.DWDefs.KILOBYTE;

public class UICmdServerShowStatus extends DWCommand {
  /**
   * UI Command Server Show Status.
   */
  public UICmdServerShowStatus() {
    setCommand("status");
    setShortHelp("show server status");
    setUsage("ui server show status");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    String text = "";
    text += "version|" + DriveWireServer.DW_SERVER_VERSION + "\n";
    text += "versiondate|" + DriveWireServer.DW_SERVER_VERSION_DATE + "\n";
    text += "totmem|" + Runtime.getRuntime().totalMemory() / KILOBYTE + "\n";
    text += "freemem|" + Runtime.getRuntime().freeMemory() / KILOBYTE + "\n";
    text += "instances|" + DriveWireServer.getNumHandlers() + "\n";
    text += "configpath|" + DriveWireServer.getServerConfiguration()
        .getBasePath() + "\n";
    return new DWCommandResponse(text);
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
