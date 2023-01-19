package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class UICmdInstance extends DWCommand {

  static final String command = "instance";

  public UICmdInstance(DWUIClientThread dwuiClientThread) {
    commands.addCommand(new UICmdInstanceAttach(dwuiClientThread));
    commands.addCommand(new UICmdInstanceConfig(dwuiClientThread));
    commands.addCommand(new UICmdInstanceDisk(dwuiClientThread));
    commands.addCommand(new UICmdInstanceReset(dwuiClientThread));
    commands.addCommand(new UICmdInstanceStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstanceMIDIStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstancePrinterStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstancePortStatus(dwuiClientThread));
    commands.addCommand(new UICmdInstanceTimer(dwuiClientThread));
  }


  public UICmdInstance(DWProtocol dwProto) {
    commands.addCommand(new UICmdInstanceConfig(dwProto));
    if (dwProto.hasDisks())
      commands.addCommand(new UICmdInstanceDisk((DWProtocolHandler) dwProto));

    commands.addCommand(new UICmdInstanceReset(dwProto));
    commands.addCommand(new UICmdInstanceStatus(dwProto));

    if (dwProto.hasMIDI())
      commands.addCommand(new UICmdInstanceMIDIStatus(dwProto));

    if (dwProto.hasPrinters())
      commands.addCommand(new UICmdInstancePrinterStatus((DWProtocolHandler) dwProto));

    if (dwProto.hasVSerial())
      commands.addCommand(new UICmdInstancePortStatus((DWVSerialProtocol) dwProto));

    commands.addCommand(new UICmdInstanceTimer(dwProto));
  }


  public String getCommand() {
    return command;
  }

  public DWCommandResponse parse(String cmdline) {
    return (commands.parse(cmdline));
  }


  public String getShortHelp() {
    return "Instance commands";
  }


  public String getUsage() {
    return "ui instance [command]";
  }

  public boolean validate(String cmdline) {
    return (commands.validate(cmdline));
  }

}