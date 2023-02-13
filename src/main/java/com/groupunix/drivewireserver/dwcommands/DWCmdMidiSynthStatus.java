package com.groupunix.drivewireserver.dwcommands;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynthStatus extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi synth status command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynthStatus(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("status");
    this.setShortHelp("Show internal synth status");
    this.setUsage("dw midi synth status");
  }

  /**
   * Parse command if present.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doSynthStatus();
  }

  private DWCommandResponse doSynthStatus() {
    String text;
    // dw midi synth show
    text = "\r\nInternal synthesizer status:\r\n\n";
    if (dwProtocolHandler.getVPorts().getMidiSynth() != null) {
      final MidiDevice.Info midiInfo = dwProtocolHandler
          .getVPorts()
          .getMidiSynth()
          .getDeviceInfo();
      text += "Device:\r\n";
      text += midiInfo.getVendor() + ", "
          + midiInfo.getName() + ", " + midiInfo.getVersion() + "\r\n";
      text += midiInfo.getDescription() + "\r\n";
      text += "\r\n";
      text += "Soundbank: ";
      if (dwProtocolHandler.getVPorts().getMidiSoundbankFilename() == null) {
        final Soundbank soundbank = dwProtocolHandler
            .getVPorts().getMidiSynth().getDefaultSoundbank();
        if (soundbank != null) {
          text += " (default)\r\n";
          text += soundbank.getVendor() + ", "
              + soundbank.getName() + ", " + soundbank.getVersion() + "\r\n";
          text += soundbank.getDescription() + "\r\n";
        } else {
          text += " none\r\n";
        }
      } else {
        final File file = new File(
            dwProtocolHandler.getVPorts().getMidiSoundbankFilename()
        );
        try {
          final Soundbank soundbank = MidiSystem.getSoundbank(file);
          text += " (" + dwProtocolHandler
              .getVPorts()
              .getMidiSoundbankFilename()
              + ")\r\n";
          text += soundbank.getVendor() + ", "
              + soundbank.getName() + ", " + soundbank.getVersion() + "\r\n";
          text += soundbank.getDescription() + "\r\n";
        } catch (InvalidMidiDataException e) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_MIDI_INVALID_DATA,
              e.getMessage()
          );
        } catch (IOException e) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_SERVER_IO_EXCEPTION,
              e.getMessage());
        }
      }
      text += "\r\n";
      text += "Latency:   "
          + dwProtocolHandler.getVPorts().getMidiSynth().getLatency()
          + "\r\n";
      text += "Polyphony: "
          + dwProtocolHandler.getVPorts().getMidiSynth().getMaxPolyphony()
          + "\r\n";
      text += "Position:  "
          + dwProtocolHandler
          .getVPorts()
          .getMidiSynth()
          .getMicrosecondPosition()
          + "\r\n\n";
      text += "Profile:   "
          + dwProtocolHandler.getVPorts().getMidiProfileName() + "\r\n";
      text += "Instrlock: "
          + dwProtocolHandler.getVPorts().isMidiVoicelock() + "\r\n";
    } else {
      text += "MIDI is disabled.\r\n";
    }
    return new DWCommandResponse(text);
  }

  /**
   * Validate command for start, stop, show and restart.
   *
   * @param cmdline command string
   * @return true if valid for all actions
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
