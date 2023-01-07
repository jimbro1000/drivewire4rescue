package com.groupunix.drivewireserver.dwprotocolhandler;

import java.util.GregorianCalendar;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwhelp.DWHelp;

public interface DWProtocol extends Runnable {
  void shutdown();

  boolean isDying();

  boolean isStarted();

  boolean isReady();

  boolean isConnected();

  boolean hasPrinters();

  boolean hasDisks();

  boolean hasMIDI();

  boolean hasVSerial();

  HierarchicalConfiguration getConfig();

  DWProtocolDevice getProtoDev();

  GregorianCalendar getInitTime();

  String getStatusText();

  void resetProtocolDevice();

  void syncStorage();

  int getHandlerNo();

  Logger getLogger();

  int getCMDCols();

  DWHelp getHelp();

  void submitConfigEvent(String propertyName, String string);

  long getNumOps();

  long getNumDiskOps();

  long getNumVSerialOps();

  DWProtocolTimers getTimers();

}
