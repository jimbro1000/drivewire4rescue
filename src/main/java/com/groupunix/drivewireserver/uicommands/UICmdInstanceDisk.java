package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceDisk extends DWCommand {
  public UICmdInstanceDisk(DWUIClientThread dwuiClientThread) {
    this.getCommandList().addCommand(new UICmdInstanceDiskShow(dwuiClientThread));
    // commands.addcommand(new UICmdInstanceDiskSerial(dwuiClientThread));
    // commands.addcommand(new UICmdInstanceDiskStatus(dwuiClientThread));
    this.setCommand("disk");
    this.setShortHelp("Instance disk commands");
    this.setUsage("ui instance disk [command]");
  }

  public UICmdInstanceDisk(DWProtocolHandler dwProto) {
    this.getCommandList().addCommand(new UICmdInstanceDiskShow(dwProto));
    // commands.addcommand(new UICmdInstanceDiskSerial(dwProto));
    // commands.addcommand(new UICmdInstanceDiskStatus(dwProto));

  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }


  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
