package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynth extends DWCommand {
  /**
   * command name.
   */
  static final String COMMAND = "synth";
  /**
   * component commands.
   */
  private final DWCommandList commands;
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
    commands = new DWCommandList(
        this.dwProtocolHandler,
        this.dwProtocolHandler.getCMDCols()
    );
    commands.addCommand(new DWCmdMidiSynthStatus(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthShow(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthBank(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthProfile(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthLock(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthInstr(protocolHandler, this));
  }

  /**
   * get command.
   * @return command name
   */
  public String getCommand() {
    return COMMAND;
  }

  /**
   * get command list.
   * @return component commands
   */
  public DWCommandList getCommandList() {
    return (this.commands);
  }

  /**
   * parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.commands.getShortHelp()));
    }
    return (commands.parse(cmdline));
  }

  /**
   * get short help.
   * @return short help details
   */
  public String getShortHelp() {
    return "Manage the MIDI synth";
  }

  /**
   * get usage.
   * @return usage information
   */
  public String getUsage() {
    return "dw midi synth [command]";
  }

  /**
   * validate command.
   * @param cmdline
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return (commands.validate(cmdline));
  }
}
