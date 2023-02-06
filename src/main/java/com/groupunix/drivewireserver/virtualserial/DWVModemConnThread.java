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
  public static final int IAC = 255;
  /**
   * SE protocol command.
   */
  public static final int SE = 240;
  /**
   * NOP protocol command.
   */
  public static final int NOP = 241;
  /**
   * DM protocol command.
   */
  public static final int DM = 242;
  /**
   * BREAK protocol command.
   */
  public static final int BREAK = 243;
  /**
   * IP protocol command.
   */
  public static final int IP = 244;
  /**
   * AO protocol command.
   */
  public static final int AO = 245;
  /**
   * AYT protocol command.
   */
  public static final int AYT = 246;
  /**
   * EC protocol command.
   */
  public static final int EC = 247;
  /**
   * EL protocol command.
   */
  public static final int EL = 248;
  /**
   * GA protocol command.
   */
  public static final int GA = 249;
  /**
   * SB protocol command.
   */
  public static final int SB = 250;
  /**
   * WILL protocol command.
   */
  public static final int WILL = 251;
  /**
   * WONT protocol command.
   */
  public static final int WONT = 252;
  /**
   * Do protocol command.
   */
  public static final int DO = 253;
  /**
   * DONT protocol command.
   */
  public static final int DONT = 254;
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
        if ((sktchan != null) && sktchan.isConnected()) {
          modem.getVSerialPorts().markConnected(vport);
          modem.getVSerialPorts().setUtilMode(vport, DWDefs.UTILMODE_VMODEMOUT);
          modem.getVSerialPorts().setPortChannel(vport, sktchan);
          modem.getVSerialPorts().getPortInput(vport)
              .write("CONNECT\r\n".getBytes(DWDefs.ENCODING));
        }
        while ((sktchan != null) && sktchan.isConnected()) {
          int data = sktchan.socket().getInputStream().read();

          if (data >= 0) {
            // telnet stuff
            if (telmode == 1) {
              switch (data) {
                case SE:
                case NOP:
                case DM:
                case BREAK:
                case IP:
                case AO:
                case AYT:
                case EC:
                case EL:
                case GA:
                case SB:
                  break;

                case WILL:
                  data = sktchan.socket().getInputStream().read();
                  sktchan.socket().getOutputStream().write(BYTE_MASK);
                  sktchan.socket().getOutputStream().write(DONT);
                  sktchan.socket().getOutputStream().write(data);
                  break;

                case WONT:
                case DONT:
                  data = sktchan.socket().getInputStream().read();
                  break;

                case DO:
                  data = sktchan.socket().getInputStream().read();
                  sktchan.socket().getOutputStream().write(BYTE_MASK);
                  sktchan.socket().getOutputStream().write(WONT);
                  sktchan.socket().getOutputStream().write(data);
                  break;

                default:
              }
              telmode = 0;
            }
            if (data == IAC) {
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
