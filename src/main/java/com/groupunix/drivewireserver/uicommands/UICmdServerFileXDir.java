package com.groupunix.drivewireserver.uicommands;

import java.io.File;

import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFilenameException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdServerFileXDir extends DWCommand {

  public UICmdServerFileXDir() {
    setCommand("xdir");
    setShortHelp("List directory contents (short form)");
    setUsage("ui server file xdir [path]");
  }

  public DWCommandResponse parse(String cmdline) {
    File dir = new File(cmdline);

    String text = "";

    File[] contents = dir.listFiles();

    if (contents != null) {
      for (File f : contents) {
        if (f.isDirectory())
          try {
            text += DWUtils.getFileXDescriptor(f) + "\n";
          } catch (DWFileSystemInvalidFilenameException e) {

          }
      }

      for (File f : contents) {
        if (!f.isDirectory())
          try {
            text += DWUtils.getFileXDescriptor(f) + "\n";
          } catch (DWFileSystemInvalidFilenameException e) {
          }
      }

    }

    return (new DWCommandResponse(text));
  }

  public boolean validate(String cmdline) {
    return (true);
  }

}