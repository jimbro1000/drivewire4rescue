package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCmdDiskCreate extends DWCommand {
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * disk create command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent          parent command
   */
  public DWCmdDiskCreate(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("create");
    this.setShortHelp("Create new disk image");
    this.setUsage("dw disk create # [path]");
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
        return doDiskCreate(
            dwProtocolHandler.getDiskDrives().getDriveNoFromString(args[0])
        );
      } catch (DWDriveNotValidException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_DRIVE,
            e.getMessage()
        );
      }
    } else if (args.length > 1) {
      // create disk
      try {
        return doDiskCreate(
            dwProtocolHandler.getDiskDrives().getDriveNoFromString(args[0]),
            DWUtils.dropFirstToken(cmdline)
        );
      } catch (DWDriveNotValidException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_DRIVE,
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

  private DWCommandResponse doDiskCreate(
      final int driveNumber, final String filepath
  ) {
    FileSystemManager fsManager;
    FileObject fileObject;

    try {
      // create file
      fsManager = VFS.getManager();
      fileObject = fsManager.resolveFile(filepath);
      if (fileObject.exists()) {
        fileObject.close();
        throw new IOException("File already exists");
      }
      fileObject.createFile();
      if (dwProtocolHandler.getDiskDrives().isLoaded(driveNumber)) {
        dwProtocolHandler.getDiskDrives().EjectDisk(driveNumber);
      }
      dwProtocolHandler.getDiskDrives().LoadDiskFromFile(driveNumber, filepath);
      return new DWCommandResponse(
          "New disk image created for drive " + driveNumber + "."
      );
    } catch (IOException e1) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_IO_EXCEPTION,
          e1.getMessage()
      );
    } catch (DWDriveNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_INVALID_DRIVE,
          e.getMessage()
      );
    } catch (DWDriveAlreadyLoadedException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_DRIVE_ALREADY_LOADED,
          e.getMessage()
      );
    } catch (DWDriveNotLoadedException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_DRIVE_NOT_LOADED,
          e.getMessage()
      );
    } catch (DWImageFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_IMAGE_FORMAT_EXCEPTION,
          e.getMessage()
      );
    }
  }

  private DWCommandResponse doDiskCreate(final int driveNumber) {
    try {
      if (dwProtocolHandler.getDiskDrives().isLoaded(driveNumber)) {
        dwProtocolHandler.getDiskDrives().EjectDisk(driveNumber);
      }
      dwProtocolHandler.getDiskDrives().createDisk(driveNumber);
    } catch (DWDriveNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_INVALID_DRIVE, e.getMessage()
      );
    } catch (DWDriveNotLoadedException e) {
      // dont care
    } catch (DWDriveAlreadyLoadedException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_DRIVE_ALREADY_LOADED,
          e.getMessage()
      );
    }
    return new DWCommandResponse(
        "New image created for drive " + driveNumber + "."
    );
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
