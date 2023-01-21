package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.dwcommands.*;

public class UICmdServerFile extends DWCommand {
  public UICmdServerFile() {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerFileRoots());
    commands.addCommand(new UICmdServerFileDefaultDir());
    commands.addCommand(new UICmdServerFileDir());
    commands.addCommand(new UICmdServerFileXDir());
    commands.addCommand(new UICmdServerFileInfo());
    this.setCommand("file");
    this.setShortHelp("File commands");
    this.setUsage("ui server file [command]");
  }
  public DWCommandResponse parse(String cmdline) {
    return (this.getCommandList().parse(cmdline));
  }

  public boolean validate(String cmdline) {
    return (this.getCommandList().validate(cmdline));
  }
}
