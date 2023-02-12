package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.OS9Defs;

public class DWRFMPath {
  /**
   * File already exists.
   */
  public static final int FILE_ALREADY_EXISTS = 218;
  /**
   * Success.
   */
  public static final int SUCCESS = 0;
  /**
   * Create file failed.
   */
  public static final int CREATE_FILE_FAILED = 245;
  /**
   * Open file failed.
   */
  public static final int OPEN_FILE_FAILED = 216;
  /**
   * Open directory failed.
   */
  public static final int OPEN_DIR_FAILED = 214;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWRFMPath");
  /**
   * Cap to bytes available.
   */
  private static final int BYTES_AVAILABLE_CAP = 127;
  /**
   * Handler id.
   */
  private final int handlerId;
  /**
   * File system manager instance.
   */
  private final FileSystemManager fsManager;
  /**
   * List of directory entries.
   */
  private final ArrayList<DWRFMDirEntry> dirEntries = new ArrayList<>();
  /**
   * Path number.
   */
  private int pathNumber;
  /**
   * Path string.
   */
  private String pathString;
  /**
   * Local root path.
   */
  private String localRoot;
  /**
   * Seek position.
   */
  private int seekPosition;
  /**
   * Directory mode.
   */
  private boolean dirMode = false;
  /**
   * Directory byte buffer.
   */
  @SuppressWarnings("unused")
  private byte[] dirBuffer;
  /**
   * File object instance.
   */
  private FileObject fileObject;
  /**
   * Directory entry number.
   */
  private int dirEntryNum = 0;

  /**
   * RFM path.
   *
   * @param handler handler id
   * @param pathNum path number
   * @throws FileSystemException read/write failure
   */
  public DWRFMPath(final int handler, final int pathNum)
      throws FileSystemException {
    this.setPathNumber(pathNum);
    this.setSeekPos(0);
    LOGGER.debug("new path " + pathNum);
    this.handlerId = handler;
    this.fsManager = VFS.getManager();
    this.setLocalRoot(
        DriveWireServer
            .getHandler(this.handlerId)
            .getConfig()
            .getString("RFMRoot", "/")
    );
  }

  /**
   * Get path number.
   *
   * @return path number
   */
  @SuppressWarnings("unused")
  public int getPathNumber() {
    return pathNumber;
  }

  /**
   * Set path number.
   *
   * @param pathNum path number
   */
  public void setPathNumber(final int pathNum) {
    LOGGER.debug("set path to " + pathNum);
    this.pathNumber = pathNum;
  }

  /**
   * Get path string.
   *
   * @return path string
   */
  public String getPathStr() {
    return pathString;
  }

  /**
   * Set path string.
   * <p>
   *   forces path to root regardless
   * </p>
   * @param path path string
   */
  public void setPathStr(final String path) {
    this.pathString = "/";
    LOGGER.debug("set pathstr to " + this.pathString);
  }

  /**
   * Close path.
   */
  public void close() {
    LOGGER.debug("closing path " + this.pathNumber + " to " + this.pathString);
    try {
      fileObject.close();
    } catch (FileSystemException e) {
      LOGGER.warn("error closing file: " + e.getMessage());
    }
  }

  /**
   * Get seek position.
   *
   * @return seek position
   */
  @SuppressWarnings("unused")
  public int getSeekPos() {
    return seekPosition;
  }

  /**
   * Set seek position.
   *
   * @param position seek position
   */
  public void setSeekPos(final int position) {
    this.seekPosition = position;
    LOGGER.debug("seek to " + position + " on path " + this.pathNumber);
  }

  /**
   * Open file.
   *
   * @param modeByte open file mode
   * @return response code
   */
  public int openFile(final int modeByte) {
    // attempt to open local file
    try {
      fileObject = fsManager.resolveFile(this.localRoot + this.pathString);
      if (((byte) modeByte & OS9Defs.MODE_DIR) == OS9Defs.MODE_DIR) {
        // Directory
        if (fileObject.isReadable()) {
          if (fileObject.getType() == FileType.FOLDER) {
            this.dirMode = true;
            LOGGER.debug("directory open: modebyte " + modeByte);
            for (int i = 0; i < fileObject.getChildren().length; i++) {
              this.dirEntries.add(
                  new DWRFMDirEntry(fileObject.getChildren()[i])
              );
            }
            return SUCCESS;
          } else {
            fileObject.close();
            return OPEN_DIR_FAILED;
          }
        } else {
          fileObject.close();
          return OPEN_FILE_FAILED;
        }
      } else {
        // File
        if (fileObject.isReadable()) {
          return SUCCESS;
        } else {
          fileObject.close();
          return OPEN_FILE_FAILED;
        }
      }
    } catch (FileSystemException e) {
      LOGGER.warn("open failed: " + e.getMessage());
      return OPEN_FILE_FAILED;
    }
  }

  /**
   * Get local root path.
   *
   * @return local root
   */
  @SuppressWarnings("unused")
  public String getLocalRoot() {
    return localRoot;
  }

  /**
   * Set local root path.
   *
   * @param rootPath path
   */
  public void setLocalRoot(final String rootPath) {
    this.localRoot = rootPath;
  }

  /**
   * Create file.
   *
   * @return response code
   */
  public int createFile() {
    try {
      // attempt to open local file
      fileObject = fsManager.resolveFile(this.localRoot + this.pathString);
      if (fileObject.exists()) {
        // file already exists
        fileObject.close();
        return FILE_ALREADY_EXISTS;
      } else {
        fileObject.createFile();
        return SUCCESS;
      }
    } catch (FileSystemException e) {
      LOGGER.warn("create failed: " + e.getMessage());
      return CREATE_FILE_FAILED;
    }
  }

  /**
   * Get number of bytes available.
   * <p>
   * Hard cap of 127 bytes
   * </p>
   *
   * @param maxBytes size cap
   * @return calculated number of bytes available
   */
  public int getBytesAvail(final int maxBytes) {
    if (this.dirMode) {
      // Dir mode - return # bytes left in the dirbuffer
      return this.dirBuffer.length - this.seekPosition;
    }
    // File mode
    // return # bytes left in file from current seek pos, up to maxBytes
    final File file = new File(this.localRoot + this.pathString);
    if (file.exists()) {
      // we only handle int sized files...
      final int tmpSize = Math.min(
          BYTES_AVAILABLE_CAP,
          (int) file.length() - this.seekPosition
      );
      return Math.min(tmpSize, maxBytes);
    } else {
      //TODO wrong!
      return 0;
    }
  }

  /**
   * Get N bytes from path.
   *
   * @param availBytes N
   * @return byte array
   */
  public byte[] getBytes(final int availBytes) {
    // TODO very crappy !
    // return byte array of next availbytes bytes from file, move seekpos
    // TODO structure blindly assumes this will work.
    // like above need to implement exceptions/error handling passed up to
    // caller
    final byte[] buf = new byte[availBytes];
    if (this.dirMode) {
      System.arraycopy(this.dirBuffer, this.seekPosition, buf, 0, availBytes);
      // this.seekpos += availbytes;
      return buf;
    }
    final File file = new File(this.localRoot + this.pathString);
    if (file.exists()) {
      LOGGER.debug("FILE: asked for " + availBytes);
      try (
          RandomAccessFile inFile = new RandomAccessFile(file, "r")
      ) {
        inFile.seek(seekPosition);
        //TODO what if we don't get buf.length??
        //this.seekpos +=
        inFile.read(buf);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return buf;
  }

  /**
   * Increment seek position by N bytes.
   *
   * @param bytes N
   */
  public void incSeekPos(final int bytes) {
    this.seekPosition += bytes;
    LOGGER.debug("incSeekpos to " + this.seekPosition);
  }

  /**
   * Set file descriptor bytes.
   *
   * @param buf byte array
   * @throws FileSystemException write failure
   */
  public void setFd(final byte[] buf) throws FileSystemException {
    final DWRFMFD dwrfmfd = new DWRFMFD(
        DriveWireServer
            .getHandler(this.handlerId)
            .getConfig()
            .getString("RFMRoot", "/")
            + this.pathString
    );
    dwrfmfd.readFD();
    final byte[] fdTmp = dwrfmfd.getFD();
    System.arraycopy(buf, 0, fdTmp, 0, buf.length);
    dwrfmfd.setFD(fdTmp);
    dwrfmfd.writeFD();
  }

  /**
   * Get file descriptor bytes.
   *
   * @param size bytes to fetch
   * @return byte array
   * @throws FileSystemException read failure
   */
  public byte[] getFd(final int size) throws FileSystemException {
    final byte[] bytes = new byte[size];
    final DWRFMFD dwrfmfd = new DWRFMFD(
        DriveWireServer
            .getHandler(this.handlerId)
            .getConfig()
            .getString("RFMRoot", "/")
            + this.pathString
    );
    dwrfmfd.readFD();
    System.arraycopy(dwrfmfd.getFD(), 0, bytes, 0, size);
    return bytes;
  }

  /**
   * Write Bytes to path.
   *
   * @param buf      byte array
   * @param maxBytes maximum number of bytes
   */
  public void writeBytes(final byte[] buf, final int maxBytes) {
    // write to file
    final File file = new File(this.localRoot + this.pathString);
    if (file.exists()) {
      try (
          RandomAccessFile inFile = new RandomAccessFile(file, "rw")
      ) {
        inFile.seek(this.seekPosition);
        //TODO what if we don't get buf.length??
        //this.seekpos +=
        inFile.write(buf);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      LOGGER.error("write to non existent file");
    }
  }

  /**
   * Get next directory.
   *
   * @return directory entry
   * @throws FileSystemException read/write failure
   */
  @SuppressWarnings("unused")
  public DWRFMDirEntry getNextDirEntry() throws FileSystemException {
    if (this.dirMode) {
      if (this.dirEntryNum < this.dirEntries.size()) {
        this.dirEntryNum++;
      }
    }
    return new DWRFMDirEntry(null);
  }
}
