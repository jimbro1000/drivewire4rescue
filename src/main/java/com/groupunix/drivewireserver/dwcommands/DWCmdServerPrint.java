
package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdServerPrint extends DWCommand {
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;

  /**
   * Server print command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  public DWCmdServerPrint(
      final DWProtocol protocol,
      final DWCommand parent
  ) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    commandName = "print";
    shortHelp = "Print contents of file on server";
    usage = "dw server print URI/path";
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    if (cmdline.length() == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "dw server print requires a URI or local file path as an argument"
      );
    }
    return (doPrint(cmdline));
  }

  private DWCommandResponse doPrint(final String path) {
    FileSystemManager fsManager;
    InputStream ins = null;
    FileObject fileobj = null;
    FileContent fc = null;

    try {
      fsManager = VFS.getManager();
      fileobj = fsManager.resolveFile(DWUtils.convertStarToBang(path));
      fc = fileobj.getContent();
      ins = fc.getInputStream();
      byte data = (byte) ins.read();
      while (data >= 0) {
        ((DWProtocolHandler) dwProtocol).getVPrinter().addByte(data);
        data = (byte) ins.read();
      }
      ins.close();
      ((DWProtocolHandler) dwProtocol).getVPrinter().flush();
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
        if (fc != null) {
          fc.close();
        }
        if (fileobj != null) {
          fileobj.close();
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return new DWCommandResponse("Sent item to printer");
  }

  /**
   * Validate command.
   *
   * @param cmdline command string
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
