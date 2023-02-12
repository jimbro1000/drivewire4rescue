package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceConfigSet extends DWCommand {
  /**
   * Client thread ref.
   */
  private DWUIClientThread dwuiClientThread = null;
  /**
   * Protocol.
   */
  private DWProtocol dwProtocol = null;

  /**
   * UI Command Instance Configuration Set.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceConfigSet(final DWUIClientThread clientThread) {
    super();
    this.dwuiClientThread = clientThread;
    setHelp();
  }

  /**
   * UI Command Instance Configuration Set.
   *
   * @param protocol protocol
   */
  public UICmdInstanceConfigSet(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    setHelp();
  }

  private void setHelp() {
    setCommand("set");
    setShortHelp("Set instance configuration item");
    setUsage("ui instance config set [item] [value]");
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
          "Must specify item"
      );
    }
    final String[] args = cmdline.split(" ");
    if (args.length == 1) {
      return doSetConfig(args[0]);
    } else {
      return doSetConfig(
          args[0],
          cmdline.substring(args[0].length() + 1)
      );
    }
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

  private DWCommandResponse doSetConfig(final String item) {
    if (this.dwuiClientThread != null) {
      if (DriveWireServer.getHandler(this.dwuiClientThread.getInstance())
          .getConfig()
          .containsKey(item)) {
        synchronized (DriveWireServer.getServerConfiguration()) {
          DriveWireServer.getHandler(this.dwuiClientThread.getInstance())
              .getConfig()
              .clearProperty(item);
        }
        return new DWCommandResponse(
            "Item '" + item + "' removed from config."
        );
      } else {
        return new DWCommandResponse(
            "Item '" + item + "' is not set."
        );
      }
    } else {
      if (dwProtocol.getConfig().containsKey(item)) {
        synchronized (DriveWireServer.getServerConfiguration()) {
          dwProtocol.getConfig().clearProperty(item);
        }
        return new DWCommandResponse(
            "Item '" + item + "' removed from config."
        );
      } else {
        return new DWCommandResponse(
            "Item '" + item + "' is not set."
        );
      }
    }
  }

  /**
   * Do set configuration.
   *
   * @param item  key
   * @param value value
   * @return command response
   */
  private DWCommandResponse doSetConfig(final String item, final String value) {
    synchronized (DriveWireServer.getServerConfiguration()) {
      if (this.dwuiClientThread != null) {
        DriveWireServer.getHandler(this.dwuiClientThread.getInstance())
            .getConfig()
            .setProperty(item, value);
      } else {
        dwProtocol.getConfig().setProperty(item, value);
      }
    }
    return new DWCommandResponse(
        "Item '" + item + "' set to '" + value + "'."
    );
  }
}
