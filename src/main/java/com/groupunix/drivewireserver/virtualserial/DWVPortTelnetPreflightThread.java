package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class DWVPortTelnetPreflightThread implements Runnable {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPortTelnetPreflightThread");
  /**
   * Size of telnet setup/handshake message.
   */
  public static final int TELNET_MSG_LEN = 9;
  /**
   * Telnet escape code.
   */
  public static final int TELNET_ESC = 255;
  /**
   * TELNET will command.
   */
  public static final int TELNET_WILL = 251;
  /**
   * TELNET do command.
   */
  public static final int TELNET_DO = 253;
  /**
   * TELNET break (end of message) command.
   */
  public static final int TELNET_BREAK = 243;
  /**
   * Telnet operation echo.
   */
  public static final int TELNET_OP_ECHO = 1;
  /**
   * Telnet operation suppress go ahead.
   */
  public static final int TELNET_OP_SUPPRESS_GO_AHEAD = 3;
  /**
   * Port.
   */
  private final int vPort;
  /**
   * Show banner.
   */
  private final boolean banner;
  /**
   * Telnet flag.
   */
  private final boolean telnet;
  /**
   * V.Serial Ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * V.Serial Protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;
  /**
   * Socket channel.
   */
  private final SocketChannel socketChannel;

  /**
   * Virtual Port Telnet Preflight Thread.
   *
   * @param serialProtocol Serial Port
   * @param port V.Port
   * @param channel Socket Channel
   * @param doTelnet doTelnet
   * @param doBanner doBanner
   */
  public DWVPortTelnetPreflightThread(final DWVSerialProtocol serialProtocol,
                                      final int port,
                                      final SocketChannel channel,
                                      final boolean doTelnet,
                                      final boolean doBanner) {
    this.vPort = port;
    this.socketChannel = channel;
    this.banner = doBanner;
    this.telnet = doTelnet;
    this.dwvSerialProtocol = serialProtocol;
    this.dwVSerialPorts = serialProtocol.getVPorts();
  }

  static byte[] prepTelnet() {
    int index = 0;
    byte[] buf = new byte[TELNET_MSG_LEN];
    buf[index++] = (byte) TELNET_ESC;
    buf[index++] = (byte) TELNET_WILL;
    buf[index++] = (byte) TELNET_OP_ECHO;
    buf[index++] = (byte) TELNET_ESC;
    buf[index++] = (byte) TELNET_WILL;
    buf[index++] = (byte) TELNET_OP_SUPPRESS_GO_AHEAD;
    buf[index++] = (byte) TELNET_ESC;
    buf[index++] = (byte) TELNET_DO;
    buf[index] = (byte) TELNET_BREAK;
    return buf;
  }

  /**
   * Run thread.
   */
  public void run() {
    Thread.currentThread().setName("tcppre-" + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    LOGGER.debug("preflight checks for new connection from "
        + socketChannel.socket().getInetAddress().getHostName());
    try {
      // hello
      if (this.telnet) {
        socketChannel.socket().getOutputStream()
            .write(("DriveWire Telnet Server "
            + DriveWireServer.DW_SERVER_VERSION + "\r\n\n").getBytes());
      }
      if (this.telnet) {
        // ask telnet to turn off echo, should probably be a setting
        // or left to the client
        byte[] buf = prepTelnet();
        socketChannel.socket().getOutputStream().write(buf, 0, TELNET_MSG_LEN);
        // read back the echoed controls - TODO has issues
        for (int i = 0; i < TELNET_MSG_LEN; i++) {
          socketChannel.socket().getInputStream().read();
        }
      }
      if (socketChannel.socket().isClosed()) {
        // bail out
        LOGGER.debug("thread exiting after auth");
        return;
      }
      if ((dwvSerialProtocol.getConfig().containsKey("TelnetBannerFile"))
          && (banner)) {
        displayFile(socketChannel.socket().getOutputStream(),
            dwvSerialProtocol.getConfig().getString("TelnetBannerFile"));
      }
    } catch (IOException e) {
      LOGGER.warn("IOException: " + e.getMessage());
      if (socketChannel.isConnected()) {
        LOGGER.debug("closing socket");
        try {
          socketChannel.close();
        } catch (IOException e1) {
          LOGGER.warn(e1.getMessage());
        }
      }
    }
    if (socketChannel.isConnected()) {
      //add connection to pool
      int conno = this.dwVSerialPorts
          .getListenerPool().addConn(this.vPort, socketChannel, 1);
      // announce new connection to listener
      try {
        dwVSerialPorts.sendConnectionAnnouncement(
            this.vPort,
            conno,
            socketChannel.socket().getLocalPort(),
            socketChannel.socket().getInetAddress().getHostAddress()
        );
      } catch (DWPortNotValidException e) {
        LOGGER.error("in announce: " + e.getMessage());
      }
    }
    LOGGER.debug("exiting");
  }

  private void displayFile(final OutputStream outputStream,
                           final String fname
  ) {
    FileInputStream fstream;
    try {
      fstream = new FileInputStream(fname);
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;
      LOGGER.debug("sending file '" + fname + "' to telnet client");
      while ((strLine = br.readLine()) != null) {
        outputStream.write(strLine.getBytes());
        outputStream.write("\r\n".getBytes());
      }
      fstream.close();
    } catch (FileNotFoundException e) {
      LOGGER.warn("File not found: " + fname);
    } catch (IOException e1) {
      LOGGER.warn(e1.getMessage());
    }
  }
}
