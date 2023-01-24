package com.groupunix.drivewireserver.virtualprinter;

public class DWVPrinterFX80Character {
  /**
   * Column bits array.
   */
  private final int[] columnBits;
  /**
   * Number of bits per column.
   */
  private final int length;

  /**
   * FX80 printer character.
   *
   * @param bits column bits array
   * @param len bits length
   */
  public DWVPrinterFX80Character(final int[] bits, final int len) {
    this.columnBits = bits;
    this.length = len;
  }

  /**
   * Get bits for given column.
   *
   * @param col column number
   * @return bits for column
   */
  public int getCol(final int col) {
    // get bits for this col
    return (columnBits[col]);
  }

  /**
   * Get "bits" length.
   *
   * @return character bits length
   */
  public int getLen() {
    return length;
  }
}
