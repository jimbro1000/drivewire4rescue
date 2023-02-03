package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdTest extends DWCommand {
  /**
   * UI Command Test.
   *
   * @param clientThread client thread reference
   */
  public UICmdTest(final DWUIClientThread clientThread) {
    this.getCommandList().addCommand(new UICmdTestDGraph(clientThread));
    this.setCommand("test");
    this.setShortHelp("Test commands");
    this.setUsage("ui test [command]");
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
