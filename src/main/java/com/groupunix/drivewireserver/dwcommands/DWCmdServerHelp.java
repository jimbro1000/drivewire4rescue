package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerHelp extends DWCommand {
  private DWProtocol dwProto;

  public DWCmdServerHelp(DWProtocol dwProtocol, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProtocol;
    DWCommandList commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdServerHelpShow(dwProtocol, this));
    commands.addCommand(new DWCmdServerHelpReload(dwProtocol, this));
    this.setCommand("help");
    this.setShortHelp("Manage the help system");
    this.setUsage("dw help [command]");
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
