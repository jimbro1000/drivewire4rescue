package com.groupunix.drivewireserver.virtualserial;


import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;
import static com.groupunix.drivewireserver.DWDefs.NEWLINE;

public class DWVPortTCPServerThread implements Runnable {

  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPortTCPServerThread");
  /**
   * Telnet mode.
   */
  private static final int MODE_TELNET = 1;
  /**
   * Term mode.
   */
  private static final int MODE_TERM = 3;
  /**
   * Delay to poll buffer for flush.
   */
  private static final int FLUSH_DELAY = 100;
  /**
   * Virtual port.
   */
  private final int vport;
  /**
   * Connection number.
   */
  private final int conno;
  /**
   * Serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * Socket channel.
   */
  private final SocketChannel sktchan;
  /**
   * Shutdown flag.
   */
  private boolean wanttodie = false;
  /**
   * Operation mode.
   */
  private int mode;

  /**
   * Virtual Port TCP Server Thread.
   *
   * @param protocol   serial protocol
   * @param vPort      virtual port
   * @param connNumber connection number
   * @throws DWConnectionNotValidException invalid connection
   */
  public DWVPortTCPServerThread(final DWVSerialProtocol protocol,
                                final int vPort,
                                final int connNumber)
      throws DWConnectionNotValidException {
    LOGGER.debug("init tcp server thread for conn " + connNumber);
    this.vport = vPort;
    this.conno = connNumber;
    this.dwVSerialPorts = protocol.getVPorts();
    this.mode = this.dwVSerialPorts.getListenerPool().getMode(connNumber);
    this.sktchan = this.dwVSerialPorts.getListenerPool().getConn(connNumber);
  }

  /**
   * Run server thread.
   */
  public void run() {
    Thread.currentThread().setName("tcpserv-" + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    try {
      // setup ties
      this.dwVSerialPorts.getListenerPool().setConnPort(this.conno, this.vport);
      dwVSerialPorts.setConn(this.vport, this.conno);
      LOGGER.debug("run for conn " + this.conno);
      if (sktchan == null) {
        LOGGER.warn("got a null socket, bailing out");
        return;
      }

      // set pass through mode
      dwVSerialPorts.markConnected(vport);
      dwVSerialPorts.setUtilMode(vport, DWDefs.UTILMODE_TCPIN);
      dwVSerialPorts.setPortChannel(vport, sktchan);

      int lastbyte = -1;

      while ((!wanttodie) && (sktchan.isOpen())
          && (dwVSerialPorts.isOpen(this.vport) || (mode == MODE_TERM))) {

        int databyte = sktchan.socket().getInputStream().read();
        if (databyte == -1) {
          wanttodie = true;
        } else {
          // filter CR,NULL if in telnet or term mode
          // unless PD.INT and PD.QUT = 0
          if (((mode == MODE_TELNET) || (mode == MODE_TERM))
              && ((dwVSerialPorts.getPdInt(this.vport) != 0)
              || (dwVSerialPorts.getPdQut(this.vport) != 0))) {
            // TODO filter CR/LF.. should do this better
            if (!((lastbyte == CARRIAGE_RETURN)
                && ((databyte == NEWLINE)
                || (databyte == 0)))) {
              // write it to the serial port
              dwVSerialPorts.writeToCoco(this.vport, (byte) databyte);
              lastbyte = databyte;
            }
          } else {
            dwVSerialPorts.writeToCoco(this.vport, (byte) databyte);
          }
        }
      }

      dwVSerialPorts.markDisconnected(this.vport);
      dwVSerialPorts.setPortChannel(vport, null);
      // only if we got connected... and it's not term
      if (mode != MODE_TERM) {
        if (sktchan.isConnected()) {
          LOGGER.debug("exit stage 1, flush buffer");
          // flush buffer, term port
          try {
            while ((dwVSerialPorts.bytesWaiting(this.vport) > 0)
                && (dwVSerialPorts.isOpen(this.vport))) {
              LOGGER.debug("pause for the cause: "
                  + dwVSerialPorts.bytesWaiting(this.vport) + " bytes left");
              Thread.sleep(FLUSH_DELAY);
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          LOGGER.debug("exit stage 2, send peer signal");
          dwVSerialPorts.closePort(this.vport);
        }
      }
    } catch (DWPortNotValidException | IOException
             | DWConnectionNotValidException e) {
      LOGGER.error(e.getMessage());
    }
    try {
      this.dwVSerialPorts.getListenerPool().clearConn(this.conno);
    } catch (DWConnectionNotValidException e) {
      LOGGER.error(e.getMessage());
    }
    LOGGER.debug("thread exiting");
  }

  /**
   * Shutdown thread.
   */
  public void shutdown() {
    LOGGER.debug("shutting down");
    this.wanttodie = true;
    try {
      if (this.sktchan != null) {
        this.sktchan.close();
      }
    } catch (IOException e) {
      LOGGER.warn("IOException while closing socket: " + e.getMessage());
    }
  }
}
