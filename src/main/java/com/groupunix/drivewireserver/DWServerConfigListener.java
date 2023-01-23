package com.groupunix.drivewireserver;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;

/**
 * DWServerConfigListener.
 */
public class DWServerConfigListener implements ConfigurationListener {
  /**
   * class logger.
   */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = Logger.getLogger(
      DWServerConfigListener.class
  );

  /**
   * configurationChanged.
   * @param event
   */
  public void configurationChanged(final ConfigurationEvent event) {
    if (!event.isBeforeUpdate()) {
      // indicate changed config for UI poll
      DriveWireServer.incConfigSerial();
      if (
          event.getPropertyName() != null
          && event.getPropertyValue() != null
      ) {
        DriveWireServer.submitServerConfigEvent(
            event.getPropertyName(),
            event.getPropertyValue().toString()
        );
        // logging changes
        if (event.getPropertyName().startsWith("Log")) {
          DriveWireServer.setLoggingRestart();
        }
        // UI thread
        if (event.getPropertyName().startsWith("UI")) {
          if (!DriveWireServer.isConfigFreeze()) {
            DriveWireServer.setUIRestart();
          }
        }
      }
    }
  }
}
