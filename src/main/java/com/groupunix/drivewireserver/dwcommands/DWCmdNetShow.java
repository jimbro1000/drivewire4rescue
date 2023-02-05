package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import com.groupunix.drivewireserver.virtualserial.DWVPortListenerPool;

public final class DWCmdNetShow extends DWCommand {
  /**
   * drivewire serial protocol.
   */
  private final DWVSerialProtocol dwProtocol;

  /**
   * net show command constructor.
   * @param protocol protocol
   * @param parent command parent
   */
  public DWCmdNetShow(
      final DWVSerialProtocol protocol,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("show");
    this.setShortHelp("Show networking status");
    this.setUsage("dw net show");
  }

  /**
   * parse command.
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doNetShow();
  }

  private DWCommandResponse doNetShow() {
    StringBuilder text = new StringBuilder();

    text.append("\r\nDriveWire Network Connections:\r\n\n");
    for (int i = 0; i < DWVPortListenerPool.MAX_CONN; i++) {
      try {
        text.append("Connection ")
            .append(i)
            .append(": ")
            .append(dwProtocol
                .getVPorts()
                .getListenerPool()
                .getConn(i)
                .socket()
                .getInetAddress()
                .getHostName()
            )
            .append(":")
            .append(
                dwProtocol
                    .getVPorts()
                    .getListenerPool()
                    .getConn(i)
                    .socket()
                    .getPort()
            )
            .append(" (connected to port ")
            .append(dwProtocol
                .getVPorts()
                .prettyPort(dwProtocol
                    .getVPorts()
                    .getListenerPool()
                    .getConnPort(i)
                )
            )
            .append(")\r\n");
      } catch (DWConnectionNotValidException e) {
        // text += e.getMessage();
      }
    }
    text.append("\r\n");
    for (int i = 0; i < DWVPortListenerPool.MAX_LISTEN; i++) {
      if (dwProtocol.getVPorts().getListenerPool().getListener(i) != null) {
        text.append("Listener ")
            .append(i)
            .append(": TCP port ")
            .append(dwProtocol
                .getVPorts()
                .getListenerPool()
                .getListener(i)
                .socket()
                .getLocalPort()
            )
            .append(" (control port ")
            .append(dwProtocol
                .getVPorts()
                .prettyPort(dwProtocol
                    .getVPorts()
                    .getListenerPool()
                    .getListenerPort(i)
                )
            )
            .append(")\r\n");
      }
    }
    return new DWCommandResponse(text.toString());
  }

  /**
   * validate command.
   * @param cmdline command string
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
