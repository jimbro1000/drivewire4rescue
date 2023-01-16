package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynthInstr extends DWCommand {

  /**
   * Protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Drivewire Midi Synth Instruction constructor.
   * @param protocolHandler
   * @param parent
   */
  public DWCmdMidiSynthInstr(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
  }

  /**
   * Get command.
   * @return command name
   */
  public String getCommand() {
    return "instr";
  }

  /**
   * Get short help.
   * @return short help details
   */
  public String getShortHelp() {
    return "Manually set chan X to instrument Y";
  }

  /**
   * Get usage information.
   * @return usage
   */
  public String getUsage() {
    return "dw midi synth instr #X #Y";
  }

  /**
   * Parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    String[] args = cmdline.split(" ");
    if (args.length != 2) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw midi synth instr requires a "
              + "channel # and an instrument # as arguments"
      );
    }
    int channel;
    int instr;
    try {
      channel = Integer.parseInt(args[0]) - 1;
      instr = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw midi synth instr requires a "
              + "channel # and an instrument # as arguments"
      );
    }
    if (dwProtocolHandler.getVPorts().setMIDIInstr(channel, instr)) {
      return new DWCommandResponse(
          "Set MIDI channel "
              + (channel + 1) + " to instrument " + instr
      );
    } else {
      return new DWCommandResponse(
          false,
          DWDefs.RC_MIDI_ERROR,
          "Failed to set instrument"
      );
    }
  }

  /**
   * Validate command.
   * @param cmdline
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
