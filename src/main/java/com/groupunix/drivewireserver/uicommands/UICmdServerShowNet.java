package com.groupunix.drivewireserver.uicommands;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowNet extends DWCommand {
  /**
   * UI Command Server Show Net.
   */
  public UICmdServerShowNet() {
    super();
    setCommand("net");
    setShortHelp("show available network interfaces");
    setUsage("ui server show net");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder res = new StringBuilder();
    try {
      final Enumeration<NetworkInterface> nets
          = NetworkInterface.getNetworkInterfaces();
      for (final NetworkInterface netint : Collections.list(nets)) {
        final Enumeration<InetAddress> inetAddresses
            = netint.getInetAddresses();
        for (final InetAddress inetAddress : Collections.list(inetAddresses)) {
          res.append(inetAddress.getHostAddress())
              .append("|")
              .append(netint.getName())
              .append("|")
              .append(netint.getDisplayName())
              .append("\r\n");
        }
      }
    } catch (SocketException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_NET_IO_ERROR,
          e.getMessage()
      );
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
