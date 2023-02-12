package com.groupunix.drivewireserver.virtualprinter;


public class DWVPrinterFX80CharacterSet {
  /**
   * Character set size.
   */
  public static final int CHARACTER_SET_SIZE = 256;
  /**
   * Characters.
   */
  private final DWVPrinterFX80Character[] characters
      = new DWVPrinterFX80Character[CHARACTER_SET_SIZE];

  /**
   * Set character.
   *
   * @param characterCode character code (ascii)
   * @param bits column bits array
   * @param len bits length
   */
  public void setCharacter(
      final int characterCode, final int[] bits, final int len
  ) {
    characters[characterCode] = new DWVPrinterFX80Character(bits, len);
  }

  /**
   * Get character column.
   *
   * @param characterCode character code (ascii)
   * @param column column number
   * @return column bits
   */
  public int getCharacterCol(final int characterCode, final int column) {
    return characters[characterCode].getCol(column);
  }
}
