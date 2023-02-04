package com.groupunix.drivewireserver.uicommands;

import java.io.File;

import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdServerFileRoots extends DWCommand {
  public UICmdServerFileRoots() {
    setCommand("roots");
    setShortHelp("List filesystem roots");
    setUsage("ui server file roots");
  }

  public DWCommandResponse parse(String cmdline) {

    File[] roots = File.listRoots();

    String text = "";

    for (File f : roots) {
      text += DWUtils.getFileDescriptor(f) + "|true\n";
    }

    return (new DWCommandResponse(text));
  }


  public boolean validate(String cmdline) {
    return (true);
  }

}