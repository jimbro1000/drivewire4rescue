package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigSerial extends DWCommand {
  /**
   * UI Command Server Config Serial.
   */
  public UICmdServerConfigSerial() {
    super();
    setCommand("serial");
    setShortHelp("Show server config serial#");
    setUsage("ui server config serial");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return new DWCommandResponse(DriveWireServer.getConfigSerial() + "");
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
