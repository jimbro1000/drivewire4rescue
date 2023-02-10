package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskSet extends DWCommand {
  /**
   * Maximum number of command arguments.
   */
  public static final int MAX_COMMANDS = 3;
  /**
   * Drivewire protocol handler.
   */
  private DWProtocolHandler dwProtocolHandler;
  /**
   * Disk set command constructor.
   * @param protocolHandler protocol handler
   * @param parent command parent
   */
  public DWCmdDiskSet(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("set");
    this.setShortHelp("Set disk parameters");
    this.setUsage("dw disk set # param val");
  }

  /**
   * Parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final String[] args = cmdline.split(" ");
    if (args.length < MAX_COMMANDS) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw disk set requires 3 arguments."
      );
    }
    try {
      return doDiskSet(dwProtocolHandler
          .getDiskDrives()
          .getDriveNoFromString(args[0]), DWUtils.dropFirstToken(cmdline)
      );
    } catch (DWDriveNotValidException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_INVALID_DRIVE,
          e.getMessage()
      );
    }
  }

  private DWCommandResponse doDiskSet(
      final int driveNumber,
      final String cmdline
  ) {
    // driveNumber + param/val
    final String[] parts = cmdline.split(" ");
    // set item
    try {
      if (dwProtocolHandler
          .getDiskDrives()
          .getDisk(driveNumber)
          .getParams()
          .containsKey(parts[0])
      ) {
        dwProtocolHandler
            .getDiskDrives()
            .getDisk(driveNumber)
            .getParams()
            .setProperty(parts[0], DWUtils.dropFirstToken(cmdline));
        return new DWCommandResponse(
            "Param '" + parts[0]
                + "' set for disk " + driveNumber + "."
        );
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_DISK_DEF,
            "No parameter '" + parts[0]
                + "' available for disk " + driveNumber + "."
        );
      }
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
    }
  }

  /**
   * Validate command.
   * @param cmdline
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
