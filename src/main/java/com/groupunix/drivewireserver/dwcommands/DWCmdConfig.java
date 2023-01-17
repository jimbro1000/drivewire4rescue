package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdConfig extends DWCommand {
  /**
   * command name.
   */
  static final String COMMAND = "config";
  /**
   * command list.
   */
  private final DWCommandList commands;
  /**
   * drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * command config constructor.
   * @param protocol protocol
   * @param parent command parent
   */
  public DWCmdConfig(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    commands = new DWCommandList(this.dwProtocol, this.dwProtocol.getCMDCols());
    commands.addcommand(new DWCmdConfigShow(protocol, this));
    commands.addcommand(new DWCmdConfigSet(protocol, this));
    commands.addcommand(new DWCmdConfigSave(protocol, this));
    // save/load not implemented here
  }

  /**
   * get command.
   * @return command name
   */
  public String getCommand() {
    return COMMAND;
  }

  /**
   * get component commands.
   * @return all component commands
   */
  public DWCommandList getCommandList() {
    return (this.commands);
  }

  /**
   * parse command.
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
   * get short help.
   * @return short help details
   */
  public String getShortHelp() {
    return "Commands to manipulate the config";
  }

  /**
   * get usage.
   * @return usage information
   */
  public String getUsage() {
    return "dw config [command]";
  }

  /**
   * validate command.
   * @param cmdline
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return (commands.validate(cmdline));
  }
}
