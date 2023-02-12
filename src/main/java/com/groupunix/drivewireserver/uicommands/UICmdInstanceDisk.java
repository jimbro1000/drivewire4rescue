package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceDisk extends DWCommand {
  /**
   * UI Command Instance Disk.
   *
   * @param clientThread client thread ref.
   */
  public UICmdInstanceDisk(final DWUIClientThread clientThread) {
    super();
    this.getCommandList()
        .addCommand(new UICmdInstanceDiskShow(clientThread));
    setHelp();
  }

  /**
   * UI Command Instance Disk.
   *
   * @param protocolHandler protocol handler
   */
  public UICmdInstanceDisk(final DWProtocolHandler protocolHandler) {
    super();
    this.getCommandList()
        .addCommand(new UICmdInstanceDiskShow(protocolHandler));
    setHelp();
  }

  private void setHelp() {
    this.setCommand("disk");
    this.setShortHelp("Instance disk commands");
    this.setUsage("ui instance disk [command]");
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
