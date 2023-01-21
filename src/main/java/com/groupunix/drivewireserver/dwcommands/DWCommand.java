package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;

public abstract class DWCommand implements DWCmdHelp {
  /**
   * Component command list.
   */
  private DWCommandList commands = new DWCommandList(null);
  /**
   * Command name.
   */
  private String commandName;
  /**
   * Short help text.
   */
  private String shortHelp;
  /**
   * Usage information.
   */
  private String usage;

  /**
   * Parent command.
   */
  private DWCommand parentCommand = null;

  /**
   * Provides command name.
   *
   * @return command name
   */
  public final String getCommand() {
    return commandName;
  }

  /**
   * Set command name.
   *
   * @param command command name
   */
  public final void setCommand(String command) {
    commandName = command;
  }

  /**
   * Provides component command list.
   *
   * @return command list
   */
  public final DWCommandList getCommandList() {
    return this.commands;
  }

  /**
   * Set component command list.
   *
   * @param commandList component command list
   */
  public final void setCommandList(DWCommandList commandList) {
    this.commands = commandList;
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return new DWCommandResponse(
        false,
        DWDefs.RC_SERVER_NOT_IMPLEMENTED,
        "Not implemented (yet?)."
    );
  }

  /**
   * Returns parent command.
   *
   * @return parent command
   */
  public final DWCommand getParentCmd() {
    return this.parentCommand;
  }

  /**
   * Sets parent command.
   *
   * @param command parent command
   */
  public void setParentCmd(final DWCommand command) {
    this.parentCommand = command;
  }

  /**
   * Provides short help information.
   *
   * @return short help
   */
  public final String getShortHelp() {
    return shortHelp;
  }

  /**
   * Set short help text.
   *
   * @param helpText short help text
   */
  public void setShortHelp(String helpText) {
    shortHelp = helpText;
  }

  /**
   * Provides usage information for the command.
   *
   * @return usage details
   */
  public final String getUsage() {
    return usage;
  }

  /**
   * Set usage text.
   *
   * @param usageText usage text
   */
  public final void setUsage(String usageText) {
    usage = usageText;
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return false;
  }
}
