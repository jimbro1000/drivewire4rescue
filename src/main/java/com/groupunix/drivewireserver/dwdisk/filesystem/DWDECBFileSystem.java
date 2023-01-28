package com.groupunix.drivewireserver.dwdisk.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.groupunix.drivewireserver.DECBDefs;
import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwdisk.DWDiskSector;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFullException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidDirectoryException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFilenameException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWDECBFileSystem extends DWFileSystem {
  /**
   * File system name.
   */
  private static final String FS_NAME = "DECB";
  /**
   * Buffer size for byte array copies.
   */
  public static final int BUFFER_SIZE = 256;
  /**
   * Expected sector count.
   */
  public static final int MAX_SECTORS = 630;
  /**
   * Buffer size for directories.
   */
  public static final int DIRECTORY_BUFFER_SIZE = 32;
  /**
   * Offset to entry type.
   */
  public static final int ENTRY_TYPE_OFFSET = 11;
  /**
   * Offset to flag.
   */
  public static final int ENTRY_FLAG_OFFSET = 12;
  /**
   * Offset to first granule.
   */
  public static final int FIRST_GRANULE_OFFSET = 13;
  /**
   * Offset to leftovers.
   */
  public static final int LEFTOVERS_OFFSET = 15;
  /**
   * Offset to unused field.
   */
  public static final int UNUSED_OFFSET = 14;
  /**
   * Maximum number of directory entries.
   */
  public static final int MAX_DIRECTORY_ENTRIES = 67;
  /**
   * Directory entry field length.
   */
  public static final int DIRECTORY_ENTRY_LEN = 8;
  /**
   * Directory record size.
   */
  public static final int DIRECTORY_SIZE = 32;
  /**
   * Filename extension length.
   */
  public static final int FILENAME_EXTENSION_LEN = 3;
  /**
   * Number of blocks per group.
   */
  public static final int DIRECTORY_BLOCKS = 8;
  /**
   * Number of groups.
   */
  public static final int BLOCK_GROUPS = 9;
  /**
   * Minimum descriptor safe size.
   */
  public static final int MIN_SAFE = 0xC0;
  /**
   * Maximum descriptor safe size.
   */
  public static final int MAX_SAFE = 0xCF;
  /**
   * Filesystem errors.
   */
  private final List<String> fsErrors = new ArrayList<>();

  /**
   * DECB File system constructor.
   *
   * @param disk source disk image
   */
  public DWDECBFileSystem(final DWDisk disk) {
    super(disk);
  }

  /**
   * Get directory at given path.
   *
   * @param path filepath
   * @return list of directory entries
   * @throws IOException failed to read from source
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public List<DWFileSystemDirEntry> getDirectory(final String path)
      throws IOException, DWFileSystemInvalidDirectoryException {
    List<DWFileSystemDirEntry> dir = new ArrayList<>();
    try {
      for (int i = 0; i < BLOCK_GROUPS; i++) {
        for (int j = 0; j < DIRECTORY_BLOCKS; j++) {
          byte[] buf = new byte[DIRECTORY_SIZE];
          System.arraycopy(
              this.getDisk().getSector(i + DECBDefs.DIRECTORY_OFFSET).getData(),
              DIRECTORY_SIZE * j,
              buf,
              0,
              DIRECTORY_SIZE
          );
          DWDECBFileSystemDirEntry entry = new DWDECBFileSystemDirEntry(buf);
          dir.add(entry);
        }
      }
    } catch (DWDiskInvalidSectorNumber e) {
      throw new DWFileSystemInvalidDirectoryException(
          "Invalid DECB directory: " + e.getMessage()
      );
    }
    return dir;
  }

  /**
   * Test if filesystem has file.
   * @param filename filename
   * @return true if file is present
   * @throws IOException failed to read from source
   */
  public boolean hasFile(final String filename) throws IOException {
    try {
      for (DWFileSystemDirEntry e : this.getDirectory(null)) {
        if (
            (e.getFileName().trim() + "."
                + e.getFileExt()).equalsIgnoreCase(filename)
        ) {
          return true;
        }
      }
    } catch (DWFileSystemInvalidDirectoryException ignored) {
    }
    return false;
  }

  /**
   * Get sectors.
   *
   * @param filename file name
   * @return list of file sectors
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws DWFileSystemInvalidFATException invalid fat descriptor
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public ArrayList<DWDiskSector> getFileSectors(final String filename)
      throws DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    return getFAT().getFileSectors(
        this.getDisk().getSectors(),
        ((DWDECBFileSystemDirEntry) getDirEntry(filename)).getFirstGranule()
    );
  }

  /**
   * Get directory entry.
   *
   * @param filename file name
   * @return filesystem directory entry
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws IOException failed to read from source
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public DWFileSystemDirEntry getDirEntry(final String filename)
      throws DWFileSystemFileNotFoundException,
      IOException,
      DWFileSystemInvalidDirectoryException {
    for (DWFileSystemDirEntry e : this.getDirectory(null)) {
      if (
          (e.getFileName().trim() + "." + e.getFileExt())
          .equalsIgnoreCase(filename)
      ) {
        return e;
      }
    }
    throw new DWFileSystemFileNotFoundException(
        "File '" + filename + "' not found in DOS directory."
    );
  }

  /**
   * Get file content.
   *
   * @param filename source file name
   * @return file contents byte array
   * @throws DWFileSystemFileNotFoundException file not found
   * @throws DWFileSystemInvalidFATException invalid fat descriptor
   * @throws IOException failed to read from source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public byte[] getFileContents(final String filename)
      throws DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    byte[] res = new byte[0];
    ArrayList<DWDiskSector> sectors = this.getFileSectors(filename);
    int bl = ((DWDECBFileSystemDirEntry) getDirEntry(filename))
        .getBytesInLastSector();
    if ((sectors != null) && (sectors.size() > 0)) {
      res = new byte[(sectors.size() - 1) * DWDefs.DISK_SECTORSIZE + bl];
      for (int i = 0; i < sectors.size() - 1; i++) {
        System.arraycopy(
            sectors.get(i).getData(),
            0,
            res,
            i * DWDefs.DISK_SECTORSIZE,
            DWDefs.DISK_SECTORSIZE
        );
      }
    }
    // last sector is partial bytes
    if (bl > 0) {
      assert sectors != null;
      System.arraycopy(
          sectors.get(sectors.size() - 1).getData(),
          0,
          res,
          (sectors.size() - 1) * DWDefs.DISK_SECTORSIZE, bl
      );
    }
    return res;
  }

  /**
   * Add file to filesystem.
   *
   * @param filename file name
   * @param fileContents byte array of contents
   * @throws DWFileSystemFullException file system full
   * @throws DWFileSystemInvalidFilenameException invalid filename
   * @throws DWFileSystemFileNotFoundException filesystem not found
   * @throws DWFileSystemInvalidFATException invalid FAT descriptor
   * @throws IOException failed to write to source
   * @throws DWDiskInvalidSectorNumber invalid sector number
   * @throws DWFileSystemInvalidDirectoryException invalid file path
   */
  public void addFile(final String filename, final byte[] fileContents)
      throws DWFileSystemFullException,
      DWFileSystemInvalidFilenameException,
      DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    DWDECBFileSystemFAT fat = getFAT();
    // make fat entries
    byte firstGranule = fat.allocate(fileContents.length);

    // dir entry
    this.addDirectoryEntry(
        filename, firstGranule, (byte) (fileContents.length % BUFFER_SIZE)
    );

    // put content into sectors
    ArrayList<DWDiskSector> sectors = this.getFileSectors(filename);

    int byteswritten = 0;
    byte[] buf = new byte[BUFFER_SIZE];

    for (DWDiskSector sector : sectors) {
      if (fileContents.length - byteswritten >= BUFFER_SIZE) {
        System.arraycopy(fileContents, byteswritten, buf, 0, BUFFER_SIZE);
        byteswritten += BUFFER_SIZE;
      } else {
        System.arraycopy(
            fileContents,
            byteswritten,
            buf,
            0,
            (fileContents.length - byteswritten)
        );
        // zero pad partial sectors?
        for (
            int i = (fileContents.length - byteswritten); i < BUFFER_SIZE; i++
        ) {
          buf[i] = 0;
        }
        byteswritten += (fileContents.length - byteswritten);
      }
      sector.setData(buf);
    }
  }

  /**
   * Get file allocation table.
   *
   * @return FAT
   * @throws DWFileSystemInvalidFATException invalid FAT descriptor
   * @throws DWDiskInvalidSectorNumber invalid sector number
   */
  public DWDECBFileSystemFAT getFAT()
      throws DWFileSystemInvalidFATException, DWDiskInvalidSectorNumber {
    if (this.getDisk().getDiskSectors() < DECBDefs.FAT_OFFSET) {
      throw new DWFileSystemInvalidFATException(
          "Image is too small to contain a FAT"
      );
    }
    return new DWDECBFileSystemFAT(this.getDisk().getSector(DECBDefs.FAT_OFFSET));
  }

  private void addDirectoryEntry(
      final String filename, final byte firstGranule, final byte leftovers
  ) throws DWFileSystemFullException,
      DWFileSystemInvalidFilenameException,
      IOException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    List<DWFileSystemDirEntry> dr = this.getDirectory(null);

    int dirsize = 0;

    for (DWFileSystemDirEntry d : dr) {
      if (((DWDECBFileSystemDirEntry) d).isUsed()) {
        dirsize++;
      }
    }

    if (dirsize > MAX_DIRECTORY_ENTRIES) {
      throw (new DWFileSystemFullException("No free directory entries"));
    }

    byte[] buf = new byte[DIRECTORY_BUFFER_SIZE];
    byte[] secdata;

    DWDiskSector sec = this.getDisk().getSector(
        (dirsize / DIRECTORY_ENTRY_LEN) + DECBDefs.DIRECTORY_OFFSET
    );
    secdata = sec.getData();
    String[] fileParts = filename.split("\\.");

    if (fileParts.length != 2) {
      throw new DWFileSystemInvalidFilenameException(
          "Invalid filename (parts) '" + filename + "' " + fileParts.length
      );
    }

    StringBuilder name = new StringBuilder(fileParts[0]);
    String ext = fileParts[1];

    if ((name.length() < 1) || (name.length() > DIRECTORY_ENTRY_LEN)) {
      throw new DWFileSystemInvalidFilenameException(
          "Invalid filename (name) '" + filename + "'"
      );
    }

    if (ext.length() != FILENAME_EXTENSION_LEN) {
      throw new DWFileSystemInvalidFilenameException(
          "Invalid filename (ext) '" + filename + "'"
      );
    }

    while (name.length() < DIRECTORY_ENTRY_LEN) {
      name.append(" ");
    }

    System.arraycopy(
        name.toString().getBytes(),
        0,
        buf,
        0,
        DIRECTORY_ENTRY_LEN
    );
    System.arraycopy(
        ext.getBytes(),
        0,
        buf,
        DIRECTORY_ENTRY_LEN,
        FILENAME_EXTENSION_LEN
    );
    // try to recognize filetype.. assume binary?
    DWDECBFileSystemDirExtensionMapping mapping
        = new DWDECBFileSystemDirExtensionMapping(
            ext, DECBDefs.FLAG_BIN, DECBDefs.FILETYPE_ML
    );
    if (
        DriveWireServer
            .getServerConfiguration()
            .getMaxIndex("DECBExtensionMapping") > -1
    ) {
      for (
          int i = 0;
          i <= DriveWireServer
              .getServerConfiguration()
              .getMaxIndex("DECBExtensionMapping");
          i++
      ) {
        String kp = "DECBExtensionMapping(" + i + ")";
        // validate entry first
        if (
            DriveWireServer
                .getServerConfiguration()
                .containsKey(kp + "[@extension]")
                && DriveWireServer
                .getServerConfiguration()
                .containsKey(kp + "[@ascii]")
                && DriveWireServer
                .getServerConfiguration()
                .containsKey(kp + "[@filetype]")
        ) {
          if (
              DriveWireServer
                  .getServerConfiguration()
                  .getString(kp + "[@extension]").equalsIgnoreCase(ext)
          ) {
            // we have a winner
            mapping.setType(
                DriveWireServer
                    .getServerConfiguration()
                    .getByte(kp + "[@filetype]")
            );
            if (
                DriveWireServer
                    .getServerConfiguration()
                    .getBoolean(kp + "[@ascii]")
            ) {
              mapping.setFlag(DECBDefs.FLAG_ASCII);
            } else {
              mapping.setFlag(DECBDefs.FLAG_BIN);
            }
          }
        }
      }
    }

    // set our dirinfos
    buf[ENTRY_TYPE_OFFSET] = mapping.getType();
    buf[ENTRY_FLAG_OFFSET] = mapping.getFlag();
    buf[FIRST_GRANULE_OFFSET] = firstGranule;
    buf[UNUSED_OFFSET] = 0;
    buf[LEFTOVERS_OFFSET] = leftovers;
    System.arraycopy(
        buf,
        0,
        secdata,
        (dirsize % DIRECTORY_ENTRY_LEN) * DIRECTORY_BUFFER_SIZE,
        DIRECTORY_BUFFER_SIZE
    );
    sec.setData(secdata);
  }

  /**
   * Format filesystem.
   *
   * @throws DWInvalidSectorException invalid sector
   * @throws DWSeekPastEndOfDeviceException attempt to write past end of disk
   * @throws DWDriveWriteProtectedException write protected
   * @throws IOException failed to write to filesystem source
   */
  public void format()
      throws DWInvalidSectorException,
      DWSeekPastEndOfDeviceException,
      DWDriveWriteProtectedException,
      IOException {
    // just init to all FF (mess does this?)

    if (this.getDisk() != null) {
      byte[] buf = new byte[BUFFER_SIZE];
      for (int i = 0; i < BUFFER_SIZE; i++) {
        buf[i] = (byte) BYTE_MASK;
      }
      this.getDisk().getSectors().removeAllElements();
      for (int i = 0; i < MAX_SECTORS; i++) {
        this.getDisk().getSectors().add(
            new DWDiskSector(this.getDisk(), i, BUFFER_SIZE, this.getDisk().getDirect())
        );
        this.getDisk().getSectors().get(i).setData(buf);
      }
    }

  }

  /**
   * Get filesystem name.
   *
   * @return name
   */
  @Override
  public String getFSName() {
    return FS_NAME;
  }

  /**
   * Is filesystem valid?
   *
   * @return true if valid
   */
  @Override
  public boolean isValidFS() {
    if (this.getDisk().getSectors().size() == MAX_SECTORS) {
      boolean wacky = false;

      try {
        List<DWFileSystemDirEntry> dir = this.getDirectory(null);
        // look for wacky directory entries
        for (DWFileSystemDirEntry e : dir) {
          if (
              ((DWDECBFileSystemDirEntry) e).getFirstGranule()
                  > DECBDefs.FAT_SIZE
          ) {
            this.fsErrors.add(
                e.getFileName() + "." + e.getFileExt() + ": First granule of "
                    + ((DWDECBFileSystemDirEntry) e).getFirstGranule()
                    + " is > FAT size"
            );
            wacky = true;
          } else if (
              (((DWDECBFileSystemDirEntry) e).getFileFlag() != 0)
                  && (((DWDECBFileSystemDirEntry) e).getFileFlag() != BYTE_MASK)
          ) {
            this.fsErrors.add(
                e.getFileName() + "." + e.getFileExt() + ": FileFlag of "
                    + ((DWDECBFileSystemDirEntry) e).getFileFlag()
                    + " is not defined..?"
            );
            wacky = true;
          }
        }
        // look for wacky fat
        for (int i = 0; i < DECBDefs.FAT_SIZE; i++) {
          int val;
          try {
            val = BYTE_MASK
                & this.getDisk().getSector(DECBDefs.FAT_OFFSET).getData()[i];
            if (
                ((val > DECBDefs.FAT_SIZE) && (val < MIN_SAFE))
                    || ((val > MAX_SAFE) && (val < BYTE_MASK))
            ) {
              this.fsErrors.add("FAT entry #" + i
                  + " is " + val + ", which points beyond FAT");
              wacky = true;
            }
          } catch (IOException e1) {
            this.fsErrors.add(e1.getMessage());
            wacky = true;
          } catch (DWDiskInvalidSectorNumber e) {
            wacky = true;
            this.fsErrors.add(e.getMessage());
          }
        }
      } catch (IOException | DWFileSystemInvalidDirectoryException e) {
        wacky = true;
        this.fsErrors.add(e.getMessage());
      }
      return !wacky;
    } else {
      this.fsErrors.add("Disk size doesn't match known DECB image size");
    }
    return false;
  }

  /**
   * Get filesystem errors.
   *
   * @return list of errors
   */
  @Override
  public List<String> getFSErrors() {
    return this.fsErrors;
  }
}
