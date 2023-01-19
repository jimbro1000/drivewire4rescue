package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServer extends DWCommand {

  static final String command = "server";

  public UICmdServer(DWUIClientThread dwuiClientThread) {
    commands.addCommand(new UICmdServerShow(dwuiClientThread));
    commands.addCommand(new UICmdServerConfig(dwuiClientThread));
    commands.addCommand(new UICmdServerTerminate(dwuiClientThread));
    commands.addCommand(new UICmdServerFile());
  }


  public UICmdServer(DWProtocol dwProto) {
    commands.addCommand(new UICmdServerShow(dwProto));
    commands.addCommand(new UICmdServerConfig(dwProto));
    commands.addCommand(new UICmdServerTerminate(dwProto));
    commands.addCommand(new UICmdServerFile());
  }


  public String getCommand() {
    return command;
  }

  public DWCommandResponse parse(String cmdline) {
    return (commands.parse(cmdline));
  }


  public String getShortHelp() {
    return "Server commands";
  }


  public String getUsage() {
    return "ui server [command]";
  }

  public boolean validate(String cmdline) {
    return (commands.validate(cmdline));
  }

}