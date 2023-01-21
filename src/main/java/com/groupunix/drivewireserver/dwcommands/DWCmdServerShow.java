package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;


public class DWCmdServerShow extends DWCommand {
  private DWProtocol dwProto;

  public DWCmdServerShow(DWProtocol dwProto, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProto;
    DWCommandList commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdServerShowThreads(this));
    commands.addCommand(new DWCmdServerShowTimers(this.dwProto, this));
    commands.addCommand(new DWCmdServerShowSerial(this.dwProto, this));
    this.setCommand("show");
    this.setShortHelp("Show various server information");
    this.setUsage("dw server show [option]");
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
