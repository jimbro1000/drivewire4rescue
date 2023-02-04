package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigSet extends DWCommand {

  public UICmdServerConfigSet() {
    setCommand("set");
    setShortHelp("Set server configuration item");
    setUsage("ui server config set [item] [value]");
  }

  public DWCommandResponse parse(String cmdline) {

    if (cmdline.length() == 0) {
      return (new DWCommandResponse(false, DWDefs.RC_SYNTAX_ERROR, "Must specify item"));
    }

    String[] args = cmdline.split(" ");

    if (args.length == 1) {
      return (doSetConfig(args[0]));
    } else {


      return (doSetConfig(args[0], cmdline.substring(args[0].length() + 1)));
    }

  }


  public boolean validate(String cmdline) {
    return (true);
  }


  private DWCommandResponse doSetConfig(String item) {

    if (DriveWireServer.getServerConfiguration().containsKey(item)) {
      synchronized (DriveWireServer.getServerConfiguration()) {
        DriveWireServer.getServerConfiguration().setProperty(item, null);
      }

    }

    return (new DWCommandResponse(item + " unset."));

  }


  private DWCommandResponse doSetConfig(String item, String value) {
    synchronized (DriveWireServer.getServerConfiguration()) {
      if (DriveWireServer.getServerConfiguration().containsKey(item)) {
        if (!DriveWireServer.getServerConfiguration().getProperty(item).equals(value))
          DriveWireServer.getServerConfiguration().setProperty(item, value);
      } else {
        DriveWireServer.getServerConfiguration().setProperty(item, value);
      }
    }
    return (new DWCommandResponse(item + " set."));
  }


}