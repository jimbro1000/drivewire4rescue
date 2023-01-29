package com.groupunix.drivewireserver;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

// adds logging events to an internal list, so they can be retrieved for web UI
// very basic for now

public class DWLogAppender extends AppenderSkeleton {
  /**
   * Events List.
   */
  private final LinkedList<LoggingEvent> events = new LinkedList<>();

  /**
   * Drivewire Log Appender.
   *
   * @param layout layout
   */
  public DWLogAppender(final Layout layout) {
    setLayout(layout);
  }

  /**
   * Get current layout.
   *
   * @return layout
   */
  public Layout getLayout() {
    return layout;
  }

  /**
   * Set layout.
   *
   * @param layout new layout
   */
  public void setLayout(final Layout layout) {
    this.layout = layout;
  }

  /**
   * Requires layout flag.
   *
   * @return true
   */
  public boolean requiresLayout() {
    return true;
  }

  /**
   * Shutdown.
   * <p>
   * Not implemented
   * </p>
   */
  public synchronized void shutdown() {
  }

  /**
   * Add event to log.
   *
   * @param event new event
   */
  protected void append(final LoggingEvent event) {
    // fatal to console
    if ((event.getLevel() == Level.FATAL)
        && (!DriveWireServer.isConsoleLogging())
        && (!DriveWireServer.isDebug())
    ) {
      System.out.println("FATAL: " + event.getRenderedMessage());
    }
    // ignore those pesky XMLConfiguration debug messages
    // and massive httpd client noise
    if (event.getMessage() != null
        && event.getLocationInformation().getClassName() != null
        && !event.getMessage()
        .equals("ConfigurationUtils.locate(): base is null, name is null")
        && !event.getLocationInformation()
        .getClassName().startsWith("org.apache.commons.httpclient")
    ) {
      // send it to UI listeners
      DriveWireServer.submitLogEvent(event);
      // add to our buffer (for viewing from coco)
      synchronized (events) {
        if (events.size() == DWDefs.LOGGING_MAX_BUFFER_EVENTS) {
          events.removeFirst();
        }
        events.addLast(event);
      }
    }
  }

  /**
   * Get last N events.
   *
   * @param num number of events
   * @return list of events
   */
  public ArrayList<String> getLastEvents(final int num) {
    ArrayList<String> eventsText = new ArrayList<>();
    int start = 0;
    int limit = num;
    synchronized (events) {
      if (limit > events.size()) {
        limit = events.size();
      }
      if (events.size() > limit) {
        start = events.size() - limit;
      }
      for (int i = start; i < events.size(); i++) {
        eventsText.add(layout.format(events.get(i)));
      }
    }
    return eventsText;
  }

  /**
   * Close event log.
   * <p>
   * not implemented
   * </p>
   */
  public void close() {
  }

  /**
   * Get size of events.
   *
   * @return events size
   */
  public int getEventsSize() {
    return events.size();
  }
}
