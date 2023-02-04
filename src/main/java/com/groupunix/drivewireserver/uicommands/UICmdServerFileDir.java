package com.groupunix.drivewireserver.uicommands;

import java.io.File;

import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdServerFileDir extends DWCommand {

  /**
   * UI Command Server File Dir.
   */
  public UICmdServerFileDir() {
    setCommand("dir");
    setShortHelp("List directory contents");
    setUsage("ui server file dir [path]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    File dir = new File(cmdline);
    StringBuilder text = new StringBuilder();
    File[] contents = dir.listFiles();

    if (contents != null) {
      for (File f : contents) {
        if (f.isDirectory()) {
          text.append(DWUtils.getFileDescriptor(f)).append("|false\n");
        }
      }
      for (File f : contents) {
        if (!f.isDirectory()) {
          text.append(DWUtils.getFileDescriptor(f)).append("|false\n");
        }
      }
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
