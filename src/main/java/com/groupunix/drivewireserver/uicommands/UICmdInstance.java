package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class UICmdInstance extends DWCommand {
  /**
   * UI Command Instance.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstance(final DWUIClientThread clientThread) {
    super();
    final DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceAttach(clientThread));
    commands.addCommand(new UICmdInstanceConfig(clientThread));
    commands.addCommand(new UICmdInstanceDisk(clientThread));
    commands.addCommand(new UICmdInstanceReset(clientThread));
    commands.addCommand(new UICmdInstanceStatus(clientThread));
    commands.addCommand(new UICmdInstanceMIDIStatus(clientThread));
    commands.addCommand(new UICmdInstancePrinterStatus(clientThread));
    commands.addCommand(new UICmdInstancePortStatus(clientThread));
    commands.addCommand(new UICmdInstanceTimer(clientThread));
    setHelp();
  }

  /**
   * UI Command Instance.
   *
   * @param protocol protocol
   */
  public UICmdInstance(final DWProtocol protocol) {
    super();
    final DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceConfig(protocol));
    if (protocol.hasDisks()) {
      commands.addCommand(new UICmdInstanceDisk((DWProtocolHandler) protocol));
    }
    commands.addCommand(new UICmdInstanceReset(protocol));
    commands.addCommand(new UICmdInstanceStatus(protocol));
    if (protocol.hasMIDI()) {
      commands.addCommand(new UICmdInstanceMIDIStatus(protocol));
    }
    if (protocol.hasPrinters()) {
      commands.addCommand(
          new UICmdInstancePrinterStatus((DWProtocolHandler) protocol)
      );
    }
    if (protocol.hasVSerial()) {
      commands.addCommand(
          new UICmdInstancePortStatus((DWVSerialProtocol) protocol)
      );
    }
    commands.addCommand(new UICmdInstanceTimer(protocol));
    setHelp();
  }

  private void setHelp() {
    this.setCommand("instance");
    this.setShortHelp("Instance commands");
    this.setUsage("ui instance [command]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
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
