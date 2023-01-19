package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDisk extends DWCommand {

  static final String command = "disk";
  private DWCommandList commands;
  private DWProtocolHandler dwProto;

  public DWCmdDisk(DWProtocolHandler dwProto, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProto;

    commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());

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
  }


  public String getCommand() {
    return command;
  }

  public DWCommandResponse parse(String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.commands.getShortHelp()));
    }
    return (commands.parse(cmdline));
  }

  public DWCommandList getCommandList() {
    return (this.commands);
  }


  public String getShortHelp() {
    return "Manage disks and disksets";
  }


  public String getUsage() {
    return "dw disk [command]";
  }

  public boolean validate(String cmdline) {
    return (commands.validate(cmdline));
  }

}
