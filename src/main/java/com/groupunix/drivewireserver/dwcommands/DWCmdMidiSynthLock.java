package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdMidiSynthLock extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Midi synth lock command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdMidiSynthLock(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("lock");
    this.setShortHelp("Toggle instrument lock");
    this.setUsage("dw midi synth lock");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return (doMidiSynthLock());
  }

  private DWCommandResponse doMidiSynthLock() {
    if (dwProtocolHandler.getVPorts().getMidiVoicelock()) {
      dwProtocolHandler.getVPorts().setMidiVoicelock(false);
      return new DWCommandResponse(
          "Unlocked MIDI instruments, program changes will be processed"
      );
    } else {
      dwProtocolHandler.getVPorts().setMidiVoicelock(true);
      return new DWCommandResponse(
          "Locked MIDI instruments, progam changes will be ignored"
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
