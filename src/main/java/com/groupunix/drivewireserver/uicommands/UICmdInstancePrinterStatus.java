package com.groupunix.drivewireserver.uicommands;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstancePrinterStatus extends DWCommand {

  /**
   * Client thread reference.
   */
  private final DWUIClientThread uiClientRef;
  /**
   * Protocol handler.
   */
  private DWProtocolHandler dwProtocolHandler;

  /**
   * UI Command Instance Printer Status.
   *
   * @param clientThread UI client thread
   */
  public UICmdInstancePrinterStatus(final DWUIClientThread clientThread) {
    super();
    this.uiClientRef = clientThread;
    this.dwProtocolHandler = null;
    setHelp();
  }

  /**
   * UI Command Instance Printer Status.
   *
   * @param protocol protocol handler
   */
  public UICmdInstancePrinterStatus(final DWProtocolHandler protocol) {
    super();
    this.dwProtocolHandler = protocol;
    this.uiClientRef = null;
    setHelp();
  }

  private void setHelp() {
    setCommand("printerstatus");
    setShortHelp("show printer status");
    setUsage("ui instance printerstatus");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    if (dwProtocolHandler == null) {
      if (
          DriveWireServer.getHandler(
              this.uiClientRef.getInstance()
          ).hasPrinters()
      ) {
        dwProtocolHandler = (DWProtocolHandler) DriveWireServer.getHandler(
            this.uiClientRef.getInstance()
        );
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INSTANCE_WONT,
            "This operation is not supported on this type of instance"
        );
      }
    }
    final StringBuilder res = new StringBuilder();
    res.append("currentprinter|")
        .append(dwProtocolHandler.getConfig().getString(
        "CurrentPrinter", "none"))
        .append("\r\n");

    @SuppressWarnings("unchecked")
    final List<HierarchicalConfiguration> profiles
        = dwProtocolHandler.getConfig().configurationsAt("Printer");
    for (final HierarchicalConfiguration mProfile : profiles) {
      res.append("printer|")
          .append(mProfile.getString("[@name]"))
          .append("|")
          .append(mProfile.getString("[@desc]"))
          .append("\r\n");
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
