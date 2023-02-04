package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowVersion extends DWCommand {

  public UICmdServerShowVersion() {
    setCommand("version");
    setShortHelp("show server version");
    setUsage("ui server show version");
  }

  @Override
  public DWCommandResponse parse(String cmdline) {
    String txt = new String();

    txt = "DriveWire version " + DriveWireServer.DW_SERVER_VERSION + " (" + DriveWireServer.DW_SERVER_VERSION_DATE + ")";

    return (new DWCommandResponse(txt));
  }

  public boolean validate(String cmdline) {
    return (true);
  }
}
