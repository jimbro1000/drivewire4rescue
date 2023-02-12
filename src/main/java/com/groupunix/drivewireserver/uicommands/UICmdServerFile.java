package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerFile extends DWCommand {
  /**
   * UI Command Server File.
   */
  public UICmdServerFile() {
    super();
    final DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerFileRoots());
    commands.addCommand(new UICmdServerFileDefaultDir());
    commands.addCommand(new UICmdServerFileDir());
    commands.addCommand(new UICmdServerFileXDir());
    commands.addCommand(new UICmdServerFileInfo());
    this.setCommand("file");
    this.setShortHelp("File commands");
    this.setUsage("ui server file [command]");
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
