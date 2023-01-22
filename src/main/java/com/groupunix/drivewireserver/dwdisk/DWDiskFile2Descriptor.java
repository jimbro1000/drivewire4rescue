package com.groupunix.drivewireserver.dwdisk;

import java.nio.ByteBuffer;

import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWDiskFile2Descriptor {
  // descriptor element array sizes
  /**
   * FD Seg size.
   */
  private static final int FD_SEG_SIZE = 240;
  /**
   * FD Creat size.
   */
  private static final int FD_CREAT_SIZE = 3;
  /**
   * FD Siz size.
   */
  private static final int FD_SIZ_SIZE = 4;
  /**
   * FD Dat size.
   */
  private static final int FD_DAT_SIZE = 5;
  /**
   * FD Own size.
   */
  private static final int FD_OWN_SIZE = 2;
  // descriptor element byte offsets
  /**
   * FD Seg offset.
   */
  private static final int FD_SEG_OFFSET = 16;
  /**
   * FD Creat offset.
   */
  private static final int FD_CREAT_OFFSET = 13;
  /**
   * FD Siz offset.
   */
  private static final int FD_SIZ_OFFSET = 9;
  /**
   * FD Dat offset.
   */
  private static final int FD_DAT_OFFSET = 3;
  /**
   * FD Own offset.
   */
  private static final int FD_OWN_OFFSET = 1;
  /**
   * FD Lnk offset.
   */
  private static final int FD_LNK_OFFSET = 8;
  /**
   * FD Att offset.
   */
  private static final int FD_ATT_OFFSET = 0;
  /**
   * File descriptor.
   */
  private final ByteBuffer fileDescriptor;
  /**
   * File descriptor sector byte array.
   */
  private final byte[] fdBytes;

  /**
   * Disk file to descriptor constructor.
   *
   * @param sector sector byte array
   */
  public DWDiskFile2Descriptor(final byte[] sector) {
    this.fdBytes = sector;
    this.fileDescriptor = ByteBuffer.wrap(fdBytes);
  }

  /**
   * Extract file descriptor att.
   *
   * @return att byte
   */
  public byte fdAtt() {
    return fdBytes[FD_ATT_OFFSET];
  }

  /**
   * Extract file descriptor own.
   *
   * @return own byte array
   */
  public long fdOwn() {
    byte[] fdOwnBytes = new byte[FD_OWN_SIZE];
    fileDescriptor.position(FD_OWN_OFFSET);
    fileDescriptor.get(fdOwnBytes, 0, FD_OWN_SIZE);
    return DWUtils.int2(fdOwnBytes);
  }

  /**
   * Extract file descriptor dat.
   *
   * @return dat byte array
   */
  public byte[] fdDat() {
    byte[] fdDatBytes = new byte[FD_DAT_SIZE];
    fileDescriptor.position(FD_DAT_OFFSET);
    fileDescriptor.get(fdDatBytes, 0, FD_DAT_SIZE);
    return fdDatBytes;
  }

  /**
   * Extract file descriptor lnk.
   *
   * @return lnk byte
   */
  public byte fdLnk() {
    return (fdBytes[FD_LNK_OFFSET]);
  }

  /**
   * Extract file descriptor siz.
   *
   * @return siz byte array
   */
  public long fdSiz() {
    byte[] fdSizBytes = new byte[FD_SIZ_SIZE];
    fileDescriptor.position(FD_SIZ_OFFSET);
    fileDescriptor.get(fdSizBytes, 0, FD_SIZ_SIZE);
    return DWUtils.int4(fdSizBytes);
  }

  /**
   * Extract file descriptor creat.
   *
   * @return creat byte array
   */
  public byte[] fdCreat() {
    byte[] fdCreatBytes = new byte[FD_CREAT_SIZE];
    fileDescriptor.position(FD_CREAT_OFFSET);
    fileDescriptor.get(fdCreatBytes, 0, FD_CREAT_SIZE);
    return fdCreatBytes;
  }

  /**
   * Extract file descriptor seg.
   *
   * @return seg byte array
   */
  public byte[] fdSeg() {
    byte[] fdSegBytes = new byte[FD_SEG_SIZE];
    fileDescriptor.position(FD_SEG_OFFSET);
    fileDescriptor.get(fdSegBytes, 0, FD_SEG_SIZE);
    return fdSegBytes;
  }
}
