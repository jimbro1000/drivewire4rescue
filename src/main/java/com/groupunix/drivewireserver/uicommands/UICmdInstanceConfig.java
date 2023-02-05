package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceConfig extends DWCommand {
  /**
   * UI Command Instance Configuration.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceConfig(final DWUIClientThread clientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceConfigShow(clientThread));
    commands.addCommand(new UICmdInstanceConfigSet(clientThread));
    setHelp();
  }

  /**
   * UI Command Instance Configuration.
   *
   * @param protocol protocol
   */
  public UICmdInstanceConfig(final DWProtocol protocol) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdInstanceConfigShow(protocol));
    commands.addCommand(new UICmdInstanceConfigSet(protocol));
    setHelp();
  }

  private void setHelp() {
    this.setCommand("config");
    this.setShortHelp("Configuration commands");
    this.setUsage("ui instance config [command]");
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
