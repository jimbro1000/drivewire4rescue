package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWTCPDevice implements DWProtocolDevice {
  /**
   * log appender.
   */
  private static final Logger LOGGER = Logger.getLogger("DWServer.DWTCPDevice");
  /**
   * port.
   */
  private final int tcpport;
  /**
   * handler number.
   */
  private final int handlerno;
  /**
   * Server socket.
   */
  private final ServerSocket srvr;
  /**
   * Socket.
   */
  private Socket skt = null;
  /**
   * log read byte(s) flag.
   */
  private boolean bytelog = false;
  /**
   * Client.
   */
  private String client = null;

  /**
   * TCP Device.
   *
   * @param handler handler number
   * @param port    port
   * @throws IOException read/write failure
   */
  public DWTCPDevice(final int handler, final int port)
      throws IOException {
    this.handlerno = handler;
    this.tcpport = port;
    bytelog = DriveWireServer
        .getHandler(this.handlerno)
        .getConfig()
        .getBoolean("LogDeviceBytes", false);
    LOGGER.debug("init tcp device server on port " + port
        + " for handler #" + handler + " (logging bytes: " + bytelog + ")");
    // check for listen address
    if (DriveWireServer
        .getHandler(this.handlerno)
        .getConfig()
        .containsKey("ListenAddress")
    ) {
      srvr = new ServerSocket(
          this.tcpport,
          0,
          InetAddress.getByName(
              DriveWireServer
                  .getHandler(this.handlerno)
                  .getConfig()
                  .getString("ListenAddress")
          )
      );
    } else {
      srvr = new ServerSocket(this.tcpport, 0);
    }
    LOGGER.info("listening on port " + srvr.getLocalPort());
  }

  private void logByte(final String message) {
    if (bytelog) {
      LOGGER.debug(message);
    }
  }

  /**
   * Close device.
   */
  public void close() {
    LOGGER.debug("closing tcp device in handler #" + this.handlerno);
    closeClient();
    try {
      srvr.close();
    } catch (IOException e) {
      LOGGER.debug(e.getMessage());
    }
  }

  /**
   * Close client connection.
   */
  private void closeClient() {
    LOGGER.debug("closing client connection");
    if ((skt != null) && (!skt.isClosed())) {
      try {
        skt.close();
      } catch (IOException e) {
        LOGGER.debug(e.getMessage());
      }
    }
    client = null;
    skt = null;
  }

  /**
   * Read bytes from input stream.
   *
   * @param len number of bytes to read
   * @return byte array
   */
  public byte[] comRead(final int len) {
    byte[] buf = new byte[len];
    for (int i = 0; i < len; i++) {
      buf[i] = (byte) comRead1(true);
    }
    return buf;
  }

  /**
   * Read 1 byte from input stream.
   *
   * @param timeout timeout flag
   * @return byte
   */
  public int comRead1(final boolean timeout) {
    int data = -1;
    if (skt == null) {
      getClientConnection();
    }
    if (skt != null) {
      try {
        data = skt.getInputStream().read();
      } catch (IOException e) {
        //e.printStackTrace();
        closeClient();
      }
      if (data < 0) {
        // read problem
        LOGGER.debug("socket error reading device");
        closeClient();
        // call ourselves to get another byte...
        // not sure if it is a great idea
        return comRead1(timeout);
      }
      logByte("TCPREAD: " + data);
    }
    return data;
  }

  /**
   * Write bytes to output stream.
   *
   * @param data   byte array
   * @param len    number of bytes to write
   * @param prefix require response prefix
   */
  public void comWrite(final byte[] data, final int len, final boolean prefix) {
    if ((skt == null) || (skt.isClosed())) {
      return;
    }
    try {
      skt.getOutputStream().write(data, 0, len);
      if (bytelog) {
        StringBuilder tmps = new StringBuilder();
        for (byte datum : data) {
          tmps.append(" ").append(datum & BYTE_MASK);
        }
        logByte("WRITE " + data.length + ":" + tmps);
      }
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Write 1 byte to output stream.
   *
   * @param data   byte
   * @param prefix require response prefix
   */
  public void comWrite1(final int data, final boolean prefix) {
    if ((skt == null) || (skt.isClosed())) {
      return;
    }
    try {
      skt.getOutputStream().write((byte) data);
      logByte("TCPWRITE1: " + data);
    } catch (IOException e) {
      // problem with comm port, bail out
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Is socket connected.
   *
   * @return connected
   */
  public boolean connected() {
    return skt != null;
  }

  /**
   * Shutdown.
   */
  public void shutdown() {
    close();
  }

  /**
   * Get client connection.
   * <p>
   * Wait for new client connection
   * </p>
   */
  private void getClientConnection() {
    LOGGER.debug("waiting for client...");
    try {
      skt = srvr.accept();
    } catch (IOException e1) {
      LOGGER.debug("IO error while listening for client: " + e1.getMessage());
      return;
    }
    LOGGER.debug("new client connect from "
        + skt.getInetAddress().getCanonicalHostName());
    this.client = skt.getInetAddress().getCanonicalHostName();
    try {
      skt.setTcpNoDelay(true);
    } catch (SocketException e) {
      LOGGER.warn(e.getMessage());
    }
  }

  /**
   * Get baud rate.
   * <p>
   * not implemented
   * </p>
   *
   * @return -1
   */
  public int getRate() {
    // doesn't make sense here?
    return -1;
  }

  /**
   * Get device name.
   *
   * @return device name
   */
  @Override
  public String getDeviceName() {
    return ("listen:" + this.tcpport);
  }

  /**
   * Get device type.
   *
   * @return tcp
   */
  @Override
  public String getDeviceType() {
    return ("tcp");
  }

  /**
   * Get client.
   *
   * @return client name
   */
  @Override
  public String getClient() {
    return this.client;
  }

  /**
   * Get input stream.
   * <p>
   * not implemented
   * </p>
   *
   * @return null
   */
  @Override
  public InputStream getInputStream() {
    return null;
  }
}
