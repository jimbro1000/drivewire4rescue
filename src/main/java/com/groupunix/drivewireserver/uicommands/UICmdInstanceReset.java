package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceReset extends DWCommand {
  public UICmdInstanceReset(DWUIClientThread dwuiClientThread) {
    this.getCommandList().addCommand(new UICmdInstanceResetProtodev(dwuiClientThread));
    setHelp();
  }

  public UICmdInstanceReset(DWProtocol dwProto) {
    this.getCommandList().addCommand(new UICmdInstanceResetProtodev(dwProto));
    setHelp();
  }

  private void setHelp() {
    this.setCommand("reset");
    this.setShortHelp("Restart commands");
    this.setUsage("ui instance reset [command]");
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }

}