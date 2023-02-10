package com.groupunix.drivewireserver.dwcommands;

import gnu.io.UnsupportedCommOperationException;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWSerialDevice;

public final class DWCmdServerTurbo extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Server turbo command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdServerTurbo(final DWProtocol protocol, final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("turbo");
    this.setShortHelp("Turn on DATurbo mode (testing only)");
    this.setUsage("dw server turbo");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    return doServerTurbo();
  }

  private DWCommandResponse doServerTurbo() {
    String text;
    final DWSerialDevice serialDevice =
        (DWSerialDevice) this.dwProtocol.getProtoDev();
    try {
      serialDevice.enableDATurbo();
      text = "Device is now in DATurbo mode";
    } catch (UnsupportedCommOperationException e) {
      text = "Failed to enable DATurbo mode: " + e.getMessage();
    }
    return new DWCommandResponse(text);
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
