package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServer extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Server command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdServer(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    DWCommandList commands = new DWCommandList(
        this.dwProtocol, this.dwProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdServerStatus(protocol, this));
    commands.addCommand(new DWCmdServerShow(protocol, this));
    commands.addCommand(new DWCmdServerList(this));
    commands.addCommand(new DWCmdServerDir(this));
    commands.addCommand(new DWCmdServerTerminate(this));
    commands.addCommand(new DWCmdServerTurbo(protocol, this));
    commands.addCommand(new DWCmdServerPrint(protocol, this));
    commands.addCommand(new DWCmdServerHelp(protocol, this));
    this.setCommand("server");
    this.setShortHelp("Various server based tools");
    this.setUsage("dw server [command]");
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
