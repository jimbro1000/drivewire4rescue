package com.groupunix.drivewireserver.uicommands;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowSerialDevs extends DWCommand {

  public UICmdServerShowSerialDevs() {
    setCommand("serialdevs");
    setShortHelp("show available serial devices");
    setUsage("ui server show serialdevs");
  }

  @Override
  public DWCommandResponse parse(String cmdline) {
    String txt = new String();

    ArrayList<String> ports = DriveWireServer.getAvailableSerialPorts();

    for (int i = 0; i < ports.size(); i++)
      txt += ports.get(i) + "\n";

    return (new DWCommandResponse(txt));
  }

  public boolean validate(String cmdline) {
    return (true);
  }
}
