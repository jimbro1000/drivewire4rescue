package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidi extends DWCommand {

  private DWProtocolHandler dwProto;

  public DWCmdMidi(DWProtocolHandler dwProto, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProto;
    DWCommandList commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdMidiStatus(dwProto, this));
    commands.addCommand(new DWCmdMidiOutput(dwProto, this));
    commands.addCommand(new DWCmdMidiSynth(dwProto, this));
    this.setCommand("midi");
    this.setShortHelp("Manage the MIDI subsystem");
    this.setUsage("dw midi [command]");
  }

  public DWCommandResponse parse(String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.getCommandList().getShortHelp()));
    }
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
