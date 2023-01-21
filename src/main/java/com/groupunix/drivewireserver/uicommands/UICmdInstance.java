package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.*;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class UICmdInstance extends DWCommand {
  public UICmdInstance(DWUIClientThread dwuiClientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceAttach(dwuiClientThread));
    commands.addCommand(new UICmdInstanceConfig(dwuiClientThread));
    commands.addCommand(new UICmdInstanceDisk(dwuiClientThread));
    commands.addCommand(new UICmdInstanceReset(dwuiClientThread));
    commands.addCommand(new UICmdInstanceStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstanceMIDIStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstancePrinterStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstancePortStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstanceTimer(dwuiClientThread));
    this.setCommand("instance");
    this.setShortHelp("Instance commands");
    this.setUsage("ui instance [command]");
  }

  public UICmdInstance(DWProtocol dwProto) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceConfig(dwProto));
    if (dwProto.hasDisks()) {
      commands.addCommand(new UICmdInstanceDisk((DWProtocolHandler) dwProto));
    }

    commands.addCommand(new UICmdInstanceReset(dwProto));
    commands.addCommand(new UICmdInstanceStatus(dwProto));

    if (dwProto.hasMIDI()) {
      commands.addCommand(new UICmdInstanceMIDIStatus(dwProto));
    }

    if (dwProto.hasPrinters()) {
      commands.addCommand(new UICmdInstancePrinterStatus((DWProtocolHandler) dwProto));
    }

    if (dwProto.hasVSerial()) {
      commands.addCommand(new UICmdInstancePortStatus((DWVSerialProtocol) dwProto));
    }

    commands.addCommand(new UICmdInstanceTimer(dwProto));
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }


  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }

}