package com.groupunix.drivewireserver.dwdisk.filesystem;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.LinkedMap;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class RBFFileSystemIDSector {
  /**
   * BIT mask of most significant bit.
   */
  private static final int BIT_8_MASK = 128;
  /**
   * Bits in a byte.
   */
  private static final int BYTE_WIDTH = 8;
  /**
   * DD.TOT offset.
   */
  private static final int DD_TOT_OFFSET = 0;
  /**
   * DD.TKS offset.
   */
  private static final int DD_TKS_OFFSET = 3;
  /**
   * DD.MAP offset.
   */
  private static final int DD_MAP_OFFSET = 4;
  /**
   * DD.MAP offset.
   */
  private static final int DD_BIT_OFFSET = 6;
  /**
   * DD.DIR offset.
   */
  private static final int DD_DIR_OFFSET = 8;
  /**
   * DD.OWN offset.
   */
  private static final int DD_OWN_OFFSET = 11;
  /**
   * DD.ATT offset.
   */
  private static final int DD_ATT_OFFSET = 13;
  /**
   * DD.DSK offset.
   */
  private static final int DD_DSK_OFFSET = 14;
  /**
   * DD.FMT offset.
   */
  private static final int DD_FMT_OFFSET = 16;
  /**
   * DD.SPT offset.
   */
  private static final int DD_SPT_OFFSET = 17;
  /**
   * DD.RES offset.
   */
  private static final int DD_RES_OFFSET = 19;
  /**
   * DD.BT offset.
   */
  private static final int DD_BT_OFFSET = 21;
  /**
   * DD.BSZ offset.
   */
  private static final int DD_BSZ_OFFSET = 24;
  /**
   * DD.DAT offset.
   */
  private static final int DD_DAT_OFFSET = 26;
  /**
   * DD.NAM offset.
   */
  private static final int DD_NAM_OFFSET = 31;
  /**
   * DD.TOT length.
   */
  private static final int DD_TOT_LEN = 3;
  /**
   * DD.TKS length.
   */
  private static final int DD_TKS_LEN = 1;
  /**
   * DD.MAP length.
   */
  private static final int DD_MAP_LEN = 2;
  /**
   * DD.BIT length.
   */
  private static final int DD_BIT_LEN = 2;
  /**
   * DD.DIR length.
   */
  private static final int DD_DIR_LEN = 3;
  /**
   * DD.OWN length.
   */
  private static final int DD_OWN_LEN = 2;
  /**
   * DD.ATT length.
   */
  private static final int DD_ATT_LEN = 1;
  /**
   * DD.DSK length.
   */
  private static final int DD_DSK_LEN = 2;
  /**
   * DD.FMT length.
   */
  private static final int DD_FMT_LEN = 1;
  /**
   * DD.SPT length.
   */
  private static final int DD_SPT_LEN = 2;
  /**
   * DD.RES length.
   */
  private static final int DD_RES_LEN = 2;
  /**
   * DD.BT length.
   */
  private static final int DD_BT_LEN = 3;
  /**
   * DD.BSZ length.
   */
  private static final int DD_BSZ_LEN = 2;
  /**
   * DD.DAT length.
   */
  private static final int DD_DAT_LEN = 5;

  /**
   * Sector data array.
   */
  private final byte[] sectorData;
  /**
   * Sector attributes.
   */
  private final OrderedMap attribs = new LinkedMap();

  /**
   * RBF File system id sector.
   *
   * @param data sector byte array
   */
  public RBFFileSystemIDSector(final byte[] data) {
    this.sectorData = data;
    addIntAttrib("DD.TOT", DD_TOT_OFFSET, DD_TOT_LEN);
    addIntAttrib("DD.TKS", DD_TKS_OFFSET, DD_TKS_LEN);
    addIntAttrib("DD.MAP", DD_MAP_OFFSET, DD_MAP_LEN);
    addIntAttrib("DD.BIT", DD_BIT_OFFSET, DD_BIT_LEN);
    addIntAttrib("DD.DIR", DD_DIR_OFFSET, DD_DIR_LEN);
    addIntAttrib("DD.OWN", DD_OWN_OFFSET, DD_OWN_LEN);
    addIntAttrib("DD.ATT", DD_ATT_OFFSET, DD_ATT_LEN);
    addIntAttrib("DD.DSK", DD_DSK_OFFSET, DD_DSK_LEN);
    addIntAttrib("DD.FMT", DD_FMT_OFFSET, DD_FMT_LEN);
    addIntAttrib("DD.SPT", DD_SPT_OFFSET, DD_SPT_LEN);
    addIntAttrib("DD.RES", DD_RES_OFFSET, DD_RES_LEN);
    addIntAttrib("DD.BT", DD_BT_OFFSET, DD_BT_LEN);
    addIntAttrib("DD.BSZ", DD_BSZ_OFFSET, DD_BSZ_LEN);
    addIntAttrib("DD.DAT", DD_DAT_OFFSET, DD_DAT_LEN);
    addStrAttrib("DD.NAM", DD_NAM_OFFSET);
  }

  /**
   * Add string attribute.
   *
   * @param key    key name
   * @param offset byte offset
   */
  @SuppressWarnings("unchecked")
  private void addStrAttrib(final String key, final int offset) {
    int runningOffset = offset;
    StringBuilder val = new StringBuilder();
    while ((runningOffset < sectorData.length - 1)
        && ((sectorData[runningOffset] & BYTE_MASK) < BIT_8_MASK)) {
      val.append((char) (sectorData[runningOffset] & BYTE_MASK));
      runningOffset++;
    }
    val.append((char) ((sectorData[runningOffset] & BYTE_MASK) - BIT_8_MASK));
    attribs.put(key, val.toString());
  }

  /**
   * Add int attribute.
   *
   * @param key    key name
   * @param offset value offset
   * @param len    attribute length
   */
  @SuppressWarnings("unchecked")
  private void addIntAttrib(final String key, final int offset, final int len) {
    int val = 0;
    for (int i = 0; i < len; i++) {
      val += (sectorData[offset + i] & BYTE_MASK)
          << (BYTE_WIDTH * (len - 1 - i));
    }
    this.attribs.put(key, val);
  }

  /**
   * Get map of attributes.
   *
   * @return attribute map
   */
  @SuppressWarnings("unused")
  public OrderedMap getAttribs() {
    return this.attribs;
  }

  /**
   * Get named attribute.
   *
   * @param key key name
   * @return value
   */
  public Object getAttrib(final String key) {
    if (this.attribs.containsKey(key)) {
      return (this.attribs.get(key));
    }
    return null;
  }
}
