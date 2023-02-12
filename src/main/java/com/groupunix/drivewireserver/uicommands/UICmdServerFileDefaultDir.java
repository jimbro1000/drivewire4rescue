package com.groupunix.drivewireserver.uicommands;

import java.io.File;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdServerFileDefaultDir extends DWCommand {

  /**
   * UI Command Server File Default Dir.
   */
  public UICmdServerFileDefaultDir() {
    super();
    setCommand("defaultdir");
    setShortHelp("Show default dir dir");
    setUsage("ui server file defaultdir");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return new DWCommandResponse(
        DWUtils.getFileDescriptor(new File(
            DriveWireServer.getServerConfiguration()
                .getString("LocalDiskDir", "."))
        ) + "|false"
    );
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
