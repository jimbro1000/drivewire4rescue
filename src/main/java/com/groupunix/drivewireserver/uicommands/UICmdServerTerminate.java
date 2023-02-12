package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerTerminate extends DWCommand {
  /**
   * Client thread ref.
   */
  @SuppressWarnings("unused")
  private final DWUIClientThread dwuiClientThread;
  /**
   * Protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * UI Command Server Terminate.
   *
   * @param clientThread client thread ref
   */
  public UICmdServerTerminate(final DWUIClientThread clientThread) {
    super();
    this.dwuiClientThread = clientThread;
    this.dwProtocol = null;
    setHelp();
  }

  /**
   * UI Command Server Terminate.
   *
   * @param protocol protocol
   */
  public UICmdServerTerminate(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    this.dwuiClientThread = null;
    setHelp();
  }

  private void setHelp() {
    setCommand("terminate");
    setShortHelp("Terminate the server");
    setUsage("ui server terminate");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    DriveWireServer.shutdown();
    return new DWCommandResponse("Server shutdown requested.");
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
