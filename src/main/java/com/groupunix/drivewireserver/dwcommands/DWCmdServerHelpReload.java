package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerHelpReload extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Server help reload command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdServerHelpReload(
      final DWProtocol protocol,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("reload");
    this.setShortHelp("Reload help topics");
    this.setUsage("dw help reload");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doHelpReload();
  }


  private DWCommandResponse doHelpReload() {
    dwProtocol.getHelp().reload();
    return new DWCommandResponse("Reloaded help topics.");
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
