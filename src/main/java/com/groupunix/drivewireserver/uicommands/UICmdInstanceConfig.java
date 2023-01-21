package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.*;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceConfig extends DWCommand {
  public UICmdInstanceConfig(DWUIClientThread dwuiClientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceConfigShow(dwuiClientThread));
    commands.addCommand(new UICmdInstanceConfigSet(dwuiClientThread));
    this.setCommand("config");
    this.setShortHelp("Configuration commands");
    this.setUsage("ui instance config [command]");
  }

  public UICmdInstanceConfig(DWProtocol dwProto) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceConfigShow(dwProto));
    commands.addCommand(new UICmdInstanceConfigSet(dwProto));
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }

}