package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDisk extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent          parent command
   */
  public DWCmdDisk(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;

    final DWCommandList commands = new DWCommandList(
        this.dwProtocolHandler, this.dwProtocolHandler.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdDiskShow(protocolHandler, this));
    commands.addCommand(new DWCmdDiskEject(protocolHandler, this));
    commands.addCommand(new DWCmdDiskInsert(protocolHandler, this));
    commands.addCommand(new DWCmdDiskReload(protocolHandler, this));
    commands.addCommand(new DWCmdDiskWrite(protocolHandler, this));
    commands.addCommand(new DWCmdDiskCreate(protocolHandler, this));
    commands.addCommand(new DWCmdDiskSet(protocolHandler, this));
    commands.addCommand(new DWCmdDiskDos(protocolHandler, this));
    this.setCommand("disk");
    this.setShortHelp("Manage disks and disksets");
    this.setUsage("dw disk [command]");
  }

  /**
   * parse command.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.getCommandList().getShortHelp()));
    }
    return (this.getCommandList().parse(cmdline));
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
