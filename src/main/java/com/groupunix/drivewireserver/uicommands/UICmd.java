package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmd extends DWCommand {
  /**
   * command list.
   */
  private final DWCommandList commands;

  private void commandHelp() {
    commandName = "ui";
    shortHelp = "Management commands with machine parsable output";
    usage = "ui [command]";
  }

  /**
   * ui command constructor.
   * @param clientThread ui client thread
   */
  public UICmd(final DWUIClientThread clientThread) {
    commands = new DWCommandList(null);
    commands.addcommand(new UICmdInstance(clientThread));
    commands.addcommand(new UICmdServer(clientThread));
    commands.addcommand(new UICmdSync(clientThread));
    commands.addcommand(new UICmdTest(clientThread));
    commandHelp();
  }

  /**
   * ui command constructor.
   * @param protocol protocol
   */
  public UICmd(final DWProtocol protocol) {
    commands = new DWCommandList(null);
    commands.addcommand(new UICmdInstance(protocol));
    commands.addcommand(new UICmdServer(protocol));
    commandHelp();
  }

  /**
   * get commands.
   * @return component command list
   */
  public DWCommandList getCommandList() {
    return (this.commands);
  }

  /**
   * parse command.
   * @param cmdline
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return (new DWCommandResponse(this.commands.getShortHelp()));
    }
    return (commands.parse(cmdline));
  }

  /**
   * validate command.
   * @param cmdline
   * @return true if command valid
   */
  public boolean validate(final String cmdline) {
    return (commands.validate(cmdline));
  }
}
