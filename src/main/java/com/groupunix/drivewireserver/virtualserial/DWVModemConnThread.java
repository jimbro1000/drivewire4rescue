package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWVModemConnThread implements Runnable {

  // telnet protocol cmds
  /**
   * IAC protocol command.
   */
  public static final int CMD_IAC = 255;
  /**
   * SE protocol command.
   */
  public static final int CMD_SE = 240;
  /**
   * NOP protocol command.
   */
  public static final int CMD_NOP = 241;
  /**
   * DM protocol command.
   */
  public static final int CMD_DM = 242;
  /**
   * BREAK protocol command.
   */
  public static final int CMD_BREAK = 243;
  /**
   * IP protocol command.
   */
  public static final int CMD_IP = 244;
  /**
   * AO protocol command.
   */
  public static final int CMD_AO = 245;
  /**
   * AYT protocol command.
   */
  public static final int CMD_AYT = 246;
  /**
   * EC protocol command.
   */
  public static final int CMD_EC = 247;
  /**
   * EL protocol command.
   */
  public static final int CMD_EL = 248;
  /**
   * GA protocol command.
   */
  public static final int CMD_GA = 249;
  /**
   * SB protocol command.
   */
  public static final int CMD_SB = 250;
  /**
   * WILL protocol command.
   */
  public static final int CMD_WILL = 251;
  /**
   * WONT protocol command.
   */
  public static final int CMD_WONT = 252;
  /**
   * Do protocol command.
   */
  public static final int CMD_DO = 253;
  /**
   * DONT protocol command.
   */
  public static final int CMD_DONT = 254;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVModemConnThread");
  /**
   * virtual modem.
   */
  private final DWVModem modem;
  /**
   * socket channel.
   */
  private SocketChannel sktchan;
  /**
   * port number.
   */
  private int vport = -1;
  /**
   * TCP host name.
   */
  private String tcphost;
  /**
   * TCP port number.
   */
  private int tcpport;
  /**
   * Listener thread.
   */
  private DWVModemListenerThread listenerThread = null;
  /**
   * Shutdown flag.
   */
  private boolean wanttodie = false;

  /**
   * Virtual Modem Connection Thread.
   *
   * @param dwvModem modem
   * @param skt      socket
   * @param listener listener
   */
  public DWVModemConnThread(final DWVModem dwvModem,
                            final SocketChannel skt,
                            final DWVModemListenerThread listener) {
    this.modem = dwvModem;
    this.vport = dwvModem.getVPort();
    this.sktchan = skt;
    this.listenerThread = listener;
  }

  /**
   * Virtual Modem Connection Thread.
   *
   * @param dwvModem modem
   * @param host     TCP host name
   * @param port     TCP port number
   */
  public DWVModemConnThread(final DWVModem dwvModem,
                            final String host,
                            final int port) {
    this.tcphost = host;
    this.tcpport = port;
    this.modem = dwvModem;
    this.vport = modem.getVPort();
    this.sktchan = null;
  }

  /**
   * Run threads.
   */
  public void run() {
    Thread.currentThread().setName("mdmconn-" + Thread.currentThread().getId());

    int telmode = 0;

    if (sktchan == null) {
      try {
        sktchan = SocketChannel.open(new InetSocketAddress(tcphost, tcpport));
      } catch (Exception e) {
        LOGGER.warn("while making outgoing vmodem connection: "
            + e.getMessage());
        wanttodie = true;
      }

    }

    if (!wanttodie) {
      try {
        if (sktchan != null && sktchan.isConnected()) {
          modem.getVSerialPorts().markConnected(vport);
          modem.getVSerialPorts().setUtilMode(vport, DWDefs.UTILMODE_VMODEMOUT);
          modem.getVSerialPorts().setPortChannel(vport, sktchan);
          modem.getVSerialPorts().getPortInput(vport)
              .write("CONNECT\r\n".getBytes(DWDefs.ENCODING));
        }
        while (sktchan != null && sktchan.isConnected()) {
          int data = sktchan.socket().getInputStream().read();

          if (data >= 0) {
            // telnet stuff
            if (telmode == 1) {
              switch (data) {
                case CMD_SE:
                case CMD_NOP:
                case CMD_DM:
                case CMD_BREAK:
                case CMD_IP:
                case CMD_AO:
                case CMD_AYT:
                case CMD_EC:
                case CMD_EL:
                case CMD_GA:
                case CMD_SB:
                  break;

                case CMD_WILL:
                  data = sktchan.socket().getInputStream().read();
                  sktchan.socket().getOutputStream().write(BYTE_MASK);
                  sktchan.socket().getOutputStream().write(CMD_DONT);
                  sktchan.socket().getOutputStream().write(data);
                  break;

                case CMD_WONT:
                case CMD_DONT:
                  data = sktchan.socket().getInputStream().read();
                  break;

                case CMD_DO:
                  data = sktchan.socket().getInputStream().read();
                  sktchan.socket().getOutputStream().write(BYTE_MASK);
                  sktchan.socket().getOutputStream().write(CMD_WONT);
                  sktchan.socket().getOutputStream().write(data);
                  break;

                default:
              }
              telmode = 0;
            }
            if (data == CMD_IAC) {
              telmode = 1;
            } else {
              // write it to the serial port
              modem.write((byte) data);
            }
          } else {
            LOGGER.info("connection to "
                + this.sktchan.socket().getInetAddress().getCanonicalHostName()
                + ":" + this.sktchan.socket().getPort() + " closed");
            if (sktchan.isConnected()) {
              LOGGER.debug("closing socket");
              sktchan.close();
            }
          }
        }
      } catch (IOException e) {
        LOGGER.warn("IO error in connection to "
            + this.sktchan.socket().getInetAddress().getCanonicalHostName()
            + ":" + this.sktchan.socket().getPort() + " = " + e.getMessage());
      } catch (DWPortNotValidException e) {
        LOGGER.warn("in connection to "
            + this.sktchan.socket().getInetAddress().getCanonicalHostName()
            + ":" + this.sktchan.socket().getPort() + " = " + e.getMessage());
      }
    }

    if (this.vport > -1) {
      modem.getVSerialPorts().markDisconnected(this.vport);
      // this is all wrong
      modem.write("\r\n\r\nNO CARRIER\r\n");

      if (this.listenerThread != null) {
        this.listenerThread.setConnected(false);
      }
    }
    LOGGER.debug("thread exiting");
  }
}
