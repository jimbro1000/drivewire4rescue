package com.groupunix.drivewireserver.dwcommands;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdMidiOutput extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi output command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiOutput(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("output");
    this.setShortHelp("Set midi output to device #");
    this.setUsage("dw midi output #");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: dw midi output requires a device # as an argument"
      );
    }
    return (doMidiOutput(cmdline));
  }

  private DWCommandResponse doMidiOutput(final String deviceNumber) {
    if (dwProtocolHandler.getConfig().getBoolean("UseMIDI", true)) {
      try {
        int deviceId = Integer.parseInt(deviceNumber);
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        if ((deviceId < 0) || (deviceId > infos.length)) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_MIDI_INVALID_DEVICE,
              "Invalid device number for dw midi output."
          );
        }
        dwProtocolHandler.getVPorts().setMIDIDevice(
            MidiSystem.getMidiDevice(infos[deviceId])
        );
        return new DWCommandResponse(
            "Set MIDI output device: "
                + MidiSystem.getMidiDevice(infos[deviceId])
                .getDeviceInfo()
                .getName()
        );
      } catch (NumberFormatException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SYNTAX_ERROR,
            "dw midi device requires a numeric device # as an argument"
        );
      } catch (MidiUnavailableException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_MIDI_UNAVAILABLE,
            e.getMessage()
        );
      } catch (IllegalArgumentException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_MIDI_INVALID_DEVICE,
            e.getMessage()
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
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
