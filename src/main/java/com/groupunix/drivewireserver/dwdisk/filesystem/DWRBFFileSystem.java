package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwdisk.DWDiskSector;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidDirectoryException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

@SuppressWarnings("unused")
public class DWRBFFileSystem extends DWFileSystem {
  /**
   * File system name.
   */
  private static final String FSNAME = "RBF";
  /**
   * File system descriptor length (bytes).
   */
  public static final int DESCRIPTOR_LENGTH = 32;
  /**
   * File system descriptor sector offset.
   */
  public static final int SECTOR_OFFSET = 29;
  /**
   * Maximum file segments.
   */
  public static final int MAX_SEGMENTS = 48;
  /**
   * Maximum single read (bytes).
   */
  public static final int MAX_SINGLE_READ = 256;
  /**
   * Shift left bit (multiplier).
   */
  public static final int SHIFT_BITS = 8;

  /**
   * RBF file system constructor.
   *
   * @param disk source disk
   */
  @SuppressWarnings("unused")
  public DWRBFFileSystem(final DWDisk disk) {
    super(disk);
  }

  /**
   * Get directory from file path.
   *
   * @param path filepath
   * @return list of directory entries
   * @throws IOException failed to read from source
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  @Override
  public List<DWFileSystemDirEntry> getDirectory(final String path)
      throws IOException, DWFileSystemInvalidDirectoryException {
    ArrayList<DWFileSystemDirEntry> res;
    try {
      res = new ArrayList<>(this.getDirectoryFromFD(this.getFDFromPath(path)));
    } catch (DWDiskInvalidSectorNumber | DWFileSystemFileNotFoundException e) {
      throw new DWFileSystemInvalidDirectoryException(e.getMessage());
    }
    return res;
  }

  /**
   * Check if directory has file present.
   * <p>
   *   not implemented
   * </p>
   * @param filename filename
   * @return true if present
   */
  @Override
  public boolean hasFile(final String filename) {
    return false;
  }

  /**
   * Get file sectors.
   * <p>
   *   Not implemented
   * </p>
   * @param filename file name
   * @return list of disk sectors
   */
  @Override
  public ArrayList<DWDiskSector> getFileSectors(final String filename) {
    return null;
  }

  /**
   * Get directory entry.
   *
   * @param filename source file name
   * @return directory entry
   */
  public DWRBFFileSystemDirEntry getDirEntry(final String filename) {
    return null;
  }

  /**
   * Get file contents for given source file.
   *
   * @param filename source file name
   * @return file contents byte array
   * @throws DWFileSystemFileNotFoundException invalid file path
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  @Override
  public byte[] getFileContents(final String filename)
      throws DWFileSystemFileNotFoundException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    return this.getFileContentsFromDescriptor(getFDFromPath(filename));
  }

  /**
   * Get file descriptor from path.
   *
   * @param filename source file name
   * @return file descriptor
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public DWRBFFileDescriptor getFDFromPath(final String filename)
      throws IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemFileNotFoundException,
      DWFileSystemInvalidDirectoryException {
    if (filename == null) {
      return new DWRBFFileDescriptor(
          this.getDisk().getSector(this.getRootDirectoryLSN()).getData()
      );
    }
    final String[] path = filename.split("/");
    final ArrayList<DWRBFFileSystemDirEntry> dir = this.getRootDirectory();
    DWRBFFileDescriptor res = null;
    for (final String s : path) {
      res = null;
      int index = 0;
      while (index < dir.size() && res == null) {
        if (dir.get(index).getFileName().equals(s)) {
          res = dir.get(index).getFD();
        }
        index++;
      }
      if (res == null) {
        throw new DWFileSystemFileNotFoundException(
            "File not found: " + filename
        );
      }
    }
    return res;
  }

  /**
   * Add file.
   * <p>
   *   not implemented
   * </p>
   *
   * @param filename file name
   * @param fileContents byte array of contents
   */
  @Override
  public void addFile(final String filename, final byte[] fileContents) {
  }

  /**
   * Format file system.
   *
   * @throws DWInvalidSectorException invalid sector number
   * @throws DWSeekPastEndOfDeviceException attempt to read past end of disk
   * @throws DWDriveWriteProtectedException write protected
   * @throws IOException failed to read source
   */
  @Override
  public void format()
      throws DWInvalidSectorException,
      DWSeekPastEndOfDeviceException,
      DWDriveWriteProtectedException,
      IOException {
  }

  /**
   * Get file system name.
   *
   * @return file system name
   */
  @Override
  public String getFSName() {
    return FSNAME;
  }

  /**
   * Is filesystem valid.
   *
   * @return true if valid
   */
  @Override
  public boolean isValidFS() {
    try {
      final RBFFileSystemIDSector idSector
          = new RBFFileSystemIDSector(getDisk().getSector(0).getData());
      final int ddmap = (Integer) idSector.getAttrib("DD.MAP");
      final int ddbit = (Integer) idSector.getAttrib("DD.BIT");
      if (
          ddbit > 0 && Math.abs(
              ((Integer) idSector.getAttrib("DD.TOT"))
                  - ddmap / ddbit * SHIFT_BITS) < SHIFT_BITS
      ) {
        return true;
      }
    } catch (IOException | DWDiskInvalidSectorNumber ignored) {
    }
    return false;
  }

  /**
   * Get file system errors.
   *
   * @return null (not implemented)
   */
  @Override
  public List<String> getFSErrors() {
    return null;
  }

  /**
   * Get ID sector.
   *
   * @return File system ID sector
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   */
  public RBFFileSystemIDSector getIDSector()
      throws IOException, DWDiskInvalidSectorNumber {
    return new RBFFileSystemIDSector(getDisk().getSector(0).getData());
  }

  /**
   * Get sector allocation map.
   *
   * @return map byte data
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   */
  @SuppressWarnings("unused")
  public byte[] getSectorAllocationMap()
      throws IOException, DWDiskInvalidSectorNumber {
    final RBFFileSystemIDSector idSector = this.getIDSector();
    final int mapBytes =
        Integer.parseInt(idSector.getAttrib("DD.MAP").toString());
    final byte[] res = new byte[mapBytes];
    final int lsn = 1;
    int bytesReadTotal = 0;
    while (bytesReadTotal < mapBytes) {
      final DWDiskSector sector = this.getDisk().getSector(lsn);
      final int toRead = Math.min(MAX_SINGLE_READ, mapBytes - bytesReadTotal);
      System.arraycopy(sector.getData(), 0, res, bytesReadTotal, toRead);
      bytesReadTotal += toRead;
    }
    return res;
  }

  /**
   * Get root directory.
   *
   * @return list of file system directory entries
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public ArrayList<DWRBFFileSystemDirEntry> getRootDirectory()
      throws IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    ArrayList<DWRBFFileSystemDirEntry> dir;
    try {
      final int rootsec = this.getRootDirectoryLSN();
      final DWRBFFileDescriptor descriptor = new DWRBFFileDescriptor(
          this.getDisk().getSector(rootsec).getData()
      );
      dir = this.directoryFromContents(
          this.getFileContentsFromDescriptor(descriptor)
      );
    } catch (NumberFormatException e) {
      throw new DWDiskInvalidSectorNumber(e.getMessage());
    }
    return dir;
  }

  /**
   * Get root directory sector number.
   *
   * @return logical sector number
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws IOException failed to read from source
   */
  private int getRootDirectoryLSN()
      throws DWDiskInvalidSectorNumber, IOException {
    int rootSec;
    try {
      rootSec = Integer.parseInt(
          this.getIDSector().getAttrib("DD.DIR").toString()
      );
    } catch (NumberFormatException e) {
      throw new DWDiskInvalidSectorNumber(e.getMessage());
    }
    return rootSec;
  }

  /**
   * Get directory from file descriptor.
   *
   * @param descriptor file descriptor
   * @return list of file system directory entries
   * @throws IOException                           failed to read from source
   * @throws DWDiskInvalidSectorNumber             invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public ArrayList<DWRBFFileSystemDirEntry> getDirectoryFromFD(
      final DWRBFFileDescriptor descriptor
  ) throws IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    return this.directoryFromContents(
        this.getFileContentsFromDescriptor(descriptor)
    );
  }

  /**
   * Get directory entry from file contents.
   *
   * @param data file contents
   * @return list of directory entries
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   */
  private ArrayList<DWRBFFileSystemDirEntry> directoryFromContents(
      final byte[] data
  ) throws IOException, DWDiskInvalidSectorNumber {
    final ArrayList<DWRBFFileSystemDirEntry> res = new ArrayList<>();
    for (int i = 0; i < data.length / DESCRIPTOR_LENGTH; i++) {
      final byte[] entry = new byte[DESCRIPTOR_LENGTH];
      System.arraycopy(
          data,
          i * DESCRIPTOR_LENGTH,
          entry,
          0,
          DESCRIPTOR_LENGTH
      );
      if (entry[0] != 0) {
        final int lsn =
            (entry[SECTOR_OFFSET] & BYTE_MASK) * BYTE_SHIFT * BYTE_SHIFT
                + (entry[SECTOR_OFFSET + 1] & BYTE_MASK) * BYTE_SHIFT
                + (entry[SECTOR_OFFSET + 2] & BYTE_MASK);
        final DWRBFFileDescriptor descriptor = new DWRBFFileDescriptor(
            this.getDisk().getSector(lsn).getData()
        );
        res.add(new DWRBFFileSystemDirEntry(
            DWUtils.os9String(entry), lsn, descriptor)
        );
      }
    }
    return res;
  }

  /**
   * Get file contents from file descriptor.
   *
   * @param descriptor file descriptor
   * @return byte array of file contents
   * @throws IOException                           failed to read from source
   * @throws DWDiskInvalidSectorNumber             invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public byte[] getFileContentsFromDescriptor(
      final DWRBFFileDescriptor descriptor
  ) throws IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    if (descriptor.getFilesize() < 0) {
      throw new DWFileSystemInvalidDirectoryException("Negative file size?");
    }
    final byte[] res = new byte[descriptor.getFilesize()];
    int bytesread = 0;
    int segmentsread = 0;
    while (bytesread < res.length && segmentsread < MAX_SEGMENTS) {
      final int lsn = descriptor.getSegmentList()[segmentsread].getLsn();
      final int siz = descriptor.getSegmentList()[segmentsread].getSize();
      int index = lsn;
      while (index < lsn + siz && bytesread < res.length) {
        final int toRead = Math.min(MAX_SINGLE_READ, res.length - bytesread);
        System.arraycopy(
            this.getDisk().getSector(index).getData(),
            0,
            res,
            bytesread,
            toRead
        );
        bytesread += toRead;
        index++;
      }
      segmentsread++;
    }
    return res;
  }

  /**
   * Get disk sector.
   *
   * @param sector sector number
   * @return disk sector
   * @throws DWDiskInvalidSectorNumber invalid disk sector number
   */
  public DWDiskSector getSector(final int sector)
      throws DWDiskInvalidSectorNumber {
    return this.getDisk().getSector(sector);
  }
}
