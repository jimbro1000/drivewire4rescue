package com.groupunix.drivewireserver.virtualprinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWPrinterFileError;
import com.groupunix.drivewireserver.dwexceptions.DWPrinterNotDefinedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.virtualserial.DWVSerialCircularBuffer;

public class DWVPrinterTEXT implements DWVPrinterDriver {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVPrinter.DWVPrinterTEXT");
  /**
   * Printer buffer size.
   */
  private static final int BUFFER_SIZE = 256;
  /**
   * Printer buffer.
   */
  private final DWVSerialCircularBuffer printBuffer
      = new DWVSerialCircularBuffer(-1, true);
  /**
   * Configuration.
   */
  private final HierarchicalConfiguration hierarchicalConfiguration;

  /**
   * Text printer constructor.
   *
   * @param config configuration set
   */
  public DWVPrinterTEXT(final HierarchicalConfiguration config) {
    this.hierarchicalConfiguration = config;
    this.printBuffer.clear();
  }

  /**
   * Add byte data to printer buffer.
   *
   * @param data byte value
   * @throws IOException
   */
  @Override
  public void addByte(final byte data) throws IOException {
    this.printBuffer.getOutputStream().write(data);
  }

  /**
   * Get driver name.
   *
   * @return driver name
   */
  @Override
  public String getDriverName() {
    return "TEXT";
  }

  /**
   * Flush printer buffer.
   *
   * @throws IOException
   * @throws DWPrinterNotDefinedException
   * @throws DWPrinterFileError
   */
  @Override
  public void flush()
      throws IOException, DWPrinterNotDefinedException, DWPrinterFileError {
    final File file = getPrinterFile();
    final FileOutputStream fos = new FileOutputStream(file);
    while (this.printBuffer.getAvailable() > 0) {
      final byte[] buf = new byte[BUFFER_SIZE];
      final int read = this.printBuffer.getInputStream().read(buf);
      fos.write(buf, 0, read);
    }
    fos.flush();
    fos.close();
    LOGGER.info("Flushed print job to " + file.getCanonicalPath());
    // execute coco dw command..
    if (hierarchicalConfiguration.containsKey("FlushCommand")) {
      doExec(doVarSubst(
          file,
          hierarchicalConfiguration.getString("FlushCommand")
      ));
    }
  }

  private void doExec(final String cmd) {
    try {
      LOGGER.info("executing flush command: " + cmd);
      Runtime.getRuntime().exec(cmd);
    } catch (IOException e) {
      LOGGER.warn("Error during flush command: " + e.getMessage());
    }
  }

  private String doVarSubst(final File file, final String cmd)
      throws IOException {
    // substitute vars in cmdline
    // $name - printer name
    // $file - full file path
    String result = cmd;
    final HashMap<String, String> vars = new HashMap<>();
    vars.put("name", hierarchicalConfiguration.getString("Name"));
    // double \ so the replaceall doesn't eat it later
    vars.put("file", file.getCanonicalPath().replaceAll("\\\\", "\\\\\\\\"));
    for (final Map.Entry<String, String> e : vars.entrySet()) {
      result = result.replaceAll("\\$" + e.getKey(), e.getValue());
    }
    return result;
  }

  /**
   * Get printer file extension.
   *
   * @return file extension
   */
  public String getFileExtension() {
    return ".txt";
  }

  /**
   * Get printer file prefix.
   *
   * @return file prefix
   */
  public String getFilePrefix() {
    return "dw_text_";
  }

  /**
   * Get printer output file.
   *
   * @return file object
   * @throws IOException
   * @throws DWPrinterFileError
   */
  public File getPrinterFile()
      throws IOException, DWPrinterFileError {
    if (this.hierarchicalConfiguration.containsKey("OutputFile")) {
      if (DWUtils.fileExistsOrCreate(
          this.hierarchicalConfiguration.getString("OutputFile")
      )) {
        return new File(
            this.hierarchicalConfiguration.getString("OutputFile")
        );
      } else {
        throw new DWPrinterFileError(
            "Cannot find or create the output file '"
                + this.hierarchicalConfiguration.getString("OutputFile") + "'"
        );
      }
    } else if (this.hierarchicalConfiguration.containsKey("OutputDir")) {
      if (DWUtils.dirExistsOrCreate(
          this.hierarchicalConfiguration.getString("OutputDir")
      )) {
        return File.createTempFile(
            getFilePrefix(),
            getFileExtension(),
            new File(this.hierarchicalConfiguration.getString("OutputDir"))
        );
      } else {
        throw new DWPrinterFileError(
            "Cannot find or create the output directory '"
                + this.hierarchicalConfiguration.getString("OutputDir") + "'"
        );
      }
    } else {
      throw new DWPrinterFileError(
          "No OutputFile or OutputDir defined in printer config, "
              + "don't know where to send output."
      );
    }
  }

  /**
   * Get printer name.
   *
   * @return printer name
   */
  @Override
  public String getPrinterName() {
    return this.hierarchicalConfiguration.getString("[@name]", "?noname?");
  }
}
