package com.groupunix.drivewireserver.dwcommands;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.vfs2.FileSystemException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCmdDiskInsert extends DWCommand {
  /**
   * Drivewire Protocol Handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk insert command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent          parent command
   */
  public DWCmdDiskInsert(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("insert");
    this.setShortHelp("Load disk into drive #");
    this.setUsage("dw disk insert # path");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final String[] args = cmdline.split(" ");

    if (args.length > 1) {
      // insert disk
      try {
        return doDiskInsert(
            dwProtocolHandler
                .getDiskDrives()
                .getDriveNoFromString(args[0]),
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

  private DWCommandResponse doDiskInsert(
      final int driveNumber,
      final String path
  ) {
    // hack for os9 vs URLs
    try {
      // load new disk
      dwProtocolHandler.getDiskDrives().loadDiskFromFile(
          driveNumber,
          DWUtils.convertStarToBang(path)
      );
      return new DWCommandResponse(
          "Disk inserted in drive " + driveNumber + "."
      );
    } catch (DWDriveNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_INVALID_DRIVE,
          e.getMessage()
      );
    } catch (FileSystemException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,
          e.getMessage()
      );
    } catch (DWDriveAlreadyLoadedException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_DRIVE_ALREADY_LOADED,
          e.getMessage()
      );
    } catch (FileNotFoundException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_FILE_NOT_FOUND,
          e.getMessage()
      );
    } catch (IOException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_IO_EXCEPTION,
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

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
