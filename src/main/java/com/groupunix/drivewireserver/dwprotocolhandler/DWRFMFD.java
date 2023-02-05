package com.groupunix.drivewireserver.dwprotocolhandler;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.OS9Defs;

import static com.groupunix.drivewireserver.DWDefs.BYTE_BITS;
import static com.groupunix.drivewireserver.DWDefs.GREGORIAN_YEAR_OFFSET;

public class DWRFMFD {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWRFMFD");
  /**
   * Length of datetime fields (bytes).
   */
  public static final int DATE_TIME_LEN = 5;
  /**
   * Sector size (assumed).
   */
  public static final int SECTOR_SIZE = 256;
  /**
   * Offset to attributes byte.
   */
  public static final int OFFSET_ATTRIBUTES = 0;
  /**
   * Offset to owner field.
   */
  public static final int OFFSET_OWNER = 1;
  /**
   * Offset to date field.
   */
  public static final int OFFSET_DATE = 3;
  /**
   * Offset to link byte.
   */
  public static final int OFFSET_LINK = 8;
  /**
   * Offset to size field.
   */
  public static final int OFFSET_SIZE = 9;
  /**
   * Offset to CREAT field.
   */
  public static final int OFFSET_CREAT = 13;
  /**
   * Length of owner field size (bytes).
   */
  public static final int OWNER_SIZE = 2;
  /**
   * Length of date field (bytes).
   */
  public static final int DATE_LENGTH = 5;
  /**
   * Length of size field (bytes).
   */
  public static final int SIZE_LENGTH = 4;
  /**
   * Length of creat field (bytes).
   */
  public static final int CREAT_LENGTH = 3;
  /**
   * Source file path.
   */
  private final String pathstr;
  /**
   * File system manager.
   */
  private final FileSystemManager fsManager;
  /**
   * Source file object.
   */
  private final FileObject fileobj;
  /**
   * Attributes byte.
   */
  private byte attributes;
  /**
   * Owner bytes.
   */
  private byte[] owner;
  /**
   * Last modified bytes.
   */
  private byte[] dateLastModified;
  /**
   * Link byte.
   */
  private byte link;
  /**
   * File size bytes.
   */
  private byte[] fileSize;
  /**
   * Segment list extension.
   * <p>
   *   See DWRFMFD.md
   * </p>
   */
  private byte[] segmentListExtension;

  /**
   * RFM File Descriptor.
   *
   * @param pathString file path
   * @throws FileSystemException failed to read from source
   */
  public DWRFMFD(final String pathString) throws FileSystemException {
    this.pathstr = pathString;
    this.fsManager = VFS.getManager();
    this.fileobj = this.fsManager.resolveFile(pathString);
    LOGGER.info("New FD for '" + pathString + "'");
  }

  /**
   * Read file descriptor parameters.
   *
   * @return byte array
   */
  public byte[] getFD() {
    byte[] b = new byte[SECTOR_SIZE];

    for (int i = 0; i < SECTOR_SIZE; i++) {
      b[i] = 0;
    }
    b[OFFSET_ATTRIBUTES] = getAttributes();
    System.arraycopy(getOwner(), 0, b, OFFSET_OWNER, OWNER_SIZE);
    System.arraycopy(getLastModifiedDate(), 0, b, OFFSET_DATE, DATE_LENGTH);
    b[OFFSET_LINK] = getLink();
    System.arraycopy(getSize(), 0, b, OFFSET_SIZE, SIZE_LENGTH);
    System.arraycopy(getCreat(), 0, b, OFFSET_CREAT, CREAT_LENGTH);
    return (b);
  }

  /**
   * Set file descriptor parameters.
   *
   * @param fd file descriptor
   */
  public void setFD(final byte[] fd) {
    byte[] b;

    setAttributes(fd[OFFSET_ATTRIBUTES]);
    b = new byte[OWNER_SIZE];
    System.arraycopy(fd, OFFSET_OWNER, b, 0, OWNER_SIZE);
    setOwner(b);
    b = new byte[DATE_LENGTH];
    System.arraycopy(fd, OFFSET_DATE, b, 0, DATE_LENGTH);
    setLastModifiedDate(b);
    setLink(fd[OFFSET_LINK]);
    b = new byte[SIZE_LENGTH];
    System.arraycopy(fd, OFFSET_SIZE, b, 0, SIZE_LENGTH);
    setSize(b);
    b = new byte[CREAT_LENGTH];
    System.arraycopy(fd, OFFSET_CREAT, b, 0, CREAT_LENGTH);
    setCreat(b);
  }

  /**
   * Write file descriptor.
   * <p>
   *   Not implemented
   * </p>
   */
  public void writeFD() {
  }

  /**
   * Read file descriptor.
   *
   * @throws FileSystemException failed to read from source
   */
  public void readFD() throws FileSystemException {
    setOwner(new byte[]{0, 0});
    setLink((byte) 1);

    if (this.fileobj.exists()) {
      // attributes
      byte tmpmode = 0;
      // for now.. user = public
      if (fileobj.isReadable()) {
        tmpmode += OS9Defs.MODE_R + OS9Defs.MODE_PR;
      }
      if (fileobj.isWriteable()) {
        tmpmode += OS9Defs.MODE_W + OS9Defs.MODE_PW;
      }
      // everything is executable for now
      tmpmode += OS9Defs.MODE_E + OS9Defs.MODE_PE;
      if (fileobj.getType() == FileType.FOLDER) {
        tmpmode += OS9Defs.MODE_DIR;
      }
      setAttributes(tmpmode);
      // date and time modified
      setLastModifiedDate(
          timeToBytes(fileobj.getContent().getLastModifiedTime())
      );
      // size
      setSize(lengthToBytes(fileobj.getContent().getSize()));
      // date created (java doesn't know)
      setCreat(new byte[]{0, 0, 0});
    } else {
      LOGGER.error("attempt to read FD for non existent file '"
          + this.pathstr + "'");
    }
  }

  private byte[] lengthToBytes(final long length) {
    double maxLen = Math.pow(SECTOR_SIZE, SIZE_LENGTH) / 2;

    if (length > maxLen) {
      LOGGER.error("File too big: " + length + " bytes in '"
          + this.pathstr + "' (max " + maxLen + ")");
      return (new byte[]{0, 0, 0, 0});
    }
    byte[] b = new byte[SIZE_LENGTH];
    for (int i = 0; i < SIZE_LENGTH; i++) {
      b[SIZE_LENGTH - 1 - i] = (byte) (length >>> (i * BYTE_BITS));
    }
    return b;
  }

  /**
   * Convert long (time) to byte array.
   *
   * @param time (long) time
   * @return byte array
   */
  private byte[] timeToBytes(final long time) {
    GregorianCalendar c = new GregorianCalendar();
    int index = 0;
    c.setTime(new Date(time));
    byte[] b = new byte[DATE_TIME_LEN];
    b[index++] = (byte) (c.get(Calendar.YEAR) - GREGORIAN_YEAR_OFFSET);
    b[index++] = (byte) (c.get(Calendar.MONTH) + 1);
    b[index++] = (byte) (c.get(Calendar.DAY_OF_MONTH));
    b[index++] = (byte) (c.get(Calendar.HOUR_OF_DAY));
    b[index] = (byte) (c.get(Calendar.MINUTE));
    return b;
  }

  /**
   * Convert byte array to long (time).
   *
   * @param b byte array
   * @return long time
   */
  @SuppressWarnings("unused")
  private long bytesToTime(final byte[] b) {
    GregorianCalendar c = new GregorianCalendar();
    int index = 0;
    c.set(Calendar.YEAR, b[index++] + GREGORIAN_YEAR_OFFSET);
    c.set(Calendar.MONTH, b[index++] - 1);
    c.set(Calendar.DAY_OF_MONTH, b[index++]);
    c.set(Calendar.HOUR_OF_DAY, b[index++]);
    c.set(Calendar.MINUTE, b[index]);
    return c.getTimeInMillis();
  }

  /**
   * Get attributes.
   *
   * @return attributes
   */
  public byte getAttributes() {
    return attributes;
  }

  /**
   * Set attributes.
   *
   * @param attributesByte attributes
   */
  public void setAttributes(final byte attributesByte) {
    attributes = attributesByte;
  }

  /**
   * Get owner.
   *
   * @return owner byte array
   */
  public byte[] getOwner() {
    return owner;
  }

  /**
   * Set owner.
   *
   * @param ownerBytes owner byte array
   */
  public void setOwner(final byte[] ownerBytes) {
    owner = ownerBytes;
  }

  /**
   * Get last modified date.
   *
   * @return date byte array
   */
  public byte[] getLastModifiedDate() {
    return dateLastModified;
  }

  /**
   * Set last modified date.
   *
   * @param dateArray date byte array
   */
  public void setLastModifiedDate(final byte[] dateArray) {
    dateLastModified = dateArray;
  }

  /**
   * Get link.
   *
   * @return link
   */
  public byte getLink() {
    return link;
  }

  /**
   * Set link.
   *
   * @param linkByte link
   */
  public void setLink(final byte linkByte) {
    this.link = linkByte;
  }

  /**
   * Get file size.
   *
   * @return file size
   */
  public byte[] getSize() {
    return fileSize;
  }

  /**
   * Set file size.
   *
   * @param size file size
   */
  public void setSize(final byte[] size) {
    fileSize = size;
  }

  /**
   * Get segment list extension record.
   *
   * @return segment list
   */
  public byte[] getCreat() {
    return segmentListExtension;
  }

  /**
   * Set segment list record.
   *
   * @param creat segment list extension record
   */
  public void setCreat(final byte[] creat) {
    segmentListExtension = creat;
  }
}
