package com.groupunix.drivewireserver.dwdisk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWImageHasNoSourceException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;


public class DWRawDisk extends DWDisk {


  private static final Logger logger = Logger.getLogger("DWServer.DWRawDisk");
  private boolean direct = false;


  public DWRawDisk(FileObject fileobj, int sectorsize, int maxsectors) throws IOException, DWImageFormatException {
    super(fileobj);

    commonConstructor(false, sectorsize, maxsectors);
  }


  public DWRawDisk(FileObject fileobj, int sectorsize, int maxsectors, boolean forcecache) throws IOException, DWImageFormatException {
    super(fileobj);

    commonConstructor(forcecache, sectorsize, maxsectors);
  }


  public DWRawDisk(int sectorsize, int maxsectors) {
    super();

    setDefaultOptions(sectorsize, maxsectors);

    logger.debug("New DWRawDisk (in memory only)");

  }


  public DWRawDisk(Vector<DWDiskSector> sectors) {
    // used only for temp objs..

    super();

    this.setSectors(sectors);
  }

  public static int considerImage(byte[] header, long fobjsize) {
    // is it right size for raw sectors
    if (fobjsize % DWDefs.DISK_SECTORSIZE == 0) {

      // is it an os9 filesystem
      if (fobjsize > 3) {

        if (fobjsize == ((0xFF & header[0]) * 65535 + (0xFF & header[1]) * 256 + (0xFF & header[2])) * 256) {
          // exact match, lets claim it
          return (DWDefs.DISK_CONSIDER_YES);
        }
      }

      // not os9 so can't be sure?
      return (DWDefs.DISK_CONSIDER_MAYBE);
    }

    // not /256
    return (DWDefs.DISK_CONSIDER_NO);
  }

  private void commonConstructor(boolean forcecache, int sectorsize, int maxsectors) throws IOException, DWImageFormatException {
    // expose user options
    this.setParam("syncfrom", DWDefs.DISK_DEFAULT_SYNCFROM);
    this.setParam("syncto", DWDefs.DISK_DEFAULT_SYNCTO);

    setDefaultOptions(sectorsize, maxsectors);

    load(forcecache);

    logger.debug("New DWRawDisk for '" + this.getFilePath() + "'");
  }

  private void setDefaultOptions(int sectorsize, int maxsectors) {
    // set internal info
    this.setParam("_sectorsize", sectorsize);
    this.setParam("_maxsectors", maxsectors);
    this.setParam("_format", "raw");

    // expose user options
    this.setParam("offset", DWDefs.DISK_DEFAULT_OFFSET);
    this.setParam("offsetdrv", 0);
    this.setParam("sizelimit", DWDefs.DISK_DEFAULT_SIZELIMIT);
    this.setParam("expand", DWDefs.DISK_DEFAULT_EXPAND);
  }

  public int getDiskFormat() {
    return (DWDefs.DISK_FORMAT_RAW);
  }

  public void seekSector(int newLSN) throws DWInvalidSectorException, DWSeekPastEndOfDeviceException {


    if ((newLSN < 0) || (newLSN > this.getMaxSectors())) {
      throw new DWInvalidSectorException("Sector " + newLSN + " is not valid");

    } else if ((newLSN >= this.getDiskSectors()) && (!this.getParams().getBoolean("expand", DWDefs.DISK_DEFAULT_EXPAND))) {
      throw new DWSeekPastEndOfDeviceException("Sector " + newLSN + " is beyond end of file, and expansion is not allowed");
    } else if ((this.getSizelimit() > -1) && (newLSN >= this.getSizelimit())) {
      throw new DWSeekPastEndOfDeviceException("Sector " + newLSN + " is beyond specified sector size limit");
    } else {
      this.setParam("_lsn", newLSN);

    }
  }

  public void load() throws IOException, DWImageFormatException {
    this.load(false);
  }

  public void load(boolean forcecache) throws IOException, DWImageFormatException {
    // load file into sector array

    int sector = 0;
    int sectorsize = this.getSectorSize();

    long filesize = this.getFileObject().getContent().getSize();

    if ((filesize > Integer.MAX_VALUE) || ((filesize / this.getSectorSize()) > DWDefs.DISK_MAXSECTORS))
      throw new DWImageFormatException("Image file is too large");

    if (!forcecache) {
      if ((this.getFileObject().getName().toString()).startsWith("file://")) // && !(this.drive.getDiskDrives().getDWProtocolHandler().getConfig().getBoolean("CacheLocalImages",false)))
      {
        this.direct = true;
      }
    }

    if (!direct) {
      logger.debug("Caching " + this.getFileObject().getName() + " in memory");
      long memfree = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
      if (filesize > memfree) {
        throw new DWImageFormatException("Image file will not fit in memory (" + (memfree / 1024) + " Kbytes free)");
      }

      BufferedInputStream fis = new BufferedInputStream(this.getFileObject().getContent().getInputStream());

      int readres = 0;
      int bytesRead = 0;
      byte[] buffer = new byte[sectorsize];

      this.getSectors().setSize((int) (filesize / sectorsize));

      readres = fis.read(buffer, 0, sectorsize);

      while (readres > -1) {

        bytesRead += readres;

        if (bytesRead == sectorsize) {

          this.getSectors().set(sector, new DWDiskSector(this, sector, sectorsize, false));
          this.getSectors().get(sector).setData(buffer, false);

          sector++;
          bytesRead = 0;
        }

        readres = fis.read(buffer, bytesRead, (sectorsize - bytesRead));
      }

      if (bytesRead > 0) {

        throw new DWImageFormatException("Incomplete sector data on sector " + sector);
      }

      logger.debug("read " + sector + " sectors from '" + this.getFileObject().getName() + "'");
      fis.close();

    } else {
      int sz = 0;

      this.getSectors().setSize((int) (filesize / sectorsize));

      while (sz < filesize) {
        this.getSectors().set(sector, new DWDiskSector(this, sector, sectorsize, true));
        sector++;
        sz += sectorsize;
      }
    }


    long lastmodtime = -1;

    try {
      lastmodtime = this.getFileObject().getContent().getLastModifiedTime();
    } catch (FileSystemException e) {
      logger.warn(e.getMessage());
    }

    this.setLastModifiedTime(lastmodtime);

    this.setParam("_sectors", sector);

    this.setParam("_filesystem", DWUtils.prettyFileSystem(DWDiskDrives.getDiskFSType(this.getSectors())));


  }

  public byte[] readSector() throws IOException, DWImageFormatException {
    // logger.debug("Read sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));
    this.incParam("_reads");


    // check source for changes...
    if (this.isSyncFrom() && (this.getFileObject() != null)) {
      if (this.getFileObject().getContent().getLastModifiedTime() != this.getLastModifiedTime()) {
        // source has changed.. have we?
        if (this.getDirtySectors() > 0) {
          // doh
          logger.warn("Sync conflict on " + getFilePath() + ", both the source and our cached image have changed.  Source will be overwritten!");
          try {
            this.write();
          } catch (DWImageHasNoSourceException e) {
            //don't care
          }
        } else {
          logger.info("Disk source " + getFilePath() + " has changed, reloading");
          this.reload();
        }
      }
    }


    int effLSN = this.getLSN() + this.getOffset();

    // we can read beyond the current size of the image
    if ((effLSN >= this.getSectors().size()) || (this.getSectors().get(effLSN) == null)) {
      logger.debug("request for undefined sector, effLSN: " + effLSN + "  rawLSN: " + this.getLSN() + "  curSize: " + (this.getSectors().size() - 1));

      // no need to expand disk on read, give a blank sector
      return (new byte[this.getSectorSize()]);

    }

    return (this.getSectors().get(effLSN).getData());
  }

  private void expandDisk(final int target) {
    @SuppressWarnings("unused") final int start = this.getSectors().size();
    @SuppressWarnings("unused") final int sectorsize = this.getSectorSize();
    @SuppressWarnings("unused") final DWDisk disk = this;

    this.getSectors().setSize(target);

		/*
		Runnable expander = new Runnable()
		{

			@Override
			public void run()
			{
				// do expansion in separate thread
				long starttime = System.currentTimeMillis();

				for (int i = start;i <= target;i++)
				{
					if (sectors.get(i) == null)
						sectors.set(i, new DWDiskSector(disk, i, sectorsize, false));
				}
				logger.debug("Expander init sectors " + start +" to " + target + "in " + (System.currentTimeMillis() - starttime) + " ms");

			}

		};

		Thread eT = new Thread(expander);
		eT.start();
		*/

    this.setParam("_sectors", target + 1);
  }

  public void writeSector(byte[] data) throws DWDriveWriteProtectedException, IOException {

    if (this.getWriteProtect()) {
      throw new DWDriveWriteProtectedException("Disk is write protected");
    } else {
      int effLSN = this.getLSN() + this.getOffset();

      // we can write beyond our current size
      if (effLSN >= this.getSectors().size()) {
        // expand disk / add sector
        expandDisk(effLSN);
        this.getSectors().add(effLSN, new DWDiskSector(this, effLSN, this.getSectorSize(), false));
        //logger.debug("new sector " + effLSN);
      }

      // jit sector maker
      if (this.getSectors().get(effLSN) == null)
        this.getSectors().set(effLSN, new DWDiskSector(this, effLSN, this.getSectorSize(), false));

      this.getSectors().get(effLSN).setData(data);

      this.incParam("_writes");

      // logger.debug("write sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));


    }
  }

  public void write() throws IOException, DWImageHasNoSourceException {
    // write in memory image to source
    if (this.getFileObject() == null) {
      throw (new DWImageHasNoSourceException("The image has no source object, must specify write path."));
    }

    if (this.getFileObject().isWriteable()) {
      if (this.getFileObject().getFileSystem().hasCapability(Capability.RANDOM_ACCESS_WRITE)) {
        // we can sync individual sectors
        syncSectors();
      } else if (this.getFileObject().getFileSystem().hasCapability(Capability.WRITE_CONTENT)) {
        // we must rewrite the entire object
        writeSectors(this.getFileObject());
      } else {
        // no way to write to this filesystem
        throw new FileSystemException("Filesystem is unwriteable");
      }
    } else {
      throw new FileSystemException("File is unwriteable");
    }


  }

  private void syncSectors() {
    long sectorswritten = 0;
    long starttime = System.currentTimeMillis();
    long sleeptime = 0;

    try {
      RandomAccessContent raf = getFileObject().getContent().getRandomAccessContent(RandomAccessMode.READWRITE);

      for (int i = 0; i < this.getSectors().size(); i++) {
        if (getSector(i) != null) {
          if (getSector(i).isDirty()) {
            if (this.getDrive().getDiskDrives().getDWProtocolHandler().isInOp()) {
              try {
                long sleepstart = System.currentTimeMillis();
                Thread.sleep(DWDefs.DISK_SYNC_INOP_PAUSE);
                sleeptime += System.currentTimeMillis() - sleepstart;
              } catch (InterruptedException e) {
                //  this would be weird..
                e.printStackTrace();
              }

            }
            long pos = i * this.getSectorSize();
            raf.seek(pos);
            raf.write(getSector(i).getData());
            sectorswritten++;
            getSector(i).makeClean();
          }
        }
      }


      raf.close();
      this.getFileObject().close();
      this.setLastModifiedTime(this.getFileObject().getContent().getLastModifiedTime());
    } catch (IOException e) {
      logger.error("Error writing sectors in " + this.getFilePath() + ": " + e.getMessage());
    } catch (DWDiskInvalidSectorNumber e) {
      logger.error("Error writing sectors in " + this.getFilePath() + ": " + e.getMessage());
    }

    if (sectorswritten > 0)
      logger.debug("wrote " + sectorswritten + " sectors in " + (System.currentTimeMillis() - starttime) + " ms (" + sleeptime + "ms sleep), to " + getFilePath());

  }

  private int getMaxSectors() {
    return this.getParams().getInt("_maxsectors", DWDefs.DISK_MAXSECTORS);
  }

  private int getSectorSize() {
    return this.getParams().getInt("_sectorsize", DWDefs.DISK_SECTORSIZE);
  }

  private boolean isSyncFrom() {
    return this.getParams().getBoolean("syncfrom", DWDefs.DISK_DEFAULT_SYNCFROM);
  }

  private boolean isSyncTo() {
    return this.getParams().getBoolean("syncto", DWDefs.DISK_DEFAULT_SYNCTO);
  }

  private int getOffset() {
    return (this.getParams().getInt("offset", DWDefs.DISK_DEFAULT_OFFSET) + (this.getParams().getInt("offsetdrv", 0) * DWDefs.DISK_HDBDOS_DISKSIZE));
  }

  private int getSizelimit() {
    return this.getParams().getInt("sizelimit", DWDefs.DISK_DEFAULT_SIZELIMIT);
  }

  public void sync() throws IOException {
    if (this.isSyncTo())
      if (this.getFileObject() != null)
        if (this.getDirtySectors() > 0)
          try {
            this.write();
          } catch (DWImageHasNoSourceException e) {
            // dont care
          }
  }

  @Override
  public boolean getDirect() {
    return this.direct;
  }

}
