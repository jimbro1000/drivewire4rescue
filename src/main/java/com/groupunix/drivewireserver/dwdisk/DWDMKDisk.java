package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWDMKDisk extends DWDisk {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWDMKDisk");
  /**
   * Sectors per track.
   */
  public static final int SECTORS_PER_TRACK = 18;
  /**
   * DMK Header size (bytes).
   */
  private static final int HEADER_SIZE = 16;
  /**
   * Default initial gap.
   */
  public static final int INITIAL_GAP = 43;
  /**
   * Initial gap for single density.
   */
  public static final int SINGLE_DENSITY_GAP = 30;
  /**
   * Initial pointer offset.
   */
  public static final int LOCATION_OFFSET = 7;
  /**
   * Sync value for single density.
   */
  public static final int SYNC_VALUE = 0xA1;
  /**
   * minimum track data value.
   */
  public static final int TRACK_MINIMUM = 0xF8;
  /**
   * maximum track data value.
   */
  public static final int TRACK_MAXIMUM = 0xFB;
  /**
   * Maximum IDAM index.
   */
  public static final int IDAM_MAX = 64;
  /**
   * Disk tracks.
   */
  private final ArrayList<DWDMKDiskTrack> tracks = new ArrayList<>();
  /**
   * Disk header.
   */
  private DWDMKDiskHeader header;

  /**
   * DMK disc constructor.
   *
   * @param fileObj source file object
   * @throws IOException
   * @throws DWImageFormatException
   */
  public DWDMKDisk(final FileObject fileObj)
      throws IOException, DWImageFormatException {
    super(fileObj);
    this.setParam("_format", "dmk");
    load();
    LOGGER.debug("New DMK disk for " + fileObj.getName().getURI());
  }

  /**
   * Determine if header describes a valid disk image.
   *
   * @param hdr header bytes
   * @param fObjSize file object size
   * @return decision
   */
  public static int considerImage(final byte[] hdr, final long fObjSize) {
    // is it big enough to have a header
    if (fObjSize > HEADER_SIZE) {
      // make a header object
      DWDMKDiskHeader header = new DWDMKDiskHeader(hdr);
      // is the size right?
      if (
          fObjSize == HEADER_SIZE + (
              (long) header.getSides()
                  * header.getTracks()
                  * header.getTrackLength()
          )
      ) {
        // good enough for mess, good enough for me
        return (DWDefs.DISK_CONSIDER_YES);
      }
    }
    return (DWDefs.DISK_CONSIDER_NO);
  }

  /**
   * Get disk format.
   *
   * @return disk format (DMK)
   */
  public int getDiskFormat() {
    return DWDefs.DISK_FORMAT_DMK;
  }

  /**
   * Load sectors from file.
   *
   * @throws IOException
   * @throws DWImageFormatException
   */
  public void load() throws IOException, DWImageFormatException {
    // load file into sector array
    InputStream fis;
    fis = this.getFileObject().getContent().getInputStream();
    // read disk header
    int readres = 0;
    byte[] hbuff = new byte[HEADER_SIZE];

    while (readres < HEADER_SIZE) {
      readres += fis.read(hbuff, readres, HEADER_SIZE - readres);
    }
    this.header = new DWDMKDiskHeader(hbuff);
    this.setParam("writeprotect", header.isWriteProtected());
    this.setParam("_tracks", header.getTracks());
    this.setParam("_sides", header.getSides());
    this.setParam("_density", header.getDensity());
    if (!header.isSingleSided() || header.isSingleDensity()) {
      String format = "";
      if (header.isSingleSided()) {
        format += "SS";
      } else {
        format += "DS";
      }
      if (header.isSingleDensity()) {
        format += "SD";
      } else {
        format += "DD";
      }
      fis.close();
      throw new DWImageFormatException(
          "Unsupported DMK format " + format
              + ", only SSDD is supported at this time"
      );
    }
    // read tracks
    byte[] tbuf;
    for (int i = 0; i < header.getTracks(); i++) {
      // read track data
      tbuf = new byte[header.getTrackLength()];
      readres = 0;
      while ((readres < header.getTrackLength()) && (readres > -1)) {
        int res = fis.read(tbuf, readres, header.getTrackLength() - readres);
        if (res > -1) {
          readres += res;
        } else {
          readres = -1;
        }
      }
      if (readres == -1) {
        fis.close();
        throw new DWImageFormatException(
            "DMK format appears corrupt, incomplete data for track " + i
        );
      }
      DWDMKDiskTrack dmktrack = new DWDMKDiskTrack(tbuf);
      if (dmktrack.getNumSectors() != SECTORS_PER_TRACK) {
        fis.close();
        throw new DWImageFormatException(
            "Unsupported DMK format, "
                + "only 18 sectors per track is supported at this time"
        );
      }
      this.tracks.add(dmktrack);
    }
    fis.close();
    // all tracks loaded ok, find sector data
    loadSectors();
    this.setParam(
        "_filesystem",
        DWUtils.prettyFileSystem(DWDiskDrives.getDiskFSType(this.getSectors()))
    );
  }

  private void loadSectors()
      throws DWImageFormatException, FileSystemException {
    this.getSectors().clear();
    // hard coded to 18 spt until i find a reason not to
    this.getSectors().setSize(header.getTracks() * SECTORS_PER_TRACK);
    this.setParam("_sectors", header.getTracks() * SECTORS_PER_TRACK);
    for (int t = 0; t < header.getTracks(); t++) {
      // track header / IDAM ptr table
      for (int i = 0; i < IDAM_MAX; i++) {
        DWDMKDiskIDAM idam = this.tracks.get(t).getIDAM(i);
        if (idam.getPtr() != 0) {
          if (idam.getTrack() != t) {
            System.out.println("mismatch track in IDAM?");
          }
          addSectorFrom(idam, t);
        }
      }
    }
  }

  private void addSectorFrom(final DWDMKDiskIDAM idam, final int track)
      throws DWImageFormatException, FileSystemException {
    int lsn = calcLSN(idam);
    if ((lsn > -1) && (lsn < this.getSectors().size())) {
      this.getSectors().set(
          lsn,
          new DWDiskSector(this, lsn, idam.getSectorSize(), false)
      );
      this.getSectors()
          .get(lsn)
          .setData(getSectorDataFrom(idam, track), false);
    } else {
      throw new DWImageFormatException(
          "Invalid LSN " + lsn + " while adding sector from DMK!"
      );
    }
  }

  private byte[] getSectorDataFrom(final DWDMKDiskIDAM idam, final int track)
      throws DWImageFormatException {
    byte[] buf = new byte[idam.getSectorSize()];
    int loc = idam.getPtr() + LOCATION_OFFSET;
    int gap = INITIAL_GAP;
    boolean sync = false;
    if (this.header.isSingleDensity()) {
      gap = SINGLE_DENSITY_GAP;
    }
    while (gap > 0) {
      if (((DWDefs.BYTE_MASK & this.tracks.get(track).getData()[loc])
          >= TRACK_MINIMUM)
          && ((DWDefs.BYTE_MASK & this.tracks.get(track).getData()[loc])
          <= TRACK_MAXIMUM)
          && (this.header.isSingleDensity() || sync)
      ) {
        break;
      }
      if (!this.header.isSingleDensity()) {
        sync = (DWDefs.BYTE_MASK
            & this.tracks.get(track).getData()[loc]) == SYNC_VALUE;
      }
      loc++;
      gap--;
    }
    if (gap > 0) {
      // found the data
      System.arraycopy(
          this.tracks.get(track).getData(),
          loc + 1,
          buf,
          0,
          idam.getSectorSize()
      );
    } else {
      throw new DWImageFormatException(
          "Sector data missing for track " + track
              + " sector " + idam.getSector()
      );
    }
    return buf;
  }

  private int calcLSN(final DWDMKDiskIDAM idam) {
    int t = idam.getTrack() * SECTORS_PER_TRACK;
    if (!header.isSingleSided()) {
      t = t * 2;
    }
    t += (idam.getSector() - 1) + (SECTORS_PER_TRACK * idam.getSide());
    return t;
  }

  /**
   * Seek disk sector by LSN.
   *
   * @param newLSN logical sector number
   * @throws DWInvalidSectorException
   * @throws DWSeekPastEndOfDeviceException
   */
  public void seekSector(final int newLSN)
      throws DWInvalidSectorException, DWSeekPastEndOfDeviceException {
    if (newLSN < 0) {
      throw new DWInvalidSectorException(
          "Sector " + newLSN + " is not valid"
      );
    } else if (newLSN > (this.getSectors().size() - 1)) {
      throw new DWSeekPastEndOfDeviceException(
          "Attempt to seek beyond end of image"
      );
    } else {
      this.setParam("_lsn", newLSN);
    }
  }

  /**
   * Write sector data.
   *
   * @param data byte array
   * @throws DWDriveWriteProtectedException
   * @throws IOException
   */
  public void writeSector(final byte[] data)
      throws DWDriveWriteProtectedException, IOException {
    if (this.getWriteProtect()) {
      throw new DWDriveWriteProtectedException("Disk is write protected");
    } else {
      this.getSectors().get(this.getLSN()).setData(data);
      this.incParam("_writes");
    }
  }

  /**
   * Read disk sector.
   *
   * @return sector data
   * @throws IOException
   */
  public byte[] readSector() throws IOException {
    this.incParam("_reads");
    return this.getSectors().get(this.getLSN()).getData();
  }
}
