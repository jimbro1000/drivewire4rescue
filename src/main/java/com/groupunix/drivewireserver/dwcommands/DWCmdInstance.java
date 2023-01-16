package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdInstance extends DWCommand {
  /**
   * Drivewire command name.
   */
  private static final String COMMAND = "instance";
  /**
   * Drivewire commands.
   */
  private final DWCommandList commands;
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Command instance constructor.
   * @param protocol protocol
   * @param parent parent
   */
  public DWCmdInstance(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    commands = new DWCommandList(this.dwProtocol, this.dwProtocol.getCMDCols());
    commands.addcommand(new DWCmdInstanceShow(protocol, this));
    commands.addcommand(new DWCmdInstanceStart(protocol, this));
    commands.addcommand(new DWCmdInstanceStop(protocol, this));
    commands.addcommand(new DWCmdInstanceRestart(protocol, this));
  }

  /**
   * Get command.
   * @return command name
   */
  public String getCommand() {
    return COMMAND;
  }

  /**
   * Get commands.
   * @return list of commands
   */
  public DWCommandList getCommandList() {
    return (this.commands);
  }

  /**
   * Parse command if present.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.commands.getShortHelp()));
    }
    return (commands.parse(cmdline));
  }

  /**
   * Get short help for command instance.
   * @return short help info
   */
  public String getShortHelp() {
    return "Commands to control instances";
  }

  /**
   * Get usage details.
   * @return usage information
   */
  public String getUsage() {
    return "dw instance [command]";
  }

  /**
   * Validate command for start, stop, show and restart.
   * @param cmdline
   * @return true if valid for all actions
   */
  public boolean validate(final String cmdline) {
    return (commands.validate(cmdline));
  }
}
