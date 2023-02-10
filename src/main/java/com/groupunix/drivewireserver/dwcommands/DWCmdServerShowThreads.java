package com.groupunix.drivewireserver.dwcommands;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCmdServerShowThreads extends DWCommand {

  /**
   * Server show threads command constructor.
   *
   * @param parent parent command
   */
  public DWCmdServerShowThreads(final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.setCommand("threads");
    this.setShortHelp("Show server threads");
    this.setUsage("dw server show threads");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final StringBuilder text = new StringBuilder();
    text.append("\r\nDriveWire Server Threads:\r\n\n");
    final Thread[] threads = getAllThreads();
    for (final Thread thread : threads) {
      if (thread != null) {
        text
            .append(
                String.format(
                    "%40s %3d %-8s %-14s",
                    shortenName(thread.getName()),
                    thread.getPriority(),
                    thread.getThreadGroup().getName(),
                    thread.getState())
            )
            .append("\r\n");
      }
    }
    return new DWCommandResponse(text.toString());
  }

  private Object shortenName(final String name) {
    return name;
  }

  private Thread[] getAllThreads() {
    final ThreadGroup root = DWUtils.getRootThreadGroup();
    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
    int nAlloc = thbean.getThreadCount();
    int count;
    Thread[] threads;
    do {
      nAlloc *= 2;
      threads = new Thread[nAlloc];
      count = root.enumerate(threads, true);
    } while (count == nAlloc);
    final Thread[] copy = new Thread[threads.length];
    System.arraycopy(threads, 0, copy, 0, threads.length);
    return copy;
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
