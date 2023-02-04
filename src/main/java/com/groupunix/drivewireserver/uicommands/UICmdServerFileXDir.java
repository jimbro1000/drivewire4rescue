package com.groupunix.drivewireserver.uicommands;

import java.io.File;

import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFilenameException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdServerFileXDir extends DWCommand {
  /**
   * UI Command Server File XDir.
   */
  public UICmdServerFileXDir() {
    setCommand("xdir");
    setShortHelp("List directory contents (short form)");
    setUsage("ui server file xdir [path]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    File dir = new File(cmdline);
    File[] contents = dir.listFiles();
    if (contents == null) {
      return new DWCommandResponse("");
    }
    StringBuilder text = new StringBuilder();
    for (File f : contents) {
      if (f.isDirectory()) {
        try {
          text.append(DWUtils.getFileXDescriptor(f)).append("\n");
        } catch (DWFileSystemInvalidFilenameException ignored) {
        }
      }
    }

    for (File f : contents) {
      if (!f.isDirectory()) {
        try {
          text.append(DWUtils.getFileXDescriptor(f)).append("\n");
        } catch (DWFileSystemInvalidFilenameException ignored) {
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
