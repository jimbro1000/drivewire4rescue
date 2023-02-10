package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public final class DWCmdNet extends DWCommand {
  /**
   * Drivewire serial protocol.
   */
  private DWVSerialProtocol dwvSerialProtocol;

  /**
   * Net command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdNet(
      final DWVSerialProtocol protocol, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwvSerialProtocol = protocol;
    final DWCommandList commands = new DWCommandList(
        this.dwvSerialProtocol, this.dwvSerialProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdNetShow(protocol, this));
    this.setCommand("net");
    this.setShortHelp("Manage network connections");
    this.setUsage("dw net [command]");
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
