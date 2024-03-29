package com.groupunix.drivewireserver.uicommands;

import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceMIDIStatus extends DWCommand {
  /**
   * Client thread ref.
   */
  private DWUIClientThread dwuiClientThread = null;
  /**
   * Protocol.
   */
  private DWProtocol dwProtocol = null;

  /**
   * UI Command Instance MIDI status.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceMIDIStatus(final DWUIClientThread clientThread) {
    super();
    this.dwuiClientThread = clientThread;
    setHelp();
  }

  /**
   * UI Command Instance MIDI status.
   *
   * @param protocol protocol
   */
  public UICmdInstanceMIDIStatus(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    setHelp();
  }

  private void setHelp() {
    setCommand("midistatus");
    setShortHelp("show MIDI status");
    setUsage("ui instance midistatus");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder res = new StringBuilder("enabled|false\n\n");
    if (this.dwProtocol == null) {
      if (DriveWireServer.isValidHandlerNo(
          this.dwuiClientThread.getInstance())
      ) {
        this.dwProtocol = DriveWireServer.getHandler(
            this.dwuiClientThread.getInstance()
        );
      } else {
        return new DWCommandResponse(res.toString());
      }
    }
    if (this.dwProtocol.hasMIDI()) {
      final DWProtocolHandler dwProto = (DWProtocolHandler) dwProtocol;

      if (dwProto != null
          && dwProto.getVPorts() != null
          && dwProto.getVPorts().getMidiDeviceInfo() != null) {
        try {
          res = new StringBuilder(
              "enabled|"
                  + dwProto.getConfig().getBoolean("UseMIDI", false)
                  + "\r\n"
          );
          res.append("cdevice|")
              .append(dwProto.getVPorts().getMidiDeviceInfo().getName())
              .append("\r\n");
          res.append("cprofile|")
              .append(dwProto.getVPorts().getMidiProfileName())
              .append("\r\n");
          final MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
          for (int j = 0; j < infos.length; j++) {
            final MidiDevice.Info info = infos[j];
            final MidiDevice dev = MidiSystem.getMidiDevice(info);
            res.append("device|")
                .append(j)
                .append("|")
                .append(dev.getClass().getSimpleName())
                .append("|")
                .append(info.getName())
                .append("|")
                .append(info.getDescription())
                .append("|")
                .append(info.getVendor())
                .append("|")
                .append(info.getVersion())
                .append("\r\n");
          }
          @SuppressWarnings("unchecked")
          final List<HierarchicalConfiguration> profiles =
              DriveWireServer.getServerConfiguration().configurationsAt(
                  "midisynthprofile");
          for (final HierarchicalConfiguration midiProfile : profiles) {
            res.append("profile|")
                .append(midiProfile.getString("[@name]"))
                .append("|")
                .append(midiProfile.getString("[@desc]"))
                .append("\r\n");
          }
        } catch (MidiUnavailableException e) {
          res = new StringBuilder("enabled|false\n\n");
        }
      }
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
