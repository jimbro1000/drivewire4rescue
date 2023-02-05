package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceDiskShow extends DWCommand {
  /**
   * Client thread ref.
   */
  private DWUIClientThread dwuiClientThread = null;

  /**
   * Protocol handler.
   */
  private DWProtocolHandler dwProtocolHandler = null;

  /**
   * UI Command Instance Disk Show.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceDiskShow(final DWUIClientThread clientThread) {
    this.dwuiClientThread = clientThread;
    setHelp();
  }

  /**
   * UI Command Instance Disk Show.
   *
   * @param protocolHandler protocol handler
   */
  public UICmdInstanceDiskShow(final DWProtocolHandler protocolHandler) {
    this.dwProtocolHandler = protocolHandler;
    setHelp();
  }

  private void setHelp() {
    setCommand("show");
    setShortHelp("Show current disks");
    setUsage("ui instance disk show");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @SuppressWarnings("unchecked")
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder res = new StringBuilder();

    // TODO hackish!
    if (this.dwProtocolHandler == null) {
      if (DriveWireServer.getHandler(this.dwuiClientThread.getInstance())
          .hasDisks()) {
        dwProtocolHandler = (DWProtocolHandler) DriveWireServer
            .getHandler(this.dwuiClientThread.getInstance());
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INSTANCE_WONT,
            "This operation is not supported on this type of instance"
        );
      }
    }
    if (cmdline.length() == 0) {
      if (dwProtocolHandler.getDiskDrives() == null) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_NO_SUCH_DISKSET,
            "Disk drives are null, is server restarting?"
        );
      }
      for (
          int i = 0; i < dwProtocolHandler.getDiskDrives().getMaxDrives(); i++
      ) {
        if (dwProtocolHandler.getDiskDrives().isLoaded(i)) {
          try {
            res.append(i)
                .append("|")
                .append(dwProtocolHandler.getDiskDrives()
                    .getDisk(i)
                    .getFilePath())
                .append("\n");
          } catch (DWDriveNotLoadedException | DWDriveNotValidException e) {
            e.printStackTrace();
          }
        }
      }
    } else {
      // disk details
      try {
        int driveno = Integer.parseInt(cmdline);
        if ((!(dwProtocolHandler.getDiskDrives() == null))
            && (dwProtocolHandler.getDiskDrives().isLoaded(driveno))) {
          res.append("*loaded|true\n");
          HierarchicalConfiguration disk = dwProtocolHandler.getDiskDrives()
              .getDisk(driveno)
              .getParams();
          Iterator<String> itk = disk.getKeys();
          while (itk.hasNext()) {
            String option = itk.next();
            res.append(option)
                .append("|")
                .append(disk.getProperty(option))
                .append("\n");
          }
        } else {
          res.append("*loaded|false\n");
        }
      } catch (NumberFormatException e) {
        return (new DWCommandResponse(
            false,
            DWDefs.RC_SYNTAX_ERROR,
            "Non numeric drive number")
        );
      } catch (DWDriveNotLoadedException e) {
        res.append("*loaded|false\n");
      } catch (DWDriveNotValidException e) {
        return (new DWCommandResponse(
            false, DWDefs.RC_INVALID_DRIVE, e.getMessage())
        );
      }
    }
    return new DWCommandResponse(res.toString());
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
