package com.groupunix.drivewireserver.dwprotocolhandler;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

public class DWProtocolConfigListener implements ConfigurationListener {
  /**
   * Host protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Protocol configuration listener.
   *
   * @param protocol host protocol
   */
  public DWProtocolConfigListener(final DWProtocol protocol) {
    super();
    this.dwProtocol = protocol;
  }

  /**
   * Updated configuration event.
   * <p>
   * Propagates event to host protocol
   * </p>
   *
   * @param event new event
   */
  @Override
  public void configurationChanged(final ConfigurationEvent event) {
    if (!event.isBeforeUpdate() && event.getPropertyName() != null) {
      if (event.getPropertyValue() == null) {
        this.dwProtocol.submitConfigEvent(event.getPropertyName(), "");
      } else {
        this.dwProtocol.submitConfigEvent(
            event.getPropertyName(), event.getPropertyValue().toString()
        );
      }
    }
  }
}
