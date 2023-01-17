package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

public class DWCmdPortShow extends DWCommand {

  /**
   * drivewire protocol.
   */
  private final DWVSerialProtocol dwProtocol;

  /**
   * port show command constructor.
   * @param protocol serial protocol
   * @param parent command parent
   */
  public DWCmdPortShow(
      final DWVSerialProtocol protocol,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
  }

  /**
   * get command.
   * @return command name
   */
  public String getCommand() {
    return "show";
  }

  /**
   * get short help.
   * @return short help details
   */
  public String getShortHelp() {
    return "Show port status";
  }

  /**
   * get usage.
   * @return usage information
   */
  public String getUsage() {
    return "dw port show";
  }

  /**
   * parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doPortShow();
  }

  private DWCommandResponse doPortShow() {
    StringBuilder text = new StringBuilder();

    text.append("\r\nCurrent port status:\r\n\n");
    for (int i = 0; i < dwProtocol.getVPorts().getMaxPorts(); i++) {
      text.append(String.format("%6s", dwProtocol.getVPorts().prettyPort(i)));
      try {
        if (dwProtocol.getVPorts().isOpen(i)) {
          text.append(
              String.format(
                  " %-8s",
                  "open(" + dwProtocol.getVPorts().getOpen(i) + ")")
          );
          text.append(
              String.format(
                  " %-9s",
                  "buf: " + dwProtocol.getVPorts().bytesWaiting(i)
              )
          );
        } else {
          text.append(" closed");
        }
        if (dwProtocol.getVPorts().getUtilMode(i) != DWDefs.UTILMODE_UNSET) {
          text.append(DWUtils.prettyUtilMode(
              dwProtocol.getVPorts().getUtilMode(i))
          );
        }
      } catch (DWPortNotValidException e) {
        text.append(" Error: ").append(e.getMessage());
      }
      text.append("\r\n");
    }
    return new DWCommandResponse(text.toString());
  }

  /**
   * validate command.
   * @param cmdline
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
