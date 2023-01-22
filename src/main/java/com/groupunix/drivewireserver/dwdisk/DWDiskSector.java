package com.groupunix.drivewireserver.dwdisk;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.util.*;

import java.io.*;

public class DWDiskSector {
  private final int LSN;
  private byte[] data;
  //private byte[] dirtydata;
  private boolean dirty = false;
  private final int sectorsize;
  private final DWDisk disk;
  private final boolean direct;
  private RandomAccessContent raf;

  /**
   * Disk sector constructor.
   *
   * @param disk       drivewire disk
   * @param lsn        logical sector number
   * @param sectorsize sector size
   * @param direct     is direct
   * @throws FileSystemException
   */
  public DWDiskSector(
      DWDisk disk, int lsn, int sectorsize, boolean direct
  ) throws FileSystemException {
    this.LSN = lsn;
    this.sectorsize = sectorsize;
    this.direct = direct;
    this.disk = disk;

    if (!direct) {
      this.data = new byte[sectorsize];
    }
  }

  /**
   * Get logical sector number.
   *
   * @return LSN
   */
  public int getLSN() {
    return this.LSN;
  }

  /**
   * Set sector data to new byte array data.
   *
   * @param newdata new byte array
   * @param dirty   dirty flag
   */
  public synchronized void setData(byte[] newdata, boolean dirty) {
    this.dirty = dirty;
    if (this.data == null) {
      this.data = new byte[newdata.length];
    }
    System.arraycopy(newdata, 0, this.data, 0, this.sectorsize);
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
    } else if (this.direct) {
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
    this.dirty = true;
    System.arraycopy(newdata, 0, this.data, 0, this.sectorsize);
  }

  /**
   * Remove dirty tag on sector.
   */
  public synchronized void makeClean() {
    if (this.dirty) {
      if (this.direct) {
        this.data = null;
      }
      this.dirty = false;
    }
  }

  /**
   * Get file sector as byte array.
   *
   * @return byte array
   * @throws IOException
   */
  private byte[] getFileSector() throws IOException {
    raf = this.disk
        .getFileObject()
        .getContent()
        .getRandomAccessContent(RandomAccessMode.READ);
    long pos = (long) this.LSN * this.sectorsize;
    raf.seek(pos);
    byte[] buf = new byte[this.sectorsize];
    raf.readFully(buf);
    raf.close();
    raf = null;
    return buf;
  }

  /**
   * Is sector dirty?.
   *
   * @return true if dirty
   */
  public synchronized boolean isDirty() {
    return dirty;
  }

  /**
   * Modify byte at offset.
   * <p>
   * Tags sector as dirty
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
    this.dirty = true;
  }

  /**
   * Tag sector as dirty.
   */
  public void makeDirty() {
    this.dirty = true;
  }
}
