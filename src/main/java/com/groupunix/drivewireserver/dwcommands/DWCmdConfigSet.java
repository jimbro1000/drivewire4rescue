package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public final class DWCmdConfigSet extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Configuration set command constructor.
   *
   * @param protocol protcol
   * @param parent   parent command
   */
  public DWCmdConfigSet(
      final DWProtocol protocol, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("set");
    this.setShortHelp("Set config item, omit value to remove item");
    this.setUsage("dw config set item [value]");
  }

  /**
   * parse command.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Syntax error: dw config set requires an item and value as arguments"
      );
    }
    final String[] args = cmdline.split(" ");
    if (args.length == 1) {
      return doSetConfig(args[0]);
    } else {
      return doSetConfig(
          args[0], cmdline.substring(args[0].length() + 1)
      );
    }
  }

  private DWCommandResponse doSetConfig(final String item) {
    if (dwProtocol.getConfig().containsKey(item)) {
      synchronized (DriveWireServer.getServerConfiguration()) {
        dwProtocol.getConfig().clearProperty(item);
      }
    }
    return new DWCommandResponse(
        "Item '" + item + "' removed from config"
    );
  }

  private DWCommandResponse doSetConfig(
      final String item, final String value
  ) {
    synchronized (DriveWireServer.getServerConfiguration()) {
      dwProtocol.getConfig().setProperty(item, value);
    }
    return new DWCommandResponse(
        "Item '" + item + "' set to '" + value + "'"
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
