package com.groupunix.drivewireserver.uicommands;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowMIDIDevs extends DWCommand {

  /**
   * UI Command Server Show MIDI Devices.
   */
  public UICmdServerShowMIDIDevs() {
    setCommand("mididevs");
    setShortHelp("show available MIDI devices");
    setUsage("ui server show mididevs");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder res = new StringBuilder();
    // hack.. should look at current instance, but I just don't care
    if (DriveWireServer.getHandler(0)
        .getConfig().getBoolean("UseMIDI", true)) {
      MidiDevice device;
      MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
      for (int i = 0; i < infos.length; i++) {
        try {
          device = MidiSystem.getMidiDevice(infos[i]);
          res.append(i)
              .append(" ")
              .append(device.getDeviceInfo().getName())
              .append("\n");
        } catch (MidiUnavailableException e) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_MIDI_UNAVAILABLE,
              "MIDI unavailable during UI device listing"
          );
        }
      }
    } else {
      return new DWCommandResponse(
          false,
          DWDefs.RC_MIDI_UNAVAILABLE,
          "MIDI is disabled."
      );
    }
    return new DWCommandResponse(res.toString());
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
