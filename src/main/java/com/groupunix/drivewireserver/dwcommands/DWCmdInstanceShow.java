package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

import java.util.Objects;

public final class DWCmdInstanceShow extends DWCommand {
  /**
   * Protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * Command instance constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdInstanceShow(final DWProtocol protocol, final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("show");
    this.setShortHelp("Show instance status");
    this.setUsage("dw instance show");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder text = new StringBuilder();
    text.append("DriveWire protocol handler instances:\r\n\n");
    for (int i = 0; i < DriveWireServer.getNumHandlers(); i++) {
      text.append("#").append(i).append("  (");
      if (Objects.requireNonNull(DriveWireServer.getHandler(i)).isDying()) {
        text.append("Dying..)   ");
      } else if (Objects.requireNonNull(DriveWireServer.getHandler(i))
          .isReady()) {
        text.append("Ready)     ");
      } else if (Objects.requireNonNull(DriveWireServer.getHandler(i))
          .isStarted()) {
        text.append("Starting)  ");
      } else {
        text.append("Not ready) ");
      }
      if (DriveWireServer.getHandler(i) == null) {
        text.append(" Null (?)\r\n");
      } else {
        final String proto = Objects.requireNonNull(DriveWireServer
            .getHandler(i))
            .getConfig()
            .getString("Protocol", "DriveWire");
        text.append(String.format("Proto: %-11s", proto));
        final String dwType = Objects.requireNonNull(DriveWireServer
            .getHandler(i))
            .getConfig()
            .getString("DeviceType", "Unknown");
        if (dwType.equals("serial") || proto.equals("VModem")) {
          text.append(String.format("Type: %-11s", "serial"));
          text.append(" Dev: ")
              .append(Objects.requireNonNull(DriveWireServer.getHandler(i))
              .getConfig()
              .getString("SerialDevice", "Unknown"));
        } else if (dwType.equals("tcp-server")) {
          text.append(String.format("Type: %-11s", "tcp-server"));
          text.append("Port: ")
              .append(Objects.requireNonNull(DriveWireServer.getHandler(i))
              .getConfig()
              .getString("TCPServerPort", "Unknown"));
        } else if (dwType.equals("tcp-client")) {
          text.append(String.format("Type: %-11s", dwType));
          text.append("Host: ")
              .append(Objects.requireNonNull(DriveWireServer.getHandler(i))
              .getConfig()
              .getString("TCPClientHost", "Unknown"))
              .append(":")
              .append(Objects.requireNonNull(DriveWireServer.getHandler(i))
              .getConfig()
              .getString("TCPClientPort", "Unknown"));
        }
        text.append("\r\n");
      }
    }
    return new DWCommandResponse(text.toString());
  }

  /**
   * Validate command.
   *
   * @param cmdline command string
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
