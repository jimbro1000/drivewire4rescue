package com.groupunix.drivewireserver.uicommands;

import java.util.List;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerShowErrors extends DWCommand {

  /**
   * Protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * UI Command Server Show Errors.
   *
   * @param clientThread client thread ref
   */
  public UICmdServerShowErrors(final DWUIClientThread clientThread) {
    this.dwProtocol = DriveWireServer.getHandler(clientThread.getInstance());
    setHelp();
  }

  /**
   * UI Command Server Show Errors.
   *
   * @param protocol protocol
   */
  public UICmdServerShowErrors(final DWProtocol protocol) {
    this.dwProtocol = protocol;
    setHelp();
  }

  private void setHelp() {
    setCommand("errors");
    setShortHelp("show error descriptions");
    setUsage("ui server show errors");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder res = new StringBuilder();
    if (dwProtocol != null) {
      List<String> rconfs = dwProtocol.getHelp().getSectionTopics("resultcode");
      if (rconfs != null) {
        for (String rc : rconfs) {
          try {
            res.append(rc.substring(1))
                .append("|")
                .append(dwProtocol.getHelp()
                    .getTopicText("resultcode." + rc).trim())
                .append("\r\n");
          } catch (DWHelpTopicNotFoundException ignored) {
          }
        }
        return new DWCommandResponse(res.toString());
      }
    }
    return new DWCommandResponse(
        false,
        DWDefs.RC_HELP_TOPIC_NOT_FOUND,
        "No error descriptions available from server"
    );
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
