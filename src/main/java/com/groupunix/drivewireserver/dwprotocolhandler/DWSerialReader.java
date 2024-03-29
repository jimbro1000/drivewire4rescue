package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

public class DWSerialReader implements SerialPortEventListener {
  /**
   * Event queue.
   */
  private final ArrayBlockingQueue<Byte> queue;
  /**
   * Input stream.
   */
  private final InputStream inputStream;
  /**
   * Shutdown flag.
   */
  private boolean wantToDie = false;

  /**
   * Serial Reader.
   *
   * @param input         input stream
   * @param blockingQueue event queue
   */
  public DWSerialReader(
      final InputStream input, final ArrayBlockingQueue<Byte> blockingQueue
  ) {
    this.queue = blockingQueue;
    this.inputStream = input;
  }

  /**
   * Queue serial event.
   *
   * @param event serial port event
   */
  @Override
  public void serialEvent(final SerialPortEvent event) {
    int data;

    try {
      while (!wantToDie && (data = inputStream.read()) > -1) {
        queue.add((byte) data);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Shutdown.
   */
  public void shutdown() {
    this.wantToDie = true;
  }
}
