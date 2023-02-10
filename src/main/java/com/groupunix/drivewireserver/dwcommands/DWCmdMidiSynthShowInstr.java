package com.groupunix.drivewireserver.dwcommands;

import javax.sound.midi.Instrument;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdMidiSynthShowInstr extends DWCommand {
  /**
   * Maximum number of output columns.
   */
  private static final int COLUMN_LIMIT = 4;
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi synth show instrument command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynthShowInstr(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("instr");
    this.setShortHelp("Show internal synth instruments");
    this.setUsage("dw midi synth show instr");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder text = new StringBuilder();
    text.append("\r\nInternal synthesizer instrument list:\r\n\n");
    if (dwProtocolHandler.getVPorts().getMidiSynth() != null) {
      final Instrument[] instruments = dwProtocolHandler
          .getVPorts()
          .getMidiSynth()
          .getLoadedInstruments();
      if (instruments.length == 0) {
        text.append(
            "No instruments found, you may need to load a soundbank.\r\n"
        );
      }
      for (int i = 0; i < instruments.length; i++) {
        text.append(String.format("%3d:%-15s", i, instruments[i].getName()));
        if ((i % COLUMN_LIMIT) == 0) {
          text.append("\r\n");
        }
      }
      text.append("\r\n");
    } else {
      text.append("MIDI is disabled.\r\n");
    }
    return new DWCommandResponse(text.toString());
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
