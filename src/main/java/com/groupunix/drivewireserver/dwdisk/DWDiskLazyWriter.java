package com.groupunix.drivewireserver.dwdisk;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWDiskLazyWriter implements Runnable {

  private static final Logger logger = Logger.getLogger("DWServer.DWProtoReader");
  private boolean wantToDie = false;
  private boolean inSync = false;

  public void run() {
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    Thread.currentThread().setName("dskwriter-" + Thread.currentThread().getId());

    logger.debug("started, write interval is " + DriveWireServer.serverConfiguration.getLong("DiskLazyWriteInterval", 15000));

    while (wantToDie == false) {
      try {
        logger.debug("sleeping for " + DriveWireServer.serverConfiguration.getLong("DiskLazyWriteInterval", 15000) + " ms...");
        Thread.sleep(DriveWireServer.serverConfiguration.getLong("DiskLazyWriteInterval", 5000));
        syncDisks();
      } catch (InterruptedException e) {
        logger.debug("interrupted");
        wantToDie = true;
      }
    }
    logger.debug("exit");
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

  public boolean isInSync() {
    return this.inSync;
  }

}
