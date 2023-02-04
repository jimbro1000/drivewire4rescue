package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowInstances extends DWCommand {

  public UICmdServerShowInstances() {
    setCommand("instances");
    setShortHelp("show available instances");
    setUsage("ui server show instances");
  }

  @Override
  public DWCommandResponse parse(String cmdline) {
    String txt = new String();

    for (int i = 0; i < DriveWireServer.getNumHandlers(); i++) {
      txt += i + "|" + DriveWireServer.getHandlerName(i) + "\n";

    }

    return (new DWCommandResponse(txt));
  }

  public boolean validate(String cmdline) {
    return (true);
  }
}
