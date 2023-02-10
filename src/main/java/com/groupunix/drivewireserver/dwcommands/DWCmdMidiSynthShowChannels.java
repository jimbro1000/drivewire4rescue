package com.groupunix.drivewireserver.dwcommands;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynthShowChannels extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private DWProtocolHandler dwProtocolHandler;

  /**
   * Show Midi Synthesiser Channels constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynthShowChannels(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("channels");
    this.setShortHelp("Show internal synth channel status");
    this.setUsage("dw midi synth show channels");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder text = new StringBuilder();
    text.append("\r\nInternal synthesizer channel status:\r\n\n");
    if (dwProtocolHandler.getVPorts().getMidiSynth() != null) {
      final MidiChannel[] midiChannels = dwProtocolHandler
          .getVPorts()
          .getMidiSynth()
          .getChannels();
      final Instrument[] instruments = dwProtocolHandler
          .getVPorts()
          .getMidiSynth()
          .getLoadedInstruments();
      text.append("Chan#  Instr#  Orig#   Instrument\r\n");
      text.append("--------------------------------------")
          .append("---------------------------------------\r\n"
      );
      for (int i = 0; i < midiChannels.length; i++) {
        if (midiChannels[i] != null) {
          text.append(
              String.format(
                  " %2d      %-3d    %-3d    ",
                  i + 1,
                  midiChannels[i].getProgram(),
                  dwProtocolHandler.getVPorts().getGMInstrumentCache(i)
              )
          );
          if (midiChannels[i].getProgram() < instruments.length) {
            text.append(instruments[midiChannels[i].getProgram()].getName());
          } else {
            text.append("(unknown instrument or no soundbank loaded)");
          }
          text.append("\r\n");
        }
      }
    } else {
      text.append("MIDI is disabled.\r\n");
    }
    return new DWCommandResponse(text.toString());
  }

  /**
   * Validates command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
