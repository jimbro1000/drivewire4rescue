package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerFile extends DWCommand {

  static final String command = "file";


  public UICmdServerFile() {

    commands.addCommand(new UICmdServerFileRoots());
    commands.addCommand(new UICmdServerFileDefaultDir());
    commands.addCommand(new UICmdServerFileDir());
    commands.addCommand(new UICmdServerFileXDir());
    commands.addCommand(new UICmdServerFileInfo());
  }


  public String getCommand() {
    return command;
  }

  public DWCommandResponse parse(String cmdline) {
    return (commands.parse(cmdline));
  }


  public String getShortHelp() {
    return "File commands";
  }


  public String getUsage() {
    return "ui server file [command]";
  }

  public boolean validate(String cmdline) {
    return (commands.validate(cmdline));
  }

}