package com.groupunix.drivewireserver.uicommands;

import java.io.File;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdServerFileDefaultDir extends DWCommand {

  public UICmdServerFileDefaultDir() {
    setCommand("defaultdir");
    setShortHelp("Show default dir dir");
    setUsage("ui server file defaultdir");
  }

  public DWCommandResponse parse(String cmdline) {
    return (new DWCommandResponse(DWUtils.getFileDescriptor(new File(DriveWireServer.getServerConfiguration().getString("LocalDiskDir", "."))) + "|false"));
  }

  public boolean validate(String cmdline) {
    return (true);
  }

}