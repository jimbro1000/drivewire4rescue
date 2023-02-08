package com.groupunix.drivewireserver.dwdisk;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

public class DWDiskConfigListener implements ConfigurationListener {
  /**
   * Drivewire disk.
   */
  private final DWDisk dwDisk;

  /**
   * Disk Configuration Listener constructor.
   *
   * @param disk associated disk
   */
  public DWDiskConfigListener(final DWDisk disk) {
    super();
    this.dwDisk = disk;
  }

  /**
   * Handle configuration changed event.
   *
   * @param event configuration change
   */
  @Override
  public void configurationChanged(final ConfigurationEvent event) {
    if (!event.isBeforeUpdate()) {
      if (event.getPropertyName() != null && event.getPropertyValue() != null) {
        this.dwDisk.submitEvent(
            event.getPropertyName(), event.getPropertyValue().toString()
        );
      } else {
        this.dwDisk.submitEvent(event.getPropertyName(), "");
      }
    }
  }
}
