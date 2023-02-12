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
    super();
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
    final File dir = new File(cmdline);
    final File[] contents = dir.listFiles();
    if (contents == null) {
      return new DWCommandResponse("");
    }
    final StringBuilder text = new StringBuilder();
    for (final File file : contents) {
      if (file.isDirectory()) {
        try {
          text.append(DWUtils.getFileXDescriptor(file)).append("\n");
        } catch (DWFileSystemInvalidFilenameException ignored) {
        }
      }
    }

    for (final File file : contents) {
      if (!file.isDirectory()) {
        try {
          text.append(DWUtils.getFileXDescriptor(file)).append("\n");
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
