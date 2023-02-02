package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.OS9Defs;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;

public class DWRFMHandler {
  // RFM opcodes
  /**
   * RFM op CREATE.
   */
  public static final byte RFM_OP_CREATE = (byte) 1;
  /**
   * RFM op OPEN.
   */
  public static final byte RFM_OP_OPEN = (byte) 2;
  /**
   * RFM op MAKE DIR.
   */
  public static final byte RFM_OP_MAKDIR = (byte) 3;
  /**
   * RFM op CHANGE DIR.
   */
  public static final byte RFM_OP_CHGDIR = (byte) 4;
  /**
   * RFM op DELETE.
   */
  public static final byte RFM_OP_DELETE = (byte) 5;
  /**
   * RFM op SEEK.
   */
  public static final byte RFM_OP_SEEK = (byte) 6;
  /**
   * RFM op READ.
   */
  public static final byte RFM_OP_READ = (byte) 7;
  /**
   * RFM op WRITE.
   */
  public static final byte RFM_OP_WRITE = (byte) 8;
  /**
   * RFM op READ LINE.
   */
  public static final byte RFM_OP_READLN = (byte) 9;
  /**
   * RFM op WRITE LINE.
   */
  public static final byte RFM_OP_WRITLN = (byte) 10;
  /**
   * RFM op GET STT.
   */
  public static final byte RFM_OP_GETSTT = (byte) 11;
  /**
   * RFM op SET STT.
   */
  public static final byte RFM_OP_SETSTT = (byte) 12;
  /**
   * RFM op CLOSE.
   */
  public static final byte RFM_OP_CLOSE = (byte) 13;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWRFMHandler");
  /**
   * Maximum number of RFM paths handled.
   */
  private static final int MAX_PATHS = 256;
  /**
   * Length of seek position (bytes).
   */
  private static final int SEEK_LEN = 4;
  /**
   * RFM paths.
   */
  private final DWRFMPath[] paths = new DWRFMPath[MAX_PATHS];
  /**
   * Handler Id.
   */
  private final int handlerId;

  /**
   * RFM Handler.
   *
   * @param handler handler Id
   */
  public DWRFMHandler(final int handler) {
    LOGGER.debug("init for handler #" + handler);
    this.handlerId = handler;
  }

  /**
   * Route device operation.
   *
   * @param device protocol device
   * @param rfmOp operation id
   */
  public void doRfmOp(final DWProtocolDevice device, final int rfmOp) {
    switch (rfmOp) {
      case RFM_OP_CREATE -> doOpRfmCreate(device);
      case RFM_OP_OPEN -> doOpRfmOpen(device);
      case RFM_OP_MAKDIR -> doOpRfmMakeDir();
      case RFM_OP_CHGDIR -> doRfmOpChangeDir();
      case RFM_OP_DELETE -> doOpRfmDelete();
      case RFM_OP_SEEK -> doOpRfmSeek(device);
      case RFM_OP_READ -> doOpRfmRead(device);
      case RFM_OP_WRITE -> doOpRfmWrite();
      case RFM_OP_READLN -> doOpRfmReadLn(device);
      case RFM_OP_WRITLN -> doOpRfmWriteLn(device);
      case RFM_OP_GETSTT -> doOpRfmGetStt(device);
      case RFM_OP_SETSTT -> doOpRfmSetStt(device);
      case RFM_OP_CLOSE -> doOpRfmClose(device);
      default -> {
      }
    }
  }

  private void doOpRfmClose(final DWProtocolDevice device) {
    LOGGER.debug("CLOSE");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      if (this.paths[pathNumber] == null) {
        LOGGER.error("close on null path: " + pathNumber);
      } else {
        this.paths[pathNumber].close();
        this.paths[pathNumber] = null;
      }
      // send response
      device.comWrite1(0, true);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  private void doOpRfmSetStt(final DWProtocolDevice device) {
    LOGGER.debug("SETSTT");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      // read call
      int call = device.comRead1(true);
      LOGGER.debug("SETSTT path " + pathNumber + " call " + call);
      if (call == OS9Defs.SS_FD) {
        setSttFd(device, pathNumber);
      }
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  private void doOpRfmGetStt(final DWProtocolDevice device) {
    LOGGER.debug("GETSTT");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      // read call
      int call = device.comRead1(true);
      LOGGER.debug("GETSTT path " + pathNumber + " call " + call);
      switch (call) {
        case OS9Defs.SS_FD -> getSttFd(device, pathNumber);
        case OS9Defs.SS_DIR_ENT -> setSttFd(device, pathNumber);
        default -> {
        }
      }
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }


  private void getSttFd(final DWProtocolDevice device, final int pathNumber) {
    LOGGER.debug("getstt_fd");
    // read # bytes wanted
    try {
      int size = DWUtils.int2(device.comRead(2));
      byte[] buf = this.paths[pathNumber].getFd(size);
      device.comWrite(buf, size, true);
      LOGGER.debug("sent " + size + " bytes of FD for path " + pathNumber);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  private void setSttFd(final DWProtocolDevice device, final int pathNumber) {
    LOGGER.debug("getstt_fd");
    // read # bytes coming
    try {
      int size = DWUtils.int2(device.comRead(2));
      byte[] buf = device.comRead(size);
      this.paths[pathNumber].setFd(buf);
      LOGGER.debug("read " + size + " bytes of FD for path " + pathNumber);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  /**
   * Write line to protocol path.
   *
   * @param device protocol device
   */
  private void doOpRfmWriteLn(final DWProtocolDevice device) {
    LOGGER.debug("WRITLN");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      // read sending bytes
      byte[] maxBytesB = device.comRead(2);
      int maxBytes = DWUtils.int2(maxBytesB);
      // read bytes
      byte[] buf = device.comRead(maxBytes);
      // write to file
      this.paths[pathNumber].writeBytes(buf, maxBytes);
      this.paths[pathNumber].incSeekPos(maxBytes);
      LOGGER.debug("writln on path " + pathNumber + " bytes: " + maxBytes);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  /**
   * Read line from protocol path.
   *
   * @param device protocol device
   */
  private void doOpRfmReadLn(final DWProtocolDevice device) {
    LOGGER.debug("READLN");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      // read max bytes
      byte[] maxBytesB = device.comRead(2);
      int maxBytes = DWUtils.int2(maxBytesB);
      int availBytes = this.paths[pathNumber].getBytesAvail(maxBytes);
      LOGGER.debug("initial AB: " + availBytes);
      byte[] buf = new byte[availBytes];
      System.arraycopy(
          this.paths[pathNumber].getBytes(availBytes),
          0,
          buf,
          0,
          availBytes
      );
      // find $0D or end
      int x = 0;
      while (x < availBytes) {
        if (buf[x] == (byte) CARRIAGE_RETURN) {
          availBytes = x + 1;
        }
        x++;
      }
      LOGGER.debug("adjusted AB: " + availBytes);
      device.comWrite1(availBytes, true);
      if (availBytes > 0) {
        // possible prefix needed?
        device.comWrite(buf, availBytes, false);
        this.paths[pathNumber].incSeekPos(availBytes);
      }
      LOGGER.debug("readln on path " + pathNumber + " maxbytes: " + maxBytes
          + " availbytes: " + availBytes);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  /**
   * Write protocol path.
   * <p>
   *   not implemented
   * </p>
   */
  private void doOpRfmWrite() {
    LOGGER.debug("WRITE");
  }

  /**
   * Read protocol path.
   *
   * @param device protocol device
   */
  private void doOpRfmRead(final DWProtocolDevice device) {
    LOGGER.debug("READ");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      // read max bytes
      byte[] maxBytesB = device.comRead(2);
      int maxBytes = DWUtils.int2(maxBytesB);
      int availBytes = this.paths[pathNumber].getBytesAvail(maxBytes);
      if (maxBytes > availBytes) {
        maxBytes = availBytes;
      }
      byte[] buf = new byte[maxBytes];
      System.arraycopy(
          this.paths[pathNumber].getBytes(maxBytes),
          0,
          buf,
          0,
          maxBytes
      );
      device.comWrite1(maxBytes, true);
      if (maxBytes > 0) {
        // possible prefix needed
        device.comWrite(buf, maxBytes, false);
        this.paths[pathNumber].incSeekPos(maxBytes);
        LOGGER.debug("buf: " + DWUtils.byteArrayToHexString(buf));
      }
      LOGGER.debug("read on path " + pathNumber + " maxbytes: " + maxBytes);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  /**
   * Seek position.
   *
   * @param device protocol device
   */
  private void doOpRfmSeek(final DWProtocolDevice device) {
    LOGGER.debug("SEEK");
    try {
      int pathNumber = device.comRead1(true);
      // read seek pos
      byte[] seekPosition = device.comRead(SEEK_LEN);
      this.paths[pathNumber].setSeekPos(DWUtils.int4(seekPosition));
      // assume it worked, for now
      device.comWrite1(0, true);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  /**
   * Delete device path.
   * <p>
   * not implemented
   * </p>
   */
  private void doOpRfmDelete() {
    LOGGER.debug("DELETE");
  }

  /**
   * Change directory.
   * <p>
   * not implemented
   * </p>
   */
  private void doRfmOpChangeDir() {
    LOGGER.debug("CHGDIR");
  }

  /**
   * Make directory.
   * <p>
   * not implemented
   * </p>
   */
  private void doOpRfmMakeDir() {
    LOGGER.debug("MAKDIR");
  }

  /**
   * Create device path.
   *
   * @param device protocol device
   */
  private void doOpRfmCreate(final DWProtocolDevice device) {
    LOGGER.debug("CREATE");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      int modeByte = device.comRead1(true) & BYTE_MASK;
      String pathString = buildPath(device);
      // send result
      this.paths[pathNumber] = new DWRFMPath(this.handlerId, pathNumber);
      this.paths[pathNumber].setPathStr(pathString);
      int result = this.paths[pathNumber].createFile();
      device.comWrite1(result, true);
      LOGGER.debug("create path " + pathNumber + " mode " + modeByte + ", to "
          + pathString + ": result " + result);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  /**
   * Open device path.
   *
   * @param device protocol device
   */
  private void doOpRfmOpen(final DWProtocolDevice device) {
    LOGGER.debug("OPEN");
    // read path #
    try {
      int pathNumber = device.comRead1(true);
      int modeByte = device.comRead1(true) & BYTE_MASK;
      String pathString = buildPath(device);
      // send result
      // anything needed for dealing with multiple opens..
      this.paths[pathNumber] = new DWRFMPath(this.handlerId, pathNumber);
      this.paths[pathNumber].setPathStr(pathString);
      int result = this.paths[pathNumber].openFile(modeByte);
      device.comWrite1(result, true);
      LOGGER.debug("open path " + pathNumber + " mode " + modeByte + ", to "
          + pathString + ": result " + result);
    } catch (IOException | DWCommTimeOutException e) {
      e.printStackTrace();
    }
  }

  private String buildPath(final DWProtocolDevice device)
      throws DWCommTimeOutException, IOException {
    StringBuilder pathString = new StringBuilder();
    int nextChar = device.comRead1(true);
    while (nextChar != CARRIAGE_RETURN) {
      pathString.append((char) nextChar);
      nextChar = device.comRead1(true);
    }
    return pathString.toString();
  }
}
