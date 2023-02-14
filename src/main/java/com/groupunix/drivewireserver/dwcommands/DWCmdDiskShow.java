package com.groupunix.drivewireserver.dwcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCmdDiskShow extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk show command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent parent command
   */
  public DWCmdDiskShow(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("show");
    this.setShortHelp("Show current disk details");
    this.setUsage("dw disk show [#]");
  }

  /**
   * Parse command if present.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    // show loaded disks
    if (cmdline.length() == 0) {
      return doDiskShow();
    }
    final String[] args = cmdline.split(" ");
    if (args.length == 1) {
      try {
        return doDiskShow(
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
    return new DWCommandResponse(
        false,
        DWDefs.RC_SYNTAX_ERROR,
        "Syntax error"
    );
  }

  private DWCommandResponse doDiskShow(final int driveNumber) {
    String text;

    try {
      final DWDisk disk
          = dwProtocolHandler.getDiskDrives().getDisk(driveNumber);
      text = "Details for disk in drive #" + driveNumber + ":\r\n\r\n";
      text += DWUtils.shortenLocalURI(disk.getFilePath()) + "\r\n";
      text += "\r\n";

      // optional/warning type info
      if (disk.getParams().containsKey("_readerrors")) {
        text += "This drive reports "
            + disk.getParams().getInt("_readerrors")
            + " read errors.\r\n";
      }
      if (disk.getParams().containsKey("_writeerrors")) {
        text += "This drive reports "
            + disk.getParams().getInt("_writeerrors")
            + " write errors.\r\n";
      }
      if (disk.getDirtySectors() > 0) {
        text += "This drive reports "
            + disk.getDirtySectors()
            + " dirty sectors.\r\n";
      }
      final HierarchicalConfiguration params = disk.getParams();
      final ArrayList<String> ignores = new ArrayList<>();
      final ArrayList<String> syss = new ArrayList<>();
      final ArrayList<String> usrs = new ArrayList<>();

      ignores.add("_readerrors");
      ignores.add("_writeerrors");
      ignores.add("_path");
      ignores.add("_last_modified");

      final Iterator<String> itk = params.getKeys();
      while (itk.hasNext()) {
        final String param = itk.next();
        if (!ignores.contains(param)) {
          if (param.startsWith("_")) {
            syss.add(param.substring(1) + ": "
                + params.getProperty(param));
          } else {
            usrs.add(param + ": " + params.getProperty(param));
          }
        }
      }
      Collections.sort(syss);
      Collections.sort(usrs);
      text += "System params:\r\n";
      text += DWCommandList.colLayout(
          syss,
          this.dwProtocolHandler.getCMDCols()
      );
      text += "\r\nUser params:\r\n";
      text += DWCommandList.colLayout(
          usrs,
          this.dwProtocolHandler.getCMDCols()
      );
      return new DWCommandResponse(text);
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

  private DWCommandResponse doDiskShow() {
    final StringBuilder text = new StringBuilder();
    text.append("\r\nCurrent DriveWire disks:\r\n\r\n");
    for (int i = 0; i < dwProtocolHandler.getDiskDrives().getMaxDrives(); i++) {
      if (dwProtocolHandler.getDiskDrives().isLoaded(i)) {
        try {
          text.append(String.format("X%-3d", i));
          if (dwProtocolHandler.getDiskDrives().getDisk(i).isWriteProtect()) {
            text.append("*");
          } else {
            text.append(" ");
          }
          text.append(DWUtils.shortenLocalURI(
              dwProtocolHandler.getDiskDrives().getDisk(i).getFilePath()
          )).append("\r\n");
        } catch (DWDriveNotLoadedException | DWDriveNotValidException ignored) {
        }
      }
    }
    return new DWCommandResponse(text.toString());
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
