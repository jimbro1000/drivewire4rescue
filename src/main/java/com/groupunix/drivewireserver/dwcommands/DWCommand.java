package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;

public abstract class DWCommand implements DWCmdHelp {
  /**
   * Component command list.
   */
  protected DWCommandList commands = new DWCommandList(null);
  /**
   * Command name.
   */
  protected String commandName;
  /**
   * Short help text.
   */
  protected String shortHelp;
  /**
   * Usage information.
   */
  protected String usage;

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
   * Provides component command list.
   *
   * @return command list
   */
  public final DWCommandList getCommandList() {
    return this.commands;
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
   * Provides usage information for the command.
   *
   * @return usage details
   */
  public final String getUsage() {
    return usage;
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
