package com.groupunix.drivewireserver.dwcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCmdServerList extends DWCommand {
  /**
   * Buffer size (bytes).
   */
  private static final int BUFFER_SIZE = 256;

  /**
   * Server list command constructor.
   *
   * @param parent parent command
   */
  public DWCmdServerList(final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.setCommand("list");
    this.setShortHelp("List contents of file on server");
    this.setUsage("dw server list URI/path");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw server list requires a URI or local file path as an argument"
      );
    }
    return doList(cmdline);
  }


  private DWCommandResponse doList(final String path) {
    InputStream ins = null;
    FileObject fileobj = null;
    FileContent content = null;

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      final FileSystemManager fsManager = VFS.getManager();

      fileobj = fsManager.resolveFile(DWUtils.convertStarToBang(path));
      content = fileobj.getContent();
      ins = content.getInputStream();

      final byte[] buffer = new byte[BUFFER_SIZE];
      int size = 0;

      while ((size = ins.read(buffer)) >= 0) {
        baos.write(buffer, 0, size);
      }
      ins.close();
    } catch (FileSystemException e) {
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
    } finally {
      try {
        if (ins != null) {
          ins.close();
        }
        if (content != null) {
          content.close();
        }
        if (fileobj != null) {
          fileobj.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new DWCommandResponse(baos.toByteArray());
  }

  /**
   * Validate command .
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
