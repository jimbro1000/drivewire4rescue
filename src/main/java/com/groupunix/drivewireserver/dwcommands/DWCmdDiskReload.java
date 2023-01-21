package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdDiskReload extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk reload command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskReload(
      final DWProtocolHandler protocolHandler, final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("reload");
    this.setShortHelp("Reload disk in drive #");
    this.setUsage("dw disk reload {# | all}");
  }

  /**
   * Parse command if present.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw disk reload requires a drive # or 'all' as an argument"
      );
    }
    return doDiskReload(cmdline);
  }

  private DWCommandResponse doDiskReload(final String driveId) {
    try {
      if (driveId.equals("all")) {
        dwProtocolHandler.getDiskDrives().ReLoadAllDisks();
        return new DWCommandResponse("All disks reloaded.");
      } else {
        dwProtocolHandler.getDiskDrives().ReLoadDisk(
            dwProtocolHandler
                .getDiskDrives()
                .getDriveNoFromString(driveId)
        );
        return new DWCommandResponse(
            "Disk in drive #" + driveId + " reloaded."
        );
      }
    } catch (NumberFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: non numeric drive #"
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
    } catch (DWImageFormatException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_IMAGE_FORMAT_EXCEPTION,
          e.getMessage()
      );
    }
  }

  /**
   * Validate command for start, stop, show and restart.
   *
   * @param cmdline command string
   * @return true if valid for all actions
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
