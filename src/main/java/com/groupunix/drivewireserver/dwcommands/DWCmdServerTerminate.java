package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdServerTerminate extends DWCommand {
  /**
   * Server terminate command constructor.
   *
   * @param parent parent command
   */
  public DWCmdServerTerminate(final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.setCommand("terminate");
    this.setShortHelp("Shut down server");
    this.setUsage("dw server terminate [force]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.equals("force")) {
      System.exit(1);
    }
    DriveWireServer.shutdown();
    return new DWCommandResponse("Server shutdown requested.");
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
