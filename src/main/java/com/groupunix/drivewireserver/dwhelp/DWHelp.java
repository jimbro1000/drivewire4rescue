package com.groupunix.drivewireserver.dwhelp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import net.htmlparser.jericho.Source;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwcommands.DWCmd;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWHelp {
  /**
   * Text topic suffix.
   */
  public static final String TEXT_SUFFIX = ".text";
  /**
   * Topic prefix.
   */
  public static final String TOPICS_PREFIX = "topics.";
  /**
   * Log appender.
   */
  private static final Logger LOGGER = Logger.getLogger("DWHelp");
  /**
   * Maximum line length.
   */
  private static final int MAX_LINE_LENGTH = 32;
  /**
   * Delimiter character.
   */
  private static final char DELIMITER = (char) 10;
  /**
   * Number of blanks for termintor.
   */
  private static final int KEY_TERMINATOR_LEN = 3;
  /**
   * Help configuration.
   */
  private XMLConfiguration help;
  /**
   * Help file source.
   */
  private String helpfile = null;

  /**
   * Drivewire help.
   *
   * @param helpFile local help source file (xml config)
   */
  public DWHelp(final String helpFile) {
    this.helpfile = helpFile;
    this.reload();
  }

  /**
   * Drivewire help.
   *
   * @param dwProto protocol
   */
  public DWHelp(final DWProtocol dwProto) {
    help = new XMLConfiguration();
    addAllTopics(new DWCmd(dwProto), "");
  }

  /**
   * Reload help.
   */
  public void reload() {
    // load helpfile if possible
    LOGGER.debug("reading help from '" + this.helpfile + "'");
    try {
      this.help = new XMLConfiguration(helpfile);
      // local help file
      this.help.setListDelimiter(DELIMITER);
      //this.help.setAutoSave(true);
    } catch (ConfigurationException e1) {
      LOGGER.warn("Error loading help file: " + e1.getMessage());
    }
  }

  /**
   * Load wiki topics.
   *
   * @param sourceUrlString source url
   * @throws IOException failed to read from source
   */
  @SuppressWarnings("unused")
  private void loadWikiTopics(final String sourceUrlString)
      throws IOException {
    Source source = new Source(new URL(sourceUrlString));
    source.getRenderer().setMaxLineLength(MAX_LINE_LENGTH);
    String renderedText = source.getRenderer().toString();
    String[] lines = renderedText.split("\n");
    String curkey = null;
    int blanks = 0;
    for (int i = 0; i < lines.length; i++) {
      lines[i] = lines[i].trim();
      if (this.hasTopic(lines[i])) {
        curkey = lines[i];
        this.clearTopic(lines[i]);
        blanks = 0;
      } else {
        if (lines[i].equals("")) {
          blanks++;
        } else {
          blanks = 0;
        }
        if ((blanks < 2) && (curkey != null)) {
          System.out.println("Line: " + lines[i]);
          this.help.addProperty(
              packageTopic(this.spaceToDot(curkey)),
              lines[i]
          );
        }
        if (blanks == KEY_TERMINATOR_LEN) {
          curkey = null;
        }
      }
    }
  }

  /**
   * Remove topic.
   *
   * @param topic topic name
   */
  private void clearTopic(final String topic) {
    this.help.clearTree(packageTopic(topic));
  }

  /**
   * Test if topic is known.
   *
   * @param topic topic name
   * @return true if topic is known
   */
  public boolean hasTopic(final String topic) {
    if (this.help != null) {
      return (this.help.containsKey(packageTopic(topic)));
    }
    return false;
  }

  /**
   * Get topic text.
   *
   * @param topic topic name
   * @return topic text
   * @throws DWHelpTopicNotFoundException topic name not found
   */
  public String getTopicText(final String topic)
      throws DWHelpTopicNotFoundException {
    String topicName;
    if (this.hasTopic(topic)) {
      StringBuilder text = new StringBuilder();
      topicName = this.spaceToDot(topic);
      String[] txts = help.getStringArray(packageTopic(topicName));
      for (String txt : txts) {
        text.append(txt).append("\r\n");
      }
      return (text.toString());
    } else {
      throw new DWHelpTopicNotFoundException(
          "There is no help available for the topic '" + topic + "'."
      );
    }
  }

  private String packageTopic(final String topicName) {
    return TOPICS_PREFIX + topicName + TEXT_SUFFIX;
  }

  /**
   * Get (all) topics.
   *
   * @param topic topic name (not used)
   * @return list of topics
   */
  @SuppressWarnings("unchecked")
  public ArrayList<String> getTopics(final String topic) {
    ArrayList<String> res = new ArrayList<>();
    if (this.help != null) {
      Iterator<String> itk = help.configurationAt("topics").getKeys();
      while (itk.hasNext()) {
        String key = itk.next();
        if (key.endsWith(TEXT_SUFFIX)) {
          res.add(
              this.dotToSpace(
                  key.substring(0, key.length() - TEXT_SUFFIX.length())
              )
          );
        }
      }
    }
    return (res);
  }

  /**
   * Substitute spaces with dots.
   *
   * @param topic topic
   * @return revised topic
   */
  private String spaceToDot(final String topic) {
    return topic.replaceAll(" ", "\\.");
  }

  /**
   * Substitute dots with spaces.
   *
   * @param topic topic
   * @return revised topic
   */
  private String dotToSpace(final String topic) {
    return topic.replaceAll("\\.", " ");
  }

  /**
   * Add all topics.
   *
   * @param dwc    drivewire command
   * @param prefix help prefix
   */
  private void addAllTopics(final DWCommand dwc, final String prefix) {
    String key = TOPICS_PREFIX;
    if (!prefix.equals("")) {
      key += spaceToDot(prefix) + ".";
    }
    key += spaceToDot(dwc.getCommand()) + TEXT_SUFFIX;
    help.addProperty(key, dwc.getUsage());
    help.addProperty(key, "");
    help.addProperty(key, dwc.getShortHelp());
    if (dwc.getCommandList() != null) {
      for (DWCommand dwsc : dwc.getCommandList().getCommands()) {
        if (!prefix.equals("")) {
          addAllTopics(dwsc, prefix + " " + dwc.getCommand());
        } else {
          addAllTopics(dwsc, dwc.getCommand());
        }
      }
    }
  }

  /**
   * Get help section topics.
   *
   * @param section section name
   * @return list of topics
   */
  @SuppressWarnings("unchecked")
  public ArrayList<String> getSectionTopics(final String section) {
    ArrayList<String> res = new ArrayList<>();
    if (this.help != null) {
      Iterator<String> itk = help
          .configurationAt(TOPICS_PREFIX + section)
          .getKeys();
      while (itk.hasNext()) {
        String key = itk.next();
        if (key.endsWith(TEXT_SUFFIX)) {
          res.add(
              this.dotToSpace(
                  key.substring(0, key.length() - TEXT_SUFFIX.length())
              )
          );
        }
      }
    }
    return res;
  }
}
