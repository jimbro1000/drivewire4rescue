package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.groupunix.drivewireserver.DWDefs;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;


public class DWVModemListenerThread implements Runnable {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVModemListenerThread");
  /**
   * Backlog.
   */
  private static final int BACKLOG = 20;
  /**
   * V.modem port.
   */
  private final int vport;
  /**
   * TCP port.
   */
  private final int tcpport;
  /**
   * Virtual modem.
   */
  private final DWVModem dwVModem;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwProto;
  /**
   * Serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * Shutdown flag.
   */
  private boolean wanttodie = false;
  /**
   * Client connected flag.
   */
  private Boolean clientConnected = false;

  /**
   * Virtual Modem Listener Thread.
   *
   * @param modem virtual modem
   */
  public DWVModemListenerThread(final DWVModem modem) {
    this.dwVSerialPorts = modem.getVSerialPorts();
    this.dwVModem = modem;
    this.tcpport = modem.getListenPort();
    this.vport = modem.getVPort();
    this.dwProto = modem.getVProto();
    LOGGER.debug("init modem listener thread on port " + tcpport);
  }

  /**
   * Run threads.
   */
  public void run() {
    Thread.currentThread().setName("mdmlisten-"
        + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    LOGGER.debug("run");

    // startup server
    try {
      final ServerSocketChannel srvr = ServerSocketChannel.open();
      final InetSocketAddress sktaddr = new InetSocketAddress(this.tcpport);
      srvr.socket().setReuseAddress(true);
      srvr.socket().bind(sktaddr, BACKLOG);

      while (!wanttodie
          && dwVSerialPorts.isOpen(this.vport)
          && srvr.isOpen()) {
        LOGGER.debug("waiting for connection");
        final SocketChannel skt = srvr.accept();
        LOGGER.info("new connection from "
            + skt.socket().getInetAddress().getHostAddress());
        synchronized (this.clientConnected) {
          if (this.clientConnected) {
            LOGGER.info("Rejecting new connection to vmodem #" + this.vport
                + " because modem is already connected");
            try {
              skt.socket()
                  .getOutputStream()
                  .write(dwProto.getConfig().getString(
                      "ModemInUseMessage",
                      """
                          This DriveWire virtual modem is in use.\r
                          Please try again later.\r
                          """
                  ).getBytes(DWDefs.ENCODING));
              skt.close();
            } catch (IOException e) {
              LOGGER.warn("in new modem connection: " + e.getMessage());
            }
          } else {
            this.clientConnected = true;
            final Thread vmthread = new Thread(
                new DWVModemConnThread(dwVModem, skt, this)
            );
            vmthread.start();
            LOGGER.info("started thread to handle new vmodem connection");
          }
        }
      }
      if (srvr != null) {
        try {
          srvr.close();
        } catch (IOException e) {
          LOGGER.error("error closing server socket: " + e.getMessage());
        }
      }
    } catch (IOException e2) {
      LOGGER.error(e2.getMessage());
    }
    LOGGER.debug("modem listener thread exiting");
  }

  /**
   * Set connected flag.
   *
   * @param connected connected
   */
  public void setConnected(final boolean connected) {
    synchronized (this.clientConnected) {
      this.clientConnected = connected;
    }
  }
}
