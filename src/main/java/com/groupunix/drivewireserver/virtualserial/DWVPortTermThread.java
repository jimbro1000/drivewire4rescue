package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.groupunix.drivewireserver.DWDefs;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class DWVPortTermThread implements Runnable {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPortTermThread");
  /**
   * Terminal port.
   */
  private static final int TERM_PORT = 0;
  /**
   * Terminal Mode.
   */
  private static final int MODE_TERM = 3;
  /**
   * Backlog.
   */
  private static final int BACKLOG = 0;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwProto;
  /**
   * Serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * TCP port.
   */
  private int tcpport;
  /**
   * Shutdown flag.
   */
  private boolean wanttodie = false;
  /**
   * Virtual port.
   * <p>
   * port 0 is always term now...
   */
  private int vport = 0;
  /**
   * Connection thread.
   */
  private Thread connthread;
  /**
   * Server thread ref.
   */
  private DWVPortTCPServerThread connobj;
  /**
   * Connection number.
   */
  private int conno;
  /**
   * Socket channel.
   */
  private ServerSocketChannel srvr;

  /**
   * Virtual Port Terminal Thread.
   *
   * @param serialProtocol serial protocol
   * @param tcpPort        tcp port
   */
  public DWVPortTermThread(final DWVSerialProtocol serialProtocol,
                           final int tcpPort) {
    LOGGER.debug("init term device thread on port " + tcpPort);
    this.tcpport = tcpPort;
    this.dwProto = serialProtocol;
    this.dwVSerialPorts = serialProtocol.getVPorts();
  }

  /**
   * Run threads.
   */
  public void run() {

    Thread.currentThread().setName("termdev-" + Thread.currentThread().getId());

    LOGGER.debug("run");

    // setup port
    try {
      dwVSerialPorts.resetPort(TERM_PORT);
    } catch (DWPortNotValidException e3) {
      LOGGER.warn("while resetting term port: " + e3.getMessage());
    }

    // startup server
    srvr = null;

    try {
      dwVSerialPorts.openPort(TERM_PORT);

      InetSocketAddress sktaddr = new InetSocketAddress(this.tcpport);

      srvr.socket().setReuseAddress(true);
      srvr.socket().bind(sktaddr, BACKLOG);

      LOGGER.info("listening on port " + srvr.socket().getLocalPort());
    } catch (IOException e2) {
      LOGGER.error("Error opening socket on port "
          + this.tcpport + ": " + e2.getMessage());
      return;
    } catch (DWPortNotValidException e) {
      LOGGER.error("Error opening term port: " + e.getMessage());
      return;
    }
    while ((!wanttodie) && (srvr.isOpen())) {
      LOGGER.debug("waiting for connection");
      SocketChannel skt;
      try {
        skt = srvr.accept();
      } catch (IOException e1) {
        LOGGER.info("IO error: " + e1.getMessage());
        wanttodie = true;
        return;
      }
      LOGGER.info("new connection from " + skt.socket()
          .getInetAddress().getHostAddress());
      if (this.connthread != null) {
        if (this.connthread.isAlive()) {
          // no room at the inn
          LOGGER.debug("term connection already in use");
          try {
            skt.socket()
                .getOutputStream()
                .write(
                    ("The term device is already connected to a session (from "
                        + this.dwVSerialPorts.getListenerPool()
                        .getConn(conno)
                        .socket()
                        .getInetAddress()
                        .getHostName()
                        + ")\r\n"
                    ).getBytes(DWDefs.ENCODING));
            skt.close();
          } catch (IOException e) {
            LOGGER.debug("io error closing socket: " + e.getMessage());
          } catch (DWConnectionNotValidException e) {
            LOGGER.error(e.getMessage());
          }
        } else {
          startConn(skt);
        }
      } else {
        startConn(skt);
      }
    }
    if (srvr != null) {
      try {
        srvr.close();
      } catch (IOException e) {
        LOGGER.error("error closing server socket: " + e.getMessage());
      }
    }
    LOGGER.debug("exiting");
  }

  private void startConn(final SocketChannel skt) {
    // do telnet init stuff
    byte[] buf = DWVPortTelnetPreflightThread.prepTelnet();
    int len = buf.length;
    try {
      skt.socket().getOutputStream().write(buf, 0, len);
      for (int i = 0; i < len; i++) {
        skt.socket().getInputStream().read();
      }
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
    try {
      conno = this.dwVSerialPorts
          .getListenerPool()
          .addConn(this.vport, skt, MODE_TERM);
      connobj = new DWVPortTCPServerThread(dwProto, TERM_PORT, conno);
      connthread = new Thread(connobj);
      connthread.start();
    } catch (DWConnectionNotValidException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Shutdown thread.
   */
  public void shutdown() {
    LOGGER.debug("shutting down");
    wanttodie = true;
    if (connobj != null) {
      connobj.shutdown();
      connthread.interrupt();
    }
    try {
      srvr.close();
    } catch (IOException e) {
      LOGGER.warn("IOException closing server socket: " + e.getMessage());
    }
  }
}
