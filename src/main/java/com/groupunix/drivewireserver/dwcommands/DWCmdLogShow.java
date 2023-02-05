package com.groupunix.drivewireserver.dwcommands;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdLogShow extends DWCommand {

  /**
   * Commmand log show constructor.
   * @param parent
   */
  public DWCmdLogShow(final DWCommand parent) {
    setParentCmd(parent);
    this.setCommand("show");
    this.setShortHelp("Show last 20 (or #) log entries");
    this.setUsage("dw log show [#]");
  }
  /**
   * Parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return doShowLog("20");
    }
    return doShowLog(cmdline);
  }

  /**
   * Show log output.
   * @param lineCount lines of log to return
   * @return log lines
   */
  private DWCommandResponse doShowLog(final String lineCount) {
    StringBuilder text = new StringBuilder();
    try {
      int lines = Integer.parseInt(lineCount);
      text.append("\r\nDriveWire Server Log (")
          .append(DriveWireServer.getLogEventsSize())
          .append(" events in buffer):\r\n\n");
      ArrayList<String> logLines = DriveWireServer.getLogEvents(lines);
      for (int i = 0; i < logLines.size(); i++) {
        text.append(logLines.get(i));
      }
      return new DWCommandResponse(text.toString());
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: non numeric # of log lines"
      );
    }
  }

  /**
   * Validate command.
   * @param cmdline
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
