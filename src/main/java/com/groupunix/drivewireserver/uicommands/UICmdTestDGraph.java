package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdTestDGraph extends DWCommand {
  /**
   * Disk sectors.
   */
  public static final int SECTORS = 630;
  /**
   * Pause between sectors.
   */
  private static final int DELAY_TO_NEXT_SECTOR = 15;
  /**
   * Disk ref 3.
   */
  private static final int DISK_NUM_3 = 3;
  /**
   * Disk ref 2.
   */
  private static final int DISK_NUM_2 = 2;
  /**
   * Disk ref 1.
   */
  private static final int DISK_NUM_1 = 1;
  /**
   * Disk ref 0.
   */
  private static final int DISK_NUM_0 = 0;
  /**
   * Client thread ref.
   */
  private final DWUIClientThread dwuiClientThread;

  /**
   * UI Command Test Disk Graphics.
   *
   * @param clientThread client thread ref
   */
  public UICmdTestDGraph(final DWUIClientThread clientThread) {
    this.dwuiClientThread = clientThread;
    setCommand("dgraph");
    setShortHelp("Test disk graphics");
    setUsage("ui test dgraph");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    DriveWireServer.submitDiskEvent(
        this.dwuiClientThread.getInstance(),
        DISK_NUM_0,
        "_sectors",
        SECTORS + ""
    );
    for (int i = 0; i < SECTORS; i++) {
      DriveWireServer.submitDiskEvent(
          this.dwuiClientThread.getInstance(),
          DISK_NUM_0,
          "_lsn",
          i + ""
      );
      if (i % 2 == 0) {
        DriveWireServer.submitDiskEvent(
            this.dwuiClientThread.getInstance(),
            DISK_NUM_0,
            "_writes",
            i + ""
        );
      } else {
        DriveWireServer.submitDiskEvent(
            this.dwuiClientThread.getInstance(),
            DISK_NUM_0,
            "_reads",
            i + ""
        );
      }
      int rndsec = (int) (Math.random() * SECTORS);
      DriveWireServer.submitDiskEvent(
          this.dwuiClientThread.getInstance(),
          DISK_NUM_1,
          "_lsn",
          rndsec + ""
      );
      DriveWireServer.submitDiskEvent(
          this.dwuiClientThread.getInstance(),
          DISK_NUM_1,
          "_reads",
          1 + ""
      );
      rndsec = (int) (Math.random() * SECTORS);
      DriveWireServer.submitDiskEvent(
          this.dwuiClientThread.getInstance(),
          DISK_NUM_2,
          "_lsn",
          rndsec + ""
      );
      DriveWireServer.submitDiskEvent(
          this.dwuiClientThread.getInstance(),
          DISK_NUM_2,
          "_reads",
          rndsec + ""
      );
      rndsec = (int) (Math.random() * SECTORS);
      DriveWireServer.submitDiskEvent(
          this.dwuiClientThread.getInstance(),
          DISK_NUM_3,
          "_lsn",
          rndsec + ""
      );
      DriveWireServer.submitDiskEvent(
          this.dwuiClientThread.getInstance(),
          DISK_NUM_3,
          "_reads",
          1 + ""
      );
      try {
        Thread.sleep(DELAY_TO_NEXT_SECTOR);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }
    return new DWCommandResponse(
        "Test data submitted at: " + System.currentTimeMillis()
    );
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
