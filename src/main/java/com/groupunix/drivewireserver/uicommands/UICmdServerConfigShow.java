package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigShow extends DWCommand {

  /**
   * UI Cmd Server Config Show.
   */
  public UICmdServerConfigShow() {
    super();
    setCommand("show");
    setShortHelp("Show server configuration");
    setUsage("ui server config show [item]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @SuppressWarnings("unchecked")
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder res = new StringBuilder();
    if (cmdline.length() == 0) {
      final Iterator<String> iterator
          = DriveWireServer.getServerConfiguration().getKeys();
      while (iterator.hasNext()) {
        final String key = iterator.next();
        final String value = StringUtils.join(
            DriveWireServer.getServerConfiguration().getStringArray(key),
            ", "
        );
        res.append(key).append(" = ").append(value).append("\r\n");
      }
    } else {
      if (DriveWireServer.getServerConfiguration().containsKey(cmdline)) {
        final String value = StringUtils.join(
            DriveWireServer.getServerConfiguration().getStringArray(cmdline),
            ", "
        );
        return new DWCommandResponse(value);
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_CONFIG_KEY_NOT_SET,
            "Key '" + cmdline + "' is not set."
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
