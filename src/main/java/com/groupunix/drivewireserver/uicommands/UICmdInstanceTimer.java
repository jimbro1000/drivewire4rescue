package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.*;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceTimer extends DWCommand {
  public UICmdInstanceTimer(DWProtocol dwProtocol) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceTimerShow(dwProtocol));
    commands.addCommand(new UICmdInstanceTimerReset(dwProtocol));
    setHelp();
  }

  public UICmdInstanceTimer(DWUIClientThread dwuiClientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceTimerShow(dwuiClientThread));
    commands.addCommand(new UICmdInstanceTimerReset(dwuiClientThread));
    setHelp();
  }

  private void setHelp() {
    this.setCommand("timer");
    this.setShortHelp("Timer commands");
    this.setUsage("ui instance timer [command]");
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
