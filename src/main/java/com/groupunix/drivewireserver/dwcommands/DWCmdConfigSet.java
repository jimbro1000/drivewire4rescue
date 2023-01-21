package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdConfigSet extends DWCommand {

  DWProtocol dwProto;

  public DWCmdConfigSet(DWProtocol dwProtocol, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProtocol;
    this.setCommand("set");
    this.setShortHelp("Set config item, omit value to remove item");
    this.setUsage("dw config set item [value]");
  }

  public DWCommandResponse parse(String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(false, DWDefs.RC_SYNTAX_ERROR, "Syntax error: dw config set requires an item and value as arguments"));
    }

    String[] args = cmdline.split(" ");

    if (args.length == 1) {
      return (doSetConfig(args[0]));
    } else {
      return (doSetConfig(args[0], cmdline.substring(args[0].length() + 1)));
    }

  }

  private DWCommandResponse doSetConfig(String item) {

    if (dwProto.getConfig().containsKey(item)) {
      synchronized (DriveWireServer.serverConfiguration) {
        dwProto.getConfig().clearProperty(item);
      }
    }

    return (new DWCommandResponse("Item '" + item + "' removed from config"));

  }


  private DWCommandResponse doSetConfig(String item, String value) {
    synchronized (DriveWireServer.serverConfiguration) {
      dwProto.getConfig().setProperty(item, value);
    }
    return (new DWCommandResponse("Item '" + item + "' set to '" + value + "'"));
  }


  public boolean validate(String cmdline) {

    return true;
  }


}
