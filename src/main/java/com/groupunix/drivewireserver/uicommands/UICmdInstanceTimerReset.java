package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class UICmdInstanceTimerReset extends DWCommand {
  /**
   * Protocol.
   */
  private DWProtocol dwProtocol = null;
  /**
   * Client thread ref.
   */
  private DWUIClientThread dwuiClientThread;

  /**
   * UI Command Instance Timer Reset.
   *
   * @param protocol protocol
   */
  public UICmdInstanceTimerReset(final DWProtocol protocol) {
    this.dwProtocol = protocol;
    setHelp();
  }

  /**
   * UI Command Instance Timer Reset.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceTimerReset(final DWUIClientThread clientThread) {
    this.dwuiClientThread = clientThread;
    setHelp();
  }

  private void setHelp() {
    setCommand("reset");
    setShortHelp("reset instance timer");
    setUsage("ui instance timer reset [#]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Must specify timer #"
      );
    } else {
      String[] args = cmdline.split(" ");
      // TODO ASSumes we are using DW protocol
      if (this.dwProtocol == null) {
        dwProtocol = DriveWireServer
            .getHandler(this.dwuiClientThread.getInstance());
      }
      try {
        byte tno = (byte) Integer.parseInt(args[0]);
        this.dwProtocol.getTimers().resetTimer(tno);
        return new DWCommandResponse(
            "Reset timer " + (tno & BYTE_MASK)
        );
      } catch (NumberFormatException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SYNTAX_ERROR,
            "Timer # must be 0-255");
      }
    }
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
