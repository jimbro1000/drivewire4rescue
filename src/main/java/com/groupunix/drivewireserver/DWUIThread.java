package com.groupunix.drivewireserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class DWUIThread implements Runnable {
  /**
   * class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DWUIThread.class);
  /**
   * tcp port for thread.
   */
  private int tcpPort;
  /**
   * waiting to die flag.
   */
  private boolean wantToDie = false;
  /**
   * owning server socket.
   */
  private ServerSocket serverSocket = null;
  /**
   * list of UI client threads.
   */
  private final LinkedList<DWUIClientThread> clientThreads = new LinkedList<>();
  /**
   * counter for dropped events.
   */
  private int droppedEvents = 0;
  /**
   * queue size at last count.
   */
  private int lastQueueSize;

  /**
   * DWUIThread.
   *
   * @param port specify tcp port number
   */
  public DWUIThread(final int port) {
    this.tcpPort = port;
  }

  /**
   * die.
   *
   * kill thread safely
   */
  public void die() {
    this.wantToDie = true;
    try {
      for (final DWUIClientThread ct : this.clientThreads) {
        ct.die();
      }
      if (this.serverSocket != null) {
        this.serverSocket.close();
      }
    } catch (IOException e) {
      LOGGER.warn("IO Error closing socket: " + e.getMessage());
    } catch (ConcurrentModificationException ignored) {
    }
  }

  /**
   * run.
   *
   * start thread action
   */
  public void run() {
    Thread.currentThread().setName(
        "dwUIserver-" + Thread.currentThread().getId()
    );
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    // open server socket
    try {
      // check for listen address
      serverSocket = new ServerSocket(this.tcpPort);
      LOGGER.info("UI listening on port " + serverSocket.getLocalPort());
    } catch (IOException e2) {
      LOGGER.error(
          "Error opening UI socket: "
              + e2.getClass().getSimpleName()
              + " " + e2.getMessage()
      );
      wantToDie = true;
      // hrmmmm
      if (
          DriveWireServer
              .getServerConfiguration()
              .getBoolean("UIorBust", true)
      ) {
        DriveWireServer.shutdown();
      }
    }

    while (!wantToDie && !serverSocket.isClosed()) {
      //logger.debug("UI waiting for connection");
      Socket skt = null;
      try {
        skt = serverSocket.accept();
        if (
            DriveWireServer
                .getServerConfiguration()
                .getBoolean("LogUIConnections", false)
        ) {
          LOGGER.debug(
              "new UI connection from "
                  + skt.getInetAddress().getHostAddress()
          );
        }
        final Thread uiclientthread = new Thread(
            new DWUIClientThread(skt, this.clientThreads)
        );
        uiclientthread.setDaemon(true);
        uiclientthread.start();
      } catch (IOException e1) {
        if (wantToDie) {
          LOGGER.debug("IO error (while dying): " + e1.getMessage());
        } else {
          LOGGER.warn("IO error: " + e1.getMessage());
        }
        wantToDie = true;
      }
    }

    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        LOGGER.error("error closing server socket: " + e.getMessage());
      }
    }
    LOGGER.debug("exiting");
  }

  /**
   * submitEvent.
   * <p>
   * push event to all registered clients
   *
   * @param evt event
   */
  public void submitEvent(final DWEvent evt) {
    synchronized (this.clientThreads) {
      for (final DWUIClientThread client : this.clientThreads) {
        // filter for instance
        if (
            (client.getInstance() == -1)
                || (client.getInstance() == evt.getEventInstance())
                || (evt.getEventInstance() == -1)
        ) {
          final LinkedBlockingQueue<DWEvent> queue = client.getEventQueue();
          synchronized (queue) {
            if (
                !(client.isDropLog()
                    && evt.getEventType() == DWDefs.EVENT_TYPE_LOG)
            ) {
              this.lastQueueSize = queue.size();
              if (queue.size() < DWDefs.EVENT_QUEUE_LOGDROP_SIZE) {
                queue.add(evt);
              } else if (
                  queue.size() < DWDefs.EVENT_MAX_QUEUE_SIZE
                      && evt.getEventType() != DWDefs.EVENT_TYPE_LOG
              ) {
                queue.add(evt);
              } else {
                this.droppedEvents++;
                System.out.println(
                    "queue drop: " + queue.size() + "/"
                        + this.droppedEvents + "  " + evt.getEventType()
                        + " thr " + client.getThreadName()
                        + " cmd " + client.getCurCmd()
                        + " state " + client.getState());
              }
            }
          }
        }
      }
    }
  }

  /**
   * getNumUIClients.
   * @return int total number of client threads
   */
  public int getNumUIClients() {
    return this.clientThreads.size();
  }

  /**
   * getQueueSize.
   * @return int queue size
   */
  public int getQueueSize() {
    return lastQueueSize;
  }
}
