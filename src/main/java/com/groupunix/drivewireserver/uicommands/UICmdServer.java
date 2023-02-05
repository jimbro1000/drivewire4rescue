package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServer extends DWCommand {
  /**
   * UI Command Server.
   *
   * @param clientThread client thread ref
   */
  public UICmdServer(final DWUIClientThread clientThread) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerShow(clientThread));
    commands.addCommand(new UICmdServerConfig(clientThread));
    commands.addCommand(new UICmdServerTerminate(clientThread));
    commands.addCommand(new UICmdServerFile());
    setHelp();
  }

  /**
   * UI Command Server.
   *
   * @param protocol protocol
   */
  public UICmdServer(final DWProtocol protocol) {
    DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerShow(protocol));
    commands.addCommand(new UICmdServerConfig(protocol));
    commands.addCommand(new UICmdServerTerminate(protocol));
    commands.addCommand(new UICmdServerFile());
    setHelp();
  }

  private void setHelp() {
    this.setCommand("server");
    this.setShortHelp("Server commands");
    this.setUsage("ui server [command]");
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
