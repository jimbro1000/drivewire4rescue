package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFATException;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWDECBFileSystemDirEntry extends DWFileSystemDirEntry {
  /**
   * Sector size.
   */
  public static final int MAX_SECTOR_SIZE = 256;
  /**
   * First granule.
   */
  public static final int FIRST_GRANULE_OFFSET = 13;
  /**
   * Sector size MSB.
   */
  public static final int HIGH_SECTOR_SIZE_OFFSET = 14;
  /**
   * Sector size LSB.
   */
  public static final int LOW_SECTOR_SIZE_OFFSET = 15;
  /**
   * Flag offset.
   */
  public static final int FILE_FLAG_OFFSET = 12;
  /**
   * File type offset.
   */
  public static final int FILE_TYPE_OFFSET = 11;
  /**
   * File type basic.
   */
  public static final int BASIC_FILE_TYPE = 0;
  /**
   * File type data.
   */
  public static final int DATA_FILE_TYPE = 1;
  /**
   * File type ML.
   */
  public static final int MACHINE_CODE_TYPE = 2;
  /**
   * File type text.
   */
  public static final int TEXT_FILE_TYPE = 3;
  /**
   * Dead file offset.
   */
  public static final int DEAD_FILE_OFFSET = 0;
  /**
   * Start offset of file extension.
   */
  public static final int BEGIN_EXT = 8;
  /**
   * End offset of file extension.
   */
  public static final int END_EXT = 11;
  /**
   * Start offset of filename.
   */
  public static final int BEGIN_FILENAME = 0;
  /**
   * End offset of filename.
   */
  public static final int END_FILENAME = 8;

  /**
   * DECB file system directory entry constructor.
   *
   * @param buf directory entry byte array
   */
  public DWDECBFileSystemDirEntry(final byte[] buf) {
    super(buf);
  }

  /**
   * Get Filename.
   * <p>
   *   First 8 characters if file name
   * </p>
   * @return filename
   */
  public String getFileName() {
    return (new String(getData(), DWDefs.ENCODING)
        .substring(BEGIN_FILENAME, END_FILENAME));
  }

  /**
   * Get file extension.
   * <p>
   *   last 3 characters of file name
   * </p>
   * @return extension
   */
  public String getFileExt() {
    return new String(getData(), DWDefs.ENCODING).substring(BEGIN_EXT, END_EXT);
  }

  /**
   * Is file used.
   *
   * @return true if used
   */
  public boolean isUsed() {
    if (getData()[DEAD_FILE_OFFSET] == (byte) BYTE_MASK) {
      return false;
    }
    return getData()[DEAD_FILE_OFFSET] != (byte) 0;
  }

  /**
   * Has file been deleted.
   *
   * @return true if killed
   */
  @SuppressWarnings("unused")
  public boolean isKilled() {
    return getData()[DEAD_FILE_OFFSET] == (byte) 0;
  }

  /**
   * Get raw file type.
   *
   * @return file type
   */
  public int getFileType() {
    return this.getData()[FILE_TYPE_OFFSET] & BYTE_MASK;
  }

  /**
   * Get pretty file type.
   *
   * @return file type
   */
  public String getPrettyFileType() {
    String res = "unknown";
    return switch (this.getData()[FILE_TYPE_OFFSET]) {
      case BASIC_FILE_TYPE -> ("BASIC");
      case DATA_FILE_TYPE -> ("Data");
      case MACHINE_CODE_TYPE -> ("ML");
      case TEXT_FILE_TYPE -> ("Text");
      default -> res;
    };
  }

  /**
   * Get file flag.
   *
   * @return raw file flag value
   */
  public int getFileFlag() {
    return (this.getData()[FILE_FLAG_OFFSET] & BYTE_MASK);
  }

  /**
   * File flag is ascii.
   *
   * @return true if ascii
   */
  public boolean isAscii() {
    return getFileFlag() == BYTE_MASK;
  }

  /**
   * Get pretty file flag.
   *
   * @return file flag as text
   */
  @SuppressWarnings("unused")
  public String getPrettyFileFlag() {
    String res = "unknown";
    if ((this.getData()[FILE_FLAG_OFFSET] & BYTE_MASK) == BYTE_MASK) {
      return "ASCII";
    }
    if (this.getData()[FILE_FLAG_OFFSET] == 0) {
      return "Binary";
    }
    return res;
  }

  /**
   * Get first granule.
   *
   * @return first granule
   */
  public byte getFirstGranule() {
    return this.getData()[FIRST_GRANULE_OFFSET];
  }

  /**
   * Get bytes in last sector.
   *
   * @return number of bytes in sector
   * @throws DWFileSystemInvalidFATException invalid fat descriptor
   */
  public int getBytesInLastSector() throws DWFileSystemInvalidFATException {
    int res = (BYTE_MASK & this.getData()[HIGH_SECTOR_SIZE_OFFSET]) * BYTE_SHIFT
        + (BYTE_MASK & this.getData()[LOW_SECTOR_SIZE_OFFSET]);
    if (res > MAX_SECTOR_SIZE) {
      throw new DWFileSystemInvalidFATException(
          "file " + this.getFileName() + "." + this.getFileExt()
              + " claims to use " + res + " bytes in last sector?"
      );
    }
    return res;
  }

  /**
   * Get file path.
   * <p>
   *   Not implemented
   * </p>
   * @return filepath
   */
  @Override
  public String getFilePath() {
    return null;
  }

  /**
   * Get parent directory entry.
   * <p>
   *   Not implemented
   * </p>
   * @return parent
   */
  @Override
  public DWFileSystemDirEntry getParentEntry() {
    return null;
  }

  /**
   * Is entry a directory.
   *
   * @return bool
   */
  @Override
  public boolean isDirectory() {
    return false;
  }
}
