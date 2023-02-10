package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerStatus extends DWCommand {
  /**
   * Bytes is a kilobyte.
   */
  public static final int KILOBYTE_FACTOR = 1024;
  /**
   * Protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * Server status command constructor.
   *
   * @param protocol protocol
   * @param parent   parent command
   */
  public DWCmdServerStatus(final DWProtocol protocol, final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("status");
    this.setShortHelp("Show server status information");
    this.setUsage("dw server status");
  }

  /**
   * Parse command.
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doServerStatus();
  }

  private DWCommandResponse doServerStatus() {
    final String text = "DriveWire version "
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
   * @param cmdline command line
   * @return true if valid
  */
  public boolean validate(final String cmdline) {
    return true;
  }
}
