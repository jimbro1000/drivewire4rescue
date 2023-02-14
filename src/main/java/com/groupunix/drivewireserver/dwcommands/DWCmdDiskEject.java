package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskEject extends DWCommand {
  /**
   * protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * disk eject command constructor.
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskEject(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("eject");
    this.setShortHelp("Eject disk from drive #");
    this.setUsage("dw disk eject {# | all}");
  }

  /**
   * parse command.
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final String[] args = cmdline.split(" ");
    if (args.length == 1) {
      if (args[0].equals("all")) {
        // eject all disks
        return doDiskEjectAll();
      } else {
        // eject specified disk
        try {
          return doDiskEject(
              dwProtocolHandler.getDiskDrives().getDriveNoFromString(args[0])
          );
        } catch (DWDriveNotValidException e) {
          return new DWCommandResponse(
              false,
              DWDefs.RC_INVALID_DRIVE,
              e.getMessage()
          );
        }
      }
    }
    return new DWCommandResponse(
        false,
        DWDefs.RC_SYNTAX_ERROR,
        "Syntax error"
    );
  }

  private DWCommandResponse doDiskEjectAll() {
    dwProtocolHandler.getDiskDrives().ejectAllDisks();
    return new DWCommandResponse("Ejected all disks.\r\n");
  }

  private DWCommandResponse doDiskEject(final int driveNumber) {
    try {
      dwProtocolHandler.getDiskDrives().ejectDisk(driveNumber);
      return new DWCommandResponse(
          "Disk ejected from drive " + driveNumber + ".\r\n"
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
    }
  }



  /**
   * validate command.
   * @param cmdline command line
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
