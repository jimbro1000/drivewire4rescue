package com.groupunix.drivewireserver.dwcommands;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynthBank extends DWCommand {
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi synth bank command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynthBank(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("bank");
    this.setShortHelp("Load soundbank file");
    this.setUsage("dw midi synth bank filepath");
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw midi synth bank requires a file path as an argument"
      );
    }
    return doMidiSynthBank(cmdline);
  }

  private DWCommandResponse doMidiSynthBank(final String path) {
    Soundbank soundbank = null;

    if (dwProtocolHandler.getConfig().getBoolean("UseMIDI", true)) {
      final File file = new File(path);
      try {
        soundbank = MidiSystem.getSoundbank(file);
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
            e.getMessage()
        );
      }
      if (dwProtocolHandler.getVPorts().isSoundbankSupported(soundbank)) {
        if (dwProtocolHandler.getVPorts().setMidiSoundbank(soundbank, path)) {
          return new DWCommandResponse(
              "Soundbank loaded without error"
          );
        } else {
          return new DWCommandResponse(
              false,
              DWDefs.RC_MIDI_SOUNDBANK_FAILED,
              "Failed to load soundbank"
          );
        }
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_MIDI_SOUNDBANK_NOT_SUPPORTED,
            "Soundbank not supported"
        );
      }
    } else {
      return new DWCommandResponse(
          false,
          DWDefs.RC_MIDI_UNAVAILABLE,
          "MIDI is disabled."
      );
    }
  }

  /**
   * Validate command.
   *
   * @param cmdline command string
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
