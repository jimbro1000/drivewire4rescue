package com.groupunix.drivewireserver.dwprotocolhandler;

import com.groupunix.drivewireserver.DWDefs;

import static com.groupunix.drivewireserver.DWDefs.BYTE_BITS;
import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWProtocolTimers {
  /**
   * Timer byte array length.
   */
  public static final int TIMER_LEN = 4;
  /**
   * Maximum number of timers.
   */
  public static final int MAX_TIMERS = 256;
  /**
   * Timers.
   */
  private final long[] timers;

  /**
   * Protocol Timers.
   */
  public DWProtocolTimers() {
    this.timers = new long[MAX_TIMERS];
  }

  /**
   * Reset timer.
   * <p>
   * Sets timer to current time
   * </p>
   *
   * @param tno timer id
   */
  public void resetTimer(final byte tno) {
    resetTimer(tno, System.currentTimeMillis());
  }

  /**
   * Reset timer.
   *
   * @param timer timer id
   * @param time time
   */
  public void resetTimer(final byte timer, final long time) {
    this.timers[timer & BYTE_MASK] = time;
    // Dependencies
    switch (timer) {
      // init and reset are also np ops
      case DWDefs.TIMER_DWINIT,
          DWDefs.TIMER_RESET -> resetTimer(DWDefs.TIMER_NP_OP, time);
      // read/write is an io op
      case DWDefs.TIMER_READ,
          DWDefs.TIMER_WRITE -> resetTimer(DWDefs.TIMER_IO, time);
      // io ops are also np ops
      case DWDefs.TIMER_IO -> resetTimer(DWDefs.TIMER_NP_OP, time);
      // poll is an op
      case DWDefs.TIMER_POLL -> resetTimer(DWDefs.TIMER_OP, time);
      // np ops are also ops
      case DWDefs.TIMER_NP_OP -> resetTimer(DWDefs.TIMER_OP, time);
      default -> {
      }
    }
  }

  /**
   * Get timer as long.
   *
   * @param tno timer id
   * @return timer
   */
  public long getTimer(final byte tno) {
    if (this.timers[tno & BYTE_MASK] > 0) {
      return System.currentTimeMillis() - this.timers[tno & BYTE_MASK];
    }
    return 0;
  }

  /**
   * Get timer as byte array.
   *
   * @param tno timer id
   * @return byte array
   */
  public byte[] getTimerBytes(final byte tno) {
    byte[] res = new byte[TIMER_LEN];
    final long input = getTimer(tno);
    int index = 0;
    res[index++] = (byte) ((input >> (BYTE_BITS + BYTE_BITS + BYTE_BITS))
        & BYTE_MASK);
    res[index++] = (byte) ((input >> (BYTE_BITS + BYTE_BITS)) & BYTE_MASK);
    res[index++] = (byte) ((input >> BYTE_BITS) & BYTE_MASK);
    res[index] = (byte) (input & BYTE_MASK);
    return res;
  }
}
