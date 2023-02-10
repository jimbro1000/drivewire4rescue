package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWDECBFileSystem;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFullException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidDirectoryException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFilenameException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public final class DWCmdDiskDosAdd extends DWCommand {
  /**
   * Drivewire protocol handler.
   */
  private final DWProtocolHandler dwProtocolHandler;

  /**
   * Disk dos add file command constructor.
   *
   * @param protocolHandler protocol handler
   * @param parent          parent command
   */
  public DWCmdDiskDosAdd(
      final DWProtocolHandler protocolHandler,
      final DWCommand parent
  ) {
    super();
    setParentCmd(parent);
    this.dwProtocolHandler = protocolHandler;
    this.setCommand("add");
    this.setShortHelp("Add file to disk image with DOS filesystem");
    this.setUsage("dw disk dos add # path");
  }

  /**
   * parse command.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    final String[] args = cmdline.split(" ");

    if (args.length == 2) {
      try {
        return doDiskDosAdd(
            dwProtocolHandler.getDiskDrives().getDriveNoFromString(args[0]),
            args[1]
        );
      } catch (DWDriveNotValidException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_INVALID_DRIVE,
            e.getMessage()
        );
      } catch (DWDriveNotLoadedException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_DRIVE_NOT_LOADED,
            e.getMessage()
        );
      } catch (DWFileSystemFullException
               | DWFileSystemInvalidFilenameException
               | DWFileSystemInvalidFATException
               | DWDiskInvalidSectorNumber
               | DWFileSystemInvalidDirectoryException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,
            e.getMessage()
        );
      } catch (IOException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_IO_EXCEPTION,
            e.getMessage()
        );
      } catch (DWFileSystemFileNotFoundException e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_FILE_NOT_FOUND,
            e.getMessage()
        );
      }
    }

    return new DWCommandResponse(
        false,
        DWDefs.RC_SYNTAX_ERROR,
        "Syntax error"
    );
  }

  private DWCommandResponse doDiskDosAdd(
      final int driveno,
      final String path
  ) throws DWDriveNotLoadedException,
      DWDriveNotValidException,
      DWFileSystemFullException,
      DWFileSystemInvalidFilenameException,
      IOException,
      DWFileSystemFileNotFoundException,
      DWFileSystemInvalidFATException,
      DWDiskInvalidSectorNumber,
      DWFileSystemInvalidDirectoryException {
    final DWDECBFileSystem decbFs = new DWDECBFileSystem(
        dwProtocolHandler.getDiskDrives().getDisk(driveno)
    );
    final FileObject fileObj = VFS.getManager().resolveFile(path);
    if (fileObj.exists() && fileObj.isReadable()) {
      final FileContent fileContent = fileObj.getContent();
      final long fObjSize = fileContent.getSize();
      // size check
      if (fObjSize > Integer.MAX_VALUE) {
        throw new DWFileSystemFullException(
            "File too big, maximum size is "
                + Integer.MAX_VALUE + " bytes."
        );
      }
      // get header
      final byte[] content = new byte[(int) fObjSize];
      if (content.length > 0) {
        int readres = 0;
        final InputStream inputStream = fileContent.getInputStream();
        while (readres < content.length) {
          readres += inputStream.read(
              content, readres, content.length - readres
          );
        }
        inputStream.close();
      }
      decbFs.addFile(
          fileObj.getName().getBaseName().toUpperCase(),
          content
      );
    } else {
      throw new IOException("Unreadable source path");
    }
    return new DWCommandResponse("File added to DOS disk.");
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
