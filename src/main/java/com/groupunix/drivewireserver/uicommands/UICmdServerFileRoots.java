package com.groupunix.drivewireserver.uicommands;

import java.io.File;

import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdServerFileRoots extends DWCommand {
  /**
   * UI Command Server File Roots.
   */
  public UICmdServerFileRoots() {
    setCommand("roots");
    setShortHelp("List filesystem roots");
    setUsage("ui server file roots");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    File[] roots = File.listRoots();
    StringBuilder text = new StringBuilder();
    for (File f : roots) {
      text.append(DWUtils.getFileDescriptor(f)).append("|true\n");
    }
    return new DWCommandResponse(text.toString());
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
