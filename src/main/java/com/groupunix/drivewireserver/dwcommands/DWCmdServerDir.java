package com.groupunix.drivewireserver.dwcommands;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCmdServerDir extends DWCommand {
  /**
   * Max screen width.
   */
  public static final int SCREEN_WIDTH = 80;

  /**
   * Server directory command constructor.
   *
   * @param parent parent command
   */
  public DWCmdServerDir(final DWCommand parent) {
    super();
    setParentCmd(parent);
    this.setCommand("dir");
    this.setShortHelp("Show directory of URI or local path");
    this.setUsage("dw server dir URI/path");
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
          "dw server dir requires a URI or path as an argument"
      );
    }
    return doDir(cmdline);
  }

  private DWCommandResponse doDir(final String path) {
    final StringBuilder text = new StringBuilder();

    try {
      final FileSystemManager fsManager = VFS.getManager();

      final FileObject dirobj = fsManager.resolveFile(
          DWUtils.convertStarToBang(path)
      );

      final FileObject[] children = dirobj.getChildren();

      text.append("Directory of ")
          .append(dirobj.getName().getURI())
          .append("\r\n\n");

      int longest = 0;

      for (final FileObject child : children) {
        if (child.getName().getBaseName().length() > longest) {
          longest = child.getName().getBaseName().length();
        }
      }

      longest++;
      longest++;
      final int cols = Math.max(1, SCREEN_WIDTH / longest);
      for (int i = 0; i < children.length; i++) {
        text.append(
            String.format(
                "%-" + longest + "s", children[i].getName().getBaseName()
            )
        );
        if ((i + 1) % cols == 0) {
          text.append("\r\n");
        }
      }
    } catch (FileSystemException e) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,
          e.getMessage()
      );
    }
    return new DWCommandResponse(text.toString());
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
