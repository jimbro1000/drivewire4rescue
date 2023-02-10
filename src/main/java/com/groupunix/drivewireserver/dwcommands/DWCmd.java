package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public final class DWCmd extends DWCommand {
  /**
   * command protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Drivewire command constructor.
   *
   * @param protocol protocol
   */
  public DWCmd(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    final DWCommandList commands = new DWCommandList(
        this.dwProtocol,
        this.dwProtocol.getCMDCols()
    );
    this.setCommandList(commands);
    commands.addCommand(new DWCmdServer(protocol, this));
    commands.addCommand(new DWCmdConfig(protocol, this));
    commands.addCommand(new DWCmdLog(protocol, this));
    commands.addCommand(new DWCmdInstance(protocol, this));
    if (this.dwProtocol.hasDisks()) {
      commands.addCommand(
          new DWCmdDisk((DWProtocolHandler) protocol, this)
      );
    }
    if (this.dwProtocol.hasVSerial()) {
      commands.addCommand(
          new DWCmdPort((DWVSerialProtocol) protocol, this)
      );
      commands.addCommand(
          new DWCmdNet((DWVSerialProtocol) protocol, this)
      );
      commands.addCommand(
          new DWCmdClient((DWVSerialProtocol) protocol, this)
      );
    }

    if (this.dwProtocol.hasMIDI()) {
      commands.addCommand(
          new DWCmdMidi((DWProtocolHandler) protocol, this)
      );
    }
    this.setCommand("dw");
    this.setShortHelp("Manage all aspects of the server");
    this.setUsage("dw [command]");
  }

  /**
   * parse command.
   *
   * @param cmdline command string
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
   *
   * @param cmdline command string
   * @return true if command is valid
   */
  public boolean validate(final String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
