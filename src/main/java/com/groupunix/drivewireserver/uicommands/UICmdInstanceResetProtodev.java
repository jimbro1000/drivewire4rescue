package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceResetProtodev extends DWCommand {
  /**
   * Client thread ref.
   */
  private final DWUIClientThread dwuiClientThread;
  /**
   * Protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * UI Command Instance Reset Protocol Device.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceResetProtodev(final DWUIClientThread clientThread) {
    super();
    this.dwuiClientThread = clientThread;
    this.dwProtocol = null;
    setHelp();
  }

  /**
   * UI Command Instance Reset Protocol Device.
   *
   * @param protocol protocol
   */
  public UICmdInstanceResetProtodev(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    this.dwuiClientThread = null;
    setHelp();
  }

  private void setHelp() {
    setCommand("protodev");
    setShortHelp("Reset protocol device");
    setUsage("ui instance reset protodev");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    String res = "Resetting protocol device in instance ";
    if (this.dwuiClientThread != null) {
      res += this.dwuiClientThread.getInstance();
      DriveWireServer.getHandler(
          this.dwuiClientThread.getInstance()
      ).resetProtocolDevice();
    } else {
      res += this.dwProtocol.getHandlerNo();
      dwProtocol.resetProtocolDevice();
    }
    return new DWCommandResponse(res);
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
