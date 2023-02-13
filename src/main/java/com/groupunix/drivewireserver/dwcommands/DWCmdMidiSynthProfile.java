package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdMidiSynthProfile extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi synth profile command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynthProfile(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("profile");
    this.setShortHelp("Load synth translation profile");
    this.setUsage("dw midi synth profile name");
  }

  /**
   * Parse command if present.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw midi synth profile requires a profile name as an argument"
      );
    }
    return doMidiSynthProfile(cmdline);
  }

  private DWCommandResponse doMidiSynthProfile(final String path) {
    if (dwProtocolHandler.getVPorts().setupMidiProfile(path)) {
      return new DWCommandResponse(
          "Set translation profile to '" + path + "'"
      );
    } else {
      return new DWCommandResponse(
          false,
          DWDefs.RC_MIDI_INVALID_PROFILE,
          "Invalid translation profile '" + path + "'"
      );
    }
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
