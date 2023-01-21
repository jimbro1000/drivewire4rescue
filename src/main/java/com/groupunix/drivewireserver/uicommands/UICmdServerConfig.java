package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.*;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerConfig extends DWCommand {
  public UICmdServerConfig(DWUIClientThread dwuiClientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerConfigShow());
    commands.addCommand(new UICmdServerConfigSet());
    commands.addCommand(new UICmdServerConfigSerial());
    commands.addCommand(new UICmdServerConfigWrite());
    commands.addCommand(new UICmdServerConfigFreeze());
    setHelp();
  }

  public UICmdServerConfig(DWProtocol dwProto) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerConfigShow());
    commands.addCommand(new UICmdServerConfigSet());
    commands.addCommand(new UICmdServerConfigWrite());
    setHelp();
  }

  private void setHelp() {
    this.setCommand("config");
    this.setShortHelp("Configuration commands");
    this.setUsage("ui server config [command]");
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }

}