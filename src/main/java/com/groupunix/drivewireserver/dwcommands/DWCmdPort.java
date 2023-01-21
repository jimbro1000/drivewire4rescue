package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class DWCmdPort extends DWCommand {
  private DWVSerialProtocol dwProto;

  public DWCmdPort(DWVSerialProtocol dwProtocol, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProtocol;
    DWCommandList commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdPortShow(dwProtocol, this));
    commands.addCommand(new DWCmdPortClose(dwProtocol, this));
    commands.addCommand(new DWCmdPortOpen(dwProtocol, this));
    this.setCommand("port");
    this.setShortHelp("Manage virtual serial ports");
    this.setUsage("dw port [command]");
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
