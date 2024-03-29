package com.groupunix.drivewireserver.dwprotocolhandler;

import com.groupunix.drivewireserver.OS9Defs;
import gnu.io.CommPortIdentifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFilenameException;

import static com.groupunix.drivewireserver.DWDefs.BYTE_BITS;
import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.DOUBLE_WORD_LEN;
import static com.groupunix.drivewireserver.DWDefs.HIGH_NIBBLE_MASK;
import static com.groupunix.drivewireserver.DWDefs.LOW_NIBBLE_MASK;
import static com.groupunix.drivewireserver.DWDefs.NIBBLE_BITS;
import static com.groupunix.drivewireserver.DWDefs.OP_FASTWRITE_BASE_MAX;
import static com.groupunix.drivewireserver.DWDefs.WORD_BITS;

public class DWUtils {
  /**
   * COCO string terminator (bit flag).
   */
  public static final int COCO_STRING_TERMINATOR = 128;
  /**
   * Maximum possible file length.
   */
  public static final long MAX_FILE_LENGTH = 4_294_967_295L;
  /**
   * Chunk size for file copy.
   */
  public static final int COPY_CHUNK_SIZE = 4096;
  /**
   * Midi note channel base.
   */
  public static final int MIDI_NOTE_CHANNEL = 128;
  /**
   * Midi note channel max.
   */
  public static final int MIDI_NOTE_CHANNEL_MAX = 143;
  /**
   * Mini note on channel base.
   */
  public static final int MIDI_NOTE_ON_CHANNEL = 144;
  /**
   * Midi note on channel max.
   */
  public static final int MIDI_NOTE_ON_CHANNEL_MAX = 159;
  /**
   * Midi key press base.
   */
  public static final int MIDI_KEY_PRESS = 160;
  /**
   * Midi key press max.
   */
  public static final int MIDI_KEY_PRESS_MAX = 175;
  /**
   * Midi ctr change base.
   */
  public static final int MIDI_CTR_CHANGE = 176;
  /**
   * Midi ctr change max.
   */
  public static final int MIDI_CTR_CHANGE_MAX = 191;
  /**
   * Midi prg change base.
   */
  public static final int MIDI_PRG_CHANGE = 192;
  /**
   * Midi prg change max.
   */
  public static final int MIDI_PRG_CHANGE_MAX = 207;
  /**
   * Midi Channel press base.
   */
  public static final int MIDI_CHAN_PRESS = 208;
  /**
   * Midi channel press max.
   */
  public static final int MIDI_CHAN_PRESS_MAX = 223;
  /**
   * Midi pitch bend channel base.
   */
  public static final int MIDI_PITCH_BEND_CHANNEL = 224;
  /**
   * Midi pitch bend channel max.
   */
  public static final int MIDI_PITCH_BEND_CHANNEL_MAX = 239;
  /**
   * Midi timing tick.
   */
  public static final int MIDI_TIMING_TICK = 248;
  /**
   * Offset to 2 digit century (y2k bug).
   */
  public static final int CENTURY_OFFSET = 1900;
  /**
   * Hours byte offset.
   */
  public static final int HOURS = 3;
  /**
   * Minutes byte offset.
   */
  public static final int MINUTES = 4;
  /**
   * Year byte offset.
   */
  public static final int YEAR = 0;
  /**
   * Month byte offset.
   */
  public static final int MONTH = 1;
  /**
   * Day byte offset.
   */
  public static final int DAY = 2;
  /**
   * FXD header length.
   */
  public static final int FXD_HEADER_LEN = 12;
  /**
   * Filename max length.
   */
  public static final int FILENAME_LEN_MAX = 255;
  /**
   * File URI prefix.
   */
  public static final String FILE_URI_PREFIX = "file:///";
  /**
   * Notes per octave.
   */
  public static final int NOTES_PER_OCTAVE = 12;
  /**
   * Hexadecimal radix.
   */
  public static final int HEX_RADIX = 16;
  /**
   * Position of extraneous : when a drive letter is used (for windoze).
   */
  public static final int DRIVE_LETTER_MARKER = 9;

  /**
   * Convert double word to int.
   * <p>
   * Only converts first four bytes
   * </p>
   *
   * @param data byte array
   * @return double word int
   */
  public static int int4(final byte[] data) {
    int result = 0;
    for (int i = 0; i < DOUBLE_WORD_LEN; ++i) {
      result += (data[i] & BYTE_MASK) << BYTE_BITS * (DOUBLE_WORD_LEN - i - 1);
    }
    return result;
  }

  /**
   * Convert word and a half (24 bits) to int.
   * <p>
   * Only converts first three bytes of array
   * </p>
   *
   * @param data byte array
   * @return 24bit int
   */
  public static int int3(final byte[] data) {
    return ((data[0] & BYTE_MASK) << WORD_BITS)
        + ((data[1] & BYTE_MASK) << BYTE_BITS)
        + (data[2] & BYTE_MASK);
  }

  /**
   * Convert word to int.
   * <p>
   * Only converts first two bytes of array
   * </p>
   *
   * @param data byte array
   * @return word value
   */
  public static int int2(final byte[] data) {
    return ((data[0] & BYTE_MASK) << BYTE_BITS) + (data[1] & BYTE_MASK);
  }

  /**
   * Twos complement of byte value.
   *
   * @param value byte
   * @return reversed byte
   */
  public static byte reverseByte(final int value) {
    return (byte) Integer.reverseBytes(Integer.reverse(value));
  }

  /**
   * Reverse the order of a byte array.
   *
   * @param data byte array
   * @return reversed byte array
   */
  public static byte[] reverseByteArray(final byte[] data) {
    byte[] reverseData = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
      reverseData[i] = reverseByte(data[i]);
    }
    return reverseData;
  }

  /**
   * Convert hex string to a byte array.
   * <p>
   * String must have an even number of digits
   * </p>
   *
   * @param hexString hexadecimal string
   * @return byte array
   */
  @SuppressWarnings("unused")
  public static byte[] hexStringToByteArray(final String hexString) {
    if (hexString.length() == 0 || (hexString.length() % 2) != 0) {
      return null;
    }
    byte[] res = new byte[hexString.length() / 2];
    for (int i = 0; i < (hexString.length() / 2); i++) {
      res[i] = (byte) Integer.parseInt(
          hexString.substring(i * 2, i * 2 + 1), HEX_RADIX
      );
    }
    return res;
  }

  /**
   * Format byte array as hex string.
   *
   * @param byteArray byte array
   * @return formatted string
   */
  public static String byteArrayToHexString(final byte[] byteArray) {
    return byteArrayToHexString(byteArray, byteArray.length);
  }

  /**
   * Format byte array as hex string.
   *
   * @param byteArray byte array
   * @param len       array length
   * @return formatted string
   */
  public static String byteArrayToHexString(
      final byte[] byteArray, final int len
  ) {
    if (byteArray == null || byteArray.length == 0) {
      return null;
    }

    final String[] pseudo = {"0", "1",
        "2", "3", "4", "5", "6", "7",
        "8", "9", "A", "B", "C", "D",
        "E", "F"};

    final StringBuilder out = new StringBuilder();
    byte charByte;
    int index = 0;
    while (index < len) {
      charByte = (byte) (byteArray[index] & HIGH_NIBBLE_MASK);
      charByte = (byte) (charByte >>> NIBBLE_BITS);
      // shift the bits down
      charByte = (byte) (charByte & LOW_NIBBLE_MASK);
      // must do this is high order bit is on!
      out.append(pseudo[charByte]); // convert the
      charByte = (byte) (byteArray[index] & LOW_NIBBLE_MASK); // Strip off
      out.append(pseudo[charByte]); // convert the
      index++;
    }
    return out.toString();
  }

  /**
   * Prettify timer.
   *
   * @param timer timer number
   * @return formatted string
   */
  public static String prettyTimer(final byte timer) {
    if ((timer & BYTE_MASK) >= (DWDefs.TIMER_USER & BYTE_MASK)) {
      return "user " + ((timer & BYTE_MASK) - (DWDefs.TIMER_USER & BYTE_MASK));
    }
    return switch (timer) {
      case DWDefs.TIMER_BAD_DATA -> "invalid protocol data";
      case DWDefs.TIMER_DWINIT -> "DWINIT operation";
      case DWDefs.TIMER_IO -> "I/O operation";
      case DWDefs.TIMER_NP_OP -> "protocol operation (non poll)";
      case DWDefs.TIMER_OP -> "protocol operation";
      case DWDefs.TIMER_READ -> "read operation";
      case DWDefs.TIMER_RESET -> "instance reset";
      case DWDefs.TIMER_START -> "server start";
      case DWDefs.TIMER_WRITE -> "write operation";
      case DWDefs.TIMER_POLL -> "poll operation";
      default -> "unknown " + (timer & BYTE_MASK);
    };
  }

  /**
   * Prettify set status code.
   *
   * @param statCode set stat code
   * @return formatted string
   */
  public static String prettySS(final byte statCode) {
    return switch (statCode) {
      case OS9Defs.SS_OPT -> "SS.Opt";
      case OS9Defs.SS_SIZE -> "SS.Size";
      case OS9Defs.SS_RESET -> "SS.Reset";
      case OS9Defs.SS_W_TRK -> "SS.WTrk";
      case OS9Defs.SS_POS -> "SS.Pos";
      case OS9Defs.SS_EOF -> "SS.EOF";
      case OS9Defs.SS_FRZ -> "SS.Frz";
      case OS9Defs.SS_SPT -> "SS.SPT";
      case OS9Defs.SS_SQD -> "SS.SQD";
      case OS9Defs.SS_D_CMD -> "SS.DCmd";
      case OS9Defs.SS_DEV_NM -> "SS.DevNm";
      case OS9Defs.SS_FD -> "SS.FD";
      case OS9Defs.SS_TICKS -> "SS.Ticks";
      case OS9Defs.SS_LOCK -> "SS.Lock";
      case OS9Defs.SS_VAR_SECT -> "SS.VarSect";
      case OS9Defs.SS_BLK_RD -> "SS.BlkRd";
      case OS9Defs.SS_BLK_WR -> "SS.BlkWr";
      case OS9Defs.SS_RETEN -> "SS.Reten";
      case OS9Defs.SS_WFM -> "SS.WFM";
      case OS9Defs.SS_RFM -> "SS.RFM";
      case OS9Defs.SS_S_SIG -> "SS.SSig";
      case OS9Defs.SS_RELEA -> "SS.Relea";
      case OS9Defs.SS_ATTR -> "SS.Attr";
      case OS9Defs.SS_RS_BIT -> "SS.RsBit";
      case OS9Defs.SS_FD_INF -> "SS.FDInf";
      case OS9Defs.SS_D_SIZE -> "SS.DSize";
      case OS9Defs.SS_KY_SNS -> "SS.KySns";
      // added for SCF/Ns
      case OS9Defs.SS_COMST -> "SS.ComSt";
      case OS9Defs.SS_S_OPEN -> "SS.Open";
      case OS9Defs.SS_S_CLOSE -> "SS.Close";
      case OS9Defs.SS_HNGUP -> "SS.HngUp";
      case OS9Defs.SS_NONE -> "None";
      default -> "Unknown: " + statCode;
    };
  }

  /**
   * Prettify utility mode.
   *
   * @param mode mode
   * @return formatted string
   */
  public static String prettyUtilMode(final int mode) {
    return switch (mode) {
      case DWDefs.UTILMODE_URL -> "url";
      case DWDefs.UTILMODE_DWCMD -> "dw cmd";
      case DWDefs.UTILMODE_TCPOUT -> "tcp out";
      case DWDefs.UTILMODE_VMODEMOUT -> "vmodem out";
      case DWDefs.UTILMODE_TCPIN -> "tcp in";
      case DWDefs.UTILMODE_VMODEMIN -> "vmodem in";
      case DWDefs.UTILMODE_TCPLISTEN -> "tcp listen";
      case DWDefs.UTILMODE_NINESERVER -> "nineserver";
      default -> "unset";
    };
  }

  /**
   * Prettify op code.
   *
   * @param opcode opcode
   * @return formatted string
   */
  public static String prettyOP(final byte opcode) {
    if (opcode >= DWDefs.OP_FASTWRITE_BASE
        && opcode <= OP_FASTWRITE_BASE_MAX) {
      return "OP_FASTWRITE_" + (opcode - DWDefs.OP_FASTWRITE_BASE);
    }
    return switch (opcode) {
      case DWDefs.OP_NOP -> "OP_NOP";
      case DWDefs.OP_INIT -> "OP_INIT";
      case DWDefs.OP_READ -> "OP_READ";
      case DWDefs.OP_READEX -> "OP_READEX";
      case DWDefs.OP_WRITE -> "OP_WRITE";
      case DWDefs.OP_REREAD -> "OP_REREAD";
      case DWDefs.OP_REREADEX -> "OP_REREADEX";
      case DWDefs.OP_REWRITE -> "OP_REWRITE";
      case DWDefs.OP_TERM -> "OP_TERM";
      case DWDefs.OP_RESET1,
          DWDefs.OP_RESET2,
          DWDefs.OP_RESET3 -> "OP_RESET";
      case DWDefs.OP_GETSTAT -> "OP_GETSTAT";
      case DWDefs.OP_SETSTAT -> "OP_SETSTAT";
      case DWDefs.OP_TIME -> "OP_TIME";
      case DWDefs.OP_PRINT -> "OP_PRINT";
      case DWDefs.OP_PRINTFLUSH -> "OP_PRINTFLUSH";
      case DWDefs.OP_SERREADM -> "OP_SERREADM";
      case DWDefs.OP_SERREAD -> "OP_SERREAD";
      case DWDefs.OP_SERWRITE -> "OP_SERWRITE";
      case DWDefs.OP_SERSETSTAT -> "OP_SERSETSTAT";
      case DWDefs.OP_SERGETSTAT -> "OP_SERGETSTAT";
      case DWDefs.OP_SERINIT -> "OP_SERINIT";
      case DWDefs.OP_SERTERM -> "OP_SERTERM";
      case DWDefs.OP_DWINIT -> "OP_DWINIT";
      default -> "Unknown: " + opcode;
    };
  }

  /**
   * Get port names.
   *
   * @return list of port names
   */
  @SuppressWarnings({"unchecked", "unused"})
  public static ArrayList<String> getPortNames() {
    final ArrayList<String> ports = new ArrayList<>();
    final Enumeration<CommPortIdentifier> portEnum
        = CommPortIdentifier.getPortIdentifiers();
    while (portEnum.hasMoreElements()) {
      final CommPortIdentifier portIdentifier = portEnum.nextElement();
      if (portIdentifier.getPortType() == 1) {
        ports.add(portIdentifier.getName());
      }
    }
    return ports;
  }

  /**
   * Midi message to text.
   *
   * @param statusByte message status byte
   * @param data1      message data 1
   * @param data2      message data 2
   * @return formatted string
   */
  public static String midiMsgToText(
      final int statusByte, final int data1, final int data2
  ) {
    // make midi messages into something humans can read...
    String action;
    String chan = "";
    String data1String = "";
    String data2String = "";

    if (statusByte >= MIDI_NOTE_CHANNEL
        && statusByte <= MIDI_NOTE_CHANNEL_MAX) {
      action = "Note off";
      chan = "Chan: " + (statusByte - MIDI_NOTE_CHANNEL);
      data1String = "Pitch: " + prettyMidiPitch(data1);
      data2String = "Vel: " + data2;
    } else if (statusByte >= MIDI_NOTE_ON_CHANNEL
        && statusByte <= MIDI_NOTE_ON_CHANNEL_MAX) {
      action = "Note on";
      chan = "Chan: " + (statusByte - MIDI_NOTE_ON_CHANNEL);
      data1String = "Pitch: " + prettyMidiPitch(data1);
      data2String = "Vel: " + data2;
    } else if (statusByte >= MIDI_KEY_PRESS
        && statusByte <= MIDI_KEY_PRESS_MAX) {
      action = "Key press";
      chan = "Chan: " + (statusByte - MIDI_KEY_PRESS);
      data1String = "Key: " + data1;
      data2String = "Pressure: " + data2;
    } else if (statusByte >= MIDI_CTR_CHANGE
        && statusByte <= MIDI_CTR_CHANGE_MAX) {
      action = "Ctr change";
      chan = "Chan: " + (statusByte - MIDI_CTR_CHANGE);
      data1String = "Controller: " + data1;
      data2String = "Value: " + data2;
    } else if (statusByte >= MIDI_PRG_CHANGE
        && statusByte <= MIDI_PRG_CHANGE_MAX) {
      action = "Prg change";
      chan = "Chan: " + (statusByte - MIDI_PRG_CHANGE);
      data1String = "Preset: " + data1;
    } else if (statusByte >= MIDI_CHAN_PRESS
        && statusByte <= MIDI_CHAN_PRESS_MAX) {
      action = "Chan press";
      chan = "Chan: " + (statusByte - MIDI_CHAN_PRESS);
      data1String = "Pressure: " + data1;
    } else if (statusByte >= MIDI_PITCH_BEND_CHANNEL
        && statusByte <= MIDI_PITCH_BEND_CHANNEL_MAX) {
      action = "Pitch bend";
      chan = "Chan: " + (statusByte - MIDI_PITCH_BEND_CHANNEL);
      data1String = "LSB: " + data1;
      data2String = "MSB: " + data2;
    } else if (statusByte == MIDI_TIMING_TICK) {
      action = "Timing tick";
    } else {
      action = "Unknown: " + statusByte;
      data1String = "Data1: " + data1;
      data2String = "Data2: " + data2;
    }
    return String.format("%-10s %-10s %-20s %-20s",
        action, chan, data1String, data2String);
  }

  /**
   * Prettify pitch value as note.
   *
   * @param pitch pitch
   * @return formatted note string
   */
  private static String prettyMidiPitch(final int pitch) {
    final String[] notes = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    return String.format(
        "%-3d %-2s %d",
        pitch,
        notes[pitch % NOTES_PER_OCTAVE],
        pitch / NOTES_PER_OCTAVE - 1
    );
  }

  /**
   * Strip first token from string.
   * <p>
   * Splits string on space and
   * removes the first element
   * </p>
   *
   * @param txt source string
   * @return modified string
   */
  public static String dropFirstToken(final String txt) {
    // drop first token in string
    final StringBuilder rest = new StringBuilder();
    final String[] tokens = txt.split(" ");
    for (int x = 1; x < tokens.length; x++) {
      if (rest.length() > 0) {
        rest.append(" ");
      }
      rest.append(tokens[x]);
    }
    return rest.toString();
  }

  /**
   * Convert asterisk to exclamation mark.
   * <p>
   * Replaces all asterisks with bangs
   * </p>
   *
   * @param txt string
   * @return banged up string
   */
  public static String convertStarToBang(final String txt) {
    return txt.replaceAll("\\*", "!");
  }

  /**
   * Test if string evaluates to falsy.
   * <p>
   * True if string is false or off.
   * Ignores case
   * </p>
   *
   * @param test string
   * @return boolean
   */
  @SuppressWarnings("unused")
  public static boolean isStringFalse(final String test) {
    if (test.equalsIgnoreCase("false")) {
      return true;
    }
    return test.equalsIgnoreCase("off");
  }

  /**
   * Test if string evaluates to truthy.
   * <p>
   * True if string is true or on.
   * Ignores case
   * </p>
   *
   * @param test string
   * @return boolean
   */
  @SuppressWarnings("unused")
  public static boolean isStringTrue(final String test) {
    if (test.equalsIgnoreCase("true")) {
      return true;
    }
    return test.equalsIgnoreCase("on");
  }

  /**
   * Test if class path exists.
   *
   * @param fullClassName fully qualified class name
   * @return true if class exists
   */
  public static boolean testClassPath(final String fullClassName) {
    boolean result = false;
    try {
      Class.forName(fullClassName);
      result = true;
    } catch (Throwable ignored) {
    }
    return result;
  }

  /**
   * Create directory if it doesn't exist.
   *
   * @param directoryName directory name
   * @return true
   */
  public static boolean dirExistsOrCreate(final String directoryName) {
    final File theDir = new File(directoryName);
    // if the directory does not exist, create it
    if (theDir.exists()) {
      return true;
    }
    return theDir.mkdir();
  }

  /**
   * Create file if it doesn't exist.
   *
   * @param fileName file name
   * @return true
   * @throws IOException write failure
   */
  public static boolean fileExistsOrCreate(final String fileName)
      throws IOException {
    final File theFile = new File(fileName);
    // if the file does not exist, create it
    if (theFile.exists()) {
      return true;
    }
    return theFile.createNewFile();
  }

  /**
   * Copy file contents.
   *
   * @param fromFileName source file name
   * @param toFileName   target file name
   * @throws IOException read/write failure
   */
  public static void copyFile(
      final String fromFileName,
      final String toFileName
  ) throws IOException {
    final File fromFile = new File(fromFileName);

    if (!fromFile.exists()) {
      throw new IOException("no source file: " + fromFileName);
    }
    if (!fromFile.isFile()) {
      throw new IOException("can't copy directory: " + fromFileName);
    }
    if (!fromFile.canRead()) {
      throw new IOException("source file is unreadable: " + fromFileName);
    }
    File toFile = new File(toFileName);
    if (toFile.isDirectory()) {
      toFile = new File(toFile, fromFile.getName());
    }
    if (toFile.exists()) {
      if (!toFile.canWrite()) {
        throw new IOException("destination file is unwriteable: " + toFileName);
      }
      String parent = toFile.getParent();
      if (parent == null) {
        parent = System.getProperty("user.dir");
      }
      final File dir = new File(parent);
      if (!dir.exists()) {
        throw new IOException("destination directory doesn't exist: "
            + parent);
      }
      if (dir.isFile()) {
        throw new IOException("destination is not a directory: "
            + parent);
      }
      if (!dir.canWrite()) {
        throw new IOException("destination directory is unwriteable: "
            + parent);
      }
    }
    try (
        FileInputStream input = new FileInputStream(fromFile);
        FileOutputStream output = new FileOutputStream(toFile)
    ) {
      final byte[] buffer = new byte[COPY_CHUNK_SIZE];
      int bytesRead;
      while ((bytesRead = input.read(buffer)) != -1) {
        output.write(buffer, 0, bytesRead);
      }
    }
  }

  /**
   * Shorten local uri.
   *
   * @param uriPath
   * @return short uri
   */
  public static String shortenLocalURI(final String uriPath) {
    if (uriPath.startsWith(FILE_URI_PREFIX)) {
      if (uriPath.charAt(DRIVE_LETTER_MARKER) == ':') {
        return uriPath.substring(FILE_URI_PREFIX.length());
      } else {
        return uriPath.substring(FILE_URI_PREFIX.length() - 1);
      }
    }
    return uriPath;
  }

  /**
   * Get file descriptor.
   *
   * @param file file
   * @return descriptor string
   */
  public static String getFileDescriptor(final File file) {
    String res = "";
    try {
      res = File.separator;
      res += "|" + file.getCanonicalPath();
      res += "|" + file.getParent();
      if (file.getParent() != null) {
        // these checks take a long time on removable media
        // with no disk in the drive
        res += "|" + file.length();
        res += "|" + file.lastModified();
        res += "|" + file.isDirectory();
      } else {
        res += "|0|0|true";
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return res;
  }

  /**
   * Get file XDir descriptor.
   *
   * @param file file
   * @return descriptor string
   * @throws DWFileSystemInvalidFilenameException invalid filename
   */
  @SuppressWarnings("deprecation")
  public static String getFileXDescriptor(final File file)
      throws DWFileSystemInvalidFilenameException {
    if (file.length() > MAX_FILE_LENGTH) {
      throw new DWFileSystemInvalidFilenameException(
          "File too large for XDir"
      );
    }
    if (file.getName().length() > FILENAME_LEN_MAX) {
      throw new DWFileSystemInvalidFilenameException(
          "Filename too long for XDir"
      );
    }
    byte[] res = new byte[FXD_HEADER_LEN + file.getName().length()];
    int pos = 0;
    final long len = file.length();

    int bytePos = DOUBLE_WORD_LEN;
    while (bytePos > 0) {
      res[pos++] = (byte) (len >>> BYTE_BITS * --bytePos);
    }

    // 5 byte OS9 style modified date - Y M D Hr Min
    final Date moddate = new Date(file.lastModified());
    res[pos++] = (byte) (moddate.getYear());
    res[pos++] = (byte) (moddate.getMonth());
    res[pos++] = (byte) (moddate.getDate());
    res[pos++] = (byte) (moddate.getHours());
    res[pos++] = (byte) (moddate.getMinutes());

    // is directory
    if (file.isDirectory()) {
      res[pos++] = (byte) 1;
    } else {
      res[pos++] = (byte) 0;
    }
    // is readonly
    if (file.canWrite()) {
      res[pos++] = (byte) 0;
    } else {
      res[pos++] = (byte) 1;
    }
    // name length
    res[pos++] = (byte) file.getName().length();
    final byte[] nameBytes = file.getName().getBytes(DWDefs.ENCODING);
    for (int i = 0; i < file.getName().length(); i++) {
      res[pos++] = nameBytes[i];
    }
    return new String(res, DWDefs.ENCODING);
  }

  /**
   * Pretty disk format.
   *
   * @param diskFormat format id
   * @return format string
   */
  @SuppressWarnings("unused")
  public static String prettyFormat(final int diskFormat) {
    return switch (diskFormat) {
      case DWDefs.DISK_FORMAT_DMK -> "DMK";
      case DWDefs.DISK_FORMAT_JVC -> "JVC";
      case DWDefs.DISK_FORMAT_RAW -> "DSK (raw)";
      case DWDefs.DISK_FORMAT_VDK -> "VDK";
      case DWDefs.DISK_FORMAT_NONE -> "none";
      default -> "unknown";
    };
  }

  /**
   * Pretty format file system.
   *
   * @param format format id
   * @return format string
   */
  public static String prettyFileSystem(final int format) {
    return switch (format) {
      case DWDefs.DISK_FILESYSTEM_OS9 -> "OS9";
      case DWDefs.DISK_FILESYSTEM_DECB -> "DECB";
      case DWDefs.DISK_FILESYSTEM_LWFS -> "LWFS";
      case DWDefs.DISK_FILESYSTEM_CCB -> "CCB";
      default -> "unknown";
    };
  }

  /**
   * Get root thread group.
   * <p>
   * Follows thread parent to the top
   * </p>
   *
   * @return thread group
   */
  public static ThreadGroup getRootThreadGroup() {
    ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
    ThreadGroup ptg;
    while ((ptg = threadGroup.getParent()) != null) {
      threadGroup = ptg;
    }
    return threadGroup;
  }

  /**
   * Format coco style string to OS9 style.
   *
   * @param buf byte array
   * @return os9 formatted string
   */
  public static String os9String(final byte[] buf) {
    final StringBuilder res = new StringBuilder();
    int pos = 0;
    while (
        pos < buf.length
            && (buf[pos] & BYTE_MASK) < COCO_STRING_TERMINATOR
    ) {
      res.append((char) (buf[pos] & BYTE_MASK));
      pos++;
    }
    if (pos < buf.length) {
      res.append((char) ((buf[pos] & BYTE_MASK) - COCO_STRING_TERMINATOR));
    }
    return res.toString();
  }

  /**
   * Format 5 byte date and time.
   *
   * @param data date time
   * @return date time string
   */
  public static String pretty5ByteDateTime(final byte[] data) {
    return String.format("%02d:%02d ",
        data[HOURS] & BYTE_MASK,
        data[MINUTES] & BYTE_MASK
    ) + pretty3ByteDate(data);
  }

  /**
   * Format 3 byte date.
   *
   * @param data date
   * @return date string
   */
  public static String pretty3ByteDate(final byte[] data) {
    return String.format(
        "%02d/%02d/%04d",
        data[MONTH] & BYTE_MASK,
        data[DAY] & BYTE_MASK,
        CENTURY_OFFSET + (data[YEAR] & BYTE_MASK)
    );
  }

  /**
   * Convert native 6809 style string to coco format.
   *
   * @param bytes byte array
   * @return coco formatted string
   */
  @SuppressWarnings("unused")
  public String cocoString(final byte[] bytes) {
    final StringBuilder res = new StringBuilder();
    int index = 0;
    // thanks to Christopher Hawks
    while (index < bytes.length - 1 && bytes[index] > 0) {
      res.append((char) bytes[index]);
      index++;
    }
    res.append((char) (bytes[index] + COCO_STRING_TERMINATOR));
    return res.toString();
  }
}
