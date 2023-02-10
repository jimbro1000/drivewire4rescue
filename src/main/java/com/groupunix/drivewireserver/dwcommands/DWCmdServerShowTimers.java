package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCmdServerShowTimers extends DWCommand {
  /**
   * maximum timer index.
   */
  public static final int MAX_TIMER = 256;
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * server show timers command constructor.
   *
   * @param protocol protocol
   * @param parent   parent command
   */
  public DWCmdServerShowTimers(
      final DWProtocol protocol,
      final DWCommand parent
  ) {
    super();
    this.dwProtocol = protocol;
    setParentCmd(parent);
    this.setCommand("timers");
    this.setShortHelp("Show instance timers");
    this.setUsage("dw server show timers");
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder text = new StringBuilder();
    text.append("DriveWire instance timers (not shown == 0):\r\n\r\n");
    for (int i = 0; i < MAX_TIMER; i++) {
      if (dwProtocol.getTimers().getTimer((byte) i) > 0) {
        text.append(DWUtils.prettyTimer((byte) i))
            .append(": ")
            .append(dwProtocol.getTimers().getTimer((byte) i))
            .append("\r\n");
      }
    }
    return new DWCommandResponse(text.toString());
  }

  /**
   * validate command.
   *
   * @param cmdline command string
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
