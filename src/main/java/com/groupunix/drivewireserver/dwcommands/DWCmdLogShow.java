package com.groupunix.drivewireserver.dwcommands;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdLogShow extends DWCommand {

  /**
   * Command log show constructor.
   * @param parent parent command
   */
  public DWCmdLogShow(final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.setCommand("show");
    this.setShortHelp("Show last 20 (or #) log entries");
    this.setUsage("dw log show [#]");
  }
  /**
   * Parse command.
   * @param cmdline command line
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
    final StringBuilder text = new StringBuilder();
    try {
      final int lines = Integer.parseInt(lineCount);
      text.append("\r\nDriveWire Server Log (")
          .append(DriveWireServer.getLogEventsSize())
          .append(" events in buffer):\r\n\n");
      final ArrayList<String> logLines = DriveWireServer.getLogEvents(lines);
      for (final String logLine : logLines) {
        text.append(logLine);
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
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
