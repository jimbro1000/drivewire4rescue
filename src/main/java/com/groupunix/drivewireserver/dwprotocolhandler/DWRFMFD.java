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

public class DWRFMFD {

  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWRFMFD");
  private final String pathstr;
  private final FileSystemManager fsManager;
  private final FileObject fileobj;
  private byte attributes;
  private byte[] owner;
  private byte[] dateLastModified;
  private byte link;
  private byte[] fileSize;
  private byte[] segmentListExtension;


  public DWRFMFD(final String pathstr) throws FileSystemException {
    this.pathstr = pathstr;
    this.fsManager = VFS.getManager();
    this.fileobj = this.fsManager.resolveFile(pathstr);
    LOGGER.info("New FD for '" + pathstr + "'");
  }

  public byte[] getFD() {
    byte[] b = new byte[256];

    for (int i = 0; i < 256; i++) {
      b[i] = 0;
    }
    b[0] = getAttributes();
    System.arraycopy(getOwner(), 0, b, 1, 2);
    System.arraycopy(getLastModifiedDate(), 0, b, 3, 5);
    b[8] = getLink();
    System.arraycopy(getSize(), 0, b, 9, 4);
    System.arraycopy(getCreat(), 0, b, 13, 3);
    return (b);
  }

  public void setFD(final byte[] fd) {
    byte[] b = new byte[5];

    setAttributes(fd[0]);
    System.arraycopy(fd, 1, b, 0, 2);
    setOwner(b);
    System.arraycopy(fd, 3, b, 0, 5);
    setLastModifiedDate(b);
    setLink(fd[8]);
    System.arraycopy(fd, 9, b, 0, 4);
    setSize(b);
    System.arraycopy(fd, 13, b, 0, 3);
    setCreat(b);
  }


  public void writeFD() {
    // no implementation
  }


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
      setLastModifiedDate(timeToBytes(fileobj.getContent().getLastModifiedTime()));
      // size
      setSize(lengthToBytes(fileobj.getContent().getSize()));
      // date created (java doesn't know)
      setCreat(new byte[]{0, 0, 0});
    } else {
      LOGGER.error("attempt to read FD for non existent file '" + this.pathstr + "'");
    }
  }

  private byte[] lengthToBytes(final long length) {
    double maxlen = Math.pow(256, 4) / 2;

    if (length > maxlen) {
      LOGGER.error("File too big: " + length + " bytes in '" + this.pathstr + "' (max " + maxlen + ")");
      return (new byte[]{0, 0, 0, 0});
    }
    byte[] b = new byte[4];
    for (int i = 0; i < 4; i++) {
      b[3 - i] = (byte) (length >>> (i * 8));
    }
    return (b);
  }

  private byte[] timeToBytes(final long time) {
    GregorianCalendar c = new GregorianCalendar();

    c.setTime(new Date(time));
    byte[] b = new byte[5];
    b[0] = (byte) (c.get(Calendar.YEAR) - 108);
    b[1] = (byte) (c.get(Calendar.MONTH) + 1);
    b[2] = (byte) (c.get(Calendar.DAY_OF_MONTH));
    b[3] = (byte) (c.get(Calendar.HOUR_OF_DAY));
    b[4] = (byte) (c.get(Calendar.MINUTE));
    return (b);
  }

  @SuppressWarnings("unused")
  private long bytesToTime(final byte[] b) {
    GregorianCalendar c = new GregorianCalendar();

    c.set(Calendar.YEAR, b[0] + 108);
    c.set(Calendar.MONTH, b[1] - 1);
    c.set(Calendar.DAY_OF_MONTH, b[2]);
    c.set(Calendar.HOUR_OF_DAY, b[3]);
    c.set(Calendar.MINUTE, b[4]);
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
