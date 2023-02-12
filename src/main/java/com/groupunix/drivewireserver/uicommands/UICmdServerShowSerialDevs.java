package com.groupunix.drivewireserver.uicommands;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowSerialDevs extends DWCommand {

  /**
   * UI Command Server Show Serial Devices.
   */
  public UICmdServerShowSerialDevs() {
    super();
    setCommand("serialdevs");
    setShortHelp("show available serial devices");
    setUsage("ui server show serialdevs");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder txt = new StringBuilder();
    final ArrayList<String> ports = DriveWireServer.getAvailableSerialPorts();
    for (final String port : ports) {
      txt.append(port).append("\n");
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
