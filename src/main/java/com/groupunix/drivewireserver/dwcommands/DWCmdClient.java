package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public final class DWCmdClient extends DWCommand {
  /**
   * Drivewire serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;

  /**
   * Client command constructor.
   *
   * @param protocol serial protocol
   * @param parent   parent command
   */
  public DWCmdClient(
      final DWVSerialProtocol protocol, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwvSerialProtocol = protocol;
    final DWCommandList commands = new DWCommandList(
        this.dwvSerialProtocol, this.dwvSerialProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdClientRestart(protocol, this));
    this.setCommand("client");
    this.setShortHelp("Commands that manage the attached client device");
    this.setUsage("dw client [command]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          this.getCommandList().getShortHelp()
      );
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
