package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;
import static com.groupunix.drivewireserver.DWDefs.NEWLINE;

public class DWVPortTCPListenerThread implements Runnable {

  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPortTCPListenerThread");
  /**
   * Backlog.
   */
  private static final int BACKLOG = 20;
  /**
   * Virtual port.
   */
  private final int vport;
  /**
   * tcp port.
   */
  private final int tcpport;
  /**
   * serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwProto;
  /**
   * operation mode.
   */
  private int opMode = 0;
  /**
   * do banner.
   */
  private boolean doBanner = false;
  /**
   * Do telnet.
   */
  private boolean doTelnet = false;
  /**
   * shutdown flag.
   */
  private boolean wantToDie = false;

  /**
   * Virtual Port TCP Listener Thread.
   *
   * @param serialProtocol serial protocol
   * @param vPort          virtual port
   * @param tcpPort        tcp port
   */
  public DWVPortTCPListenerThread(final DWVSerialProtocol serialProtocol,
                                  final int vPort,
                                  final int tcpPort) {
    LOGGER.debug("init tcp listener thread on port " + tcpPort);
    this.vport = vPort;
    this.tcpport = tcpPort;
    this.dwProto = serialProtocol;
    this.dwVSerialPorts = serialProtocol.getVPorts();
  }

  /**
   * Run listener thread.
   */
  public void run() {
    Thread.currentThread().setName("tcplisten-"
        + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    LOGGER.debug("run");
    try {
      // startup server
      ServerSocketChannel srvr = ServerSocketChannel.open();
      try {
        InetSocketAddress sktaddr = new InetSocketAddress(this.tcpport);
        srvr.socket().setReuseAddress(true);
        srvr.socket().bind(sktaddr, BACKLOG);
        this.dwVSerialPorts.getListenerPool().addListener(this.vport, srvr);
        LOGGER.info("tcp listening on port " + srvr.socket().getLocalPort());
      } catch (IOException e2) {
        LOGGER.error(e2.getMessage());
        dwVSerialPorts.sendUtilityFailResponse(
            this.vport, DWDefs.RC_NET_IO_ERROR, e2.getMessage()
        );
        return;
      }

      dwVSerialPorts.writeToCoco(vport, "OK listening on port "
          + this.tcpport + (char) NEWLINE + (char) CARRIAGE_RETURN);

      this.dwVSerialPorts.setUtilMode(vport, DWDefs.UTILMODE_TCPLISTEN);

      while ((!wantToDie) && dwVSerialPorts.isOpen(this.vport)
          && (srvr.isOpen()) && (!srvr.socket().isClosed())) {
        LOGGER.debug("waiting for connection");
        SocketChannel skt = srvr.accept();
        LOGGER.info("new connection from " + skt.socket().getInetAddress());
        this.dwVSerialPorts.getListenerPool().addConn(this.vport, skt, opMode);
        if (opMode == 2) {
          // http mode
          LOGGER.error("HTTP MODE NO LONGER SUPPORTED");
        } else {
          // run telnet preflight, let it add the connection to the pool
          // if things work out
          Thread pfthread = new Thread(new DWVPortTelnetPreflightThread(
              this.dwProto, this.vport, skt, this.doTelnet, this.doBanner
          ));
          pfthread.start();
        }
      }

      try {
        srvr.close();
      } catch (IOException e) {
        LOGGER.error("error closing server socket: " + e.getMessage());
      }

    } catch (IOException | DWPortNotValidException e2) {
      LOGGER.error(e2.getMessage());
    }
    LOGGER.debug("tcp listener thread exiting");
  }

  /**
   * Is do banner set.
   *
   * @return do banner
   */
  @SuppressWarnings("unused")
  public boolean isDoBanner() {
    return doBanner;
  }

  /**
   * Set do banner.
   *
   * @param bannerFlag do banner
   */
  public void setDoBanner(final boolean bannerFlag) {
    this.doBanner = bannerFlag;
  }

  /**
   * Get mode.
   *
   * @return mode
   */
  public int getMode() {
    return (this.opMode);
  }

  /**
   * Set mode.
   *
   * @param mode mode
   */
  public void setMode(final int mode) {
    this.opMode = mode;
  }

  /**
   * Is do telnet set.
   *
   * @return bool
   */
  @SuppressWarnings("unused")
  public boolean isDoTelnet() {
    return doTelnet;
  }

  /**
   * Set do telnet.
   *
   * @param telnetFlag bool
   */
  public void setDoTelnet(final boolean telnetFlag) {
    this.doTelnet = telnetFlag;
  }
}
