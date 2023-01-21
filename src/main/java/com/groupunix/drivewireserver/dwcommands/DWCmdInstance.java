package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdInstance extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Command instance constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdInstance(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    DWCommandList commands = new DWCommandList(this.dwProtocol, this.dwProtocol.getCMDCols());
    this.setCommandList(commands);
    commands.addCommand(new DWCmdInstanceShow(protocol, this));
    commands.addCommand(new DWCmdInstanceStart(protocol, this));
    commands.addCommand(new DWCmdInstanceStop(protocol, this));
    commands.addCommand(new DWCmdInstanceRestart(protocol, this));
    this.setCommand("instance");
    this.setShortHelp("Commands to control instances");
    this.setUsage("dw instance [command]");
  }

  /**
   * Parse command if present.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.getCommandList().getShortHelp()));
    }
    return (this.getCommandList().parse(cmdline));
  }

  /**
   * Validate command for start, stop, show and restart.
   *
   * @param cmdline command string
   * @return true if valid for all actions
   */
  public boolean validate(final String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
