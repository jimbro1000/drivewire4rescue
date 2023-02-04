package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class UICmdInstancePortStatus extends DWCommand {
  /**
   * Client thread ref.
   */
  private DWUIClientThread dwuiClientThread = null;
  /**
   * Serial protocol.
   */
  private DWVSerialProtocol dwvSerialProtocol;

  /**
   * UI Command Instance Port Status.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstancePortStatus(final DWUIClientThread clientThread) {
    this.dwuiClientThread = clientThread;
    setHelp();
  }

  /**
   * UI Command Instance Port Status.
   *
   * @param protocol protocol
   */
  public UICmdInstancePortStatus(final DWVSerialProtocol protocol) {
    this.dwvSerialProtocol = protocol;
    setHelp();
  }

  private void setHelp() {
    setCommand("portstatus");
    setShortHelp("show port status");
    setUsage("ui instance portstatus");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder res = new StringBuilder();
    if (this.dwvSerialProtocol == null) {
      if (
          (DriveWireServer.isValidHandlerNo(this.dwuiClientThread.getInstance())
              && (DriveWireServer.getHandler(
              this.dwuiClientThread.getInstance()).hasVSerial())
          )
      ) {
        dwvSerialProtocol = (DWVSerialProtocol) DriveWireServer
            .getHandler(this.dwuiClientThread.getInstance());
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INSTANCE_WONT,
            "The operation is not supported by this instance"
        );
      }
    }
    if (!(dwvSerialProtocol == null)
        && !(dwvSerialProtocol.getVPorts() == null)) {
      for (int p = 0; p < dwvSerialProtocol.getVPorts().getMaxPorts(); p++) {
        if (!dwvSerialProtocol.getVPorts().isNull(p)) {
          try {
            res.append(dwvSerialProtocol.getVPorts().prettyPort(p))
                .append("|");
            if (dwvSerialProtocol.getVPorts().isOpen(p)) {
              res.append("open|");
              res.append(dwvSerialProtocol.getVPorts().getOpen(p)).append("|");
              res.append(dwvSerialProtocol.getVPorts().getUtilMode(p))
                  .append("|");
              res.append(
                  DWUtils.prettyUtilMode(
                      dwvSerialProtocol.getVPorts().getUtilMode(p)
                  )
              ).append("|");
              res.append(dwvSerialProtocol.getVPorts().bytesWaiting(p))
                  .append("|");
              res.append(dwvSerialProtocol.getVPorts().getConn(p)).append("|");
              if (dwvSerialProtocol.getVPorts().getConn(p) > -1) {
                try {
                  res.append(dwvSerialProtocol.getVPorts().getHostIP(p))
                      .append("|");
                  res.append(dwvSerialProtocol.getVPorts().getHostPort(p))
                      .append("|");
                } catch (DWConnectionNotValidException e) {
                  res.append("||");
                }
              } else {
                res.append("||");
              }
              res.append(new String(dwvSerialProtocol.getVPorts().getDD(p)))
                  .append("|");
            } else {
              res.append("closed|");
            }
          } catch (DWPortNotValidException e) {
            e.printStackTrace();
          }
          res.append("\r\n");
        }
      }
    }
    return new DWCommandResponse(res.toString());
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
