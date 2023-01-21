package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServer extends DWCommand {
  private DWProtocol dwProto;

  public DWCmdServer(DWProtocol dwProto, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProto;
    DWCommandList commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdServerStatus(dwProto, this));
    commands.addCommand(new DWCmdServerShow(dwProto, this));
    commands.addCommand(new DWCmdServerList(this));
    commands.addCommand(new DWCmdServerDir(this));
    commands.addCommand(new DWCmdServerTerminate(this));
    commands.addCommand(new DWCmdServerTurbo(dwProto, this));
    commands.addCommand(new DWCmdServerPrint(dwProto, this));
    commands.addCommand(new DWCmdServerHelp(dwProto, this));

    //	commands.addcommand(new DWCmdServerRestart(handlerno));
    this.setCommand("server");
    this.setShortHelp("Various server based tools");
    this.setUsage("dw server [command]");
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
