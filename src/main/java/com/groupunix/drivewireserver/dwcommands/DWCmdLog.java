package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdLog extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Log command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdLog(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    commands = new DWCommandList(this.dwProtocol, this.dwProtocol.getCMDCols());
    commands.addCommand(new DWCmdLogShow(this));
    commandName = "log";
    shortHelp = "View the server log";
    usage = "dw log [command]";
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(this.commands.getShortHelp());
    }
    return commands.parse(cmdline);
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return commands.validate(cmdline);
  }
}
