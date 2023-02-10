package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdDiskDosFormat extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk dos format command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskDosFormat(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("format");
    this.setShortHelp("Format disk image with DOS filesystem");
    this.setUsage("dw disk dos format #");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final String[] args = cmdline.split(" ");
    if (args.length == 1) {
      try {
        return doDiskDosCreate(
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
      } catch (DWInvalidSectorException
               | DWDriveWriteProtectedException
               | DWSeekPastEndOfDeviceException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_DRIVE_ERROR,
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

  private DWCommandResponse doDiskDosCreate(final int driveNumber)
      throws DWDriveNotValidException,
      DWDriveNotLoadedException,
      DWInvalidSectorException,
      DWSeekPastEndOfDeviceException,
      DWDriveWriteProtectedException,
      IOException {
    dwProtocolHandler.getDiskDrives().formatDOSFS(driveNumber);
    return new DWCommandResponse(
        "Formatted new DOS disk image in drive " + driveNumber + "."
    );
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
