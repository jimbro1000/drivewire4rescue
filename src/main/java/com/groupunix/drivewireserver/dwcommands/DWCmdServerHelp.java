package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdServerHelp extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Server help command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdServerHelp(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    DWCommandList commands = new DWCommandList(
        this.dwProtocol, this.dwProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdServerHelpShow(protocol, this));
    commands.addCommand(new DWCmdServerHelpReload(protocol, this));
    this.setCommand("help");
    this.setShortHelp("Manage the help system");
    this.setUsage("dw help [command]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(this.getCommandList().getShortHelp());
    }
    return this.getCommandList().parse(cmdline);
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return this.getCommandList().validate(cmdline);
  }
}
