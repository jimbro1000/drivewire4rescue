package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDisk extends DWCommand {
  private DWProtocolHandler dwProto;

  public DWCmdDisk(DWProtocolHandler dwProto, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProto;

    DWCommandList commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdDiskShow(dwProto, this));
    commands.addCommand(new DWCmdDiskEject(dwProto, this));
    commands.addCommand(new DWCmdDiskInsert(dwProto, this));
    commands.addCommand(new DWCmdDiskReload(dwProto, this));
    commands.addCommand(new DWCmdDiskWrite(dwProto, this));
    commands.addCommand(new DWCmdDiskCreate(dwProto, this));
    commands.addCommand(new DWCmdDiskSet(dwProto, this));
    commands.addCommand(new DWCmdDiskDos(dwProto, this));
    // testing only, little point
    //commands.addcommand(new DWCmdDiskDump(dwProto,this));
    this.setCommand("disk");
    this.setShortHelp("Manage disks and disksets");
    this.setUsage("dw disk [command]");
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
