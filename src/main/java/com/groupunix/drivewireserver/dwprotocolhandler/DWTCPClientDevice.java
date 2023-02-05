package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;


public class DWTCPClientDevice implements DWProtocolDevice {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWTCPClientDev");
  /**
   * Port number.
   */
  private final int tcpport;
  /**
   * Hostname.
   */
  private final String tcphost;
  /**
   * Handler id.
   */
  private final int handlerno;
  /**
   * Device socket.
   */
  private final Socket sock;
  /**
   * Log bytes flag.
   */
  private boolean bytelog = false;

  /**
   * TCP client device.
   *
   * @param handler handler number
   * @param host    host name
   * @param port    port number
   * @throws IOException read/write failure
   */
  public DWTCPClientDevice(final int handler, final String host, final int port)
      throws IOException {
    this.handlerno = handler;
    this.tcpport = port;
    this.tcphost = host;
    bytelog = DriveWireServer
        .getHandler(this.handlerno)
        .getConfig()
        .getBoolean("LogDeviceBytes", false);
    LOGGER.debug("init tcp device client to " + host + " port " + port
        + " for handler #" + handler + " (logging bytes: " + bytelog + ")");
    // check for listen address
    sock = new Socket(this.tcphost, this.tcpport);
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
    LOGGER.info("closing tcp client device in handler #" + this.handlerno);
    try {
      sock.close();
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
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
   * Read byte from input stream.
   *
   * @param timeout timeout flag
   * @return byte
   */
  public int comRead1(final boolean timeout) {
    int data = -1;
    try {
      data = sock.getInputStream().read();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (data < 0) {
      // read problem
      LOGGER.info("socket error reading device");
      return (-1);
    }
    logByte("TCPREAD: " + data);
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
    try {
      sock.getOutputStream().write(data, 0, len);
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
    try {
      sock.getOutputStream().write((byte) data);
      logByte("TCP-C-WRITE1: " + data);
    } catch (IOException e) {
      // problem with comm port, bail out
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Is connected.
   *
   * @return true if socket open
   */
  public boolean connected() {
    return sock != null && !sock.isClosed();
  }

  /**
   * Shutdown.
   */
  public void shutdown() {
    close();
  }

  /**
   * Get rate.
   * <p>
   * not implemented
   * </p>
   *
   * @return -1;
   */
  public int getRate() {
    // doesn't make sense here?
    return -1;
  }

  /**
   * Get device name.
   *
   * @return device name (host:port)
   */
  @Override
  public String getDeviceName() {
    return this.tcphost + ":" + this.tcpport;
  }

  /**
   * Get device type.
   *
   * @return tcp-client
   */
  @Override
  public String getDeviceType() {
    return "tcp-client";
  }

  /**
   * Get client.
   *
   * @return client (host:port)
   */
  @Override
  public String getClient() {
    if (this.connected()) {
      return getDeviceName();
    }
    return null;
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
