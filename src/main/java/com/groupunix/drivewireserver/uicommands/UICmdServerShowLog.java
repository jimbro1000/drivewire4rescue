package com.groupunix.drivewireserver.uicommands;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowLog extends DWCommand {
  /**
   * UI Command Server Show Log.
   */
  public UICmdServerShowLog() {
    super();
    setCommand("log");
    setShortHelp("show log buffer");
    setUsage("ui server show log");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder txt = new StringBuilder();
    final ArrayList<String> log = DriveWireServer
        .getLogEvents(DriveWireServer.getLogEventsSize());
    for (final String l : log) {
      txt.append(l);
    }
    return new DWCommandResponse(txt.toString());
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
