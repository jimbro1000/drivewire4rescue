package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;
import static com.groupunix.drivewireserver.DWDefs.NEWLINE;

public class DWVPortTCPConnectionThread implements Runnable {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPortTCPConnectionThread");
  /**
   * Buffer read size.
   */
  public static final int READ_BUFFER_SIZE = 256;
  /**
   * Delay between read cycles.
   */
  public static final int READ_DELAY_MILLIS = 100;
  /**
   * virtual port.
   */
  private final int vport;
  /**
   * TCP host port.
   */
  private final int tcpport;
  /**
   * TCP host name.
   */
  private final String tcphost;
  /**
   * Shutdown flag.
   */
  private boolean wanttodie = false;
  /**
   * Serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * Report connection.
   */
  private boolean reportConnect = true;
  /**
   * wc data.
   */
  private byte[] wcdata = null;
  /**
   * Socket channel.
   */
  private SocketChannel sktchan;
  /**
   * Net socket address.
   */
  private InetSocketAddress sktaddr;

  /**
   * V Port TCP Connection Thread.
   *
   * @param protocol  serial protocol
   * @param vPort     virtual port
   * @param tcpHostIn tcp host in
   * @param tcpPortIn tcp port in
   */
  public DWVPortTCPConnectionThread(final DWVSerialProtocol protocol,
                                    final int vPort,
                                    final String tcpHostIn,
                                    final int tcpPortIn) {
    LOGGER.debug("init tcp connection thread");
    this.vport = vPort;
    this.tcpport = tcpPortIn;
    this.tcphost = tcpHostIn;
    this.dwVSerialPorts = protocol.getVPorts();
  }

  /**
   * V Port TCP Connection Thread.
   *
   * @param protocol  serial protocol
   * @param vPort     virtual port
   * @param tcpHostIn tcp host in
   * @param tcpPortIn tcp port in
   * @param report    report connect
   */
  public DWVPortTCPConnectionThread(final DWVSerialProtocol protocol,
                                    final int vPort,
                                    final String tcpHostIn,
                                    final int tcpPortIn,
                                    final boolean report) {
    LOGGER.debug("init tcp connection thread");
    this.vport = vPort;
    this.tcpport = tcpPortIn;
    this.tcphost = tcpHostIn;
    this.reportConnect = report;
    this.dwVSerialPorts = protocol.getVPorts();

  }

  /**
   * V Port TCP Connection Thread.
   *
   * @param protocol  serial protocol
   * @param vPort     virtual port
   * @param tcpHostIn tcp host in
   * @param tcpPortIn tcp port in
   * @param report    report connect
   * @param wcData    wc data
   */
  public DWVPortTCPConnectionThread(final DWVSerialProtocol protocol,
                                    final int vPort,
                                    final String tcpHostIn,
                                    final int tcpPortIn,
                                    final boolean report,
                                    final byte[] wcData) {
    LOGGER.debug("init NineServer connection thread");
    this.vport = vPort;
    this.tcpport = tcpPortIn;
    this.tcphost = tcpHostIn;
    this.reportConnect = report;
    this.wcdata = wcData;
    this.dwVSerialPorts = protocol.getVPorts();
  }

  /**
   * Run threads.
   */
  public void run() {
    Thread.currentThread().setName("tcpconn-" + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    LOGGER.debug("run");
    // try to establish connection
    try {
      sktaddr = new InetSocketAddress(this.tcphost, this.tcpport);
      sktchan = SocketChannel.open();
      sktchan.configureBlocking(true);
      sktchan.connect(sktaddr);
      if (sktchan.finishConnect()) {
        dwVSerialPorts.setPortChannel(vport, this.sktchan);
        if (this.reportConnect) {
          dwVSerialPorts.writeToCoco(
              this.vport,
              "OK Connected to " + this.tcphost + ":"
                  + this.tcpport + (char) NEWLINE + (char) CARRIAGE_RETURN
          );
        }
        dwVSerialPorts.markConnected(vport);
        LOGGER.debug("Connected to " + this.tcphost + ":" + this.tcpport);
        dwVSerialPorts.setUtilMode(vport, DWDefs.UTILMODE_TCPOUT);
        if (this.wcdata != null) {
          dwVSerialPorts.write(this.vport,
              new String(this.wcdata, DWDefs.ENCODING));
        }
      }
    } catch (UnknownHostException e) {
      LOGGER.debug("unknown host " + tcphost);
      if (this.reportConnect) {
        try {
          dwVSerialPorts.sendUtilityFailResponse(
              this.vport,
              DWDefs.RC_NET_UNKNOWN_HOST,
              "Unknown host '" + this.tcphost + "'"
          );
        } catch (DWPortNotValidException e1) {
          LOGGER.warn(e1.getMessage());
        }
      }
      this.wanttodie = true;
    } catch (IOException e1) {
      LOGGER.debug("IO error: " + e1.getMessage());

      if (this.reportConnect) {
        try {
          dwVSerialPorts.sendUtilityFailResponse(
              this.vport, DWDefs.RC_NET_IO_ERROR, e1.getMessage()
          );
        } catch (DWPortNotValidException e) {
          LOGGER.warn(e1.getMessage());
        }
      }
      this.wanttodie = true;
    } catch (DWPortNotValidException e) {
      LOGGER.warn(e.getMessage());
    }

    final byte[] readbytes = new byte[READ_BUFFER_SIZE];
    final ByteBuffer readBuffer = ByteBuffer.wrap(readbytes);

    while (!wanttodie && sktchan.isOpen()
        && dwVSerialPorts.isOpen(this.vport)) {
      try {
        final int readsize = sktchan.read(readBuffer);
        if (readsize == -1) {
          LOGGER.debug("got end of input stream");
          wanttodie = true;
        } else if (readsize > 0) {
          dwVSerialPorts.writeToCoco(this.vport, readbytes, 0, readsize);
          readBuffer.clear();
        }
      } catch (IOException e) {
        LOGGER.debug("IO error reading tcp: " + e.getMessage());
        wanttodie = true;
      } catch (DWPortNotValidException e) {
        LOGGER.error(e.getMessage());
        wanttodie = true;
      }
    }
    if (wanttodie) {
      LOGGER.debug("exit because wanttodie");
    } else if (sktchan.isConnected()) {
      if (!dwVSerialPorts.isOpen(this.vport)) {
        LOGGER.debug("exit because port is not open");
      }
    } else {
      LOGGER.debug("exit because skt isClosed");
    }
    // only if we got connected...
    if (sktchan != null) {
      if (sktchan.isConnected()) {
        LOGGER.debug("exit stage 1, flush buffer");
        // flush buffer, term port
        try {
          while (dwVSerialPorts.bytesWaiting(this.vport) > 0
              && dwVSerialPorts.isOpen(this.vport)) {
            LOGGER.debug("pause for the cause: "
                + dwVSerialPorts.bytesWaiting(this.vport) + " bytes left");
            Thread.sleep(READ_DELAY_MILLIS);
          }
        } catch (InterruptedException | DWPortNotValidException e) {
          LOGGER.error(e.getMessage());
        }
        LOGGER.debug("exit stage 2, send peer signal");
        try {
          dwVSerialPorts.closePort(this.vport);
        } catch (DWPortNotValidException e) {
          LOGGER.error("in close port: " + e.getMessage());
        }
      }
      LOGGER.debug("thread exiting");
    }
  }
}
