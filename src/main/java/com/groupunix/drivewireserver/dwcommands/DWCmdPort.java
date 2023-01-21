package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public final class DWCmdPort extends DWCommand {
  /**
   * Drivewire serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;

  /**
   * Port command constructor.
   *
   * @param protocol serial protocol
   * @param parent parent command
   */
  public DWCmdPort(final DWVSerialProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwvSerialProtocol = protocol;
    DWCommandList commands = new DWCommandList(
        this.dwvSerialProtocol, this.dwvSerialProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdPortShow(protocol, this));
    commands.addCommand(new DWCmdPortClose(protocol, this));
    commands.addCommand(new DWCmdPortOpen(protocol, this));
    this.setCommand("port");
    this.setShortHelp("Manage virtual serial ports");
    this.setUsage("dw port [command]");
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
