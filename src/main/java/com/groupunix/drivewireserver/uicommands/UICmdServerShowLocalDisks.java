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

  public UICmdServerShowLocalDisks() {
    setCommand("localdisks");
    setShortHelp("show server local disks");
    setUsage("ui server show localdisks");
  }

  @Override
  public DWCommandResponse parse(String cmdline) {

    String res = new String();

    try {
      if (!DriveWireServer.getServerConfiguration().containsKey("LocalDiskDir"))
        return (new DWCommandResponse(false, DWDefs.RC_CONFIG_KEY_NOT_SET, "LocalDiskDir must be defined in configuration"));

      String path = DriveWireServer.getServerConfiguration().getString(
          "LocalDiskDir");

      FileSystemManager fsManager;

      fsManager = VFS.getManager();

      FileObject dirobj = fsManager.resolveFile(path);

      FileObject[] children = dirobj.getChildren();

      for (int i = 0; i < children.length; i++) {
				if (children[i].getType() == FileType.FILE)
					res += children[i].getName() + "\n";
			}

		} catch (IOException e) {
			return (new DWCommandResponse(false, DWDefs.RC_SERVER_IO_EXCEPTION, e.getMessage()));
		}

		return (new DWCommandResponse(res));
	}


	public boolean validate(String cmdline) {
		return (true);
	}
}
