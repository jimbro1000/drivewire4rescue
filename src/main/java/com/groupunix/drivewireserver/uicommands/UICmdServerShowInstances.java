package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowInstances extends DWCommand {
  /**
   * UI Command Server Show Instances.
   */
  public UICmdServerShowInstances() {
    setCommand("instances");
    setShortHelp("show available instances");
    setUsage("ui server show instances");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder txt = new StringBuilder();
    for (int i = 0; i < DriveWireServer.getNumHandlers(); i++) {
      txt.append(i)
          .append("|")
          .append(DriveWireServer.getHandlerName(i))
          .append("\n");
    }
    return new DWCommandResponse(txt.toString());
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
