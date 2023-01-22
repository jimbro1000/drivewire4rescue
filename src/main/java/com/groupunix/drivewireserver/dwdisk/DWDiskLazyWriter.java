package com.groupunix.drivewireserver.dwdisk;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWDiskLazyWriter implements Runnable {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWProtoReader");
  /**
   * Lazy write sleep interval in milliseconds.
   */
  private static final int DISK_LAZY_WRITE_SLEEP_INTERVAL = 5000;
  /**
   * Lazy write interval in milliseconds.
   */
  private static final int WRITE_INTERVAL = 15000;
  /**
   * Thread waiting to die.
   */
  private boolean wantToDie = false;
  /**
   * Lazy disk in sync.
   */
  private boolean inSync = false;

  /**
   * Run thread.
   */
  public void run() {
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    Thread.currentThread().setName(
        "dskwriter-" + Thread.currentThread().getId()
    );
    LOGGER.debug(
        "started, write interval is "
            + DriveWireServer.serverConfiguration.getLong(
            "DiskLazyWriteInterval",
            WRITE_INTERVAL
        )
    );
    while (!wantToDie) {
      try {
        LOGGER.debug(
            "sleeping for "
                + DriveWireServer.serverConfiguration.getLong(
                "DiskLazyWriteInterval",
                DISK_LAZY_WRITE_SLEEP_INTERVAL
            ) + " ms..."
        );
        Thread.sleep(
            DriveWireServer.serverConfiguration.getLong(
                "DiskLazyWriteInterval",
                DISK_LAZY_WRITE_SLEEP_INTERVAL
            )
        );
        syncDisks();
      } catch (InterruptedException e) {
        LOGGER.debug("interrupted");
        wantToDie = true;
      }
    }
    LOGGER.debug("exit");
  }

  private void syncDisks() {
    this.inSync = true;
    for (int h = 0; h < DriveWireServer.getNumHandlers(); h++) {
      if (DriveWireServer.handlerIsAlive(h)) {
        DriveWireServer.getHandler(h).syncStorage();
      }
    }
    this.inSync = false;
  }

  /**
   * Disk in sync?.
   *
   * @return true if in sync
   */
  public boolean isInSync() {
    return this.inSync;
  }
}
