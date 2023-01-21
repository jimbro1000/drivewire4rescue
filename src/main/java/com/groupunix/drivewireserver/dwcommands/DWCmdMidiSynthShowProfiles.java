package com.groupunix.drivewireserver.dwcommands;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiSynthShowProfiles extends DWCommand {

  /**
   * Midi synth show profiles command constructor.
   *
   * @param parent parent command
   */
  public DWCmdMidiSynthShowProfiles(final DWCommand parent) {
    setParentCmd(parent);
    this.setCommand("profiles");
    this.setShortHelp("Show internal synth profiles");
    this.setUsage("dw midi synth show profiles");
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  @SuppressWarnings("unchecked")
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder text = new StringBuilder();

    text.append("\r\nAvailable sound translation profiles:\r\n\n");

    List<HierarchicalConfiguration> profiles = DriveWireServer
        .serverConfiguration
        .configurationsAt("midisynthprofile");

    for (HierarchicalConfiguration midiProfile : profiles) {
      text.append(
          String.format(
              "%-10s: %-35s dev_adjust: %2d  gm_adjust: %2d",
              midiProfile.getString("[@name]", "n/a"),
              midiProfile.getString("[@desc]", "n/a"),
              midiProfile.getInt("[@dev_adjust]", 0),
              midiProfile.getInt("[@gm_adjust]", 0)
          )
      );
      text.append("\r\n");
    }
    text.append("\r\n");
    return new DWCommandResponse(text.toString());
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
