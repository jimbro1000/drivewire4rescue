package com.groupunix.drivewireserver.uicommands;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerShowTopics extends DWCommand {

  /**
   * Protocol.
   */
  private DWProtocol dwProtocol;
  /**
   * Client thread ref.
   */
  private final DWUIClientThread clientThreadRef;

  /**
   * UI Command Server Show Topics.
   * @param clientThread client thread ref
   */
  public UICmdServerShowTopics(final DWUIClientThread clientThread) {
    super();
    this.clientThreadRef = clientThread;
    this.dwProtocol = null;
    setHelp();
  }

  /**
   * UI Command Server Show Topics.
   *
   * @param protocol protocol
   */
  public UICmdServerShowTopics(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
    clientThreadRef = null;
    setHelp();
  }

  private void setHelp() {
    setCommand("topics");
    setShortHelp("show available help topics");
    setUsage("ui server show topics");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  @Override
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder txt = new StringBuilder();
    if (this.dwProtocol == null) {
      this.dwProtocol = DriveWireServer.getHandler(
          clientThreadRef.getInstance()
      );
    }
    final ArrayList<String> tops = dwProtocol.getHelp().getTopics(null);
    for (final String top : tops) {
      txt.append(top).append("\n");
    }
    return new DWCommandResponse(txt.toString());
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
