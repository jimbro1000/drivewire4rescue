package com.groupunix.drivewireserver;

import java.util.HashMap;
import java.util.Set;

public class DWEvent {
  /**
   * Parameters (key, value).
   */
  private final HashMap<String, String> params = new HashMap<>();
  /**
   * event type.
   */
  private byte eventType;
  /**
   * event instance.
   */
  private int eventInstance = -1;

  /**
   * Drivewire event.
   *
   * @param type event type
   * @param instance instance id
   */
  public DWEvent(final byte type, final int instance) {
    this.setEventInstance(instance);
    this.setEventType(type);
  }

  /**
   * Set parameter adds value.
   *
   * @param key key
   * @param value parameter value
   */
  public void setParam(final String key, final String value) {
    this.params.put(key, value);
  }

  /**
   * Test if key is present in parameter set.
   *
   * @param key key
   * @return true if key is present
   */
  public boolean hasParam(final String key) {
    return (this.params.containsKey(key));
  }

  /**
   * Get named parameter.
   *
   * @param key parameter key
   * @return parameter value
   */
  public String getParam(final String key) {
    if (this.params.containsKey(key)) {
      return this.params.get(key);
    }
    return null;
  }

  /**
   * Get parameter key set.
   *
   * @return parameter keys
   */
  public Set<String> getParamKeys() {
    return this.params.keySet();
  }

  /**
   * Get event type.
   *
   * @return event type
   */
  public byte getEventType() {
    return eventType;
  }

  /**
   * Set event type.
   *
   * @param type event type
   */
  public void setEventType(final byte type) {
    this.eventType = type;
  }

  /**
   * Get event instance.
   *
   * @return event instance id
   */
  public int getEventInstance() {
    return eventInstance;
  }

  /**
   * Set event instance.
   *
   * @param instanceId instance
   */
  public void setEventInstance(final int instanceId) {
    this.eventInstance = instanceId;
  }
}
