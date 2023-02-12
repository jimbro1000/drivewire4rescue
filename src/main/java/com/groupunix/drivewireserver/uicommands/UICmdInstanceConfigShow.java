package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdInstanceConfigShow extends DWCommand {
  /**
   * UI Client thread reference.
   */
  private DWUIClientThread uiRef = null;
  /**
   * Associated protocol.
   */
  private DWProtocol dwProtocol = null;

  /**
   * I Command Instance Config Show.
   *
   * @param clientThread UI Client thread
   */
  public UICmdInstanceConfigShow(final DWUIClientThread clientThread) {
    super();
    this.uiRef = clientThread;
    setHelp();
  }

  /**
   * UI Command Instance Config Show.
   *
   * @param protocol Drivewire protocol
   */
  public UICmdInstanceConfigShow(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    setHelp();
  }

  private void setHelp() {
    setCommand("show");
    setShortHelp("Show instance configuration");
    setUsage("ui instance config show [item]");
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
    int instance;
    if (this.uiRef != null) {
      instance = this.uiRef.getInstance();
    } else {
      instance = this.dwProtocol.getHandlerNo();
    }
    if (cmdline.length() == 0) {
      final Iterator<String> iterator = DriveWireServer
          .getHandler(instance)
          .getConfig()
          .getKeys();
      while (iterator.hasNext()) {
        final String key = iterator.next();
        final String value = StringUtils.join(
            DriveWireServer
                .getHandler(instance)
                .getConfig()
                .getStringArray(key),
            ", "
        );
        res.append(key).append(" = ").append(value).append("\r\n");
      }
    } else {
      if (DriveWireServer
                .getHandler(instance)
                .getConfig()
                .containsKey(cmdline)) {
        final String value = StringUtils.join(
            DriveWireServer
                .getHandler(instance)
                .getConfig()
                .getStringArray(cmdline),
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
