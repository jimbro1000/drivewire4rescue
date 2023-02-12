package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdInstanceStatus extends DWCommand {
  /**
   * Client thread ref.
   */
  private final DWUIClientThread clientRef;
  /**
   * Protocol.
   */
  private DWProtocol dwProtocol;

  /**
   * UI Command Instance Status.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceStatus(final DWUIClientThread clientThread) {
    super();
    this.clientRef = clientThread;
    this.dwProtocol = null;
    setHelp();
  }

  /**
   * UI Command Instance Status.
   *
   * @param protocol protocol
   */
  public UICmdInstanceStatus(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    this.clientRef = null;
    setHelp();
  }

  private void setHelp() {
    setCommand("status");
    setShortHelp("show instance status");
    setUsage("ui instance status");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    int hno = 0;
    if (cmdline.length() > 0) {
      try {
        hno = Integer.parseInt(cmdline);
        if (DriveWireServer.isValidHandlerNo(hno)) {
          dwProtocol = DriveWireServer.getHandler(hno);
        } else {
          return new DWCommandResponse(
              false,
              DWDefs.RC_INVALID_HANDLER,
              "Invalid handler number"
          );
        }
      } catch (NumberFormatException ne) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SYNTAX_ERROR,
            "Syntax error: non numeric instance #"
        );
      }
    } else {
      if (this.clientRef != null) {
        dwProtocol = DriveWireServer.getHandler(clientRef.getInstance());
        hno = this.clientRef.getInstance();
      }
    }
    String txt = "";
    txt = "num|" + hno + "\n";
    txt += "name|" + dwProtocol.getConfig()
        .getString("[@name]", "not set") + "\n";
    txt += "desc|" + dwProtocol.getConfig()
        .getString("[@desc]", "not set") + "\n";
    txt += "proto|" + dwProtocol.getConfig()
        .getString("Protocol", "DriveWire") + "\n";
    txt += "autostart|" + dwProtocol.getConfig()
        .getBoolean("AutoStart", true) + "\n";
    txt += "dying|" + dwProtocol.isDying() + "\n";
    txt += "started|" + dwProtocol.isStarted() + "\n";
    txt += "ready|" + dwProtocol.isReady() + "\n";
    txt += "connected|" + dwProtocol.isConnected() + "\n";
    if (dwProtocol.getProtoDev() != null) {
      txt += "devicetype|" + dwProtocol.getProtoDev().getDeviceType() + "\n";
      txt += "devicename|" + dwProtocol.getProtoDev().getDeviceName() + "\n";
      txt += "deviceconnected|" + dwProtocol.getProtoDev().connected() + "\n";
      if (dwProtocol.getProtoDev().getRate() > -1) {
        txt += "devicerate|" + dwProtocol.getProtoDev().getRate() + "\n";
      }
      if (dwProtocol.getProtoDev().getClient() != null) {
        txt += "deviceclient|" + dwProtocol.getProtoDev().getClient() + "\n";
      }
    }
    if (
        dwProtocol.getConfig()
            .getString("Protocol", "DriveWire")
            .equals("DriveWire")
    ) {
      final DWProtocolHandler dwProto = (DWProtocolHandler) dwProtocol;
      txt += "lastopcode|" + DWUtils.prettyOP(dwProto.getLastOpcode()) + "\n";
      txt += "lastgetstat|" + DWUtils.prettySS(dwProto.getLastGetStat()) + "\n";
      txt += "lastsetstat|" + DWUtils.prettySS(dwProto.getLastSetStat()) + "\n";
      txt += "lastlsn|" + DWUtils.int3(dwProto.getLastLSN()) + "\n";
      txt += "lastdrive|" + dwProto.getLastDrive() + "\n";
      txt += "lasterror|" + dwProto.getLastError() + "\n";
      txt += "lastchecksum|" + dwProto.getLastChecksum() + "\n";
    }
    return new DWCommandResponse(txt);
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
