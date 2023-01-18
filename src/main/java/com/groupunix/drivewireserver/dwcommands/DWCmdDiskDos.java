package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdDiskDos extends DWCommand {
  /**
   * component commands.
   */
  private final DWCommandList commands;
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk Dos command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskDos(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    commands = new DWCommandList(
        this.dwProtocolHandler,
        this.dwProtocolHandler.getCMDCols()
    );
    commands.addcommand(new DWCmdDiskDosDir(protocolHandler, this));
    commands.addcommand(new DWCmdDiskDosList(protocolHandler, this));
    commands.addcommand(new DWCmdDiskDosFormat(protocolHandler, this));
    commands.addcommand(new DWCmdDiskDosAdd(protocolHandler, this));
    commandName = "dos";
    shortHelp = "Manage DOS disks";
    usage = "dw disk dos [command]";
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.commands.getShortHelp()));
    }
    return commands.parse(cmdline);
  }

  /**
   * get commands.
   *
   * @return component command list
   */
  public DWCommandList getCommandList() {
    return this.commands;
  }

  /**
   * validate command.
   *
   * @param cmdline command string
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return (commands.validate(cmdline));
  }
}
