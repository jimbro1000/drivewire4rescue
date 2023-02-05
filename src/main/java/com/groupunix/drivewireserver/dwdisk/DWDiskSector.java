package com.groupunix.drivewireserver.dwdisk;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;

public class DWDiskSector {
  /**
   * Logical sector number.
   */
  private final int lsn;
  /**
   * Sector byte array.
   */
  private byte[] data;
  /**
   * Associated drivewire disk.
   */
  private final DWDisk dwDisk;
  /**
   * Sector size.
   */
  private final int sectorsize;
  /**
   * Direct flag.
   */
  private final boolean directFlag;
  /**
   * Dirty flag.
   */
  private boolean dirtyFlag = false;

  /**
   * Disk sector constructor.
   *
   * @param disk       drivewire disk
   * @param sector     logical sector number
   * @param sectorSize sector size
   * @param direct     is direct
   * @throws FileSystemException
   */
  public DWDiskSector(
      final DWDisk disk, final int sector,
      final int sectorSize, final boolean direct
  ) throws FileSystemException {
    this.lsn = sector;
    this.sectorsize = sectorSize;
    this.directFlag = direct;
    this.dwDisk = disk;

    if (!direct) {
      this.data = new byte[sectorSize];
    }
  }

  /**
   * Get logical sector number.
   *
   * @return LSN
   */
  public int getLsn() {
    return this.lsn;
  }

  /**
   * Set sector data to new byte array data.
   *
   * @param newData new byte array
   * @param dirty   dirty flag
   */
  public synchronized void setData(final byte[] newData, final boolean dirty) {
    this.dirtyFlag = dirty;
    if (this.data == null) {
      this.data = new byte[newData.length];
    }
    System.arraycopy(newData, 0, this.data, 0, this.sectorsize);
  }

  /**
   * Get sector data as byte array.
   *
   * @return byte array
   * @throws IOException
   */
  public synchronized byte[] getData() throws IOException {
    if (this.data != null) {
      return this.data;
    } else if (this.directFlag) {
      return this.getFileSector();
    } else {
      return new byte[this.sectorsize];
    }
  }

  /**
   * Set sector data to new byte array data.
   * <p>
   * Tags sector as dirty
   * </p>
   *
   * @param newdata new byte array
   */
  public void setData(final byte[] newdata) {
    if (this.data == null) {
      this.data = new byte[newdata.length];
    }
    this.dirtyFlag = true;
    System.arraycopy(newdata, 0, this.data, 0, this.sectorsize);
  }

  /**
   * Remove dirty tag on sector.
   */
  public synchronized void makeClean() {
    if (this.dirtyFlag) {
      if (this.directFlag) {
        this.data = null;
      }
      this.dirtyFlag = false;
    }
  }

  /**
   * Get file sector as byte array.
   *
   * @return byte array
   * @throws IOException
   */
  private byte[] getFileSector() throws IOException {
    RandomAccessContent raf = this.dwDisk
        .getFileObject()
        .getContent()
        .getRandomAccessContent(RandomAccessMode.READ);
    long pos = (long) this.lsn * this.sectorsize;
    raf.seek(pos);
    byte[] buf = new byte[this.sectorsize];
    raf.readFully(buf);
    raf.close();
    return buf;
  }

  /**
   * Is sector dirty?.
   *
   * @return true if dirty
   */
  public synchronized boolean isDirty() {
    return dirtyFlag;
  }

  /**
   * Modify byte at offset.
   * <p>
   *   Tags sector as dirty
   * </p>
   *
   * @param i offset
   * @param b byte data
   * @throws IOException
   */
  public void setDataByte(final int i, final byte b) throws IOException {
    if (this.data == null) {
      this.data = this.getFileSector();
    }
    this.data[i] = b;
    this.dirtyFlag = true;
  }

  /**
   * Tag sector as dirty.
   */
  public void makeDirty() {
    this.dirtyFlag = true;
  }
}
