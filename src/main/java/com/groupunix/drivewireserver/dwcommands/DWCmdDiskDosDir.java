package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWDECBFileSystem;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWDECBFileSystemDirEntry;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWFileSystemDirEntry;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidDirectoryException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdDiskDosDir extends DWCommand {
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk Dos Dir command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskDosDir(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    commandName = "dir";
    shortHelp = "Show DOS directory of disk in drive #";
    usage = "dw disk dos dir #";
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    String[] args = cmdline.split(" ");

    if (args.length == 1) {
      try {
        return doDiskDosDir(
            dwProtocolHandler.getDiskDrives().getDriveNoFromString(args[0])
        );
      } catch (DWDriveNotValidException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_DRIVE,
            e.getMessage()
        );
      } catch (DWDriveNotLoadedException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_DRIVE_NOT_LOADED,
            e.getMessage()
        );
      } catch (IOException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_IO_EXCEPTION,
            e.getMessage()
        );
      } catch (DWFileSystemInvalidDirectoryException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,
            e.getMessage()
        );
      }
    }
    return new DWCommandResponse(
        false,
        DWDefs.RC_SYNTAX_ERROR,
        "Syntax error"
    );
  }

  private DWCommandResponse doDiskDosDir(
      final int driveNumber
  ) throws DWDriveNotLoadedException,
      DWDriveNotValidException,
      IOException,
      DWFileSystemInvalidDirectoryException {
    String res = "Directory of drive " + driveNumber + "\r\n\r\n";

    DWDECBFileSystem tmp = new DWDECBFileSystem(
        dwProtocolHandler.getDiskDrives().getDisk(driveNumber)
    );
    ArrayList<String> dir = new ArrayList<>();
    for (DWFileSystemDirEntry e : tmp.getDirectory(null)) {
      if (((DWDECBFileSystemDirEntry) e).isUsed()) {
        dir.add(e.getFileName() + "." + e.getFileExt());
      }
    }
    Collections.sort(dir);
    res += DWCommandList.colLayout(dir, this.dwProtocolHandler.getCMDCols());
    return new DWCommandResponse(res);
  }

  /**
   * validate command.
   *
   * @param cmdline command string
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
