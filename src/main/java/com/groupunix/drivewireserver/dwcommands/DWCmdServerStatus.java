package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerStatus extends DWCommand {
  /**
   * Bytes is a kilobyte.
   */
  public static final int KILOBYTE_FACTOR = 1024;

  /**
   * Server status command constructor.
   * @param protocol
   * @param parent
   */
  public DWCmdServerStatus(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
  }

  /**
   * Get command.
   * @return command name
   */
  public String getCommand() {
    return "status";
  }

  /**
   * Get short help.
   * @return short help details
   */
  public String getShortHelp() {
    return "Show server status information";
  }

  /**
   * Get usage.
   * @return usage information
   */
  public String getUsage() {
    return "dw server status";
  }

  /**
   * Parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return (doServerStatus());
  }

  private DWCommandResponse doServerStatus() {
    String text = "DriveWire version "
        + DriveWireServer.DW_SERVER_VERSION
        + " ("
        + DriveWireServer.DW_SERVER_VERSION_DATE
        + ") status:\r\n\n"
        + "Total memory:  "
        + Runtime.getRuntime().totalMemory() / KILOBYTE_FACTOR
        + " KB"
        + "\r\nFree memory:   "
        + Runtime.getRuntime().freeMemory() / KILOBYTE_FACTOR
        + " KB"
        + "\r\n";
    return new DWCommandResponse(text);
  }

  /**
  * Validate command.
  * @param cmdline
  * @return true if valid
  */
  public boolean validate(final String cmdline) {
    return (true);
  }

}
