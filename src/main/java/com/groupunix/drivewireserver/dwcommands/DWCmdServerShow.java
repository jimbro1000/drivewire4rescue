package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;


public class DWCmdServerShow extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Server show command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdServerShow(final DWProtocol protocol, final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    final DWCommandList commands = new DWCommandList(
        this.dwProtocol, this.dwProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdServerShowThreads(this));
    commands.addCommand(new DWCmdServerShowTimers(this.dwProtocol, this));
    commands.addCommand(new DWCmdServerShowSerial(this.dwProtocol, this));
    this.setCommand("show");
    this.setShortHelp("Show various server information");
    this.setUsage("dw server show [option]");
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
