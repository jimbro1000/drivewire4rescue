package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynth extends DWCommand {
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi synch command constructor.
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynth(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    DWCommandList commands = new DWCommandList(
        this.dwProtocolHandler,
        this.dwProtocolHandler.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdMidiSynthStatus(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthShow(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthBank(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthProfile(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthLock(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthInstr(protocolHandler, this));
    this.setCommand("synth");
    this.setShortHelp("Manage the MIDI synth");
    this.setUsage("dw midi synth [command]");
  }

  /**
   * parse command.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.getCommandList().getShortHelp()));
    }
    return (this.getCommandList().parse(cmdline));
  }


  /**
   * validate command.
   * @param cmdline
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
