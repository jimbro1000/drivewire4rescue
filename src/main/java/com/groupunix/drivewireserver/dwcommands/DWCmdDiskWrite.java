package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWImageHasNoSourceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskWrite extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk write command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskWrite(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("write");
    this.setShortHelp("Write disk image in drive #");
    this.setUsage("dw disk write # [path]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error"
      );
    }
    final String[] args = cmdline.split(" ");
    if (args.length == 1) {
      try {
        return doDiskWrite(
            dwProtocolHandler.getDiskDrives().getDriveNoFromString(args[0])
        );
      } catch (DWDriveNotValidException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_DRIVE,
            e.getMessage()
        );
      }
    } else {
      try {
        return doDiskWrite(
            dwProtocolHandler
                .getDiskDrives()
                .getDriveNoFromString(args[0]), DWUtils.dropFirstToken(cmdline)
        );
      } catch (DWDriveNotValidException e) {
        // it's an int, but it's not a valid drive
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_DRIVE,
            "Invalid drive number."
        );
      }
    }
  }

  private DWCommandResponse doDiskWrite(final int driveNumber) {
    try {
      dwProtocolHandler.getDiskDrives().writeDisk(driveNumber);
      return new DWCommandResponse(
          "Wrote disk #" + driveNumber + " to source image."
      );
    } catch (IOException e1) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_IO_EXCEPTION,
          e1.getMessage()
      );
    } catch (DWDriveNotLoadedException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_DRIVE_NOT_LOADED,
          e.getMessage()
      );
    } catch (DWDriveNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_INVALID_DRIVE,
          e.getMessage()
      );
    } catch (DWImageHasNoSourceException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_FILE_NOT_FOUND,
          e.getMessage()
      );
    }
  }

  private DWCommandResponse doDiskWrite(
      final int driveNumber, final String path
  ) {
    final String safePath = DWUtils.convertStarToBang(path);
    try {
      System.out.println("write " + safePath);
      dwProtocolHandler.getDiskDrives().writeDisk(driveNumber, safePath);
      return new DWCommandResponse(
          "Wrote disk #" + driveNumber + " to '" + safePath + "'"
      );
    } catch (IOException e1) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_IO_EXCEPTION,
          e1.getMessage()
      );
    } catch (DWDriveNotLoadedException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_DRIVE_NOT_LOADED,
          e.getMessage()
      );
    } catch (DWDriveNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_INVALID_DRIVE,
          e.getMessage());
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
