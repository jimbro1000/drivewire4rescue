package com.groupunix.drivewireserver.virtualserial.api;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import com.groupunix.drivewireserver.virtualserial.DWVPortTCPConnectionThread;
import com.groupunix.drivewireserver.virtualserial.DWVPortTCPListenerThread;
import com.groupunix.drivewireserver.virtualserial.DWVPortTCPServerThread;

public class DWAPITCP {
  /**
   * Start of actual arguments in command line.
   */
  private static final int ARG_START = 3;
  /**
   * Arguments needed for connect command.
   */
  private static final int CONNECT_ARGS_LEN = 4;
  /**
   * Arguments needed for listen command.
   */
  private static final int LISTEN_ARGS_LEN_MIN = 3;
  /**
   * Arguments needed for listentelnet command.
   */
  private static final int LISTEN_TELNET_ARGS_LEN = 3;
  /**
   * Arguments needed for join command.
   */
  private static final int JOIN_ARGS_LEN = 3;
  /**
   * Arguments needed for kill command.
   */
  private static final int KILL_ARGS_LEN = 3;
  /**
   * PORT argument index.
   */
  private static final int CONNECT_PORT_ARG_INDEX = 3;
  /**
   * HOST argument index.
   */
  private static final int CONNECT_HOST_ARG_INDEX = 2;
  /**
   * Command arguments.
   */
  private final String[] cmdParts;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;
  /**
   * Virtual port.
   */
  private final int virtualPort;

  /**
   * API TCP.
   *
   * @param cmd            command arguments.
   * @param serialProtocol serial protocol
   * @param port           port
   */
  public DWAPITCP(final String[] cmd,
                  final DWVSerialProtocol serialProtocol,
                  final int port) {
    this.dwvSerialProtocol = serialProtocol;
    this.virtualPort = port;
    this.cmdParts = cmd;
  }

  /**
   * Process command.
   *
   * @return command response
   */
  public DWCommandResponse process() {
    if ((cmdParts.length == CONNECT_ARGS_LEN)
        && (cmdParts[1].equalsIgnoreCase("connect"))) {
      return doTCPConnect(
          cmdParts[CONNECT_HOST_ARG_INDEX], cmdParts[CONNECT_PORT_ARG_INDEX]
      );
    } else if ((cmdParts.length >= LISTEN_ARGS_LEN_MIN)
        && (cmdParts[1].equalsIgnoreCase("listen"))) {
      return doTCPListen(cmdParts);
    } else if ((cmdParts.length == LISTEN_TELNET_ARGS_LEN)
        && (cmdParts[1].equalsIgnoreCase("listentelnet"))) {
      // old
      return doTCPListen(cmdParts[2], 1);
    } else if ((cmdParts.length == JOIN_ARGS_LEN)
        && (cmdParts[1].equalsIgnoreCase("join"))) {
      return doTCPJoin(cmdParts[2]);
    } else if ((cmdParts.length == KILL_ARGS_LEN)
        && (cmdParts[1].equalsIgnoreCase("kill"))) {
      return doTCPKill(cmdParts[2]);
    }
    return new DWCommandResponse(
        false,
        DWDefs.RC_SYNTAX_ERROR,
        "Syntax error in TCP command"
    );
  }

  private DWCommandResponse doTCPJoin(final String constr) {
    int conno;

    try {
      conno = Integer.parseInt(constr);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "non-numeric port in tcp join command"
      );
    }
    try {
      this.dwvSerialProtocol.getVPorts().getListenerPool().validateConn(conno);
      // start TCP thread
      Thread utilthread = new Thread(new DWVPortTCPServerThread(
          this.dwvSerialProtocol, this.virtualPort, conno
      ));
      utilthread.start();
      return new DWCommandResponse("attaching to connection " + conno);
    } catch (DWConnectionNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_NET_INVALID_CONNECTION,
          "invalid connection number"
      );
    }
  }

  private DWCommandResponse doTCPKill(final String constr) {
    int conno;

    try {
      conno = Integer.parseInt(constr);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "non-numeric port in tcp kill command"
      );
    }
    // close socket
    try {
      this.dwvSerialProtocol.getVPorts().getListenerPool().killConn(conno);
      return new DWCommandResponse("killed connection " + conno);
    } catch (DWConnectionNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_NET_INVALID_CONNECTION,
          "invalid connection number"
      );
    }
  }

  private DWCommandResponse doTCPConnect(final String tcphost,
                                         final String tcpportstr) {
    int tcpport;
    // get port #
    try {
      tcpport = Integer.parseInt(tcpportstr);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "non-numeric port in tcp connect command"
      );
    }
    // start TCP thread
    Thread utilthread = new Thread(new DWVPortTCPConnectionThread(
        this.dwvSerialProtocol, this.virtualPort, tcphost, tcpport
    ));
    utilthread.start();
    return new DWCommandResponse("connected to " + tcphost + ":" + tcpportstr);
  }

  private DWCommandResponse doTCPListen(final String strport, final int mode) {
    int tcpport;
    // get port #
    try {
      tcpport = Integer.parseInt(strport);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "non-numeric port in tcp listen command"
      );
    }
    DWVPortTCPListenerThread listener = new DWVPortTCPListenerThread(
        this.dwvSerialProtocol, this.virtualPort, tcpport
    );
    // simulate old behavior
    listener.setMode(mode);
    listener.setDoBanner(true);
    listener.setDoTelnet(true);
    // start TCP listener thread
    Thread listenThread = new Thread(listener);
    listenThread.start();
    return new DWCommandResponse("listening on port " + tcpport);
  }

  private DWCommandResponse doTCPListen(final String[] cmdparts) {
    int tcpport;
    // get port #
    try {
      tcpport = Integer.parseInt(cmdparts[2]);
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "non-numeric port in tcp listen command"
      );
    }
    DWVPortTCPListenerThread listener = new DWVPortTCPListenerThread(
        this.dwvSerialProtocol, this.virtualPort, tcpport
    );
    // parse options
    if (cmdparts.length > ARG_START) {
      for (int i = ARG_START; i < cmdparts.length; i++) {
        if (cmdparts[i].equalsIgnoreCase("telnet")) {
          listener.setDoTelnet(true);
        } else if (cmdparts[i].equalsIgnoreCase("httpd")) {
          listener.setMode(2);
        } else if (cmdparts[i].equalsIgnoreCase("banner")) {
          listener.setDoBanner(true);
        }
      }
    }
    // start TCP listener thread
    Thread listenThread = new Thread(listener);
    listenThread.start();
    return new DWCommandResponse("listening on port " + tcpport);
  }
}
