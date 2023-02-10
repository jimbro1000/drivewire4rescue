package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdMidiSynthShow extends DWCommand {
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
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    final DWCommandList commands = new DWCommandList(
        this.dwProtocolHandler,
        this.dwProtocolHandler.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdMidiSynthShowChannels(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthShowInstr(protocolHandler, this));
    commands.addCommand(new DWCmdMidiSynthShowProfiles(this));
    this.setCommand("show");
    this.setShortHelp("View details about the synth");
    this.setUsage("dw midi synth show [item]");
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
   * @param cmdline command line
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
