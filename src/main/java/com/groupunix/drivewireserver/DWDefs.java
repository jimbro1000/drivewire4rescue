package com.groupunix.drivewireserver;

/**
 * Static helper class.
 * <p>
 * Provides drivewire protocol constants
 * </p>
 */
public class DWDefs {
  /**
   * Drivewire protocol version.
   */
  public static final byte DW_PROTOCOL_VERSION = 4;

  /**
   * NOP - no operation.
   */
  public static final byte OP_NOP = (byte) 0;    // 0x00
  /**
   * Named object mount.
   */
  public static final byte OP_NAMEOBJ_MOUNT = (byte) 1;    // 0x01
  /**
   * Named object create.
   */
  public static final byte OP_NAMEOBJ_CREATE = (byte) 2;    // 0x02
  /**
   * Named object type.
   */
  public static final byte OP_NAMEOBJ_TYPE = (byte) 3;    // 0x03
  // 0x04 - 0x0F reserved for named object future use
  /**
   * Time op.
   */
  public static final byte OP_TIME = (byte) 35;  // 0x23 #
  /**
   * Set time op.
   */
  public static final byte OP_SETTIME = (byte) 36;  // 0x24 $
  /**
   * Timer op.
   */
  public static final byte OP_TIMER = (byte) 37;  // 0x25
  /**
   * Reset timer op.
   */
  public static final byte OP_RESET_TIMER = (byte) 38;  // 0x26

  public static final byte OP_AARON = (byte) 65;  // 0x41 A
  public static final byte OP_WIREBUG_MODE = (byte) 66;  // 0x42 B
  /**
   * Serial read.
   */
  public static final byte OP_SERREAD = (byte) 67;  // 0x43 C
  /**
   * Serial get status.
   */
  public static final byte OP_SERGETSTAT = (byte) 68;  // 0x44 D
  /**
   * Serial initialize.
   */
  public static final byte OP_SERINIT = (byte) 69;  // 0x45 E
  /**
   * Flush print.
   */
  public static final byte OP_PRINTFLUSH = (byte) 70;  // 0x46 F
  /**
   * Get status.
   */
  public static final byte OP_GETSTAT = (byte) 71;    // 0x47 G
  /**
   * Initialize.
   */
  public static final byte OP_INIT = (byte) 73;  // 0x49 I
  /**
   * Print.
   */
  public static final byte OP_PRINT = (byte) 80;  // 0x50 P
  /**
   * Read.
   */
  public static final byte OP_READ = (byte) 82;    // 0x52 R
  /**
   * Set status.
   */
  public static final byte OP_SETSTAT = (byte) 83;    // 0x53 S
  /**
   * Term?.
   */
  public static final byte OP_TERM = (byte) 84;  // 0x54 T
  /**
   * Write.
   */
  public static final byte OP_WRITE = (byte) 87;  // 0x57 W
  /**
   * Drivewire initialize.
   */
  public static final byte OP_DWINIT = (byte) 90;  // 0x5A Z
  /**
   * Serial read M?.
   */
  public static final byte OP_SERREADM = (byte) 99;  // 0x63 c
  /**
   * Serial write M?.
   */
  public static final byte OP_SERWRITEM = (byte) 100;  // 0x64 d
  /**
   * Reread.
   */
  public static final byte OP_REREAD = (byte) 114;  // 0x72 r
  /**
   * Rewrite.
   */
  public static final byte OP_REWRITE = (byte) 119;  // 0x77 w
  /**
   * Fast write base.
   */
  public static final byte OP_FASTWRITE_BASE = (byte) 128;  // 0x80
  /**
   * Fast Write Port N1.
   */
  public static final byte OP_FASTWRITE_N1 = (byte) 129;  // 0x81
  /**
   * Fast Write Port N2.
   */
  public static final byte OP_FASTWRITE_N2 = (byte) 130;  // 0x82
  /**
   * Fast Write Port N3.
   */
  public static final byte OP_FASTWRITE_N3 = (byte) 131;  // 0x83
  /**
   * Fast Write Port N4.
   */
  public static final byte OP_FASTWRITE_N4 = (byte) 132;  // 0x84
  /**
   * Fast Write Port N5.
   */
  public static final byte OP_FASTWRITE_N5 = (byte) 133;  // 0x85
  /**
   * Fast Write Port N6.
   */
  public static final byte OP_FASTWRITE_N6 = (byte) 134;  // 0x86
  /**
   * Fast Write Port N7.
   */
  public static final byte OP_FASTWRITE_N7 = (byte) 135;  // 0x87
  /**
   * Fast Write Port N8.
   */
  public static final byte OP_FASTWRITE_N8 = (byte) 136;  // 0x88
  /**
   * Fast Write Port N9.
   */
  public static final byte OP_FASTWRITE_N9 = (byte) 137;  // 0x89
  /**
   * Fast Write Port N10.
   */
  public static final byte OP_FASTWRITE_N10 = (byte) 138;  // 0x8A
  /**
   * Fast Write Port N11.
   */
  public static final byte OP_FASTWRITE_N11 = (byte) 139;  // 0x8B
  /**
   * Fast Write Port N12.
   */
  public static final byte OP_FASTWRITE_N12 = (byte) 140;  // 0x8C
  /**
   * Fast Write Port N13.
   */
  public static final byte OP_FASTWRITE_N13 = (byte) 141;  // 0x8D
  /**
   * Fast Write Port N14.
   */
  public static final byte OP_FASTWRITE_N14 = (byte) 142;  // 0x8E

  // one value here is wasted due to 15 port per type instead of 16...

  /**
   * Fast write Port Z0.
   */
  public static final byte OP_FASTWRITE_Z0 = (byte) 144;  // 0x90
  /**
   * Fast write Port Z1.
   */
  public static final byte OP_FASTWRITE_Z1 = (byte) 145;  // 0x91
  /**
   * Fast write Port Z2.
   */
  public static final byte OP_FASTWRITE_Z2 = (byte) 146;  // 0x92
  /**
   * Fast write Port Z3.
   */
  public static final byte OP_FASTWRITE_Z3 = (byte) 147;  // 0x93
  /**
   * Fast write Port Z4.
   */
  public static final byte OP_FASTWRITE_Z4 = (byte) 148;  // 0x94
  /**
   * Fast write Port Z5.
   */
  public static final byte OP_FASTWRITE_Z5 = (byte) 149;  // 0x95
  /**
   * Fast write Port Z6.
   */
  public static final byte OP_FASTWRITE_Z6 = (byte) 150;  // 0x96
  /**
   * Fast write Port Z7.
   */
  public static final byte OP_FASTWRITE_Z7 = (byte) 151;  // 0x97
  /**
   * Fast write Port Z8.
   */
  public static final byte OP_FASTWRITE_Z8 = (byte) 152;  // 0x98
  /**
   * Fast write Port Z9.
   */
  public static final byte OP_FASTWRITE_Z9 = (byte) 153;  // 0x99
  /**
   * Fast write Port Z10.
   */
  public static final byte OP_FASTWRITE_Z10 = (byte) 154;  // 0x9A
  /**
   * Fast write Port Z11.
   */
  public static final byte OP_FASTWRITE_Z11 = (byte) 155;  // 0x9B
  /**
   * Fast write Port Z12.
   */
  public static final byte OP_FASTWRITE_Z12 = (byte) 156;  // 0x9C
  /**
   * Fast write Port Z13.
   */
  public static final byte OP_FASTWRITE_Z13 = (byte) 157;  // 0x9D
  /**
   * Fast write Port Z14.
   */
  public static final byte OP_FASTWRITE_Z14 = (byte) 143;  // 0x8F
  /**
   * Serial Write.
   */
  public static final byte OP_SERWRITE = (byte) 195;  // 0xC3 C+128
  /**
   * Serial Set Status.
   */
  public static final byte OP_SERSETSTAT = (byte) 196;  // 0xC4 D+128
  /**
   * Serial term?(terminate).
   */
  public static final byte OP_SERTERM = (byte) 197;  // 0xC5 E+128
  /**
   * Read ex.
   */
  public static final byte OP_READEX = (byte) 210;  // 0xD2 R+128
  /**
   * RFM.
   */
  public static final byte OP_RFM = (byte) 214;  // 0xD6 V+128
  /**
   * 230K230K?.
   */
  public static final byte OP_230K230K = (byte) 230;  // 0xE6
  /**
   * Reread Ex.
   */
  public static final byte OP_REREADEX = (byte) 242;  // 0xF2 r+128
  /**
   * Reset3.
   */
  public static final byte OP_RESET3 = (byte) 248;  // 0xF8
  /**
   * 230K115K?.
   */
  public static final byte OP_230K115K = (byte) 253;  // 0xFD
  /**
   * Reset2.
   */
  public static final byte OP_RESET2 = (byte) 254;  // 0xFE
  /**
   * Reset1.
   */
  public static final byte OP_RESET1 = (byte) 255;  // 0xFF

  // response codes
  /**
   * WP Error.
   */
  public static final byte DWERROR_WP = (byte) 0xF2;
  /**
   * Checksum error.
   */
  public static final byte DWERROR_CRC = (byte) 0xF3;
  /**
   * Read error.
   */
  public static final byte DWERROR_READ = (byte) 0xF4;
  /**
   * Write error.
   */
  public static final byte DWERROR_WRITE = (byte) 0xF5;
  /**
   * Not ready error.
   */
  public static final byte DWERROR_NOTREADY = (byte) 0xF6;
  /**
   * Ok.
   */
  public static final byte DWOK = (byte) 0;

  // util modes
  public static final int UTILMODE_UNSET = 0;
  public static final int UTILMODE_DWCMD = 1;
  public static final int UTILMODE_URL = 2;
  public static final int UTILMODE_TCPOUT = 3;
  public static final int UTILMODE_VMODEMOUT = 4;
  public static final int UTILMODE_TCPIN = 5;
  public static final int UTILMODE_VMODEMIN = 6;
  public static final int UTILMODE_TCPLISTEN = 7;
  public static final int UTILMODE_NINESERVER = 8;

  // result codes for DW virtual channel API calls - all subject to change until I know of anyone actually using these on client side
  public static final byte RC_SUCCESS = (byte) 0;

  public static final byte RC_SYNTAX_ERROR = (byte) 10;

  public static final byte RC_DRIVE_ERROR = (byte) 100;
  public static final byte RC_INVALID_DRIVE = (byte) 101;
  public static final byte RC_DRIVE_NOT_LOADED = (byte) 102;
  public static final byte RC_DRIVE_ALREADY_LOADED = (byte) 103;
  public static final byte RC_IMAGE_FORMAT_EXCEPTION = (byte) 104;

  public static final byte RC_NO_SUCH_DISKSET = (byte) 110;
  public static final byte RC_INVALID_DISK_DEF = (byte) 111;

  public static final byte RC_NET_ERROR = (byte) 120;
  public static final byte RC_NET_IO_ERROR = (byte) 121;
  public static final byte RC_NET_UNKNOWN_HOST = (byte) 122;
  public static final byte RC_NET_INVALID_CONNECTION = (byte) 123;

  public static final byte RC_INVALID_PORT = (byte) 140;
  public static final byte RC_INVALID_HANDLER = (byte) 141;
  public static final byte RC_CONFIG_KEY_NOT_SET = (byte) 142;
  public static final byte RC_INSTANCE_WONT = (byte) 143;

  public static final byte RC_MIDI_ERROR = (byte) 150;
  public static final byte RC_MIDI_UNAVAILABLE = (byte) 151;
  public static final byte RC_MIDI_INVALID_DEVICE = (byte) 152;
  public static final byte RC_MIDI_INVALID_DATA = (byte) 153;
  public static final byte RC_MIDI_SOUNDBANK_FAILED = (byte) 154;
  public static final byte RC_MIDI_SOUNDBANK_NOT_SUPPORTED = (byte) 155;
  public static final byte RC_MIDI_INVALID_PROFILE = (byte) 156;

  public static final byte RC_SERIAL_PORTINUSE = (byte) 180;
  public static final byte RC_SERIAL_PORTINVALID = (byte) 181;
  public static final byte RC_SERIAL_PORTERROR = (byte) 182;


  public static final byte RC_SERVER_ERROR = (byte) 200;
  public static final byte RC_SERVER_FILESYSTEM_EXCEPTION = (byte) 201;
  public static final byte RC_SERVER_IO_EXCEPTION = (byte) 202;
  public static final byte RC_SERVER_FILE_NOT_FOUND = (byte) 203;
  public static final byte RC_SERVER_NOT_IMPLEMENTED = (byte) 204;
  public static final byte RC_SERVER_NOT_READY = (byte) 205;
  public static final byte RC_INSTANCE_NOT_READY = (byte) 206;
  public static final byte RC_INSTANCE_ALREADY_STARTED = (byte) 207;

  public static final byte RC_UI_ERROR = (byte) 220;
  public static final byte RC_UI_MALFORMED_REQUEST = (byte) 221;
  public static final byte RC_UI_MALFORMED_RESPONSE = (byte) 222;

  public static final byte RC_HELP_TOPIC_NOT_FOUND = (byte) 230;

  public static final byte RC_FAIL = (byte) 255;

  // disk stuff

  public static final int DISK_MAXDRIVES = 256;
  public static final int DISK_MAXSECTORS = 16777215;
  public static final int DISK_SECTORSIZE = 256;
  public static final int DISK_MAX_SYNC_SKIPS = 1;
  public static final long DISK_SYNC_INOP_PAUSE = 40;
  public static final int DISK_HDBDOS_DISKSIZE = 630;

  public static final int DISK_FORMAT_NONE = 0;
  public static final int DISK_FORMAT_RAW = 1;
  public static final int DISK_FORMAT_DMK = 2;
  public static final int DISK_FORMAT_JVC = 3;
  public static final int DISK_FORMAT_VDK = 4;
  public static final int DISK_FORMAT_CCB = 5;

  public static final int DISK_CONSIDER_NO = 0;
  public static final int DISK_CONSIDER_MAYBE = 1;
  public static final int DISK_CONSIDER_YES = 2;

  public static final Boolean DISK_DEFAULT_EXPAND = true;
  public static final Boolean DISK_DEFAULT_SYNCTO = true;
  public static final Boolean DISK_DEFAULT_SYNCFROM = false;
  public static final Boolean DISK_DEFAULT_WRITEPROTECT = false;
  public static final Boolean DISK_DEFAULT_NAMEDOBJECT = false;
  public static final int DISK_DEFAULT_OFFSET = 0;
  public static final int DISK_DEFAULT_SIZELIMIT = -1;

  public static final int DISK_IMAGE_HEADER_SIZE = 256;

  public static final int DISK_FILESYSTEM_OS9 = 1;
  public static final int DISK_FILESYSTEM_DECB = 2;
  public static final int DISK_FILESYSTEM_LWFS = 3;
  public static final int DISK_FILESYSTEM_CCB = 4;
  public static final int DISK_FILESYSTEM_UNKNOWN = 0;
  public static final int DISK_FORMAT_SIDE = 5;

  // help

  public static final String HELP_DEFAULT_FILE = "help.xml";

  public static final int DWCMD_DEFAULT_COLS = 80;

  // events
  public static final byte EVENT_TYPE_SERVERCONFIG = 'C';
  public static final byte EVENT_TYPE_INSTANCECONFIG = 'I';
  public static final byte EVENT_TYPE_DISK = 'D';
  public static final byte EVENT_TYPE_LOG = 'L';
  public static final byte EVENT_TYPE_MIDI = 'M';
  public static final byte EVENT_TYPE_NET = 'N';
  public static final byte EVENT_TYPE_PRINT = 'P';
  public static final byte EVENT_TYPE_VSERIAL = 'S';
  public static final byte EVENT_TYPE_STATUS = '@';

  public static final String EVENT_ITEM_KEY = "k";
  public static final String EVENT_ITEM_VALUE = "v";
  public static final String EVENT_ITEM_DRIVE = "d";
  public static final String EVENT_ITEM_INSTANCE = "i";

  public static final String EVENT_ITEM_LOGLEVEL = "l";
  public static final String EVENT_ITEM_TIMESTAMP = "t";
  public static final String EVENT_ITEM_LOGMSG = "m";
  public static final String EVENT_ITEM_THREAD = "r";
  public static final String EVENT_ITEM_LOGSRC = "s";

  public static final String EVENT_ITEM_INTERVAL = "0";
  public static final String EVENT_ITEM_MEMTOTAL = "1";
  public static final String EVENT_ITEM_MEMFREE = "2";
  public static final String EVENT_ITEM_OPS = "3";
  public static final String EVENT_ITEM_DISKOPS = "4";
  public static final String EVENT_ITEM_VSERIALOPS = "5";
  public static final String EVENT_ITEM_INSTANCES = "6";
  public static final String EVENT_ITEM_INSTANCESALIVE = "7";
  public static final String EVENT_ITEM_THREADS = "8";
  public static final String EVENT_ITEM_UICLIENTS = "9";
  public static final String EVENT_ITEM_MAGIC = "!";


  public static final int EVENT_MAX_QUEUE_SIZE = 800;
  public static final int EVENT_QUEUE_LOGDROP_SIZE = 500;
  public static final int LOGGING_MAX_BUFFER_EVENTS = 500;

  public static final long UITHREAD_WAIT_TICK = 200;
  public static final long UITHREAD_SERVER_WAIT_TIME = 3000;
  public static final long UITHREAD_INSTANCE_WAIT_TIME = 3000;

  public static final long SERVER_MEM_UPDATE_INTERVAL = 5000;

  public static final long SERVER_SLOW_OP = 200;

  public static final String[] LOG_LEVELS = {"ALL", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

  public static final int MODEL_COCO1 = 1;
  public static final int MODEL_COCO2 = 2;
  public static final int MODEL_COCO3 = 3;
  public static final int MODEL_FPGA = 4;
  public static final int MODEL_EMULATOR = 5;
  public static final int MODEL_ATARI = 6;
  public static final int MODEL_APPLE2 = 7;

  public static final int POLL_RESP_MODE_SERIAL = 0;
  public static final int POLL_RESP_MODE_WINDOW = 1;

  // server IDs 0-2 reserved
  public static final byte SERVER_ID_DW3_0 = 3;
  public static final byte SERVER_ID_DW4_0 = 4;

  public static final byte SERVER_ID_MARK_0 = (byte) 128;

  // client IDs

  // Timer defs
  public static final byte TIMER_START = (byte) 0;  // time since server started
  public static final byte TIMER_RESET = (byte) 1;  // time since reset
  public static final byte TIMER_OP = (byte) 2;  // time since last operation
  public static final byte TIMER_NP_OP = (byte) 3;  // time since last operation (non-poll)
  public static final byte TIMER_POLL = (byte) 4;  // time since last poll
  public static final byte TIMER_BAD_DATA = (byte) 5;  // time since last error

  public static final byte TIMER_IO = (byte) 6;  // time of last IO operation
  public static final byte TIMER_DWINIT = (byte) 8;
  public static final byte TIMER_READ = (byte) 9;
  public static final byte TIMER_WRITE = (byte) 10;

  public static final byte TIMER_USER = (byte) 128; // start of user timers

  public static final int COM_MIN_DATURBO_RATE = 57600;
  public static final int COM_MAX_DATURBO_RATE = 115200;
}
