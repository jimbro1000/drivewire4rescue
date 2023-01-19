package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerShow extends DWCommand {

  static final String command = "show";


  public UICmdServerShow(DWUIClientThread dwuiClientThread) {
    commands.addCommand(new UICmdServerShowVersion());
    commands.addCommand(new UICmdServerShowInstances());
    commands.addCommand(new UICmdServerShowMIDIDevs());
    commands.addCommand(new UICmdServerShowSynthProfiles());
    commands.addCommand(new UICmdServerShowLocalDisks());
    commands.addCommand(new UICmdServerShowSerialDevs());
    commands.addCommand(new UICmdServerShowStatus());
    commands.addCommand(new UICmdServerShowNet());
    commands.addCommand(new UICmdServerShowLog());
    commands.addCommand(new UICmdServerShowTopics(dwuiClientThread));
    commands.addCommand(new UICmdServerShowHelp(dwuiClientThread));
    commands.addCommand(new UICmdServerShowErrors(dwuiClientThread));
  }


  public UICmdServerShow(DWProtocol dwProto) {
    commands.addCommand(new UICmdServerShowVersion());
    commands.addCommand(new UICmdServerShowInstances());
    commands.addCommand(new UICmdServerShowMIDIDevs());
    commands.addCommand(new UICmdServerShowSynthProfiles());
    commands.addCommand(new UICmdServerShowLocalDisks());
    commands.addCommand(new UICmdServerShowSerialDevs());
    commands.addCommand(new UICmdServerShowStatus());
    commands.addCommand(new UICmdServerShowNet());
    commands.addCommand(new UICmdServerShowLog());
    commands.addCommand(new UICmdServerShowTopics(dwProto));
    commands.addCommand(new UICmdServerShowHelp(dwProto));
    commands.addCommand(new UICmdServerShowErrors(dwProto));
  }


  public String getCommand() {
    return command;
  }

  public DWCommandResponse parse(String cmdline) {
    return (commands.parse(cmdline));
  }


  public String getShortHelp() {
    return "Informational commands";
  }


  public String getUsage() {
    return "ui server show [item]";
  }

  public boolean validate(String cmdline) {
    return (commands.validate(cmdline));
  }

}