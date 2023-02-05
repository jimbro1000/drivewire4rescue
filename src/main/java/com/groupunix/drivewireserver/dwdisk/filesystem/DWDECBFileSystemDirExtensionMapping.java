package com.groupunix.drivewireserver.dwdisk.filesystem;

public class DWDECBFileSystemDirExtensionMapping {
  /**
   * File extension.
   */
  private String extension;
  /**
   * Flag byte.
   */
  private byte flag;
  /**
   * File type.
   */
  private byte type;

  /**
   * DECB Filesystem Directory Extension Mapping.
   *
   * @param ext      file extension
   * @param flagByte flag byte
   * @param typeByte file type byte
   */
  public DWDECBFileSystemDirExtensionMapping(
      final String ext,
      final byte flagByte,
      final byte typeByte
  ) {
    this.extension = ext;
    this.flag = flagByte;
    this.type = typeByte;
  }

  /**
   * Get file extension.
   *
   * @return file extension
   */
  @SuppressWarnings("unused")
  public String getExtension() {
    return extension;
  }

  /**
   * Set file extension.
   *
   * @param ext file extension
   */
  @SuppressWarnings("unused")
  public void setExtension(final String ext) {
    this.extension = ext;
  }

  /**
   * Get flag.
   *
   * @return flag byte
   */
  public byte getFlag() {
    return flag;
  }

  /**
   * Set flag.
   *
   * @param flagByte flag byte
   */
  public void setFlag(final byte flagByte) {
    this.flag = flagByte;
  }

  /**
   * Get type.
   *
   * @return type
   */
  public byte getType() {
    return type;
  }

  /**
   * Set type.
   *
   * @param typeByte type byte
   */
  public void setType(final byte typeByte) {
    this.type = typeByte;
  }
}
