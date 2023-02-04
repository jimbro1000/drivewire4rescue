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
        socketChannel.socket().getOutputStream().write(buf, 0, 9);
        // read back the echoed controls - TODO has issues
        for (int i = 0; i < 9; i++) {
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

  static byte[] prepTelnet() {
    byte[] buf = new byte[9];
    buf[0] = (byte) 255;
    buf[1] = (byte) 251;
    buf[2] = (byte) 1;
    buf[3] = (byte) 255;
    buf[4] = (byte) 251;
    buf[5] = (byte) 3;
    buf[6] = (byte) 255;
    buf[7] = (byte) 253;
    buf[8] = (byte) 243;
    return buf;
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
