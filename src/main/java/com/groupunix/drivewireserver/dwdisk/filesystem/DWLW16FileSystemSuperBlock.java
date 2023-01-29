package com.groupunix.drivewireserver.dwdisk.filesystem;

import com.groupunix.drivewireserver.dwdisk.DWDiskSector;

import java.io.IOException;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.BYTE_SHIFT;
import static com.groupunix.drivewireserver.DWDefs.WORD_SHIFT;

public class DWLW16FileSystemSuperBlock {
  /**
   * Offset to first inode block.
   */
  public static final int FIRST_INODE_BLOCK_OFFSET = 12;
  /**
   * Offset to data blocks.
   */
  public static final int DATA_BLOCK_OFFSET = 10;
  /**
   * Offset to first data block.
   */
  public static final int FIRST_DATA_BLOCK_OFFSET = 8;
  /**
   * Data bitmap blocks offset.
   */
  public static final int DATA_BMP_BLOCKS_OFFSET = 7;
  /**
   * INode bitmap blocks offset.
   */
  public static final int INODE_BMP_BLOCKS_OFFSET = 6;
  /**
   * Inodes offset.
   */
  public static final int INODES_OFFSET = 4;
  /**
   * Bytes in magic number.
   */
  public static final int MAGIC_LENGTH = 4;
  /**
   * Assumed sector size value.
   */
  private static final int SECTOR_SIZE = 256;
  /**
   * Assumed sectors per node value.
   */
  private static final int SECTORS_PER_NODE = 8;
  /**
   * Magic number.
   */
  private byte[] magic = new byte[MAGIC_LENGTH];
  /**
   * INode count.
   */
  private int iNodeCount;
  /**
   * INode bmp blocks count.
   */
  private int iNodeBmpBlocks;
  /**
   * Data bmp blocks count.
   */
  private int dataBmpBlockCount;
  /**
   * First data block number.
   */
  private int firstDataBlock;
  /**
   * Data block count.
   */
  private int dataBlockCount;
  /**
   * First iNode block.
   */
  private int firstInodeBlock;

  /**
   * LWL16 Filesystem Super Block.
   *
   * @param sector disk sector
   * @throws IOException failed to read from source
   */
  public DWLW16FileSystemSuperBlock(final DWDiskSector sector)
      throws IOException {
    System.arraycopy(sector.getData(), 0, magic, 0, MAGIC_LENGTH);
    this.iNodeCount = (BYTE_MASK & sector.getData()[INODES_OFFSET]) * BYTE_SHIFT
        + (BYTE_MASK & sector.getData()[INODES_OFFSET + 1]);
    this.iNodeBmpBlocks = BYTE_MASK & sector.getData()[INODE_BMP_BLOCKS_OFFSET];
    this.dataBmpBlockCount = BYTE_MASK
        & sector.getData()[DATA_BMP_BLOCKS_OFFSET];
    this.firstDataBlock =
        (BYTE_MASK & sector.getData()[FIRST_DATA_BLOCK_OFFSET]) * BYTE_SHIFT
        + (BYTE_MASK & sector.getData()[FIRST_DATA_BLOCK_OFFSET + 1]);
    this.dataBlockCount =
        (BYTE_MASK & sector.getData()[DATA_BLOCK_OFFSET])
        * BYTE_SHIFT
        + (BYTE_MASK & sector.getData()[DATA_BLOCK_OFFSET + 1]);
    this.firstInodeBlock =
        (BYTE_MASK & sector.getData()[FIRST_INODE_BLOCK_OFFSET])
        * BYTE_SHIFT
        + (BYTE_MASK & sector.getData()[FIRST_INODE_BLOCK_OFFSET + 1]);
  }

  /**
   * Is filesystem block valid.
   *
   * @return bool
   */
  public boolean isValid() {
    // magic number
    if (new String(this.magic).equals("LW16")) {
      // fs size
      if (
          (this.dataBlockCount + this.iNodeBmpBlocks
              + this.dataBmpBlockCount + 1) <= WORD_SHIFT
      ) {
        // Assumptions:
        // 256 = sector size
        // 8 = sectors per iNode
        return Math.ceil(
            (double) this.iNodeCount / SECTORS_PER_NODE / SECTOR_SIZE
        ) == this.iNodeBmpBlocks;
      }
    }
    return false;
  }

  /**
   * Get magic number.
   *
   * @return magic number
   */
  @SuppressWarnings("unused")
  public byte[] getMagic() {
    return magic;
  }

  /**
   * Set magic number.
   *
   * @param magicData magic number
   */
  @SuppressWarnings("unused")
  public void setMagic(final byte[] magicData) {
    this.magic = magicData;
  }

  /**
   * Get iNode count.
   *
   * @return iNodes
   */
  public int getInodes() {
    return iNodeCount;
  }

  /**
   * Set iNode count.
   *
   * @param inodes iNode count
   */
  @SuppressWarnings("unused")
  public void setInodes(final int inodes) {
    this.iNodeCount = inodes;
  }

  /**
   * Get iNode bitmap blocks.
   *
   * @return iNode bitmap blocks
   */
  @SuppressWarnings("unused")
  public int getInodeBmpBlocks() {
    return iNodeBmpBlocks;
  }

  /**
   * Set iNode bitmap blocks.
   *
   * @param inodeBmpBlocks iNode bitmap blocks
   */
  @SuppressWarnings("unused")
  public void setInodeBmpBlocks(final int inodeBmpBlocks) {
    this.iNodeBmpBlocks = inodeBmpBlocks;
  }

  /**
   * Get data bitmap blocks.
   *
   * @return data bitmap blocks
   */
  public int getDataBmpBlocks() {
    return dataBmpBlockCount;
  }

  /**
   * Set data bitmap block count.
   *
   * @param dataBmpBlocks data bitmap blocks
   */
  @SuppressWarnings("unused")
  public void setDataBmpBlocks(final int dataBmpBlocks) {
    this.dataBmpBlockCount = dataBmpBlocks;
  }

  /**
   * Get first data block.
   *
   * @return first data block
   */
  public int getFirstDataBlock() {
    return firstDataBlock;
  }

  /**
   * Set first data block.
   *
   * @param dataBlock first data block
   */
  @SuppressWarnings("unused")
  public void setFirstDataBlock(final int dataBlock) {
    this.firstDataBlock = dataBlock;
  }

  /**
   * Get data block count.
   *
   * @return data blocks
   */
  public int getDataBlocks() {
    return dataBlockCount;
  }

  /**
   * Set data block count.
   *
   * @param dataBlocks data blocks
   */
  @SuppressWarnings("unused")
  public void setDataBlocks(final int dataBlocks) {
    this.dataBlockCount = dataBlocks;
  }

  /**
   * Get first iNode block.
   *
   * @return first iNode block
   */
  public int getFirstInodeBlock() {
    return firstInodeBlock;
  }

  /**
   * Set first iNode block.
   *
   * @param iNodeBlock iNode block number
   */
  @SuppressWarnings("unused")
  public void setFirstInodeBlock(final int iNodeBlock) {
    this.firstInodeBlock = iNodeBlock;
  }
}
