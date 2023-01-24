package com.groupunix.drivewireserver.virtualprinter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.dwexceptions.DWPrinterFileError;
import com.groupunix.drivewireserver.dwexceptions.DWPrinterNotDefinedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.virtualserial.DWVSerialCircularBuffer;

public class DWVPrinterFX80 implements DWVPrinterDriver {
  /**
   * DPI per line.
   */
  private static final int DPI_PER_LINE = 10;
  /**
   * Points for a rectangle (polygon).
   */
  private static final int RECTANGLE = 4;
  /**
   * Escape character code.
   */
  private static final int ESCAPE_CODE = 27;
  /**
   * Cancel expanded style code.
   */
  private static final int CANCEL_EXPANDED_CODE = 20;
  /**
   * Cancel compressed style code.
   */
  private static final int CANCEL_COMPRESSED_CODE = 18;
  /**
   * Enable compressed style code.
   */
  private static final int COMPRESSED_CODE = 15;
  /**
   * Enable expanded style code.
   */
  private static final int EXPANDED_CODE = 14;
  /**
   * Newline character code.
   */
  private static final int NEWLINE_CODE = 13;
  /**
   * Tab character code.
   */
  private static final int TAB_CODE = 9;
  /**
   * Characters per tab.
   */
  private static final int TAB_CHARS = 8;
  /**
   * Maximum decimal value.
   */
  private static final int MAX_DECIMAL = 511;
  /**
   * Bits line string length.
   */
  private static final int BITS_LINE_LENGTH = 9;
  /**
   * Maximum proportional length.
   */
  private static final int MAX_PROPORTIONAL_LENGTH = 12;
  /**
   * Minimum proportional length.
   */
  private static final int MIN_PROPORTIONAL_LENGTH = 1;
  /**
   * Maximum character number.
   */
  private static final int MAX_CHARACTER_INT = 255;
  /**
   * Char bits array size.
   */
  private static final int CHAR_BITS_SIZE = 12;
  /**
   * Pica DPI.
   */
  private static final int PICA_DPI_FACTOR = 80;
  /**
   * Elite DPI.
   */
  private static final int ELITE_DPI_FACTOR = 96;
  /**
   * Compressed DPI.
   */
  private static final int COMPRESSED_DPI_FACTOR = 132;
  /**
   * Default lines.
   */
  private static final int DEFAULT_LINES = 66;
  /**
   * Default DPI.
   */
  private static final int DEFAULT_DPI = 300;
  /**
   * X dimension DPI factor.
   */
  private static final double X_DPI_FACTOR = 8.5;
  /**
   * Y dimension DPI factor.
   */
  private static final int Y_DPI_FACTOR = 11;
  /**
   * Points for an octagon (polygon).
   */
  private static final int OCTAGON = 8;
  /**
   * Default style x factor.
   */
  private static final int DEFAULT_CHAR_X = 12;
  /**
   * Emphasised style x factor.
   */
  private static final int EMPHASISED_CHAR_X = 12;
  /**
   * expanded style x factor.
   */
  private static final int EXPANDED_CHAR_X = 6;
  /**
   * Y calculation factor 1.
   * <p>
   * Origin of value is not clear
   * </p>
   */
  private static final int Y_FACTOR_1 = 66;
  /**
   * Y calculation factor 2.
   * <p>
   * Origin of value is not clear
   * </p>
   */
  private static final int Y_FACTOR_2 = 24;
  /**
   * Character columns.
   */
  private static final int CHARACTER_COLS = 12;
  /**
   * reset printer control code.
   */
  private static final int RESET_PRINTER_CODE = 64;
  /**
   * Maximum valid character code.
   */
  private static final int MAX_CHR_INT = 255;
  /**
   * Minimum printable character code.
   */
  private static final int MIN_CHR_INT = 32;
  /**
   * Delete/backspace character code.
   */
  private static final int DEL_CHR = 127;
  /**
   * Minimum graphical character(?).
   */
  private static final int MIN_GRAPH_CHAR = 160;
  /**
   * Printer buffer.
   */
  private final DWVSerialCircularBuffer printBuffer
      = new DWVSerialCircularBuffer(-1, true);
  /**
   * Printer configuration.
   */
  private final HierarchicalConfiguration configuration;

  /**
   * X dimension DPI.
   */
  private final double defXSizeDPI;
  /**
   * Y dimension DPI.
   */
  private final double defYSizeDPI;
  /**
   * Pica DPI.
   */
  private final double szPicaDPI;
  /**
   * Elite DPI.
   */
  private final double szEliteDPI;
  /**
   * Compressed DPI.
   */
  private final double szCompressedDPI;
  /**
   * Line height.
   */
  private final double lineHeight;
  /**
   * Character set.
   */
  private final DWVPrinterFX80CharacterSet charset
      = new DWVPrinterFX80CharacterSet();
  /**
   * Expanded.
   */
  private boolean mExpanded = false;
  /**
   * Pica.
   */
  @SuppressWarnings("unused")
  private boolean mPica = true;
  /**
   * Elite.
   */
  private boolean mElite = false;
  /**
   * Compressed.
   */
  private boolean mCompressed = false;
  /**
   * Double strike.
   */
  private boolean mDoublestrike = false;
  /**
   * Emphasised.
   */
  private boolean mEmphasized = false;
  /**
   * Escape mode.
   */
  private boolean mEscape = false;
  /**
   * Character width.
   */
  private double charWidth;
  /**
   * 2D Graphic handle.
   */
  private Graphics2D rGraphic;
  /**
   * Printer directory.
   */
  @SuppressWarnings("unused")
  private File printDir;
  /**
   * Printer file.
   */
  private File printFile;
  /**
   * Current x position.
   */
  private double xpos;
  /**
   * Current y position.
   */
  private double ypos;
  /**
   * Buffered image handle.
   */
  private BufferedImage rImage;

  /**
   * FX80 Printer.
   *
   * @param config configuration
   */
  public DWVPrinterFX80(final HierarchicalConfiguration config) {
    this.configuration = config;
    this.defXSizeDPI = config.getDouble("DPI", DEFAULT_DPI) * X_DPI_FACTOR;
    this.defYSizeDPI = config.getDouble("DPI", DEFAULT_DPI) * Y_DPI_FACTOR;
    this.szPicaDPI = defXSizeDPI / PICA_DPI_FACTOR;
    this.szEliteDPI = defXSizeDPI / ELITE_DPI_FACTOR;
    this.szCompressedDPI = defXSizeDPI / COMPRESSED_DPI_FACTOR;
    this.lineHeight = defYSizeDPI / config.getInt("Lines", DEFAULT_LINES);
  }

  /**
   * Add byte to buffer.
   *
   * @param data byte data
   * @throws IOException
   */
  @Override
  public void addByte(final byte data) throws IOException {
    this.printBuffer.getOutputStream().write(data);
  }

  /**
   * Get Printer Driver Name.
   *
   * @return driver name
   */
  @Override
  public String getDriverName() {
    return ("FX80");
  }

  private void processEscapeCode(final int code) {
    switch (code) {
      case RESET_PRINTER_CODE:
        resetPrinter();
        break;
      case 'E':
        mEmphasized = true;
        break;
      case 'F':
        mEmphasized = false;
      case 'G':
        mDoublestrike = true;
        break;
      case 'H':
        mDoublestrike = false;
        break;
      case 'M':
        mElite = true;
        break;
      case 'P':
        mElite = false;
        break;
      default:
    }
  }

  private void processUnprintable(final int code) throws DWPrinterFileError {
    switch (code) {
      case TAB_CODE -> {
        xpos += (TAB_CHARS * charWidth);
      }
      case NEWLINE_CODE -> {
        xpos = 0;
        newline();
      }
      case EXPANDED_CODE -> {
        mExpanded = true;
      }
      case COMPRESSED_CODE -> {
        mCompressed = true;
      }
      case CANCEL_COMPRESSED_CODE -> {
        mCompressed = false;
      }
      case CANCEL_EXPANDED_CODE -> {
        mExpanded = false;
      }
      case ESCAPE_CODE -> {
        mEscape = true;
      }
      default -> {
        // non-printable character and not a control character
      }
    }
  }

  /**
   * Flush printer.
   *
   * @throws NumberFormatException
   * @throws IOException
   * @throws DWPrinterNotDefinedException
   * @throws DWPrinterFileError
   */
  @Override
  public void flush()
      throws NumberFormatException,
      IOException,
      DWPrinterNotDefinedException,
      DWPrinterFileError {
    loadCharacter(configuration.getString(
        "CharacterFile", "default.chars"
    ));
    rImage = new BufferedImage(
        (int) defXSizeDPI,
        (int) defYSizeDPI,
        BufferedImage.TYPE_BYTE_INDEXED
    );
    rGraphic = (Graphics2D) rImage.getGraphics();
    rGraphic.setColor(Color.WHITE);
    rGraphic.fillRect(0, 0, (int) defXSizeDPI, (int) defYSizeDPI);
    this.charWidth = getCPI();
    xpos = 0;
    ypos = lineHeight;

    while (this.printBuffer.getAvailable() > 0) {
      char c = (char) this.printBuffer.getInputStream().read();
      // control codes
      if (mEscape) {
        processEscapeCode(c);
        this.charWidth = getCPI();
        this.mEscape = false;
      } else if (
          ((int) c < MIN_CHR_INT)
              || ((int) c == DEL_CHR)
              || (((int) c > DEL_CHR) && ((int) c < MIN_GRAPH_CHAR))
              || ((int) c == MAX_CHR_INT)
      ) {
        processUnprintable(c);
        // apply
        this.charWidth = getCPI();
      } else {
        drawCharacter(c, xpos, ypos);
        if (mExpanded) {
          xpos += (charWidth * 2);
        } else {
          xpos += charWidth;
        }
        if (xpos >= defXSizeDPI) {
          // line wrap
          xpos = 0;
          newline();
        }
      }
    }
    try {
      printFile = this.getPrinterFile();
      ImageIO.write(
          rImage,
          configuration.getString("ImageFormat", "PNG"),
          printFile
      );
    } catch (IOException ignored) {
    }
  }

  /**
   * Reset printer.
   */
  private void resetPrinter() {
    this.mExpanded = false;
    this.mPica = true;
    this.mElite = false;
    this.mCompressed = false;
    this.mDoublestrike = false;
    this.mEmphasized = false;
  }

  /**
   * Get characters per inch for current style.
   *
   * @return cpi
   */
  private double getCPI() {
    double sz;
    if (mElite) {
      sz = szEliteDPI;
    } else if (mCompressed) {
      sz = szCompressedDPI;
    } else {
      sz = szPicaDPI;
    }
    return (sz);
  }

  /**
   * New line.
   *
   * @throws DWPrinterFileError
   */
  private void newline()
      throws DWPrinterFileError {
    ypos += lineHeight;
    if (ypos >= defYSizeDPI) {
      // new page
      ypos = lineHeight;
      try {
        printFile = this.getPrinterFile();
        ImageIO.write(
            rImage,
            configuration.getString("ImageFormat", "PNG"),
            printFile
        );
        rImage = new BufferedImage(
            (int) defXSizeDPI,
            (int) defYSizeDPI,
            BufferedImage.TYPE_USHORT_GRAY
        );
        rGraphic = (Graphics2D) rImage.getGraphics();
        rGraphic.setColor(Color.WHITE);
        rGraphic.fillRect(0, 0, (int) defXSizeDPI, (int) defYSizeDPI);
      } catch (IOException ignored) {
      }
    }
  }

  /**
   * Identify file extension from string.
   *
   * @param set found file extension
   * @return corrected file extension
   */
  private String getFileExtension(final String set) {
    return switch (set.toUpperCase()) {
      case "JPEG", "JPG" -> ".jpg";
      case "GIF" -> ".gif";
      case "BMP", "WBMP" -> ".bmp";
      default -> ".png";
    };
  }

  /**
   * Draw character.
   *
   * @param ch character
   * @param x  x position
   * @param y  y position
   */
  private void drawCharacter(
      final int ch, final double x, final double y
  ) {
    double xPos = x;
    // draw one character... just testing
    for (int i = 0; i < CHARACTER_COLS; i++) {
      int lBits = charset.getCharacterCol(ch, i);
      drawCharCol(lBits, xPos, y);
      if (mDoublestrike) {
        drawCharCol(
            lBits, xPos, y + (defYSizeDPI / Y_FACTOR_1 / Y_FACTOR_2)
        );
      }
      if (mExpanded) {
        xPos += (this.charWidth / EXPANDED_CHAR_X);
        drawCharCol(lBits, xPos, y);
        if (mDoublestrike) {
          drawCharCol(
              lBits, xPos, y + (defYSizeDPI / Y_FACTOR_1 / Y_FACTOR_2)
          );
        }
      } else if (mEmphasized) {
        xPos += (this.charWidth / EMPHASISED_CHAR_X);
        drawCharCol(lBits, xPos, y);
        if (mDoublestrike) {
          drawCharCol(
              lBits, xPos, y + (defYSizeDPI / Y_FACTOR_1 / Y_FACTOR_2)
          );
        }
      } else {
        xPos += (this.charWidth / DEFAULT_CHAR_X);
      }
    }
  }

  /**
   * Draw character column.
   *
   * @param lBits control bits
   * @param xPos  x position
   * @param yPos  y position
   */
  private void drawCharCol(
      final int lBits, final double xPos, final double yPos
  ) {
    double dy = yPos;
    // draw one column
    for (int i = 0; i < BITS_LINE_LENGTH; i++) {
      if ((lBits & (int) Math.pow(2, i)) == Math.pow(2, i)) {
//        int r = (int) (char_width / 5);
//        @SuppressWarnings("unused")
//        int x = ((int) xPos) - (r / 2);
//        @SuppressWarnings("unused")
//        int y = ((int) yPos) - (r / 2);
        int ix = (int) xPos;
        int iy = (int) dy;
        int[] pdx = {ix - 2, ix, ix + 2, ix};
        int[] pdy = {iy, iy - 2, iy, iy + 2};
        int[] sdx = {ix - 2, ix - 1, ix + 1, ix + 2,
            ix + 2, ix + 1, ix - 1, ix - 2};
        int[] sdy = {iy - 1, iy - 2, iy - 2, iy - 1,
            iy + 1, iy + 2, iy + 2, iy + 1};
        rGraphic.setColor(Color.GRAY);
        rGraphic.drawPolygon(sdx, sdy, OCTAGON);
        rGraphic.setColor(Color.BLACK);
        rGraphic.fillPolygon(pdx, pdy, RECTANGLE);
        rGraphic.setColor(Color.DARK_GRAY);
        rGraphic.drawPolygon(pdx, pdy, RECTANGLE);
        rGraphic.setColor(Color.BLACK);
//        r = (int) (char_width / 6);
//        x = ((int) xPos) - (r / 2);
//        y = ((int) yPos) - (r / 2);
      }
      dy -= (lineHeight / DPI_PER_LINE);
    }
  }

  /**
   * Load character.
   *
   * @param fName file name
   * @throws NumberFormatException
   * @throws IOException
   */
  private void loadCharacter(final String fName)
      throws NumberFormatException, IOException {
    int curline = 0;
    int curchar = -1;
    int curpos = -1;
    int[] charbits = new int[CHAR_BITS_SIZE];
    int prop = 0;

    FileInputStream fStream = new FileInputStream(fName);
    DataInputStream in = new DataInputStream(fStream);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String strLine;
    while ((strLine = br.readLine()) != null) {
      curline++;
      if ((!strLine.startsWith("#")) && (strLine.length() != 0)) {
        // process input
        if (strLine.startsWith("c")) {
          if (curchar > -1) {
            // finish current char
            charset.setCharacter(curchar, charbits, prop);
            curpos = -1;
            curchar = -1;
            charbits = new int[CHAR_BITS_SIZE];
          }
          //start new char
          int tmpInt = Integer.parseInt(strLine.substring(1));
          if ((tmpInt < 0) || (tmpInt > MAX_CHARACTER_INT)) {
            System.err.println(
                "Error at line " + curline
                    + ": invalid character number, must be 0-255 "
            );
          } else {
            curpos = 0;
            prop = MAX_PROPORTIONAL_LENGTH;
            curchar = tmpInt;
          }
        } else if (strLine.startsWith("p")) {
          // set prop val
          int tmpInt = Integer.parseInt(strLine.substring(1));
          if ((tmpInt < MIN_PROPORTIONAL_LENGTH)
              || (tmpInt > MAX_PROPORTIONAL_LENGTH)) {
            System.err.println(
                "Error at line " + curline
                    + ": invalid proportional length, must be 1-12 "
            );
          } else {
            prop = tmpInt;
          }
        } else {
          // parse bits line
          int tmpval;
          if (strLine.length() == BITS_LINE_LENGTH) {
            // boolean bits
            tmpval = 0;
            for (int i = 0; i < BITS_LINE_LENGTH; i++) {
              char c = strLine.charAt(i);
              if (c == '1') {
                tmpval += Math.pow(2, i);
              } else if (c != '0') {
                System.err.println(
                    "Error at line " + curline
                        + " (in character " + curchar
                        + "): boolean values must contain only 0 or 1"
                );
              }
            }
          } else {
            // decimal value
            tmpval = Integer.parseInt(strLine);
            if ((tmpval < 0) || (tmpval > MAX_DECIMAL)) {
              tmpval = 0;
              System.err.println(
                  "Error at line " + curline
                      + " (in character " + curchar
                      + "): decimal values must be 0-511"
              );
            }
          }
          charbits[curpos] = tmpval;
          curpos++;
        }
      }
    }
    in.close();
    // finish last char
    charset.setCharacter(curchar, charbits, prop);
  }

  /**
   * Get printer output file.
   *
   * @return output file
   * @throws IOException
   * @throws DWPrinterFileError
   */
  public File getPrinterFile()
      throws IOException, DWPrinterFileError {
    if (configuration.containsKey("OutputFile")) {
      if (DWUtils.FileExistsOrCreate(configuration.getString("OutputFile"))) {
        return (new File(configuration.getString("OutputFile")));
      } else {
        throw new DWPrinterFileError(
            "Cannot find or create the output file '"
                + configuration.getString("OutputFile") + "'"
        );
      }

    } else if (configuration.containsKey("OutputDir")) {
      if (DWUtils.DirExistsOrCreate(configuration.getString("OutputDir"))) {
        return File.createTempFile(
            "dw_fx80_",
            getFileExtension(
                configuration.getString("ImageFormat", "PNG")
            ),
            new File(configuration.getString("OutputDir"))
        );
      } else {
        throw new DWPrinterFileError(
            "Cannot find or create the output directory '"
                + configuration.getString("OutputDir") + "'"
        );
      }
    } else {
      throw new DWPrinterFileError(
          "No OutputFile or OutputDir defined in config"
      );
    }
  }

  /**
   * Get printer name.
   *
   * @return printer name
   */
  @Override
  public String getPrinterName() {
    return this.configuration.getString("[@name]", "?noname?");
  }
}
