package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.*;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerShow extends DWCommand {

  static final String command = "show";

  public UICmdServerShow(DWUIClientThread dwuiClientThread) {
    DWCommandList commands = this.getCommandList();
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
    setHelp();
  }

  public UICmdServerShow(DWProtocol dwProto) {
    DWCommandList commands = this.getCommandList();
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
    setHelp();
  }

  private void setHelp() {
    this.setCommand("show");
    this.setShortHelp("Informational commands");
    this.setUsage("ui server show [item]");
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
