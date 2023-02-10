package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class DWCmdClientRestart extends DWCommand {
  /**
   * Drivewire serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;

  /**
   * Client restart command constructor.
   *
   * @param serialProtocol serial protocol
   * @param parent         parent command
   */
  public DWCmdClientRestart(
      final DWVSerialProtocol serialProtocol, final DWCommand parent
  ) {
    super();
    this.dwvSerialProtocol = serialProtocol;
    setParentCmd(parent);
    this.setCommand("restart");
    this.setShortHelp("Restart client device");
    this.setUsage("dw client restart");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doStart();
  }

  private DWCommandResponse doStart() {
    this.dwvSerialProtocol.getVPorts().setRebootRequested(true);
    return new DWCommandResponse("Restart pending");
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
