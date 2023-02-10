package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdConfig extends DWCommand {
  /**
   * drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * command config constructor.
   * @param protocol protocol
   * @param parent command parent
   */
  public DWCmdConfig(final DWProtocol protocol, final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    final DWCommandList commands = new DWCommandList(
        this.dwProtocol, this.dwProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdConfigShow(protocol, this));
    commands.addCommand(new DWCmdConfigSet(protocol, this));
    commands.addCommand(new DWCmdConfigSave(protocol, this));
    // save/load not implemented here
    this.setCommand("config");
    this.setShortHelp("Commands to manipulate the config");
    this.setUsage("dw config [command]");
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
   * validate command.
   * @param cmdline command line
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
