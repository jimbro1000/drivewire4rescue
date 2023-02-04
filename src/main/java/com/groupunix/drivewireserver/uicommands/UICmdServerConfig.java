package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerConfig extends DWCommand {
  /**
   * UI Command Server Configuration.
   *
   * @param ignoredClientThread not used
   */
  public UICmdServerConfig(final DWUIClientThread ignoredClientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerConfigShow());
    commands.addCommand(new UICmdServerConfigSet());
    commands.addCommand(new UICmdServerConfigSerial());
    commands.addCommand(new UICmdServerConfigWrite());
    commands.addCommand(new UICmdServerConfigFreeze());
    setHelp();
  }

  /**
   * UI Command Server Configuration.
   *
   * @param ignoredProtocol not used
   */
  public UICmdServerConfig(final DWProtocol ignoredProtocol) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerConfigShow());
    commands.addCommand(new UICmdServerConfigSet());
    commands.addCommand(new UICmdServerConfigWrite());
    setHelp();
  }

  private void setHelp() {
    this.setCommand("config");
    this.setShortHelp("Configuration commands");
    this.setUsage("ui server config [command]");
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
