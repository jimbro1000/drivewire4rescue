package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceReset extends DWCommand {
  /**
   * UI Command Instance Reset.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceReset(final DWUIClientThread clientThread) {
    this.getCommandList()
        .addCommand(new UICmdInstanceResetProtodev(clientThread));
    setHelp();
  }

  /**
   * UI Command Instance Reset.
   *
   * @param protocol protocol
   */
  public UICmdInstanceReset(final DWProtocol protocol) {
    this.getCommandList()
        .addCommand(new UICmdInstanceResetProtodev(protocol));
    setHelp();
  }

  private void setHelp() {
    this.setCommand("reset");
    this.setShortHelp("Restart commands");
    this.setUsage("ui instance reset [command]");
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
