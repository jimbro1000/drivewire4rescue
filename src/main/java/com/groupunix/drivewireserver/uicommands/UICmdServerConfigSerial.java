package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigSerial extends DWCommand {

  public UICmdServerConfigSerial() {
    setCommand("serial");
    setShortHelp("Show server config serial#");
    setUsage("ui server config serial");
  }

  public DWCommandResponse parse(String cmdline) {
    return new DWCommandResponse(DriveWireServer.getConfigSerial() + "");
  }

  public boolean validate(String cmdline) {
    return (true);
  }

}