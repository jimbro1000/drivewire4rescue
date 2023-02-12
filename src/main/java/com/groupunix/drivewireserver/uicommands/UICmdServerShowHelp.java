package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerShowHelp extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private DWProtocol dwProtocol;
  /**
   * UI client thread reference.
   */
  private final DWUIClientThread uiClientThreadRef;

  /**
   * UI Command Server Show Help.
   *
   * @param clientThread client thread reference
   */
  public UICmdServerShowHelp(final DWUIClientThread clientThread) {
    super();
    this.uiClientThreadRef = clientThread;
    this.dwProtocol = null;
    setHelp();
  }

  /**
   * UI Command Server Show Help.
   *
   * @param protocol protocol
   */
  public UICmdServerShowHelp(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    this.uiClientThreadRef = null;
    setHelp();
  }

  private void setHelp() {
    setCommand("help");
    setShortHelp("show help");
    setUsage("ui server show help topic");
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
      this.dwProtocol = DriveWireServer.getHandler(
          uiClientThreadRef.getInstance()
      );
    }
    if (dwProtocol.getHelp() == null) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_HELP_TOPIC_NOT_FOUND,
          "No help available"
      );
    }

    try {
      return new DWCommandResponse(dwProtocol.getHelp().getTopicText(cmdline));
    } catch (DWHelpTopicNotFoundException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_HELP_TOPIC_NOT_FOUND,
          e.getLocalizedMessage()
      );
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
