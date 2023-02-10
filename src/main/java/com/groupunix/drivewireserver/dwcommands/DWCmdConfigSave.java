package com.groupunix.drivewireserver.dwcommands;

import org.apache.commons.configuration.ConfigurationException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdConfigSave extends DWCommand {
  /**
   * Drivewire protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * Configuration save command constructor.
   *
   * @param protocol protocol
   * @param parent   parent command
   */
  public DWCmdConfigSave(
      final DWProtocol protocol, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("save");
    this.setShortHelp("Save configuration");
    this.setUsage("dw config save");
  }

  /**
   * parse command.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    try {
      DriveWireServer.saveServerConfig();
    } catch (ConfigurationException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_IO_EXCEPTION,
          e.getMessage()
      );
    }
    return new DWCommandResponse("Configuration saved.");
  }

  /**
   * validate command.
   *
   * @param cmdline command line
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
