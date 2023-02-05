package com.groupunix.drivewireserver;

@SuppressWarnings("unused")
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
  /**
   * Status OPT.
   */
  public static final byte SS_OPT = 0;
  /**
   * Status READY.
   */
  public static final byte SS_READY = 1;
  /**
   * Status SIZE.
   */
  public static final byte SS_SIZE = 2;
  /**
   * Status RESET.
   */
  public static final byte SS_RESET = 3;
  /**
   * Status W TRK (write track?).
   */
  public static final byte SS_W_TRK = 4;
  /**
   * Status POS.
   */
  public static final byte SS_POS = 5;
  /**
   * Status EOF.
   */
  public static final byte SS_EOF = 6;
  /**
   * Status LINK.
   */
  public static final byte SS_LINK = 7;
  /**
   * Status UNLINK.
   */
  public static final byte SS_U_LINK = 8;
  /**
   * Status FEED.
   */
  public static final byte SS_FEED = 9;
  /**
   * Status FRZ.
   */
  public static final byte SS_FRZ = 10;
  /**
   * Status SPT.
   */
  public static final byte SS_SPT = 11;
  /**
   * Status SQD.
   */
  public static final byte SS_SQD = 12;
  /**
   * Status D CMD.
   */
  public static final byte SS_D_CMD = 13;
  /**
   * Status DEV MM (device number).
   */
  public static final byte SS_DEV_NM = 14;
  /**
   * Status FD.
   */
  public static final byte SS_FD = 15;
  /**
   * Status TICKS.
   */
  public static final byte SS_TICKS = 16;
  /**
   * Status LOCK.
   */
  public static final byte SS_LOCK = 17;
  /**
   * Status var sect.
   */
  public static final byte SS_VAR_SECT = 0x12;
  /**
   * Status block read.
   */
  public static final byte SS_BLK_RD = 0x14;
  /**
   * Status block read.
   */
  public static final byte SS_BLK_WR = 0x15;
  /**
   * Status reten.
   */
  public static final byte SS_RETEN = 0x16;
  /**
   * Status W FM.
   */
  public static final byte SS_WFM = 0x17;
  /**
   * Status R FM.
   */
  public static final byte SS_RFM = 0x18;
  /**
   * Status S Sig.
   */
  public static final byte SS_S_SIG = 0x1A;
  /**
   * Status RELEA.
   */
  public static final byte SS_RELEA = 0x1B;
  /**
   * Status Attr.
   */
  public static final byte SS_ATTR = 0x1C;
  /**
   * Status RS bit.
   */
  public static final byte SS_RS_BIT = 0x1E;
  /**
   * Status FD Inf.
   */
  public static final byte SS_FD_INF = 0x20;
  /**
   * Status DIR ENT (directory entry).
   */
  public static final byte SS_DIR_ENT = 0x21;
  /**
   * Status D size.
   */
  public static final byte SS_D_SIZE = 0x26;
  /**
   * Status KY SNS.
   */
  public static final byte SS_KY_SNS = 0x27;
  /**
   * Status COMST.
   */
  public static final byte SS_COMST = 0x28;
  /**
   * Stat open serial port.
   */
  public static final byte SS_S_OPEN = 0x29;
  /**
   * Stat close serial port.
   */
  public static final byte SS_S_CLOSE = 0x2A;
  /**
   * Status hangup.
   */
  public static final byte SS_HNGUP = 0x30;
  /**
   * Status none.
   */
  public static final byte SS_NONE = (byte) 0xFF;
  // General commands
  /**
   * Command ESCAPE.
   */
  public static final byte CMD_ESCAPE = 0x1b;
  /**
   * Command DW EXT.
   */
  public static final byte CMD_DW_EXT = 0x7F;
  /**
   * Command DW EXT DEV NAME.
   */
  public static final byte CMD_DW_EXT_DEV_NAME = 0x01;
  /**
   * Command DW EXT DEF WIN.
   */
  public static final byte CMD_DW_EXT_DEF_WIN = 0x02;
  /**
   * Command DW EXT TITLE.
   */
  public static final byte CMD_DW_EXT_TITLE = 0x03;
  /**
   * Command DW EXT PALETTE.
   */
  public static final byte CMD_DW_EXT_PALETTE = 0x04;
  /**
   * Command DW EXT ICON.
   */
  public static final byte CMD_DW_EXT_ICON = 0x05;
  /**
   * Command DW EXT ICON NORMAL.
   */
  public static final byte CMD_DW_EXT_ICON_NORMAL = 0x00;
  /**
   * Command DW EXT ICON OK.
   */
  public static final byte CMD_DW_EXT_ICON_OK = 0x01;
  /**
   * Command DW EXT ICON INFO.
   */
  public static final byte CMD_DW_EXT_ICON_INFO = 0x02;
  /**
   * Command DW EXT ICON WARN.
   */
  public static final byte CMD_DW_EXT_ICON_WARN = 0x03;
  /**
   * Command DW EXT ICON ERROR.
   */
  public static final byte CMD_DW_EXT_ICON_ERROR = 0x04;
  /**
   * Command DW EXT ICON BUSY.
   */
  public static final byte CMD_DW_EXT_ICON_BUSY = 0x05;
  /**
   * Command B COLOR.
   */
  public static final byte CMD_B_COLOR = 0x33;
  /**
   * Command BOLD SW.
   */
  public static final byte CMD_BOLD_SW = 0x3D;
  /**
   * Command BORDER.
   */
  public static final byte CMD_BORDER = 0x34;
  /**
   * Command CW AREA.
   */
  public static final byte CMD_CW_AREA = 0x25;
  /**
   * Command DEF COLR.
   */
  public static final byte CMD_DEF_COLR = 0x30;
  /**
   * Command DFN GP BUF.
   */
  public static final byte CMD_DFN_GP_BUF = 0x29;
  /**
   * Command DW END.
   */
  public static final byte CMD_DW_END = 0x24;
  /**
   * Command DW PROT SW.
   */
  public static final byte CMD_DW_PROT_SW = 0x36;
  /**
   * Command DW SET.
   */
  public static final byte CMD_DW_SET = 0x20;
  /**
   * STY Current display.
   */
  public static final byte STY_CURRENT_DISPLAY = (byte) 0xff;
  /**
   * STY current process.
   */
  public static final byte STY_CURRENT_PROCESS = 0x00;
  /**
   * STY text 40.
   */
  public static final byte STY_TEXT_40 = 0x01;
  /**
   * STY text 80.
   */
  public static final byte STY_TEXT_80 = 0x02;
  /**
   * STY gfx hires 2 color.
   */
  public static final byte STY_GFX_HI_RES_2_COL = 0x05;
  /**
   * STY gfx lores 4 color.
   */
  public static final byte STY_GFX_LO_RES_4_COL = 0x06;
  /**
   * STY gfx hires 4 color.
   */
  public static final byte STY_GFX_HI_RES_4_COL = 0x07;
  /**
   * STY gfx lores 16 color.
   */
  public static final byte STY_GFX_LO_RES_16_COL = 0x08;
  /**
   * Command F COLOR.
   */
  public static final byte CMD_F_COLOR = 0x32;
  /**
   * Command FONT.
   */
  public static final byte CMD_FONT = 0x3a;
  /**
   * Command GC SET.
   */
  public static final byte CMD_GC_SET = 0x39;
  /**
   * Command GET BLK.
   */
  public static final byte CMD_GET_BLK = 0x2c;
  /**
   * Command GP LOAD.
   */
  public static final byte CMD_GP_LOAD = 0x2b;
  /**
   * Command KIL BUF.
   */
  public static final byte CMD_KIL_BUF = 0x2a;
  /**
   * Command L SET.
   */
  public static final byte CMD_L_SET = 0x2f;
  /**
   * Command OW END.
   */
  public static final byte CMD_OW_END = 0x23;
  /**
   * Command OW SET.
   */
  public static final byte CMD_OW_SET = 0x22;
  /**
   * Command PALETTE.
   */
  public static final byte CMD_PALETTE = 0x31;
  /**
   * Command PROP SW.
   */
  public static final byte CMD_PROP_SW = 0x3f;
  /**
   * Command P SET.
   */
  public static final byte CMD_P_SET = 0x2e;
  /**
   * Command PUT BLK.
   */
  public static final byte CMD_PUT_BLK = 0x2d;
  /**
   * Command SCALE SW.
   */
  public static final byte CMD_SCALE_SW = 0x35;
  /**
   * Command SELECT.
   */
  public static final byte CMD_SELECT = 0x21;
  /**
   * Command T CHAR SW.
   */
  public static final byte CMD_T_CHAR_SW = 0x3c;
  // Drawing commands
  /**
   * Command ARC 3 P.
   */
  public static final byte CMD_ARC_3_P = 0x52;
  /**
   * Command BAR.
   */
  public static final byte CMD_BAR = 0x4a;
  /**
   * Command R BAR.
   */
  public static final byte CMD_R_BAR = 0x4b;
  /**
   * Command BOX.
   */
  public static final byte CMD_BOX = 0x48;
  /**
   * Command R BOX.
   */
  public static final byte CMD_R_BOX = 0x49;
  /**
   * Command CIRCLE.
   */
  public static final byte CMD_CIRCLE = 0x50;
  /**
   * Command ELLIPSE.
   */
  public static final byte CMD_ELLIPSE = 0x51;
  /**
   * Command F FILL.
   */
  public static final byte CMD_F_FILL = 0x4f;
  /**
   * Command LINE.
   */
  public static final byte CMD_LINE = 0x44;
  /**
   * Command R LINE.
   */
  public static final byte CMD_R_LINE = 0x45;
  /**
   * Command LINE M.
   */
  public static final byte CMD_LINE_M = 0x46;
  /**
   * Command R LINE M.
   */
  public static final byte CMD_R_LINE_M = 0x47;
  /**
   * Command POINT.
   */
  public static final byte CMD_POINT = 0x42;
  /**
   * Command R POINT.
   */
  public static final byte CMD_R_POINT = 0x43;
  /**
   * Command PUT GC.
   */
  public static final byte CMD_PUT_GC = 0x4e;
  /**
   * Command SET D PTR.
   */
  public static final byte CMD_SET_D_PTR = 0x40;
  /**
   * Command R SET D PTR.
   */
  public static final byte CMD_R_SET_D_PTR = 0x41;
  // Text commands
  /**
   * Command CTL HOME.
   */
  public static final byte CTL_HOME = 0x01;
  /**
   * Command CTL POSITION.
   */
  public static final byte CTL_POSITION = 0x02;
  /**
   * Command CTL ERASE LINE.
   */
  public static final byte CTL_ERASE_LINE = 0x03;
  /**
   * Command CTL ERASE TO EOL.
   */
  public static final byte CTL_ERASE_TO_EOL = 0x04;
  /**
   * Command CTL CURSOR ON OFF.
   */
  public static final byte CTL_CURSOR_ON_OFF = 0x05;
  /**
   * Command CTL CURSOR ON OFF OFF.
   */
  public static final byte CTL_CURSOR_ON_OFF_OFF = 0x20;
  /**
   * Command CTL CURSOR ON OFF ON.
   */
  public static final byte CTL_CURSOR_ON_OFF_ON = 0x21;
  /**
   * Command CTL CURSOR RIGHT.
   */
  public static final byte CTL_CURSOR_RIGHT = 0x06;
  /**
   * Command CTL BELL.
   */
  public static final byte CTL_BELL = 0x07;
  /**
   * Command CTL CURSOR LEFT.
   */
  public static final byte CTL_CURSOR_LEFT = 0x08;
  /**
   * Command CTL CURSOR UP.
   */
  public static final byte CTL_CURSOR_UP = 0x09;
  /**
   * Command CTL CURSOR DOWN.
   */
  public static final byte CTL_CURSOR_DOWN = 0x0a;
  /**
   * Command CTL ERASE TO EOS.
   */
  public static final byte CTL_ERASE_TO_EOS = 0x0b;
  /**
   * Command CTL CLEAR SCREEN.
   */
  public static final byte CTL_CLEAR_SCREEN = 0x0c;
  /**
   * Command CTL CR.
   */
  public static final byte CTL_CR = 0x0d;
  /**
   * Command CTL EXTENDED.
   */
  public static final byte CTL_EXTENDED = 0x1f;
  /**
   * Command CTL EXT REVERSE ON.
   */
  public static final byte CTL_EXT_REVERSE_ON = 0x20;
  /**
   * Command CTL EXT REVERSE OFF.
   */
  public static final byte CTL_EXT_REVERSE_OFF = 0x21;
  /**
   * Command CTL EXT UNDERLINE ON.
   */
  public static final byte CTL_EXT_UNDERLINE_ON = 0x22;
  /**
   * Command CTL EXT UNDERLINE OFF.
   */
  public static final byte CTL_EXT_UNDERLINE_OFF = 0x23;
  /**
   * Command CTL EXT BLINK ON.
   */
  public static final byte CTL_EXT_BLINK_ON = 0x24;
  /**
   * Command CTL EXT BLINK OFF.
   */
  public static final byte CTL_EXT_BLINK_OFF = 0x25;
  /**
   * Command CTL EXT INSERT LINE.
   */
  public static final byte CTL_EXT_INSERT_LINE = 0x30;
  /**
   * Command CTL EXT DELETE LINE.
   */
  public static final byte CTL_EXT_DELETE_LINE = 0x31;

  private OS9Defs() {
    //hidden constructor
  }
}
