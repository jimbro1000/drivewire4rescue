package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmd extends DWCommand {
  private void commandHelp() {
    setCommand("ui");
    setShortHelp("Management commands with machine parsable output");
    setUsage("ui [command]");
  }

  /**
   * ui command constructor.
   * @param clientThread ui client thread
   */
  public UICmd(final DWUIClientThread clientThread) {
    super();
    final DWCommandList commands = new DWCommandList(null);
    commands.addCommand(new UICmdInstance(clientThread));
    commands.addCommand(new UICmdServer(clientThread));
    commands.addCommand(new UICmdSync(clientThread));
    commands.addCommand(new UICmdTest(clientThread));
    this.setCommandList(commands);
    commandHelp();
  }

  /**
   * ui command constructor.
   * @param protocol protocol
   */
  public UICmd(final DWProtocol protocol) {
    super();
    final DWCommandList commands = new DWCommandList(null);
    commands.addCommand(new UICmdInstance(protocol));
    commands.addCommand(new UICmdServer(protocol));
    this.setCommandList(commands);
    commandHelp();
  }

  /**
   * parse command.
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(this.getCommandList().getShortHelp());
    }
    return getCommandList().parse(cmdline);
  }

  /**
   * validate command.
   * @param cmdline command line
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return getCommandList().validate(cmdline);
  }
}
