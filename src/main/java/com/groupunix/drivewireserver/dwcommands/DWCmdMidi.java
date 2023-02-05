package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidi extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidi(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    DWCommandList commands = new DWCommandList(
        this.dwProtocolHandler, this.dwProtocolHandler.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdMidiStatus(protocolHandler, this));
    commands.addCommand(new DWCmdMidiOutput(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynth(protocolHandler, this));
    this.setCommand("midi");
    this.setShortHelp("Manage the MIDI subsystem");
    this.setUsage("dw midi [command]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(this.getCommandList().getShortHelp());
    }
    return this.getCommandList().parse(cmdline);
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return this.getCommandList().validate(cmdline);
  }
}
