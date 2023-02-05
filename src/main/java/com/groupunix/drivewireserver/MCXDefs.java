package com.groupunix.drivewireserver;

@SuppressWarnings("unused")
public final class MCXDefs {
  // protocol contants
  /**
   * MCX Protocol version.
   */
  public static final byte MCX_PROTOCOL_VERSION = 1;

  // protocol op codes
  /**
   * OP Alert.
   */
  public static final byte ALERT = '!';
  /**
   * OP Load file.
   */
  public static final byte OP_LOADFILE = 'L';
  /**
   * OP Get data block.
   */
  public static final byte OP_GETDATABLOCK = 'G';
  /**
   * OP Prepare next block.
   */
  public static final byte OP_PREPARENEXTBLOCK = 'N';
  /**
   * OP Save file.
   */
  public static final byte OP_SAVEFILE = 'S';
  /**
   * OP Write block.
   */
  public static final byte OP_WRITEBLOCK = 'W';
  /**
   * OP Open data file.
   */
  public static final byte OP_OPENDATAFILE = 'O';
  /**
   * OP Dir file request.
   */
  public static final byte OP_DIRFILEREQUEST = 'F';
  /**
   * OP Retrieve name.
   */
  public static final byte OP_RETRIEVENAME = '$';
  /**
   * OP Dir name request.
   */
  public static final byte OP_DIRNAMEREQUEST = 'D';
  /**
   * OP Set current dir.
   */
  public static final byte OP_SETCURRENTDIR = 'C';

  // response codes
  /**
   * RC Ok.
   */
  public static final byte MCXOK = 0;
  /**
   * RC Error FC.
   */
  public static final byte MCXERROR_FC = (byte) 8;
  /**
   * RC Error IO.
   */
  public static final byte MCXERROR_IO = (byte) 34;
  /**
   * RC Error FM.
   */
  public static final byte MCXERROR_FM = (byte) 36;
  /**
   * RC Error DN.
   */
  public static final byte MCXERROR_DN = (byte) 38;
  /**
   * RC Error NE.
   */
  public static final byte MCXERROR_NE = (byte) 40;
  /**
   * RC Error WP.
   */
  public static final byte MCXERROR_WP = (byte) 42;
  /**
   * RC Error FN.
   */
  public static final byte MCXERROR_FN = (byte) 44;
  /**
   * RC Error FS.
   */
  public static final byte MCXERROR_FS = (byte) 46;
  /**
   * RC Error IE.
   */
  public static final byte MCXERROR_IE = (byte) 48;
  /**
   * RC Error FD.
   */
  public static final byte MCXERROR_FD = (byte) 50;
  /**
   * RC Error AO.
   */
  public static final byte MCXERROR_AO = (byte) 52;
  /**
   * RC Error NO.
   */
  public static final byte MCXERROR_NO = (byte) 54;
  /**
   * RC Error DS.
   */
  public static final byte MCXERROR_DS = (byte) 56;

  // input buffer
  /**
   * Input wait (millis).
   */
  public static final int INPUT_WAIT = 2000;

  /**
   * MCXDefs hidden constructor.
   */
  private MCXDefs() {
  }
}
