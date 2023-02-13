package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;

public class DWVPortListenerPool {
  /**
   * Max connections.
   */
  public static final int MAX_CONN = 256;
  /**
   * Max listeners.
   */
  public static final int MAX_LISTEN = 64;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPortListenerPool");
  /**
   * Listener sockets.
   */
  private final SocketChannel[] sockets
      = new SocketChannel[MAX_CONN];
  /**
   * Listener sockets channels.
   */
  private final ServerSocketChannel[] serverSockets
      = new ServerSocketChannel[MAX_LISTEN];
  /**
   * Listener socket ports.
   */
  private final int[] serverSocketPorts = new int[MAX_LISTEN];
  /**
   * Connection sockets.
   */
  private final int[] socketPorts = new int[MAX_CONN];
  /**
   * Connection modes.
   */
  private final int[] modes = new int[MAX_CONN];

  /**
   * Add connection.
   *
   * @param port    port number
   * @param sktchan socket channel
   * @param mode    mode
   * @return index
   */
  public int addConn(final int port, final SocketChannel sktchan,
                     final int mode) {
    LOGGER.debug("add connection entry for port " + port + " mode " + mode);
    for (int i = 0; i < MAX_CONN; i++) {
      if (sockets[i] == null) {
        sockets[i] = sktchan;
        modes[i] = mode;
        socketPorts[i] = port;
        return i;
      }
    }
    return -1;
  }

  /**
   * Get connection socket at index.
   *
   * @param connectionNo index
   * @return socket channel
   * @throws DWConnectionNotValidException invalid exception
   */
  public SocketChannel getConn(final int connectionNo)
      throws DWConnectionNotValidException {
    validateConn(connectionNo);
    return sockets[connectionNo];
  }

  /**
   * Validate connection at index.
   *
   * @param connectionNo index
   * @throws DWConnectionNotValidException invalid connection
   */
  public void validateConn(final int connectionNo)
      throws DWConnectionNotValidException {
    if (
        connectionNo < 0
            || connectionNo > DWVPortListenerPool.MAX_CONN
            || this.sockets[connectionNo] == null
    ) {
      throw new DWConnectionNotValidException(
          "Invalid connection #" + connectionNo
      );
    }
  }

  /**
   * Set connection port at index.
   *
   * @param connectionNo index
   * @param port         port number
   * @throws DWConnectionNotValidException invalid connection
   */
  public void setConnPort(final int connectionNo, final int port)
      throws DWConnectionNotValidException {
    validateConn(connectionNo);
    socketPorts[connectionNo] = port;
  }

  /**
   * Add listener.
   *
   * @param port port
   * @param srvr server socket channel
   * @return outcome
   */
  public int addListener(final int port, final ServerSocketChannel srvr) {
    for (int i = 0; i < MAX_LISTEN; i++) {
      if (serverSockets[i] == null) {
        serverSocketPorts[i] = port;
        serverSockets[i] = srvr;
        LOGGER.debug("add listener entry for port " + port + " id " + i);
        return i;
      }
    }
    return -1;
  }

  /**
   * Get listener at index.
   *
   * @param connectionNo index
   * @return server socket
   */
  public ServerSocketChannel getListener(final int connectionNo) {
    return serverSockets[connectionNo];
  }

  /**
   * Close server socket with given port.
   *
   * @param port port number
   */
  public void closePortServerSockets(final int port) {
    for (int i = 0; i < MAX_LISTEN; i++) {
      if (this.getListener(i) != null && serverSocketPorts[i] == port) {
        try {
          LOGGER.debug("closing listener sockets for port " + port + "...");
          this.killListener(i);
        } catch (DWConnectionNotValidException e) {
          LOGGER.error(e.getMessage());
        }
      }
    }
  }

  /**
   * Close all open listener sockets.
   * <p>
   * Ignores terminals.
   * </p>
   *
   * @param port port number (not used)
   */
  public void closePortConnectionSockets(final int port) {
    for (int i = 0; i < DWVPortListenerPool.MAX_CONN; i++) {
      try {
        if (this.sockets[i] != null
            && this.getMode(i) != DWVSerialPorts.MODE_TERM) {
          this.killConn(i);
        }
      } catch (DWConnectionNotValidException e) {
        LOGGER.error("close sockets: " + e.getMessage());
      }
    }
  }

  /**
   * Get connection mode at index.
   *
   * @param connectionNo index
   * @return connection mode
   * @throws DWConnectionNotValidException invalid connection
   */
  public int getMode(final int connectionNo)
      throws DWConnectionNotValidException {
    validateConn(connectionNo);
    return modes[connectionNo];
  }

  /**
   * Clear connection at index.
   *
   * @param connectionNo index
   * @throws DWConnectionNotValidException invalid connection
   */
  public void clearConn(final int connectionNo)
      throws DWConnectionNotValidException {
    validateConn(connectionNo);
    sockets[connectionNo] = null;
    socketPorts[connectionNo] = -1;
  }

  /**
   * Clear listener at index.
   *
   * @param connectionNo index
   */
  public void clearListener(final int connectionNo) {
    serverSockets[connectionNo] = null;
    serverSocketPorts[connectionNo] = -1;
  }

  /**
   * Kill connection at index.
   *
   * @param connectionNo index
   * @throws DWConnectionNotValidException invalid connection
   */
  public void killConn(final int connectionNo)
      throws DWConnectionNotValidException {
    validateConn(connectionNo);
    try {
      sockets[connectionNo].close();
      LOGGER.debug("killed conn #" + connectionNo);
    } catch (IOException e) {
      LOGGER.debug(
          "IO error closing conn #" + connectionNo + ": " + e.getMessage()
      );
    }
    clearConn(connectionNo);
  }

  /**
   * Kill listener at index.
   *
   * @param connectionNo index
   * @throws DWConnectionNotValidException invalid connection
   */
  public void killListener(final int connectionNo)
      throws DWConnectionNotValidException {
    try {
      serverSockets[connectionNo].close();
      LOGGER.debug("killed listener #" + connectionNo);
    } catch (IOException e) {
      LOGGER.debug(
          "IO error closing listener #" + connectionNo + ": " + e.getMessage()
      );
    }
    clearListener(connectionNo);
  }

  /**
   * Get listener port at index.
   *
   * @param index index
   * @return server port
   */
  public int getListenerPort(final int index) {
    return serverSocketPorts[index];
  }

  /**
   * Get connection port at index.
   *
   * @param index index
   * @return port
   */
  public int getConnPort(final int index) {
    return socketPorts[index];
  }
}
