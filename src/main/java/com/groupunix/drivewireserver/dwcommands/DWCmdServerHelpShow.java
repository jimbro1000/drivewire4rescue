package com.groupunix.drivewireserver.dwcommands;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerHelpShow extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Server help show command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdServerHelpShow(
      final DWProtocol protocol,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("show");
    this.setShortHelp("Show help topic");
    this.setUsage("dw help show [topic]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return doShowHelp();
    }
    return doShowHelp(cmdline);
  }

  private DWCommandResponse doShowHelp(final String cmdline) {
    String text = "Help for " + cmdline + ":\r\n\r\n";

    try {
      text += dwProtocol.getHelp().getTopicText(cmdline);
      return new DWCommandResponse(text);
    } catch (DWHelpTopicNotFoundException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_CONFIG_KEY_NOT_SET,
          e.getMessage()
      );
    }
  }

  private DWCommandResponse doShowHelp() {
    final StringBuilder text = new StringBuilder("Help Topics:\r\n\r\n");
    final ArrayList<String> tops = dwProtocol.getHelp().getTopics(null);
    for (final String top : tops) {
      text.append(top).append("\r\n");
    }
    return new DWCommandResponse(text.toString());
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
