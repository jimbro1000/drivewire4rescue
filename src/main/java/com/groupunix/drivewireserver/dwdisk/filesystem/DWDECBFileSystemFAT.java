package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.DECBDefs;
import com.groupunix.drivewireserver.dwdisk.DWDiskSector;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFullException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFATException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.LOW_NIBBLE_MASK;

public class DWDECBFileSystemFAT {
  /**
   * Bytes per sector.
   */
  public static final int BYTES_PER_SECTOR = 256;
  /**
   * Sectors per granule.
   */
  public static final int SECTORS_PER_GRANULE = 9;
  /**
   * Last entry in granule chain.
   */
  public static final int LAST_ENTRY = 0xC0;
  /**
   * Boundary position.
   * <p>
   * After boundary a 2 position offset
   * is needed
   * </p>
   */
  public static final int GRANULE_BOUNDARY = 34;
  /**
   * Source disk sector.
   */
  private final DWDiskSector diskSector;

  /**
   * DECB File system FAT.
   *
   * @param sector disk sector
   */
  public DWDECBFileSystemFAT(final DWDiskSector sector) {
    this.diskSector = sector;
  }

  /**
   * Get file sectors covered by granule.
   *
   * @param sectors disk sectors
   * @param granule granule
   * @return filtered list of sectors
   * @throws DWFileSystemInvalidFATException invalid FAT descriptor
   * @throws IOException                     failed to read from source
   */
  public ArrayList<DWDiskSector> getFileSectors(
      final Vector<DWDiskSector> sectors, final byte granule
  ) throws DWFileSystemInvalidFATException, IOException {
    ArrayList<DWDiskSector> res = new ArrayList<>();
    byte index = granule;
    byte entry = getEntry(index);
    while (!this.isLastEntry(entry)) {
      res.addAll(this.getGranuleSectors(sectors, index));
      index = entry;
      entry = getEntry(index);
    }
    // last granule is partial, first 4 bits say how many sectors to read
    for (int i = 0; i < (entry & LOW_NIBBLE_MASK); i++) {
      res.add(sectors.get(getFirstSectorNoForGranule(index) + i));
    }
    return (res);
  }

  /**
   * Get file bytes covered by granule.
   *
   * @param granule granule index
   * @return byte array of data
   * @throws DWFileSystemInvalidFATException invalid FAT descriptor
   * @throws IOException                     failed to read from source
   */
  @SuppressWarnings("unused")
  public ArrayList<Byte> getFileGranules(final byte granule)
      throws DWFileSystemInvalidFATException, IOException {
    byte index = granule;
    ArrayList<Byte> res = new ArrayList<>();
    while (!this.isLastEntry(index)) {
      res.add(index);
      index = getEntry(index);
    }
    return (res);
  }

  /**
   * Get sectors covered by granule.
   *
   * @param sectors disk sectors
   * @param granule granule index
   * @return filtered list of sectors
   */
  private List<DWDiskSector> getGranuleSectors(
      final Vector<DWDiskSector> sectors, final byte granule
  ) {
    List<DWDiskSector> res = new ArrayList<>();
    for (int i = 0; i < SECTORS_PER_GRANULE; ++i) {
      res.add(sectors.get(this.getFirstSectorNoForGranule(granule) + i));
    }
    return res;
  }

  /**
   * Get first sector number for granule.
   *
   * @param granule granule index
   * @return sector number
   */
  private int getFirstSectorNoForGranule(final byte granule) {
    if ((granule & BYTE_MASK) < GRANULE_BOUNDARY) {
      return (granule * SECTORS_PER_GRANULE);
    } else {
      return ((granule + 2) * SECTORS_PER_GRANULE);
    }
  }

  /**
   * Get entry at granule.
   *
   * @param granule index
   * @return byte
   * @throws DWFileSystemInvalidFATException invalid FAT descriptor
   * @throws IOException                     failed to read from source
   */
  public byte getEntry(final byte granule)
      throws DWFileSystemInvalidFATException, IOException {
    if (((granule & BYTE_MASK)) <= DECBDefs.FAT_SIZE) {
      if (
          (this.diskSector.getData()[(granule & BYTE_MASK)] & BYTE_MASK)
              == BYTE_MASK
      ) {
        throw (new DWFileSystemInvalidFATException(
            "Chain links to unused FAT entry #" + granule
        ));
      } else {
        return this.diskSector.getData()[(granule & BYTE_MASK)];
      }
    } else {
      throw (
          new DWFileSystemInvalidFATException("Invalid granule #" + granule)
      );
    }
  }

  /**
   * Get byte at granule index.
   *
   * @param granule granule index
   * @return data held at granule
   * @throws IOException failed to read from source
   */
  @SuppressWarnings("unused")
  public byte getGranuleByte(final byte granule) throws IOException {
    return this.diskSector.getData()[(granule & BYTE_MASK)];
  }

  /**
   * Is entry last in chain.
   *
   * @param entry byte
   * @return true if last
   */
  public boolean isLastEntry(final byte entry) {
    return (entry & LAST_ENTRY) == LAST_ENTRY;
  }

  /**
   * Get total free granules.
   *
   * @return granules
   * @throws IOException failed to read from source
   */
  public int getFreeGanules() throws IOException {
    int free = 0;
    for (int i = 0; i < (DECBDefs.FAT_SIZE); ++i) {
      if ((this.diskSector.getData()[i] & BYTE_MASK) == BYTE_MASK) {
        ++free;
      }
    }
    return free;
  }

  /**
   * Allocate space.
   *
   * @param bytes number of bytes
   * @return first granule
   * @throws DWFileSystemFullException file system full
   * @throws IOException               failed to write to source
   */
  public byte allocate(final int bytes)
      throws DWFileSystemFullException, IOException {
    int allocated = 0;
    int sectorsNeeded = (bytes / BYTES_PER_SECTOR) + 1;
    int granulesNeeded = sectorsNeeded / SECTORS_PER_GRANULE;
    if (!(sectorsNeeded % SECTORS_PER_GRANULE == 0)) {
      granulesNeeded++;
    }
    // check for free space
    if (this.getFreeGanules() < granulesNeeded) {
      throw (
          new DWFileSystemFullException(
              "Need " + granulesNeeded
                  + " granules, have only " + this.getFreeGanules() + " free."
          )
      );
    }
    byte lastgran = this.getFreeGranule();
    byte firstgran = lastgran;
    allocated++;
    while (allocated < granulesNeeded) {
      this.setEntry(lastgran, (byte) LAST_ENTRY);
      byte nextgran = this.getFreeGranule();
      this.setEntry(lastgran, nextgran);
      lastgran = nextgran;
      allocated++;
    }
    this.setEntry(
        lastgran,
        (byte) ((sectorsNeeded % SECTORS_PER_GRANULE) + LAST_ENTRY)
    );
    return firstgran;
  }

  /**
   * Set entry.
   *
   * @param gran     granule
   * @param nextGran next granule
   * @throws IOException failed to write to source
   */
  private void setEntry(final byte gran, final byte nextGran)
      throws IOException {
    this.diskSector.setDataByte((BYTE_MASK & gran), nextGran);
    this.diskSector.makeDirty();
  }

  /**
   * Get free granule.
   *
   * @return First free granule
   * @throws IOException failed to read from source
   */
  private byte getFreeGranule() throws IOException {
    for (byte i = 0; i < (DECBDefs.FAT_SIZE); i++) {
      if ((this.diskSector.getData()[i] & BYTE_MASK) == BYTE_MASK) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Dump FAT to string.
   *
   * @return FAT
   * @throws IOException failed to read from source
   */
  @SuppressWarnings("unused")
  public String dumpFat() throws IOException {
    StringBuilder res = new StringBuilder();
    for (int i = 0; i < DECBDefs.FAT_SIZE; i++) {
      if (this.diskSector.getData()[i] != -1) {
        res.append(i)
            .append(": ")
            .append(this.diskSector.getData()[i])
            .append("\t\t");
      }
    }
    return res.toString();
  }
}
