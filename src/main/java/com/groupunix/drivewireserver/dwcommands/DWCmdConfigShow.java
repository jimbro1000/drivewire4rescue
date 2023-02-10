package com.groupunix.drivewireserver.dwcommands;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdConfigShow extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Configuration show command constructor.
   *
   * @param protocol protocol
   * @param parent   parent command
   */
  public DWCmdConfigShow(
      final DWProtocol protocol, final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("show");
    this.setShortHelp("Show current instance config (or item)");
    this.setUsage("dw config show [item]");
  }

  /**
   * parse command.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() > 0) {
      return (doShowConfig(cmdline));
    }
    return (doShowConfig());
  }

  private DWCommandResponse doShowConfig(final String item) {
    String text = "";
    if (dwProtocol.getConfig().containsKey(item)) {
      final String value = StringUtils.join(
          dwProtocol.getConfig().getStringArray(item), ", "
      );
      text += item + " = " + value;
      return new DWCommandResponse(text);
    } else {
      return new DWCommandResponse(
          false,
          DWDefs.RC_CONFIG_KEY_NOT_SET,
          "Key '" + item + "' is not set."
      );
    }
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

  @SuppressWarnings("unchecked")
  private DWCommandResponse doShowConfig() {
    final StringBuilder text = new StringBuilder(
        "Current protocol handler configuration:\r\n\n"
    );
    final Iterator<String> keys = dwProtocol.getConfig().getKeys();
    while (keys.hasNext()) {
      final String key = keys.next();
      final String value = dwProtocol.getConfig().getProperty(key).toString();
      text.append(key).append(" = ").append(value).append("\r\n");
    }
    return new DWCommandResponse(text.toString());
  }
}
