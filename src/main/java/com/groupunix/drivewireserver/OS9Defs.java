package com.groupunix.drivewireserver;

public final class OS9Defs {
  /**
   * Read.
   */
  public static final byte MODE_R = (byte) 1;
  // Constants from OS9

  // File Attributes Byte
  /**
   * Write.
   */
  public static final byte MODE_W = (byte) 2;
  /**
   * Execute.
   */
  public static final byte MODE_E = (byte) 4;
  /**
   * Public Read.
   */
  public static final byte MODE_PR = (byte) 8;
  /**
   * Public Write.
   */
  public static final byte MODE_PW = (byte) 16;
  /**
   * Public Execute.
   */
  public static final byte MODE_PE = (byte) 32;
  /**
   * Shared.
   */
  public static final byte MODE_SHARE = (byte) 64;
  /**
   * Directory.
   */
  public static final byte MODE_DIR = (byte) 128;
  // Status codes for Get/SetStat
  public static final byte SS_OPT = 0;
  public static final byte SS_READY = 1;
  public static final byte SS_SIZE = 2;
  public static final byte SS_RESET = 3;
  public static final byte SS_W_TRK = 4;
  public static final byte SS_POS = 5;
  public static final byte SS_EOF = 6;
  public static final byte SS_LINK = 7;
  public static final byte SS_U_LINK = 8;
  public static final byte SS_FEED = 9;
  public static final byte SS_FRZ = 10;
  public static final byte SS_SPT = 11;
  public static final byte SS_SQD = 12;
  public static final byte SS_D_CMD = 13;
  public static final byte SS_DEV_NM = 14;
  public static final byte SS_FD = 15;
  public static final byte SS_TICKS = 16;
  public static final byte SS_LOCK = 17;
  public static final byte SS_KY_SNS = 0x27;
  public static final byte SS_DIR_ENT = 0x21;
  // General commands
  public static final byte CMD_ESCAPE = 0x1b;
  public static final byte CMD_DW_EXT = 0x7F;
  public static final byte CMD_DW_EXT_DEV_NAME = 0x01;
  public static final byte CMD_DW_EXT_DEF_WIN = 0x02;
  public static final byte CMD_DW_EXT_TITLE = 0x03;
  public static final byte CMD_DW_EXT_PALETTE = 0x04;
  public static final byte CMD_DW_EXT_ICON = 0x05;
  public static final byte CMD_DW_EXT_ICON_NORMAL = 0x00;
  public static final byte CMD_DW_EXT_ICON_OK = 0x01;
  public static final byte CMD_DW_EXT_ICON_INFO = 0x02;
  public static final byte CMD_DW_EXT_ICON_WARN = 0x03;
  public static final byte CMD_DW_EXT_ICON_ERROR = 0x04;
  public static final byte CMD_DW_EXT_ICON_BUSY = 0x05;
  public static final byte CMD_B_COLOR = 0x33;
  public static final byte CMD_BOLD_SW = 0x3D;
  public static final byte CMD_BORDER = 0x34;
  public static final byte CMD_CW_AREA = 0x25;
  public static final byte CMD_DEF_COLR = 0x30;
  public static final byte CMD_DFN_GP_BUF = 0x29;
  public static final byte CMD_DW_END = 0x24;
  public static final byte CMD_DW_PROT_SW = 0x36;
  public static final byte CMD_DW_SET = 0x20;
  public static final byte STY_CURRENT_DISPLAY = (byte) 0xff;
  public static final byte STY_CURRENT_PROCESS = 0x00;
  public static final byte STY_TEXT_40 = 0x01;
  public static final byte STY_TEXT_80 = 0x02;
  public static final byte STY_GFX_HI_RES_2_COL = 0x05;
  public static final byte STY_GFX_LO_RES_4_COL = 0x06;
  public static final byte STY_GFX_HI_RES_4_COL = 0x07;
  public static final byte STY_GFX_LO_RES_16_COL = 0x08;
  public static final byte CMD_F_COLOR = 0x32;
  public static final byte CMD_FONT = 0x3a;
  public static final byte CMD_GC_SET = 0x39;
  public static final byte CMD_GET_BLK = 0x2c;
  public static final byte CMD_GP_LOAD = 0x2b;
  public static final byte CMD_KIL_BUF = 0x2a;
  public static final byte CMD_L_SET = 0x2f;
  public static final byte CMD_OW_END = 0x23;
  public static final byte CMD_OW_SET = 0x22;
  public static final byte CMD_PALETTE = 0x31;
  public static final byte CMD_PROP_SW = 0x3f;
  public static final byte CMD_P_SET = 0x2e;
  public static final byte CMD_PUT_BLK = 0x2d;
  public static final byte CMD_SCALE_SW = 0x35;
  public static final byte CMD_SELECT = 0x21;
  public static final byte CMD_T_CHAR_SW = 0x3c;
  // Drawing commands
  public static final byte CMD_ARC_3_P = 0x52;
  public static final byte CMD_BAR = 0x4a;
  public static final byte CMD_R_BAR = 0x4b;
  public static final byte CMD_BOX = 0x48;
  public static final byte CMD_R_BOX = 0x49;
  public static final byte CMD_CIRCLE = 0x50;
  public static final byte CMD_ELLIPSE = 0x51;
  public static final byte CMD_F_FILL = 0x4f;
  public static final byte CMD_LINE = 0x44;
  public static final byte CMD_R_LINE = 0x45;
  public static final byte CMD_LINE_M = 0x46;
  public static final byte CMD_R_LINE_M = 0x47;
  public static final byte CMD_POINT = 0x42;
  public static final byte CMD_R_POINT = 0x43;
  public static final byte CMD_PUT_GC = 0x4e;
  public static final byte CMD_SET_D_PTR = 0x40;
  public static final byte CMD_R_SET_D_PTR = 0x41;
  // Text commands
  public static final byte CTL_HOME = 0x01;
  public static final byte CTL_POSITION = 0x02;
  public static final byte CTL_ERASE_LINE = 0x03;
  public static final byte CTL_ERASE_TO_EOL = 0x04;
  public static final byte CTL_CURSOR_ON_OFF = 0x05;
  public static final byte CTL_CURSOR_ON_OFF_OFF = 0x20;
  public static final byte CTL_CURSOR_ON_OFF_ON = 0x21;
  public static final byte CTL_CURSOR_RIGHT = 0x06;
  public static final byte CTL_BELL = 0x07;
  public static final byte CTL_CURSOR_LEFT = 0x08;
  public static final byte CTL_CURSOR_UP = 0x09;
  public static final byte CTL_CURSOR_DOWN = 0x0a;
  public static final byte CTL_ERASE_TO_EOS = 0x0b;
  public static final byte CTL_CLEAR_SCREEN = 0x0c;
  public static final byte CTL_CR = 0x0d;
  public static final byte CTL_EXTENDED = 0x1f;
  public static final byte CTL_EXT_REVERSE_ON = 0x20;
  public static final byte CTL_EXT_REVERSE_OFF = 0x21;
  public static final byte CTL_EXT_UNDERLINE_ON = 0x22;
  public static final byte CTL_EXT_UNDERLINE_OFF = 0x23;
  public static final byte CTL_EXT_BLINK_ON = 0x24;
  public static final byte CTL_EXT_BLINK_OFF = 0x25;
  public static final byte CTL_EXT_INSERT_LINE = 0x30;
  public static final byte CTL_EXT_DELETE_LINE = 0x31;
  private OS9Defs() {
    //hidden constructor
  }
}
