package com.groupunix.drivewireserver.uicommands;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowSynthProfiles extends DWCommand {

  /**
   * UI Command Server Show Synth Profiles.
   */
  public UICmdServerShowSynthProfiles() {
    setHelp();
  }

  private void setHelp() {
    setCommand("synthprofiles");
    setShortHelp("show MIDI synth profiles");
    setUsage("ui server show synthprofiles");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @SuppressWarnings("unchecked")
  @Override
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder res = new StringBuilder();

    List<HierarchicalConfiguration> profiles = DriveWireServer
        .getServerConfiguration()
        .configurationsAt("midisynthprofile");

    for (HierarchicalConfiguration mprof : profiles) {
      res.append(mprof.getString("[@name]"))
          .append("|")
          .append(mprof.getString("[@desc]"))
          .append("\n");
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
