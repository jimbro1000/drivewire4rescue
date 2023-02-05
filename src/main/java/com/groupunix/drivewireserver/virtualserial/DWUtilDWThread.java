package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwcommands.DWCmd;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import com.groupunix.drivewireserver.uicommands.UICmd;

public class DWUtilDWThread implements Runnable {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWUtilDWThread");
  /**
   * Delay on output stream polls.
   */
  private static final int POLL_OUTPUT_DELAY = 100;
  /**
   * Virtual port.
   */
  private final int vport;
  /**
   * Arguments string.
   */
  private final String strargs;
  /**
   * Serial ports.
   */
  private final DWVSerialPorts dwVSerialPorts;
  /**
   * Commands list.
   */
  private final DWCommandList commands;
  /**
   * Protect flag.
   */
  private boolean protect = false;

  /**
   * Util DW Thread.
   *
   * @param serialProtocol serial protocol
   * @param virtualPort    virtual port
   * @param args           arguments
   */
  public DWUtilDWThread(final DWVSerialProtocol serialProtocol,
                        final int virtualPort,
                        final String args) {
    this.vport = virtualPort;
    this.strargs = args;
    this.dwVSerialPorts = serialProtocol.getVPorts();

    if (virtualPort <= this.dwVSerialPorts.getMaxPorts()) {
      this.protect = serialProtocol.getConfig()
          .getBoolean("ProtectedMode", false);
    }
    commands = new DWCommandList(serialProtocol, serialProtocol.getCMDCols());
    commands.addCommand(new DWCmd(serialProtocol));
    commands.addCommand(new UICmd(serialProtocol));
    LOGGER.debug("init dw util thread (protected mode: " + this.protect + ")");
  }

  /**
   * Run threads.
   */
  public void run() {
    Thread.currentThread().setName("dwutil-" + Thread.currentThread().getId());
    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    LOGGER.debug("run for port " + vport);
    try {
      this.dwVSerialPorts.markConnected(vport);
      this.dwVSerialPorts.setUtilMode(this.vport, DWDefs.UTILMODE_DWCMD);
      DWCommandResponse resp = commands.parse(this.strargs);
      if (resp.getSuccess()) {
        if (resp.isUseBytes()) {
          dwVSerialPorts.sendUtilityOKResponse(
              this.vport, resp.getResponseBytes()
          );
        } else {
          dwVSerialPorts.sendUtilityOKResponse(
              this.vport, resp.getResponseText()
          );
        }
      } else {
        dwVSerialPorts.sendUtilityFailResponse(
            this.vport, resp.getResponseCode(), resp.getResponseText()
        );
      }
      // wait for output to flush
      while ((dwVSerialPorts.bytesWaiting(this.vport) > 0)
          && (dwVSerialPorts.isOpen(this.vport))) {
        LOGGER.debug("pause for the cause: "
            + dwVSerialPorts.bytesWaiting(this.vport) + " bytes left");
        Thread.sleep(POLL_OUTPUT_DELAY);
      }
      if (this.vport < this.dwVSerialPorts.getMaxPorts()) {
        dwVSerialPorts.closePort(this.vport);
      }
    } catch (InterruptedException | DWPortNotValidException e) {
      LOGGER.error(e.getMessage());
    }
    LOGGER.debug("exiting");
  }
}
