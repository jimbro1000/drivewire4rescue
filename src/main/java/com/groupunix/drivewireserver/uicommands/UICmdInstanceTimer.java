package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceTimer extends DWCommand {

  static final String command = "timer";


  public UICmdInstanceTimer(DWProtocol dwProtocol) {
    commands.addCommand(new UICmdInstanceTimerShow(dwProtocol));
    commands.addCommand(new UICmdInstanceTimerReset(dwProtocol));

  }


  public UICmdInstanceTimer(DWUIClientThread dwuiClientThread) {
    commands.addCommand(new UICmdInstanceTimerShow(dwuiClientThread));
    commands.addCommand(new UICmdInstanceTimerReset(dwuiClientThread));

  }


  public String getCommand() {
    return command;
  }

  public DWCommandResponse parse(String cmdline) {
    return (commands.parse(cmdline));
  }


  public String getShortHelp() {
    return "Timer commands";
  }


  public String getUsage() {
    return "ui instance timer [command]";
  }

  public boolean validate(String cmdline) {
    return (commands.validate(cmdline));
  }

}