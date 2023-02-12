package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceTimer extends DWCommand {
  /**
   * UI Command Instance Timer.
   *
   * @param protocol protocol
   */
  public UICmdInstanceTimer(final DWProtocol protocol) {
    super();
    final DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceTimerShow(protocol));
    commands.addCommand(new UICmdInstanceTimerReset(protocol));
    setHelp();
  }

  /**
   * UI Command Instance Timer.
   *
   * @param clientThread client thread reference
   */
  public UICmdInstanceTimer(final DWUIClientThread clientThread) {
    super();
    final DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceTimerShow(clientThread));
    commands.addCommand(new UICmdInstanceTimerReset(clientThread));
    setHelp();
  }

  private void setHelp() {
    this.setCommand("timer");
    this.setShortHelp("Timer commands");
    this.setUsage("ui instance timer [command]");
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
