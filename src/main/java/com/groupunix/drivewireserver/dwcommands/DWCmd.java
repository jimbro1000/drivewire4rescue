package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public final class DWCmd extends DWCommand {
  /**
   * command name.
   */
  static final String COMMAND = "dw";
  /**
   * component command list.
   */
  private final DWCommandList commands;
  /**
   * command protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * drivewire command constructor.
   * @param protocol
   */
  public DWCmd(final DWProtocol protocol) {
    this.dwProtocol = protocol;

    commands = new DWCommandList(this.dwProtocol, this.dwProtocol.getCMDCols());

    commands.addcommand(new DWCmdServer(protocol, this));
    commands.addcommand(new DWCmdConfig(protocol, this));
    commands.addcommand(new DWCmdLog(protocol, this));
    commands.addcommand(new DWCmdInstance(protocol, this));

    if (this.dwProtocol.hasDisks()) {
      commands.addcommand(
          new DWCmdDisk((DWProtocolHandler) protocol, this)
      );
    }

    if (this.dwProtocol.hasVSerial()) {
      commands.addcommand(
          new DWCmdPort((DWVSerialProtocol) protocol, this)
      );
      commands.addcommand(
          new DWCmdNet((DWVSerialProtocol) protocol, this)
      );
      commands.addcommand(
          new DWCmdClient((DWVSerialProtocol) protocol, this)
      );
    }

    if (this.dwProtocol.hasMIDI()) {
      commands.addcommand(
          new DWCmdMidi((DWProtocolHandler) protocol, this)
      );
    }
  }

  /**
   * get command.
   * @return command name
   */
  public String getCommand() {
    return COMMAND;
  }

  /**
   * get component commands.
   * @return all component commands
   */
  public DWCommandList getCommandList() {
    return (this.commands);
  }

  /**
   * parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.commands.getShortHelp()));
    }
    return (commands.parse(cmdline));
  }

  /**
   * Get short help.
   * @return short help details
   */
  public String getShortHelp() {
    return "Manage all aspects of the server";
  }

  /**
   * Get usage.
   * @return usage information
   */
  public String getUsage() {
    return "dw [command]";
  }

  /**
   * validate command.
   * @param cmdline
   * @return true if command is valid
   */
  public boolean validate(final String cmdline) {
    return (commands.validate(cmdline));
  }
}
