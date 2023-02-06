package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWDECBFileSystem;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidDirectoryException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdDiskDosList extends DWCommand {
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk Dos List command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskDosList(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("list");
    this.setShortHelp("List contents of DOS file");
    this.setUsage("dw disk dos list # filename");
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    String[] args = cmdline.split(" ");

    if (args.length == 2) {
      try {
        return doDiskDosList(
            dwProtocolHandler
                .getDiskDrives()
                .getDriveNoFromString(args[0]), args[1]
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
      } catch (DWFileSystemFileNotFoundException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_FILE_NOT_FOUND,
            e.getMessage()
        );
      } catch (DWFileSystemInvalidFATException
               | DWFileSystemInvalidDirectoryException
               | DWDiskInvalidSectorNumber e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,
            e.getMessage()
        );
      } catch (IOException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_IO_EXCEPTION,
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

  private DWCommandResponse doDiskDosList(
      final int driveNumber,
      final String filename
  ) throws DWDriveNotLoadedException,
      DWDriveNotValidException,
      DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    String res = "";

    DWDECBFileSystem tmp = new DWDECBFileSystem(
        dwProtocolHandler.getDiskDrives().getDisk(driveNumber)
    );
    res = new String(tmp.getFileContents(filename), DWDefs.ENCODING);
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
