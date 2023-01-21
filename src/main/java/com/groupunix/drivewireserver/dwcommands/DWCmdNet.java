package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class DWCmdNet extends DWCommand {
  private DWVSerialProtocol dwProto;

  public DWCmdNet(DWVSerialProtocol dwProtocol, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProtocol;
    DWCommandList commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdNetShow(dwProtocol, this));
    this.setCommand("net");
    this.setShortHelp("Manage network connections");
    this.setUsage("dw net [command]");
  }

  public DWCommandResponse parse(String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.getCommandList().getShortHelp()));
    }
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
