package com.groupunix.drivewireserver.uicommands;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowLocalDisks extends DWCommand {

  /**
   * UI Command Server Show Local Disks.
   */
  public UICmdServerShowLocalDisks() {
    setCommand("localdisks");
    setShortHelp("show server local disks");
    setUsage("ui server show localdisks");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder res = new StringBuilder();

    if (!DriveWireServer.getServerConfiguration()
        .containsKey("LocalDiskDir")) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_CONFIG_KEY_NOT_SET,
          "LocalDiskDir must be defined in configuration"
      );
    }
    try {
      String path = DriveWireServer.getServerConfiguration().getString(
          "LocalDiskDir");
      FileSystemManager fsManager;
      fsManager = VFS.getManager();
      FileObject dirobj = fsManager.resolveFile(path);
      FileObject[] children = dirobj.getChildren();
      for (FileObject child : children) {
        if (child.getType() == FileType.FILE) {
          res.append(child.getName()).append("\n");
        }
      }
    } catch (IOException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_IO_EXCEPTION,
          e.getMessage()
      );
    }
    return new DWCommandResponse(res.toString());
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
