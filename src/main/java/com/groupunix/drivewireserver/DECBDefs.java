package com.groupunix.drivewireserver;

public final class DECBDefs {
  /**
   * Filetype BASIC = 0x00.
   */
  public static final byte FILETYPE_BASIC = (byte) 0;
  /**
   * Filetype DATA = 0x01.
   */
  public static final byte FILETYPE_DATA = (byte) 1;
  /**
   * Filetype ML = 0x02.
   */
  public static final byte FILETYPE_ML = (byte) 2;
  /**
   * Filetype TEXT = 0x03.
   */
  public static final byte FILETYPE_TEXT = (byte) 3;

  /**
   * ASCII flag = 0xFF.
   */
  public static final byte FLAG_ASCII = (byte) 0xFF;
  /**
   * BINARY flag = 0x00.
   */
  public static final byte FLAG_BIN = (byte) 0;

  /**
   * Byte offset for directory.
   */
  public static final int DIRECTORY_OFFSET = 308;
  /**
   * Byte offset for FAT.
   */
  public static final int FAT_OFFSET = 307;
  /**
   * FAT Size.
   */
  public static final byte FAT_SIZE = 68;

  private DECBDefs() {

  }
}
