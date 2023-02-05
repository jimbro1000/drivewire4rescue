package com.groupunix.drivewireserver;

@SuppressWarnings("unused")
public class DWShutdownHandler extends Thread {

  /**
   * Run shutdown event.
   */
  public void run() {
    Thread.currentThread()
        .setName("shutdown-" + Thread.currentThread().getId());
    DriveWireServer.serverShutdown();
  }
}
