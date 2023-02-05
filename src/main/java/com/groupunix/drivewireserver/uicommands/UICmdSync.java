package com.groupunix.drivewireserver.uicommands;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWEvent;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;

public class UICmdSync extends DWCommand {
  /**
   * Log Appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWUtilUIThread");
  /**
   * Client thread ref.
   */
  private final DWUIClientThread dwuiClientThread;
  /**
   * Last event.
   */
  private final DWEvent lastevt;

  /**
   * UI Command Sync.
   *
   * @param clientThread client thread ref.
   */
  public UICmdSync(final DWUIClientThread clientThread) {
    this.dwuiClientThread = clientThread;
    this.lastevt = new DWEvent((byte) 0, -1);
    setCommand("sync");
    setShortHelp("Sync status (real time)");
    setUsage("ui sync");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    boolean wanttodie = false;
    LOGGER.debug("adding status sync client");
    try {
      dwuiClientThread.getOutputStream().write(CARRIAGE_RETURN);
      // bring client up to date...
      sendEvent(DriveWireServer.getServerStatusEvent());
      // ready for new log events
      this.dwuiClientThread.setDropLog(false);
    } catch (IOException e1) {
      LOGGER.debug("immediate I/O error: " + e1.getMessage());
      wanttodie = true;
    }
    while ((!wanttodie) && (!dwuiClientThread.getSocket().isClosed())) {
      try {
        sendEvent(this.dwuiClientThread.getEventQueue().take());
      } catch (InterruptedException | IOException e) {
        wanttodie = true;
      }
    }
    LOGGER.debug("removing status sync client");
    return new DWCommandResponse(
        false,
        DWDefs.RC_FAIL,
        "Sync closed"
    );
  }

  private void sendEvent(final DWEvent msg) throws IOException {
    for (String key : msg.getParamKeys()) {
      // only send changed params
      if (!lastevt.hasParam(key)
          || !lastevt.getParam(key).equals(msg.getParam(key))) {
        dwuiClientThread.getOutputStream().write(
            (key + ':' + msg.getParam(key)).getBytes()
        );
        dwuiClientThread.getOutputStream().write(CARRIAGE_RETURN);
        lastevt.setParam(key, msg.getParam(key));
      }
    }
    dwuiClientThread.getOutputStream().write(msg.getEventType());
    dwuiClientThread.getOutputStream().write(CARRIAGE_RETURN);
    dwuiClientThread.getOutputStream().flush();
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
