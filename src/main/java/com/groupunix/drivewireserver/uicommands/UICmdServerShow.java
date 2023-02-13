package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerShow extends DWCommand {
  /**
   * UI Command Server Show.
   *
   * @param clientThread UI client thread
   */
  public UICmdServerShow(final DWUIClientThread clientThread) {
    super();
    final DWCommandList commands = prepareCommonCommands();
    commands.addCommand(new UICmdServerShowTopics(clientThread));
    commands.addCommand(new UICmdServerShowHelp(clientThread));
    commands.addCommand(new UICmdServerShowErrors(clientThread));
    setHelp();
  }

  /**
   * UI Command Server Show.
   *
   * @param protocol protocol
   */
  public UICmdServerShow(final DWProtocol protocol) {
    super();
    final DWCommandList commands = prepareCommonCommands();
    commands.addCommand(new UICmdServerShowTopics(protocol));
    commands.addCommand(new UICmdServerShowHelp(protocol));
    commands.addCommand(new UICmdServerShowErrors(protocol));
    setHelp();
  }

  private DWCommandList prepareCommonCommands() {
    final DWCommandList commands = this.getCommandList();
    commands.addCommand(new UICmdServerShowVersion());
    commands.addCommand(new UICmdServerShowInstances());
    commands.addCommand(new UICmdServerShowMIDIDevs());
    commands.addCommand(new UICmdServerShowSynthProfiles());
    commands.addCommand(new UICmdServerShowLocalDisks());
    commands.addCommand(new UICmdServerShowSerialDevs());
    commands.addCommand(new UICmdServerShowStatus());
    commands.addCommand(new UICmdServerShowNet());
    commands.addCommand(new UICmdServerShowLog());
    return commands;
  }

  private void setHelp() {
    this.setCommand("show");
    this.setShortHelp("Informational commands");
    this.setUsage("ui server show [item]");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return this.getCommandList().parse(cmdline);
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return this.getCommandList().validate(cmdline);
  }
}
