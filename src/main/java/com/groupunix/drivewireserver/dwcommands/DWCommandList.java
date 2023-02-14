package com.groupunix.drivewireserver.dwcommands;

import java.util.*;

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
   * @param protocols content list
   * @param columns   number of columns
   * @return formatted content
   */
  public static String colLayout(
      final ArrayList<String> protocols,
      final int columns
  ) {
    final int workingCols = columns - 1;
    final StringBuilder text = new StringBuilder();

    Iterator<String> iterator = protocols.iterator();
    int maxlen = 1;
    while (iterator.hasNext()) {
      final int currentLength = iterator.next().length();
      if (currentLength > maxlen) {
        maxlen = currentLength;
      }
    }
    // leave spaces between cols
    if (workingCols > MAX_COLS) {
      maxlen += 2;
    } else {
      maxlen++;
    }
    iterator = protocols.iterator();
    int index = 0;
    final int colLen = workingCols / maxlen;
    while (iterator.hasNext()) {
      String itxt = String.format("%-" + maxlen + "s", iterator.next());
      if (index > 0 && index % colLen == 0) {
        text.append("\r\n");
      }
      if (workingCols <= MAX_COLS) {
        itxt = itxt.toUpperCase();
      }
      text.append(itxt);
      index++;
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
    if (cmdline.length() == 0) {
      // ended here, show commands..
      return new DWCommandResponse(getShortHelp());
    }
    final String[] args = cmdline.split(" ");
    final int matches = numCommandMatches(args[0]);
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
      if (args.length == 2 && args[1].equals("?")) {
        return getLongHelp(Objects.requireNonNull(getCommandMatch(args[0])));
      } else if (args.length == 2 && args[1].equals("*")) {
        return getCmdTree(Objects.requireNonNull(getCommandMatch(args[0])));
      } else {
        return Objects.requireNonNull(getCommandMatch(args[0]))
            .parse(DWUtils.dropFirstToken(cmdline));
      }
    }
  }

  private DWCommandResponse getCmdTree(final DWCommand command) {
    // figure out whole command...
    final StringBuilder cmdline = new StringBuilder(command.getCommand());
    DWCommand tmpCommand = command;
    while (tmpCommand.getParentCmd() != null) {
      tmpCommand = tmpCommand.getParentCmd();
      cmdline.insert(0, tmpCommand.getCommand() + " ");
    }
    String text = "Tree for " + cmdline + "\r\n\n";
    text += makeTreeString(command, 0);
    if (this.outputCols <= MAX_COLS) {
      text = text.toUpperCase();
    }
    return new DWCommandResponse(text);
  }

  private String makeTreeString(final DWCommand command, final int depth) {
    final StringBuilder res = new StringBuilder();
    res.append(String.format(
        "%-30s",
        String.format(
            "%-" + (depth + 1) * TREE_PADDING + "s",
            " "
        ) + command.getCommand())
    ).append(command.getShortHelp()).append("\r\n");
    if (command.getCommandList() != null) {
      for (final DWCommand c : command.getCommandList().commands) {
        res.append(makeTreeString(c, depth + 1));
      }
    }
    return res.toString();
  }

  private DWCommandResponse getLongHelp(final DWCommand command) {
    // figure out whole command...
    final StringBuilder cmdline = new StringBuilder(command.getCommand());

    DWCommand tmp = command;

    while (tmp.getParentCmd() != null) {
      tmp = tmp.getParentCmd();
      cmdline.insert(0, tmp.getCommand() + " ");
    }
    String text = command.getUsage() + "\r\n\r\n";
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
    final ArrayList<String> options = new ArrayList<>();
    for (final DWCommand cmd : this.commands) {
      options.add(cmd.getCommand());
    }
    Collections.sort(options);
    String helpText = DWCommandList.colLayout(options, this.outputCols);
    helpText = "Possible commands:\r\n\r\n" + helpText;
    return helpText;
  }

  private String getTextMatches(final String arg) {
    final StringBuilder textMatch = new StringBuilder();
    for (final DWCommand cmd : this.commands) {
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
    for (final DWCommand command : this.commands) {
      if (command.getCommand().startsWith(arg.toLowerCase())) {
        matches++;
      }
    }
    return matches;
  }

  private DWCommand getCommandMatch(final String arg) {
    for (final DWCommand dwCommand : this.commands) {
      if (dwCommand.getCommand().startsWith(arg.toLowerCase())) {
        return dwCommand;
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
    if (cmdline.length() == 0) {
      return true;
    }
    final String[] args = cmdline.split(" ");
    final int matches = numCommandMatches(args[0]);
    if (matches == 0) {
      // no match
      return false;
    } else if (matches > 1) {
      // ambiguous
      return false;
    } else {
      return Objects.requireNonNull(getCommandMatch(args[0]))
          .validate(DWUtils.dropFirstToken(cmdline));
    }
  }

  /**
   * Get command strings.
   *
   * @return List of commands
   */
  @SuppressWarnings("unused")
  public ArrayList<String> getCommandStrings() {
    return this.getCommandStrings(this, "");
  }

  private ArrayList<String> getCommandStrings(
      final DWCommandList commandList,
      final String prefix
  ) {
    final ArrayList<String> result = new ArrayList<>();
    for (final DWCommand cmd : commandList.getCommands()) {
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
