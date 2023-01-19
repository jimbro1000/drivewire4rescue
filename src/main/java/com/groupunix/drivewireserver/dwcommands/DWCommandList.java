package com.groupunix.drivewireserver.dwcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public final class DWCommandList {
  /**
   * Maximum width.
   */
  public static final int MAX_COLS = 32;
  /**
   * Default formatting columns.
   */
  private static final int DEFAULT_COLUMNS = 80;
  /**
   * Number of spaces padding per tree depth.
   */
  private static final int TREE_PADDING = 4;
  /**
   * Command list.
   */
  private final List<DWCommand> commands = new ArrayList<>();
  /**
   * Drivewire protocol.
   */
  private final DWProtocol dwProtocol;
  /**
   * Output columns.
   */
  private final int outputCols;

  /**
   * Command list constructor.
   *
   * @param protocol protocol
   * @param columns output columns
   */
  public DWCommandList(final DWProtocol protocol, final int columns) {
    if (columns > 0) {
      this.outputCols = columns;
    } else {
      this.outputCols = DEFAULT_COLUMNS;
    }
    this.dwProtocol = protocol;
  }

  /**
   * Command list constructor.
   *
   * @param protocol protocol
   */
  public DWCommandList(final DWProtocol protocol) {
    this.dwProtocol = protocol;
    this.outputCols = DEFAULT_COLUMNS;
  }

  /**
   * Convert ps to column layout.
   *
   * @param ps content list
   * @param columns number of columns
   * @return formatted content
   */
  public static String colLayout(
      final ArrayList<String> ps,
      final int columns
  ) {
    int cols = columns - 1;
    StringBuilder text = new StringBuilder();

    Iterator<String> it = ps.iterator();
    int maxlen = 1;
    while (it.hasNext()) {
      int curlen = it.next().length();
      if (curlen > maxlen) {
        maxlen = curlen;
      }
    }
    // leave spaces between cols
    if (cols > MAX_COLS) {
      maxlen += 2;
    } else {
      maxlen++;
    }
    it = ps.iterator();
    int i = 0;
    int ll = cols / maxlen;
    while (it.hasNext()) {
      String itxt = String.format("%-" + maxlen + "s", it.next());
      if ((i > 0) && ((i % ll) == 0)) {
        text.append("\r\n");
      }
      if (cols <= MAX_COLS) {
        itxt = itxt.toUpperCase();
      }
      text.append(itxt);
      i++;
    }
    text.append("\r\n");
    return text.toString();
  }

  /**
   * Add command.
   *
   * @param dwCommand command
   */
  public void addCommand(final DWCommand dwCommand) {
    commands.add(dwCommand);
  }

  /**
   * Get component commands.
   *
   * @return command list
   */
  public List<DWCommand> getCommands() {
    return this.commands;
  }

  /**
   * parse command.
   *
   * @param cmdline command string
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    String[] args = cmdline.split(" ");
    if (cmdline.length() == 0) {
      // ended here, show commands..
      return new DWCommandResponse(getShortHelp());
    }
    int matches = numCommandMatches(args[0]);
    if (matches == 0) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Unknown command '" + args[0] + "'"
      );
    } else if (matches > 1) {
      return new DWCommandResponse(
          false,
          DWDefs.RC_SYNTAX_ERROR,
          "Ambiguous command, '" + args[0]
              + "' matches " + getTextMatches(args[0])
      );
    } else {
      if ((args.length == 2) && args[1].equals("?")) {
        return getLongHelp(getCommandMatch(args[0]));
      } else if ((args.length == 2) && args[1].equals("*")) {
        return getCmdTree(getCommandMatch(args[0]));
      } else {
        return getCommandMatch(args[0]).parse(DWUtils.dropFirstToken(cmdline));
      }
    }
  }

  private DWCommandResponse getCmdTree(final DWCommand command) {
    String text = "";
    // figure out whole command..
    StringBuilder cmdline = new StringBuilder(command.getCommand());
    DWCommand tmp = command;
    while (tmp.getParentCmd() != null) {
      tmp = tmp.getParentCmd();
      cmdline.insert(0, tmp.getCommand() + " ");
    }
    text = "Tree for " + cmdline + "\r\n\n";
    text += makeTreeString(command, 0);
    if (this.outputCols <= MAX_COLS) {
      text = text.toUpperCase();
    }
    return new DWCommandResponse(text);
  }

  private String makeTreeString(final DWCommand command, final int depth) {
    StringBuilder res = new StringBuilder();
    res.append(String.format(
        "%-30s",
        String.format(
            ("%-" + (depth + 1) * TREE_PADDING + "s"),
            " "
        ) + command.getCommand())
    ).append(command.getShortHelp()).append("\r\n");
    if (command.getCommandList() != null) {
      for (DWCommand c : command.getCommandList().commands) {
        res.append(makeTreeString(c, depth + 1));
      }
    }
    return res.toString();
  }

  private DWCommandResponse getLongHelp(final DWCommand command) {
    String text = "";
    // figure out whole command..
    StringBuilder cmdline = new StringBuilder(command.getCommand());

    DWCommand tmp = command;

    while (tmp.getParentCmd() != null) {
      tmp = tmp.getParentCmd();
      cmdline.insert(0, tmp.getCommand() + " ");
    }
    text = command.getUsage() + "\r\n\r\n";
    text += command.getShortHelp() + "\r\n";

    if (this.dwProtocol != null) {
      try {
        text = dwProtocol.getHelp().getTopicText(cmdline.toString());
      } catch (DWHelpTopicNotFoundException ignored) {
      }
    }

    if (this.outputCols <= MAX_COLS) {
      text = text.toUpperCase();
    }
    return new DWCommandResponse(text);
  }

  /**
   * Get short help.
   *
   * @return possible commands
   */
  public String getShortHelp() {
    String helpText = "";
    ArrayList<String> ps = new ArrayList<>();

    for (DWCommand cmd : this.commands) {
      ps.add(cmd.getCommand());
    }

    Collections.sort(ps);
    helpText = DWCommandList.colLayout(ps, this.outputCols);
    helpText = "Possible commands:\r\n\r\n" + helpText;
    return (helpText);
  }

  private String getTextMatches(final String arg) {
    StringBuilder textMatch = new StringBuilder();
    for (DWCommand cmd : this.commands) {
      if (cmd.getCommand().startsWith(arg.toLowerCase())) {
        if (textMatch.length() == 0) {
          textMatch.append(cmd.getCommand());
        } else {
          textMatch.append(" or ").append(cmd.getCommand());
        }
      }
    }
    return textMatch.toString();
  }

  private int numCommandMatches(final String arg) {
    int matches = 0;
    for (DWCommand command : this.commands) {
      if (command.getCommand().startsWith(arg.toLowerCase())) {
        matches++;
      }
    }
    return matches;
  }

  private DWCommand getCommandMatch(final String arg) {
    DWCommand command;
    for (DWCommand dwCommand : this.commands) {
      command = dwCommand;
      if (command.getCommand().startsWith(arg.toLowerCase())) {
        return command;
      }
    }
    return null;
  }

  /**
   * Validate commands.
   *
   * @param cmdline command string
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    String[] args = cmdline.split(" ");
    if (cmdline.length() == 0) {
      // we ended here
      return true;
    }
    int matches = numCommandMatches(args[0]);
    if (matches == 0) {
      // no match
      return false;
    } else if (matches > 1) {
      // ambiguous
      return false;
    } else {
      return getCommandMatch(args[0]).validate(DWUtils.dropFirstToken(cmdline));
    }
  }

  /**
   * Get command strings.
   *
   * @return List of commands
   */
  public ArrayList<String> getCommandStrings() {
    return this.getCommandStrings(this, "");
  }

  private ArrayList<String> getCommandStrings(
      final DWCommandList commandList,
      final String prefix
  ) {
    ArrayList<String> result = new ArrayList<>();
    for (DWCommand cmd : commandList.getCommands()) {
      result.add(prefix + " " + cmd.getCommand());
      if (cmd.getCommandList() != null) {
        result.addAll(this.getCommandStrings(
            cmd.getCommandList(),
            prefix + " " + cmd.getCommand()
            )
        );
      }
    }
    return result;
  }
}
