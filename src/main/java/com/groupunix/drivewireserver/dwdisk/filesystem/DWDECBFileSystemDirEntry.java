package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.dwexceptions.*;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;

public class DWDECBFileSystemDirEntry extends DWFileSystemDirEntry {
  public static final int MAX_SECTOR_SIZE = 256;
  public static final int FIRST_GRANULE_OFFSET = 13;
  public static final int HIGH_SECTOR_SIZE_OFFSET = 14;
  public static final int LOW_SECTOR_SIZE_OFFSET = 15;
  public static final int FILE_FLAG_OFFSET = 12;
  public static final int FILE_TYPE_OFFSET = 11;
  public static final int BASIC_FILE_TYPE = 0;
  public static final int DATA_FILE_TYPE = 1;
  public static final int MACHINE_CODE_TYPE = 2;
  public static final int TEXT_FILE_TYPE = 3;
  public static final int DEAD_FILE_OFFSET = 0;
  /*
	Byte Description
	0�7 Filename, which is left justified and blank, filled. If byte0 is 0,
	then the file has been �KILL�ed and the directory entry is available
	for use. If byte0 is $FF, then the entry and all following entries
	have never been used.
	8�10 Filename extension
	11 File type: 0=BASIC, 1=BASIC data, 2=Machine language, 3= Text editor
	source
	12 ASCII flag: 0=binary or crunched BASIC, $FF=ASCII
	13 Number of the first granule in the file
	14�15 Number of bytes used in the last sector of the file
	16�31 Unused (future use)
	*/

  /**
   * DECB file system directory entry constructor.
   *
   * @param buf directory entry byte array
   */
  public DWDECBFileSystemDirEntry(byte[] buf) {
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
    return (new String(data).substring(0, 8));
  }

  /**
   * Get file extension.
   * <p>
   *   last 3 characters of file name
   * </p>
   * @return extension
   */
  public String getFileExt() {
    return new String(data).substring(8, 11);
  }

  /**
   * Is file used.
   *
   * @return true if used
   */
  public boolean isUsed() {
    if (data[DEAD_FILE_OFFSET] == (byte) BYTE_MASK) {
      return false;
    }
    return data[DEAD_FILE_OFFSET] != (byte) 0;
  }

  /**
   * Has file been deleted.
   *
   * @return true if killed
   */
  @SuppressWarnings("unused")
  public boolean isKilled() {
    return data[DEAD_FILE_OFFSET] == (byte) 0;
  }

  /**
   * Get raw file type.
   *
   * @return file type
   */
  public int getFileType() {
    return this.data[FILE_TYPE_OFFSET] & BYTE_MASK;
  }

  /**
   * Get pretty file type.
   *
   * @return file type
   */
  public String getPrettyFileType() {
    String res = "unknown";
    return switch (this.data[FILE_TYPE_OFFSET]) {
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
    return (this.data[FILE_FLAG_OFFSET] & BYTE_MASK);
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
    if ((this.data[FILE_FLAG_OFFSET] & BYTE_MASK) == BYTE_MASK) {
      return "ASCII";
    }
    if (this.data[FILE_FLAG_OFFSET] == 0) {
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
    return this.data[FIRST_GRANULE_OFFSET];
  }

  /**
   * Get bytes in last sector.
   *
   * @return number of bytes in sector
   * @throws DWFileSystemInvalidFATException invalid fat descriptor
   */
  public int getBytesInLastSector() throws DWFileSystemInvalidFATException {
    int res = (BYTE_MASK & this.data[HIGH_SECTOR_SIZE_OFFSET]) * BYTE_SHIFT
        + (BYTE_MASK & this.data[LOW_SECTOR_SIZE_OFFSET]);
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
