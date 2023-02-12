package com.groupunix.drivewireserver.uicommands;

import java.io.StringWriter;

import org.apache.commons.configuration.ConfigurationException;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigWrite extends DWCommand {
  /**
   * UI Command Server Configuration Write.
   */
  public UICmdServerConfigWrite() {
    super();
    setCommand("write");
    setShortHelp("Write config xml");
    setUsage("ui server config write");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final StringWriter writer = new StringWriter();
    try {
      DriveWireServer.getServerConfiguration().save(writer);
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    return new DWCommandResponse(writer.getBuffer().toString());
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
