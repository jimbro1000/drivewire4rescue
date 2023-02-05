package com.groupunix.drivewireserver.dwprotocolhandler;

@SuppressWarnings("unused")
public class DWRFMPathDescriptor {
  /**
   * FST record length.
   */
  private static final int FST_LENGTH = 32;
  /**
   * FST offset.
   */
  private static final int FST_OFFSET = 10;
  /**
   * BUF offset.
   */
  private static final int BUF_OFFSET = 8;
  /**
   * RGS offset.
   */
  private static final int RGS_OFFSET = 6;
  /**
   * DEV offset.
   */
  private static final int DEV_OFFSET = 3;
  /**
   * CPR offset.
   */
  private static final int CPR_OFFSET = 5;
  /**
   * CNT offset.
   */
  private static final int CNT_OFFSET = 2;
  /**
   * MOD offset.
   */
  private static final int MOD_OFFSET = 1;
  /**
   * PD offset.
   */
  private static final int PD_OFFSET = 0;
  /**
   * Path descriptor byte array.
   */
  private byte[] pdBytes;

  /**
   * RFM path descriptor.
   *
   * @param responseBuffer source response buffer
   */
  public DWRFMPathDescriptor(final byte[] responseBuffer) {
    setPdBytes(responseBuffer);
  }

  /**
   * Get PD byte array.
   *
   * @return pd byte array
   */
  @SuppressWarnings("unused")
  public byte[] getPdBytes() {
    return pdBytes;
  }

  /**
   * Set PD byte array.
   *
   * @param pd path descriptor byte array
   */
  public void setPdBytes(final byte[] pd) {
    int pdLen = pd.length;
    byte[] tmp = new byte[pdLen];
    System.arraycopy(pd, 0, tmp, 0, pdLen);
    this.pdBytes = tmp;
  }

  /**
   * Get PD byte.
   *
   * @return pd
   */
  @SuppressWarnings("unused")
  public byte getPD() {
    return (this.pdBytes[PD_OFFSET]);
  }

  /**
   * Get MOD byte.
   *
   * @return mod
   */
  @SuppressWarnings("unused")
  public byte getMOD() {
    return (this.pdBytes[MOD_OFFSET]);
  }

  /**
   * Get CNT byte.
   *
   * @return cnt
   */
  @SuppressWarnings("unused")
  public byte getCNT() {
    return (this.pdBytes[CNT_OFFSET]);
  }

  /**
   * Get DEV word.
   *
   * @return dev word
   */
  @SuppressWarnings("unused")
  public byte[] getDEV() {
    byte[] tmp = new byte[2];
    tmp[0] = this.pdBytes[DEV_OFFSET];
    tmp[1] = this.pdBytes[DEV_OFFSET + 1];
    return tmp;
  }

  /**
   * Get CPR byte.
   *
   * @return cpr
   */
  @SuppressWarnings("unused")
  public byte getCPR() {
    return (this.pdBytes[CPR_OFFSET]);
  }

  /**
   * Get RGS word.
   *
   * @return rgs word
   */
  @SuppressWarnings("unused")
  public byte[] getRGS() {
    byte[] tmp = new byte[2];
    tmp[0] = this.pdBytes[RGS_OFFSET];
    tmp[1] = this.pdBytes[RGS_OFFSET + 1];
    return (tmp);
  }

  /**
   * Get BUF word.
   *
   * @return buffer size word
   */
  @SuppressWarnings("unused")
  public byte[] getBUF() {
    byte[] tmp = new byte[2];
    tmp[0] = this.pdBytes[BUF_OFFSET];
    tmp[1] = this.pdBytes[BUF_OFFSET + 1];
    return tmp;
  }

  /**
   * Get FST.
   *
   * @return fst byte array
   */
  @SuppressWarnings("unused")
  public byte[] getFST() {
    byte[] fstBytes = new byte[FST_LENGTH];
    System.arraycopy(this.pdBytes, FST_OFFSET, fstBytes, 0, FST_LENGTH);
    return fstBytes;
  }
}
