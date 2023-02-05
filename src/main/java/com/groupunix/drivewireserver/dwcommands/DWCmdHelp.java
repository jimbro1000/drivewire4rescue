package com.groupunix.drivewireserver.dwcommands;

public interface DWCmdHelp {
  /**
   * Get command name.
   *
   * @return command name
   */
  String getCommand();
  /**
   * Get short help information.
   *
   * @return short help text
   */
  String getShortHelp();
  /**
   * Get usage information.
   *
   * @return usage details
   */
  String getUsage();
}
