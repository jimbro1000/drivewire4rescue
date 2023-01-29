package com.groupunix.drivewireserver.dwprotocolhandler;

import java.util.GregorianCalendar;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwhelp.DWHelp;

public interface DWProtocol extends Runnable {
  /**
   * Shutdown protocol.
   */
  void shutdown();

  /**
   * Is protocol waiting to stop.
   *
   * @return true if stopping
   */
  boolean isDying();

  /**
   * Is protocol started.
   *
   * @return true if started
   */
  boolean isStarted();

  /**
   * Is protocol ready.
   *
   * @return true if ready
   */
  boolean isReady();

  /**
   * Is protocol connected.
   *
   * @return true if connected
   */
  boolean isConnected();

  /**
   * Does protocol have printers.
   *
   * @return true if printers available
   */
  boolean hasPrinters();

  /**
   * Does protocol have disks.
   *
   * @return true if disks available
   */
  boolean hasDisks();

  /**
   * Does protocol have midi.
   *
   * @return true if MIDI available
   */
  boolean hasMIDI();

  /**
   * Does protocol have virtual serial.
   *
   * @return true if serial available
   */
  boolean hasVSerial();

  /**
   * Get configuration.
   *
   * @return configuration
   */
  HierarchicalConfiguration getConfig();

  /**
   * Get protocol device.
   *
   * @return protocol device
   */
  DWProtocolDevice getProtoDev();

  /**
   * Get initial time.
   *
   * @return time
   */
  @SuppressWarnings("unused")
  GregorianCalendar getInitTime();

  /**
   * Get status text.
   *
   * @return status
   */
  @SuppressWarnings("unused")
  String getStatusText();

  /**
   * Reset protocol device.
   */
  void resetProtocolDevice();

  /**
   * Syncronise storage.
   */
  void syncStorage();

  /**
   * Get handler id.
   *
   * @return handler id
   */
  int getHandlerNo();

  /**
   * Get protocol log appender.
   *
   * @return logger
   */
  Logger getLogger();

  /**
   * Get command cols.
   *
   * @return cols
   */
  int getCMDCols();

  /**
   * Get help topics.
   *
   * @return help
   */
  DWHelp getHelp();

  /**
   * Submit configuration event.
   *
   * @param propertyName property
   * @param string       value
   */
  void submitConfigEvent(String propertyName, String string);

  /**
   * Get count of ops.
   *
   * @return count of all ops
   */
  long getNumOps();

  /**
   * Get count of disk ops.
   *
   * @return disk ops count
   */
  long getNumDiskOps();

  /**
   * Get count of virtual serial ops.
   *
   * @return serial ops count
   */
  long getNumVSerialOps();

  /**
   * Get protocol timers.
   *
   * @return timers
   */
  DWProtocolTimers getTimers();
}
