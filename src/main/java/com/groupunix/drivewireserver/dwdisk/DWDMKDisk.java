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
  public static final int BYTE_MASK = 0xFF;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWDMKDisk");
  /**
   * Multiplier for converting IDAM track to LSN.
   */
  private static final int IDAM_TRACK_MULTIPLIER = 18;
  /**
   * Track list.
   */
  private final ArrayList<DWDMKDiskTrack> tracks = new ArrayList<>();
  /**
   * Disk header.
   */
  private DWDMKDiskHeader diskHeader;

  /**
   * DMK Disk constructor.
   *
   * @param fileObject source file object
   * @throws IOException
   * @throws DWImageFormatException
   */
  public DWDMKDisk(final FileObject fileObject)
      throws IOException, DWImageFormatException {
    super(fileObject);
    this.setParam("_format", "dmk");
    load();
    LOGGER.debug("New DMK disk for " + fileObject.getName().getURI());
  }

  /**
   * Attempt to identify image from header.
   *
   * @param hdr      header byte array
   * @param fObjSize file object size
   * @return decision on identification
   */
  public static int considerImage(final byte[] hdr, final long fObjSize) {
    // is it big enough to have a header
    if (fObjSize > 16) {
      // make a header object
      DWDMKDiskHeader header = new DWDMKDiskHeader(hdr);
      // is the size right?
      if (
          fObjSize == 16 +
              (long) header.getSides() * header.getTracks() * header.getTrackLength()
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
   * @return DMK format
   */
  public int getDiskFormat() {
    return DWDefs.DISK_FORMAT_DMK;
  }

  /**
   * Load disk.
   *
   * @throws IOException
   * @throws DWImageFormatException
   */
  public void load() throws IOException, DWImageFormatException {
    // load file into sector array
    InputStream fis = this.getFileObject().getContent().getInputStream();
    // read disk header
    int readres = 0;
    byte[] hbuff = new byte[16];
    while (readres < 16) {
      readres += fis.read(hbuff, readres, 16 - readres);
    }
    this.diskHeader = new DWDMKDiskHeader(hbuff);
    this.setParam("writeprotect", diskHeader.isWriteProtected());
    this.setParam("_tracks", diskHeader.getTracks());
    this.setParam("_sides", diskHeader.getSides());
    this.setParam("_density", diskHeader.getDensity());
    if (!diskHeader.isSingleSided() || diskHeader.isSingleDensity()) {
      String format = "";
      if (diskHeader.isSingleSided()) {
        format += "SS";
      } else {
        format += "DS";
      }
      if (diskHeader.isSingleDensity()) {
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
    for (int i = 0; i < diskHeader.getTracks(); i++) {
      // read track data
      tbuf = new byte[diskHeader.getTrackLength()];
      readres = 0;
      while ((readres < diskHeader.getTrackLength()) && (readres > -1)) {
        int res = fis.read(tbuf, readres, diskHeader.getTrackLength() - readres);
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
      if (dmktrack.getNumSectors() != 18) {
        fis.close();
        throw new DWImageFormatException(
            "Unsupported DMK format, " +
                "only 18 sectors per track is supported at this time"
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
    this.getSectors().setSize(diskHeader.getTracks() * 18);
    this.setParam("_sectors", diskHeader.getTracks() * 18);
    for (int t = 0; t < diskHeader.getTracks(); t++) {
      // track header / IDAM ptr table
      for (int i = 0; i < 64; i++) {
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
          lsn, new DWDiskSector(this, lsn, idam.getSectorSize(), false)
      );
      this.getSectors().get(lsn).setData(
          getSectorDataFrom(idam, track), false
      );
    } else {
      throw new DWImageFormatException(
          "Invalid LSN " + lsn + " while adding sector from DMK!"
      );
    }
  }

  private byte[] getSectorDataFrom(final DWDMKDiskIDAM idam, final int track)
      throws DWImageFormatException {
    byte[] buf = new byte[idam.getSectorSize()];
    int loc = idam.getPtr() + 7;
    int gap = 43;

    boolean sync = false;
    if (this.diskHeader.isSingleDensity()) {
      gap = 30;
    }
    while (gap > 0) {
      if (((BYTE_MASK & this.tracks.get(track).getData()[loc]) >= 0xF8)
          && ((BYTE_MASK & this.tracks.get(track).getData()[loc]) <= 0xFB)) {
        if (this.diskHeader.isSingleDensity() || sync) {
          break;
        }
      }
      if (!this.diskHeader.isSingleDensity()) {
        sync = (BYTE_MASK & this.tracks.get(track).getData()[loc]) == 0xA1;
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

  /**
   * Calculate LSN from IDAM.
   *
   * @param idam
   * @return logical sector number
   */
  private int calcLSN(final DWDMKDiskIDAM idam) {
    int t = idam.getTrack() * IDAM_TRACK_MULTIPLIER;
    if (!diskHeader.isSingleSided()) {
      t = t * 2;
    }
    t += (idam.getSector() - 1) + (IDAM_TRACK_MULTIPLIER * idam.getSide());
    return t;
  }

  /**
   * Seek given sector by LSN.
   *
   * @param newLSN logical sector number
   * @throws DWInvalidSectorException
   * @throws DWSeekPastEndOfDeviceException
   */
  public void seekSector(final int newLSN)
      throws DWInvalidSectorException, DWSeekPastEndOfDeviceException {
    if (newLSN < 0) {
      throw new DWInvalidSectorException("Sector " + newLSN + " is not valid");
    } else if (newLSN > (this.getSectors().size() - 1)) {
      throw new DWSeekPastEndOfDeviceException(
          "Attempt to seek beyond end of image"
      );
    } else {
      this.setParam("_lsn", newLSN);
    }
  }

  /**
   * Write disk sector from byte array.
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
   * Read disk sector to byte array.
   *
   * @return byte array
   * @throws IOException
   */
  public byte[] readSector() throws IOException {
    this.incParam("_reads");
    return (this.getSectors().get(this.getLSN()).getData());
  }
}
