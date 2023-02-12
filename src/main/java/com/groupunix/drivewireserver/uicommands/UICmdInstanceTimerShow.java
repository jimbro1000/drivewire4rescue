package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class UICmdInstanceTimerShow extends DWCommand {
  /**
   * Maximum number of timers.
   */
  public static final int MAX_TIMERS = 256;
  /**
   * Protocol.
   */
  private DWProtocol dwProtocol;
  /**
   * Client thread ref.
   */
  private final DWUIClientThread dwuiClientThread;

  /**
   * UI Command Instance Timer Show.
   *
   * @param protocol protocol
   */
  public UICmdInstanceTimerShow(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    this.dwuiClientThread = null;
    setHelp();
  }

  /**
   * UI Command Instance Timer Show.
   *
   * @param clientThread client thread ref
   */
  public UICmdInstanceTimerShow(final DWUIClientThread clientThread) {
    super();
    this.dwuiClientThread = clientThread;
    this.dwProtocol = null;
    setHelp();
  }

  private void setHelp() {
    setCommand("show");
    setShortHelp("show instance timer(s)");
    setUsage("ui instance timer show {#}");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    if (this.dwProtocol == null) {
      if (
          DriveWireServer
              .isValidHandlerNo(this.dwuiClientThread.getInstance())
      ) {
        dwProtocol = DriveWireServer
            .getHandler(this.dwuiClientThread.getInstance());
      } else {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INSTANCE_WONT,
            "The operation is not supported by this instance"
        );
      }
    }
    if (cmdline.length() == 0) {
      final StringBuilder txt = new StringBuilder();
      for (int i = 0; i < MAX_TIMERS; i++) {
        if (dwProtocol.getTimers().getTimer((byte) i) > 0) {
          txt.append(getTimerData((byte) i)).append("\r\n");
        }
      }
      return new DWCommandResponse(txt.toString());
    } else {
      final String[] args = cmdline.split(" ");
      try {
        final byte tno = (byte) Integer.parseInt(args[0]);
        return new DWCommandResponse(getTimerData(tno));
      } catch (NumberFormatException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SYNTAX_ERROR,
            "Timer # must be 0-255"
        );
      }
    }
  }

  private String getTimerData(final byte tno) {
    return (tno & BYTE_MASK) + "|" + DWUtils.prettyTimer(tno) + "|"
        + dwProtocol.getTimers().getTimer(tno);
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
