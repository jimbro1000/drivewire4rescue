package com.groupunix.drivewireserver.dwcommands;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdMidiStatus extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi status command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiStatus(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("status");
    this.setShortHelp("Show MIDI status");
    this.setUsage("dw midi status");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doMidiStatus();
  }

  private DWCommandResponse doMidiStatus() {
    final StringBuilder text = new StringBuilder();
    text.append("\r\nDriveWire MIDI status:\r\n\n");
    if (
        dwProtocolHandler
            .getConfig()
            .getBoolean("UseMIDI", true)
    ) {
      text.append("Devices:\r\n");
      final MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
      for (int i = 0; i < infos.length; i++) {
        try {
          final MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
          text.append("[").append(i).append("] ");
          text.append(device.getDeviceInfo().getName())
              .append(" (")
              .append(device.getClass().getSimpleName())
              .append(")\r\n");
          text.append("    ")
              .append(device.getDeviceInfo().getDescription())
              .append(", ");
          text.append(device.getDeviceInfo().getVendor()).append(" ");
          text.append(device.getDeviceInfo().getVersion()).append("\r\n");
        } catch (MidiUnavailableException e) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_MIDI_UNAVAILABLE,
              e.getMessage()
          );
        }
      }
      text.append("\r\nCurrent MIDI output device: ");
      if (dwProtocolHandler.getVPorts().getMidiDeviceInfo() == null) {
        text.append("none\r\n");
      } else {
        text.append(dwProtocolHandler.getVPorts().getMidiDeviceInfo().getName())
            .append("\r\n");
      }
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
