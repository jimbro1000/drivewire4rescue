package com.groupunix.drivewireserver.dwprotocolhandler;

import com.groupunix.drivewireserver.DWDefs;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;


public class DWRFMDirEntry {
  /**
   * Length of directory entry byte array.
   */
  public static final int ENTRY_LEN = 64;
  /**
   * Minimum file name length.
   */
  public static final int MIN_FILENAME_LEN = 48;
  /**
   * Writeable permissions flag.
   */
  public static final byte WRITEABLE = (byte) 0x02;
  /**
   * Readable permissions flag.
   */
  public static final byte READABLE = (byte) 0x04;
  /**
   * Folder flag.
   */
  public static final byte FOLDER = (byte) 0x80;
  /**
   * Length of date byte array.
   */
  public static final int FILE_DATE_LEN = 6;
  /**
   * Offset to file name.
   */
  private static final int FILE_NAME_OFFSET = 0;
  /**
   * Offset to file size.
   */
  @SuppressWarnings("unused")
  private static final int FILE_SIZE_OFFSET = 53;
  /**
   * Offset to permissions byte.
   */
  private static final int FILE_PERMISSIONS_OFFSET = 57;
  /**
   * Offset to file date.
   */
  @SuppressWarnings("unused")
  private static final int FILE_DATE_OFFSET = 58;
  /**
   * File name.
   */
  private String fileName;
  /**
   * File size.
   */
  private long fileSize = 0;

  /**
   * File permissions.
   */
  private byte filePerms = 0;

  /**
   * File date.
   */
  @SuppressWarnings("unused")
  private byte[] fileDate = new byte[FILE_DATE_LEN]; // 58 - 63

  /**
   * RMF Directory Entry.
   *
   * @param file file object
   * @throws FileSystemException failed to read from source
   */
  public DWRFMDirEntry(final FileObject file) throws FileSystemException {
    if (file != null) {
      this.fileName = file.getName().getBaseName();
      if (file.getType() == FileType.FOLDER) {
        this.filePerms = FOLDER;
      }
      if (file.isReadable()) {
        this.filePerms += READABLE;
      }
      if (file.isWriteable()) {
        this.filePerms += WRITEABLE;
      }
      this.fileSize = file.getContent().getSize();
    }
  }

  /**
   * Get directory entry.
   *
   * @return byte array
   */
  public byte[] getEntry() {
    byte[] res = new byte[ENTRY_LEN];
    if (this.fileName != null) {
      System.arraycopy(
          this.fileName.getBytes(DWDefs.ENCODING),
          FILE_NAME_OFFSET,
          res,
          0,
          Math.max(MIN_FILENAME_LEN, this.fileName.length())
      );
      res[FILE_PERMISSIONS_OFFSET] = this.filePerms;
    }
    return res;
  }
}
