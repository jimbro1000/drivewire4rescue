package com.groupunix.drivewireserver;

import java.lang.Thread.UncaughtExceptionHandler;

public class DWExceptionHandler implements UncaughtExceptionHandler {

  /**
   * Uncaught exception handler.
   *
   * @param thread the thread
   * @param throwable the exception
   */
  @Override
  public void uncaughtException(
      final Thread thread, final Throwable throwable
  ) {
    DriveWireServer.handleUncaughtException(thread, throwable);
  }
}
