package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.*;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServer extends DWCommand {
  public UICmdServer(DWUIClientThread dwuiClientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerShow(dwuiClientThread));
    commands.addCommand(new UICmdServerConfig(dwuiClientThread));
    commands.addCommand(new UICmdServerTerminate(dwuiClientThread));
    commands.addCommand(new UICmdServerFile());
    setHelp();
  }

  public UICmdServer(DWProtocol dwProto) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerShow(dwProto));
    commands.addCommand(new UICmdServerConfig(dwProto));
    commands.addCommand(new UICmdServerTerminate(dwProto));
    commands.addCommand(new UICmdServerFile());
    setHelp();
  }

  private void setHelp() {
    this.setCommand("server");
    this.setShortHelp("Server commands");
    this.setUsage("ui server [command]");
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
