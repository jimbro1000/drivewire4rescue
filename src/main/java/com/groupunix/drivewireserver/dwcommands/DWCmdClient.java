package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.*;

public class DWCmdClient extends DWCommand {
  private DWCommandList commands;
  private DWVSerialProtocol dwProto;

  public DWCmdClient(DWVSerialProtocol dwProto, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProto;
    commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    commands.addcommand(new DWCmdClientRestart(dwProto, this));
    commandName = "client";
    shortHelp = "Commands that manage the attached client device";
    usage = "dw client [command]";
  }

  public DWCommandList getCommandList() {
    return (this.commands);
  }

  public DWCommandResponse parse(String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.commands.getShortHelp()));
    }
    return (commands.parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (commands.validate(cmdline));
  }
}
