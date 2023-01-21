package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdTest extends DWCommand {

  public UICmdTest(DWUIClientThread dwuiClientThread) {
    this.getCommandList().addCommand(new UICmdTestDGraph(dwuiClientThread));
    this.setCommand("test");
    this.setShortHelp("Test commands");
    this.setUsage("ui test [command]");
  }

  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }

}