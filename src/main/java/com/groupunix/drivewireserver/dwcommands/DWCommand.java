package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;

public abstract class DWCommand implements DWCmdHelp {
  protected DWCommandList commands = new DWCommandList(null);
  protected String commandName;
  protected String shortHelp;
  protected String usage;

  private DWCommand parentcmd = null;

  public String getCommand() {
    return commandName;
  }

  public DWCommandList getCommandList() {
    return (this.commands);
  }

  public DWCommandResponse parse(String cmdline) {
    return (new DWCommandResponse(false, DWDefs.RC_SERVER_NOT_IMPLEMENTED, "Not implemented (yet?)."));
  }

  public DWCommand getParentCmd() {
    return this.parentcmd;
  }

  public void setParentCmd(DWCommand cmd) {
    this.parentcmd = cmd;
  }

  public String getShortHelp() {
    return shortHelp;
  }

  public String getUsage() {
    return usage;
  }

  public boolean validate(String cmdline) {
    return false;
  }
}