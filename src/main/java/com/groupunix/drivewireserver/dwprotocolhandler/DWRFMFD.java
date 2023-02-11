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
    byte[] bytes = new byte[SECTOR_SIZE];

    for (int i = 0; i < SECTOR_SIZE; i++) {
      bytes[i] = 0;
    }
    bytes[OFFSET_ATTRIBUTES] = getAttributes();
    System.arraycopy(getOwner(), 0, bytes, OFFSET_OWNER, OWNER_SIZE);
    System.arraycopy(
        getLastModifiedDate(), 0, bytes, OFFSET_DATE, DATE_LENGTH
    );
    bytes[OFFSET_LINK] = getLink();
    System.arraycopy(getSize(), 0, bytes, OFFSET_SIZE, SIZE_LENGTH);
    System.arraycopy(getCreat(), 0, bytes, OFFSET_CREAT, CREAT_LENGTH);
    return bytes;
  }

  /**
   * Set file descriptor parameters.
   *
   * @param descriptor file descriptor
   */
  public void setFD(final byte[] descriptor) {
    setAttributes(descriptor[OFFSET_ATTRIBUTES]);
    final byte[] fileOwner = new byte[OWNER_SIZE];
    System.arraycopy(descriptor, OFFSET_OWNER, fileOwner, 0, OWNER_SIZE);
    setOwner(fileOwner);
    final byte[] date = new byte[DATE_LENGTH];
    System.arraycopy(descriptor, OFFSET_DATE, date, 0, DATE_LENGTH);
    setLastModifiedDate(date);
    setLink(descriptor[OFFSET_LINK]);
    final byte[] size = new byte[SIZE_LENGTH];
    System.arraycopy(descriptor, OFFSET_SIZE, size, 0, SIZE_LENGTH);
    setSize(size);
    final byte[] creat = new byte[CREAT_LENGTH];
    System.arraycopy(descriptor, OFFSET_CREAT, creat, 0, CREAT_LENGTH);
    setCreat(creat);
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
    final double maxLen = Math.pow(SECTOR_SIZE, SIZE_LENGTH) / 2;
    if (length > maxLen) {
      LOGGER.error("File too big: " + length + " bytes in '"
          + this.pathstr + "' (max " + maxLen + ")");
      return new byte[]{0, 0, 0, 0};
    }
    byte[] bytes = new byte[SIZE_LENGTH];
    for (int i = 0; i < SIZE_LENGTH; i++) {
      bytes[SIZE_LENGTH - 1 - i] = (byte) (length >>> (i * BYTE_BITS));
    }
    return bytes;
  }

  /**
   * Convert long (time) to byte array.
   *
   * @param time (long) time
   * @return byte array
   */
  private byte[] timeToBytes(final long time) {
    final GregorianCalendar calendar = new GregorianCalendar();
    int index = 0;
    calendar.setTime(new Date(time));
    byte[] bytes = new byte[DATE_TIME_LEN];
    bytes[index++] = (byte) (calendar.get(Calendar.YEAR)
        - GREGORIAN_YEAR_OFFSET);
    bytes[index++] = (byte) (calendar.get(Calendar.MONTH) + 1);
    bytes[index++] = (byte) (calendar.get(Calendar.DAY_OF_MONTH));
    bytes[index++] = (byte) (calendar.get(Calendar.HOUR_OF_DAY));
    bytes[index] = (byte) (calendar.get(Calendar.MINUTE));
    return bytes;
  }

  /**
   * Convert byte array to long (time).
   *
   * @param bytes byte array
   * @return long time
   */
  @SuppressWarnings("unused")
  private long bytesToTime(final byte[] bytes) {
    final GregorianCalendar calendar = new GregorianCalendar();
    int index = 0;
    calendar.set(Calendar.YEAR, bytes[index++] + GREGORIAN_YEAR_OFFSET);
    calendar.set(Calendar.MONTH, bytes[index++] - 1);
    calendar.set(Calendar.DAY_OF_MONTH, bytes[index++]);
    calendar.set(Calendar.HOUR_OF_DAY, bytes[index++]);
    calendar.set(Calendar.MINUTE, bytes[index]);
    return calendar.getTimeInMillis();
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
