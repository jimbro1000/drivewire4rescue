package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdMidiSynthShow extends DWCommand {
  /**
   * component commands.
   */
  private final DWCommandList commands;
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi synth show command constructor.
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynthShow(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    commands = new DWCommandList(
        this.dwProtocolHandler,
        this.dwProtocolHandler.getCMDCols()
    );
    commands.addCommand(new DWCmdMidiSynthShowChannels(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthShowInstr(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthShowProfiles(this));
    commandName = "show";
    shortHelp = "View details about the synth";
    usage = "dw midi synth show [item]";
  }

  /**
   * get commands.
   * @return component command list
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
   * validate command.
   * @param cmdline
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
