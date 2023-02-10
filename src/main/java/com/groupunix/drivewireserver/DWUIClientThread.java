package com.groupunix.drivewireserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwcommands.DWCmd;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.uicommands.UICmd;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;
import static com.groupunix.drivewireserver.DWDefs.NEWLINE;

public class DWUIClientThread implements Runnable {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWUIClientThread");
  /**
   * Socket.
   */
  private final Socket socket;
  /**
   * Shutdown/dir flag.
   */
  private boolean wanttodie = false;
  /**
   * Instance id.
   */
  private int instance = -1;
  /**
   * Command list.
   */
  private final DWCommandList commands;
  /**
   * Event queue.
   */
  private final LinkedBlockingQueue<DWEvent> eventQueue
      = new LinkedBlockingQueue<>();
  /**
   * Client threads.
   */
  private final LinkedList<DWUIClientThread> clientThreads;
  /**
   * Output stream.
   */
  private BufferedOutputStream bufferedout;
  /**
   * Drop log flag.
   */
  private boolean droplog = true;
  /**
   * Thread name.
   */
  private String tname = "not set";
  /**
   * Thread command.
   */
  private String curcmd = "not set";
  /**
   * Thread state.
   */
  private String state = "not set";

  /**
   * UI Client Thread.
   *
   * @param skt socket
   * @param threads client threads
   */
  public DWUIClientThread(
      final Socket skt, final LinkedList<DWUIClientThread> threads
  ) {
    this.socket = skt;
    this.clientThreads = threads;
    commands = new DWCommandList(null);
    commands.addCommand(new UICmd(this));
  }

  /**
   * Run thread.
   */
  public void run() {
    this.state = "add to client threads";
    synchronized (this.clientThreads) {
      this.clientThreads.add(this);
    }
    this.tname = "dwUIcliIn-" + Thread.currentThread().getId();
    Thread.currentThread().setName(tname);
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    try {
      this.state = "get output stream";
      this.bufferedout = new BufferedOutputStream(socket.getOutputStream());
      // cmd loop
      StringBuilder cmd = new StringBuilder();
      while (!socket.isClosed() && !wanttodie) {
        this.state = "read from output stream";
        final int databyte = socket.getInputStream().read();

        if (databyte == -1) {
          //logger.debug("got -1 in input stream");
          wanttodie = true;
        } else {
          if (databyte == NEWLINE) {
            if (cmd.length() > 0) {
              this.state = "do cmd";
              doCmd(cmd.toString().trim());
              wanttodie = true;
              cmd = new StringBuilder();
            }
          } else if (databyte != CARRIAGE_RETURN) {
            cmd.append((char) databyte);
          }
        }
      }
      this.bufferedout.close();
      socket.close();
      this.state = "close socket";
    } catch (IOException e) {
      LOGGER.debug("IO Exception: " + e.getMessage());
    }
    this.state = "remove from client threads";
    synchronized (this.clientThreads) {
      this.clientThreads.remove(this);
    }
    this.state = "exit";
  }

  /**
   * Do command.
   *
   * @param cmd drivewire command
   * @throws IOException read/write failed
   */
  private void doCmd(final String cmd) throws IOException {
    this.curcmd = cmd;
    // grab instance
    final int div = cmd.indexOf(0);
    // malformed command
    if (div < 1) {
      sendUiResponse(new DWCommandResponse(
          false,
          DWDefs.RC_UI_MALFORMED_REQUEST,
          "Malformed UI request (no instance.. old UI?)"
      ));
      return;
    } else {
      // non numeric instance..
      try {
        this.setInstance(Integer.parseInt(cmd.substring(0, div)));
      } catch (NumberFormatException e) {
        sendUiResponse(new DWCommandResponse(
            false,
            DWDefs.RC_UI_MALFORMED_REQUEST,
            "Malformed UI request (bad instance)"
        ));
        return;
      }
      // invalid length
      if (cmd.length() < div + 2) {
        sendUiResponse(new DWCommandResponse(
            false,
            DWDefs.RC_UI_MALFORMED_REQUEST,
            "Malformed UI request (no command)"
        ));
        return;
      }
    }
    // strip instance
    final String trimmedCmd = cmd.substring(div + 1);
    if (DriveWireServer
        .getServerConfiguration()
        .getBoolean("LogUIConnections", false)
    ) {
      LOGGER.debug("UI command '" + trimmedCmd
          + "' for instance " + this.instance);
    }
    // wait for server/instance ready
    int waits = 0;
    while (!DriveWireServer.isReady()
        && waits < DWDefs.UITHREAD_SERVER_WAIT_TIME) {
      try {
        Thread.sleep(DWDefs.UITHREAD_WAIT_TICK);
        waits += DWDefs.UITHREAD_WAIT_TICK;
      } catch (InterruptedException e) {
        LOGGER.warn("Interrupted while waiting for server to be ready");
        sendUiResponse(new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_NOT_READY,
            "Interrupted while waiting for server to be ready"
        ));
        return;
      }
    }
    if (!DriveWireServer.isReady()) {
      LOGGER.warn("Timed out waiting for server to be ready");
      sendUiResponse(new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_NOT_READY,
          "Timed out waiting for server to be ready"
      ));
      return;
    }
    sendUiResponse(this.commands.parse(trimmedCmd));
    this.bufferedout.flush();
  }

  /**
   * Send user interface response.
   *
   * @param resp command response
   * @throws IOException read/write failure
   */
  private void sendUiResponse(final DWCommandResponse resp)
      throws IOException {
    if (DriveWireServer
        .getServerConfiguration()
        .getBoolean("LogUIConnections", false)
    ) {
      if (resp.getResponseCode() == 0) {
        LOGGER.debug("UI command success");
      } else {
        LOGGER.debug("UI command failed: #"
            + (BYTE_MASK + resp.getResponseCode()) + ": "
            + resp.getResponseText());
      }
    }
    // response header 0, (single byte RC), 0
    this.bufferedout.write(0);
    this.bufferedout.write(resp.getResponseCode() & BYTE_MASK);
    this.bufferedout.write(0);
    // data
    if (resp.isUseBytes() && resp.getResponseBytes() != null) {
      this.bufferedout.write(resp.getResponseBytes());
    } else if (resp.getResponseText() != null) {
      this.bufferedout.write(resp.getResponseText().getBytes(DWDefs.ENCODING));
    }
  }

  /**
   * Get handler instance.
   *
   * @return handler id
   */
  public int getInstance() {
    return this.instance;
  }

  /**
   * Set handler instance.
   *
   * @param handler handler id
   */
  public void setInstance(final int handler) {
    this.instance = handler;
    // valid instances get a dw cmd mapping
    if (DriveWireServer.isValidHandlerNo(handler)
        && !this.commands.validate("dw")) {
      this.commands.addCommand(
          new DWCmd(DriveWireServer.getHandler(handler))
      );
    }
  }

  /**
   * Get output stream.
   *
   * @return output stream
   * @throws IOException read failed
   */
  public BufferedOutputStream getOutputStream() throws IOException {
    return this.bufferedout;
  }

  /**
   * Get input stream.
   *
   * @return input stream
   * @throws IOException read failed
   */
  public BufferedInputStream getInputStream() throws IOException {
    return new BufferedInputStream(socket.getInputStream());
  }

  /**
   * Get attached socket.
   *
   * @return socket
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Set thread to die gracefully.
   */
  public synchronized void die() {
    wanttodie = true;
    if (this.socket != null) {
      try {
        this.socket.close();
      } catch (IOException ignored) {
      }
    }
  }

  /**
   * Get event queue.
   *
   * @return event queue
   */
  public LinkedBlockingQueue<DWEvent> getEventQueue() {
    return this.eventQueue;
  }

  /**
   * Is drop log flag set.
   *
   * @return true if set
   */
  public boolean isDropLog() {
    return this.droplog;
  }

  /**
   * Set drop log.
   *
   * @param flag drop log flag
   */
  public void setDropLog(final boolean flag) {
    this.droplog = flag;
  }

  /**
   * Get thread name.
   *
   * @return thread name
   */
  public String getThreadName() {
    return this.tname;
  }

  /**
   * Get current command.
   *
   * @return command
   */
  public String getCurCmd() {
    return this.curcmd;
  }

  /**
   * Get thread state.
   *
   * @return state
   */
  public String getState() {
    return this.state;
  }
}
