package com.groupunix.drivewireserver.virtualserial;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotOpenException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWVSerialPorts {
  /**
   * Terminal mode.
   */
  public static final int MODE_TERM = 3;
  /**
   * Cache size limit.
   */
  public static final int MAX_CACHE = 16;
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVSerialPorts");
  /**
   * Default ZTerm port number.
   */
  public static final int DEFAULT_ZTERM_PORT = 16;
  /**
   * Default max Z device ports.
   */
  public static final int DEFAULT_MAX_ZDEV_PORTS = 16;
  /**
   * Default max N device ports.
   */
  public static final int DEFAULT_MAX_NDEV_PORTS = 16;
  /**
   * Default midi port.
   */
  public static final int DEFAULT_MIDI_PORT = 14;
  /**
   * Port status code.
   */
  public static final int PORT_STATUS = 16;
  /**
   * Default multi read limit.
   */
  public static final int DEFAULT_MULTI_READ_LIMIT = 3;
  /**
   * serial readM response base value.
   */
  public static final int SERIAL_READ_BASE = 16;
  /**
   * Bits to shift for response bit flag.
   */
  public static final int RESPONSE_BIT_SHIFT = 6;
  /**
   * Reboot requested response status code.
   */
  public static final int REBOOT_STATUS = 255;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwProto;
  /**
   * Log bytes switch.
   */
  private final boolean bytelog;

  /**
   * Serial ports.
   */
  private final DWVSerialPort[] vserialPorts;
  /**
   * Port listener pool.
   */
  private final DWVPortListenerPool listenerpool = new DWVPortListenerPool();
  /**
   * Data wait array.
   */
  private final int[] dataWait;

  // midi stuff
  /**
   * Maximum number of N ports.
   */
  private final int maxNports;
  /**
   * Maximum number of Z ports.
   */
  private final int maxZports;
  /**
   * Maximum number of ports.
   */
  private final int maxports;
  /**
   * NTerm port.
   */
  private final int nTermPort;
  /**
   * ZTerm port.
   */
  private final int zTermPort;
  /**
   * MIDI port.
   */
  private final int midiPort;
  /**
   * Multi read limit.
   */
  private final int multiReadLimit;
  /**
   * MIDI device.
   */
  private MidiDevice midiDevice;
  /**
   * MIDI synthesizer.
   */
  private Synthesizer midiSynth;
  /**
   * Sound bank file name.
   */
  private String soundbankfilename = null;
  /**
   * MIDI voice lock switch.
   */
  private boolean midiVoicelock = false;
  /**
   * Configuration.
   */
  private HierarchicalConfiguration midiProfConf = null;
  /**
   * GM Instrument cache.
   */
  private int[] gmInstrumentCache;
  /**
   * Reboot requested flag.
   */
  private boolean rebootRequested = false;

  /**
   * Virtual Serial Ports.
   *
   * @param serialProtocol serial protocol
   */
  public DWVSerialPorts(final DWVSerialProtocol serialProtocol) {
    this.dwProto = serialProtocol;
    bytelog = serialProtocol.getConfig()
        .getBoolean("LogVPortBytes", false);
    maxNports = serialProtocol.getConfig()
        .getInt("VSerial_MaxNDevPorts", DEFAULT_MAX_NDEV_PORTS);
    maxZports = serialProtocol.getConfig()
        .getInt("VSerial_MaxZDevPorts", DEFAULT_MAX_ZDEV_PORTS);
    nTermPort = serialProtocol.getConfig()
        .getInt("VSerial_NTermPort", 0);
    zTermPort = serialProtocol.getConfig()
        .getInt("VSerial_ZTermPort", DEFAULT_ZTERM_PORT);
    midiPort = serialProtocol.getConfig()
        .getInt("VSerial_MIDIPort", DEFAULT_MIDI_PORT);
    this.multiReadLimit = serialProtocol.getConfig()
        .getInt("VSerial_MultiReadLimit", DEFAULT_MULTI_READ_LIMIT);

    maxports = maxNports + maxZports;

    dataWait = new int[maxports];
    vserialPorts = new DWVSerialPort[maxports];

    if (serialProtocol.getConfig().getBoolean("UseMIDI", false)
        && !DriveWireServer.isNoMIDI()) {
      clearGMInstrumentCache();
      try {
        // set default output
        if (serialProtocol.getConfig()
            .containsKey("MIDIDefaultOutput")) {
          final int devno = serialProtocol.getConfig()
              .getInt("MIDIDefaultOutput", -1);
          final MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
          if (devno < 0 || devno > infos.length) {
            LOGGER.warn("Invalid MIDI output device # "
                + devno + " specified in MIDIDefaultOutput setting");
          } else {
            setMIDIDevice(MidiSystem.getMidiDevice(infos[devno]));
          }
        } else {
          midiSynth = MidiSystem.getSynthesizer();
          setMIDIDevice(midiSynth);
        }
        // soundbank
        if (serialProtocol.getConfig()
            .containsKey("MIDISynthDefaultSoundbank")) {
          loadSoundbank(serialProtocol.getConfig()
              .getString("MIDISynthDefaultSoundbank"));
        }
        // default translation profile
        if (serialProtocol.getConfig()
            .containsKey("MIDISynthDefaultProfile")) {
          if (!setupMidiProfile(serialProtocol.getConfig()
              .getString("MIDISynthDefaultProfile"))) {
            LOGGER.warn("Invalid MIDI profile specified in config file.");
          }
        }
      } catch (MidiUnavailableException e) {
        LOGGER.warn("MIDI is not available");
      }
    }
  }

  /**
   * Open port.
   *
   * @param vport virtual port
   * @throws DWPortNotValidException invalid port
   */
  public void openPort(final int vport) throws DWPortNotValidException {
    this.validateport(vport);
    if (vserialPorts[vport] == null) {
      resetPort(vport);
    }
    vserialPorts[vport].open();
  }

  /**
   * Prettify port.
   *
   * @param vport virtual port
   * @return formatted string
   */
  public String prettyPort(final int vport) {
    if (vport == this.nTermPort) {
      return "NTerm";
    } else if (vport == this.zTermPort) {
      return "ZTerm";
    } else if (vport == this.midiPort) {
      return "MIDI";
    } else if (vport < this.maxNports) {
      return "N" + vport;
    } else if (vport < this.maxNports + this.maxZports) {
      return "Z" + (vport - this.maxNports);
    } else {
      return "?" + vport;
    }
  }

  /**
   * Close port.
   *
   * @param vport virtual port
   * @throws DWPortNotValidException invalid port
   */
  public void closePort(final int vport) throws DWPortNotValidException {
    if (vport < vserialPorts.length) {
      if (vserialPorts[vport] != null) {
        vserialPorts[vport].close();
      }
    } else {
      throw new DWPortNotValidException("Valid port range is 0 - "
          + (vserialPorts.length - 1));
    }
  }


  /**
   * Serial read.
   *
   * @return data array
   */
  public byte[] serRead() {
    byte[] response = new byte[2];
    // reboot req takes absolute priority
    if (this.isRebootRequested()) {
      response[0] = (byte) PORT_STATUS;
      response[1] = (byte) REBOOT_STATUS;
      LOGGER.debug("reboot request pending, sending response "
          + response[0] + "," + response[1]);
      this.setRebootRequested(false);
      return response;
    }

    // Z devices go first...

    for (int i = this.maxNports; i < this.maxNports + this.maxZports; i++) {
      if (vserialPorts[i] != null) {
        if (vserialPorts[i].bytesWaiting() > 0) {
          // increment wait count
          dataWait[i]++;
          LOGGER.debug("waiting Z " + i + ": "
              + vserialPorts[i].bytesWaiting());
        }
      }
    }

    // second pass, look for oldest waiting ports

    int oldestZ = 0;
    int oldestZport = -1;

    for (int i = this.maxNports; i < this.maxNports + this.maxZports; i++) {
      if (vserialPorts[i] != null) {
        if (dataWait[i] > oldestZ) {
          oldestZ = dataWait[i];
          oldestZport = i;
        }
      }
    }

    if (oldestZport > -1) {
      // if we have a small byte waiter, send serread for it
      dataWait[oldestZport] = 0;
      response[0] = (byte) ((DWDefs.POLL_RESP_MODE_WINDOW << RESPONSE_BIT_SHIFT)
          + (oldestZport) - this.maxNports);
      response[1] = vserialPorts[oldestZport].read1();
      LOGGER.debug("Z poll response " + response[0] + "," + response[1]);
      return response;
    }

    // N devices

    // first look for termed ports
    for (int i = 0; i < this.maxNports; i++) {
      if (vserialPorts[i] != null) {
        if (vserialPorts[i].isTerm()) {
          response[0] = (byte) PORT_STATUS;
          response[1] = (byte) i;   // 000 portnumber

          LOGGER.debug("sending terminated status to coco for port " + i);

          vserialPorts[i] = new DWVSerialPort(this, this.dwProto, i);

          return response;
        }
      }
    }

    // first data pass, increment data waiters
    for (int i = 0; i < this.maxNports; i++) {
      if (vserialPorts[i] != null) {
        if (vserialPorts[i].bytesWaiting() > 0) {
          // increment wait count
          dataWait[i]++;
        }
      }
    }

    // second pass, look for oldest waiting ports
    int oldest1 = 0;
    int oldest1port = -1;
    int oldestM = 0;
    int oldestMport = -1;

    for (int i = 0; i < this.maxNports; i++) {
      if (vserialPorts[i] != null) {
        if (vserialPorts[i].bytesWaiting() < this.multiReadLimit) {
          if (dataWait[i] > oldest1) {
            oldest1 = dataWait[i];
            oldest1port = i;
          }
        } else {
          if (dataWait[i] > oldestM) {
            oldestM = dataWait[i];
            oldestMport = i;
          }
        }
      }
    }

    if (oldest1port > -1) {
      // if we have a small byte waiter, send serread for it
      dataWait[oldest1port] = 0;
      // add one
      response[0] = (byte) (oldest1port + 1);
      // send data byte
      response[1] = vserialPorts[oldest1port].read1();
    } else if (oldestMport > -1) {
      // send serream for oldest bulk
      dataWait[oldestMport] = 0;
      // add one and 16 for serreadm
      response[0] = (byte) (oldestMport + SERIAL_READ_BASE + 1);
      //send data size
      response[1] = (byte) vserialPorts[oldestMport].bytesWaiting();
    } else {
      // no waiting ports
      response[0] = (byte) 0;
      response[1] = (byte) 0;
    }
    return response;
  }

  /**
   * Write byte array to serial port.
   *
   * @param vport virtual port
   * @param data  byte array
   * @throws DWPortNotOpenException  port not open
   * @throws DWPortNotValidException invalid port
   */
  public void serWriteM(final int vport, final byte[] data)
      throws DWPortNotOpenException, DWPortNotValidException {
    for (final byte datum : data) {
      serWrite(vport, datum);
    }
  }

  /**
   * Write byte array to serial port.
   *
   * @param vport virtual port
   * @param data  byte array
   * @param bread bread
   * @throws DWPortNotOpenException  port not open
   * @throws DWPortNotValidException invalid port
   */
  @SuppressWarnings("unused")
  public void serWriteM(final int vport, final byte[] data, final int bread)
      throws DWPortNotOpenException, DWPortNotValidException {
    for (int i = 0; i < data.length && i < bread; i++) {
      serWrite(vport, data[i]);
    }
  }

  /**
   * Write byte to serial port.
   *
   * @param vport    virtual port
   * @param databyte data byte
   * @throws DWPortNotOpenException  port not open
   * @throws DWPortNotValidException invalid port
   */
  public void serWrite(final int vport, final int databyte)
      throws DWPortNotOpenException, DWPortNotValidException {
    if (vport < this.maxports && vport >= 0) {
      if (vserialPorts[vport] != null) {
        if (vserialPorts[vport].isOpen()) {
          if (bytelog) {
            LOGGER.debug("write to port " + vport + ": "
                + databyte + " (" + (char) databyte + ")");
          }
          // normal write
          vserialPorts[vport].write(databyte);
        } else {
          throw new DWPortNotOpenException(
              "Port " + vport + " is not open (but coco sent us a byte: "
                  + (BYTE_MASK & databyte) + " '" + (char) databyte + "')"
          );
        }
      } else {
        // should port not initialized be different from port not open?
        throw new DWPortNotOpenException(
            "Port " + vport + " is not open (but coco sent us a byte: "
                + (BYTE_MASK & databyte) + " '"
                + (char) databyte + "')"
        );
      }
    } else {
      throw new DWPortNotValidException(vport + " is not a valid port number");
    }
  }

  /**
   * Read data from port.
   *
   * @param vport virtual port
   * @param len   bytes to read
   * @return byte array
   * @throws DWPortNotOpenException  port not open
   * @throws DWPortNotValidException invalid port
   */
  public byte[] serReadM(final int vport, final int len)
      throws DWPortNotOpenException, DWPortNotValidException {
    if (vport < this.maxports && vport >= 0) {
      if (vserialPorts[vport].isOpen()) {
        return vserialPorts[vport].readM(len);
      } else {
        throw new DWPortNotOpenException("Port " + vport + " is not open");
      }
    } else {
      throw new DWPortNotValidException(vport + " is not a valid port number");
    }
  }

  /**
   * Get port input.
   *
   * @param vport virtual port
   * @return port output stream
   * @throws DWPortNotValidException invalid port
   */
  public OutputStream getPortInput(final int vport)
      throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].getPortInput();
  }

  /**
   * Get port output.
   *
   * @param vport virtual port
   * @return port input stream
   * @throws DWPortNotValidException invalid port
   */
  public InputStream getPortOutput(final int vport)
      throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].getPortOutput();
  }

  /**
   * Set virtual port channel.
   *
   * @param vport virtual port
   * @param channel    socket channel
   */
  public void setPortChannel(final int vport, final SocketChannel channel) {
    if (isNull(vport)) {
      LOGGER.debug("attempt to set io channel on null port " + vport);
    } else {
      vserialPorts[vport].setPortChannel(channel);
    }
  }

  /**
   * Mark port as connected.
   *
   * @param vport virtual port
   */
  public void markConnected(final int vport) {
    if (vport < vserialPorts.length && vserialPorts[vport] != null) {
      vserialPorts[vport].setConnected(true);
    } else {
      LOGGER.warn("mark connected on invalid port " + vport);
    }
  }

  /**
   * Mark port as disconnected.
   *
   * @param vport virtual port
   */
  public void markDisconnected(final int vport) {
    if (vport < vserialPorts.length && vserialPorts[vport] != null) {
      vserialPorts[vport].setConnected(false);
    } else {
      LOGGER.warn("mark disconnected on invalid port " + vport);
    }
  }

  /**
   * Is port connected.
   *
   * @param vport virtual port
   * @return port is connected
   */
  public boolean isConnected(final int vport) {
    if (vport < vserialPorts.length && vserialPorts[vport] != null) {
      return vserialPorts[vport].isConnected();
    }
    return false;
  }

  /**
   * Set utility mode.
   *
   * @param vport virtual port
   * @param mode  utility mode
   * @throws DWPortNotValidException invalid port
   */
  public void setUtilMode(final int vport, final int mode)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].setUtilMode(mode);
  }

  /**
   * Write data byte to port.
   *
   * @param vport virtual port
   * @param data  data byte
   * @throws IOException             write failure
   * @throws DWPortNotValidException invalid port
   */
  @SuppressWarnings("unused")
  public void write1(final int vport, final byte data) throws IOException,
      DWPortNotValidException {
    validateport(vport);
    getPortInput(vport).write(data);
  }

  /**
   * Write string to port.
   *
   * @param vport virtual port
   * @param str   string message
   * @throws DWPortNotValidException invalid port
   */
  public void write(final int vport, final String str)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].writeM(str);
  }

  /**
   * Set PD.INT.
   *
   * @param vport virtual port
   * @param pdInt PD.INT
   * @throws DWPortNotValidException invalid port
   */
  public void setPdInt(final int vport, final byte pdInt)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].setPdInt(pdInt);
  }

  /**
   * Get PD.INT.
   *
   * @param vport virtual port
   * @return PD.INT
   * @throws DWPortNotValidException invalid port
   */
  public byte getPdInt(final int vport) throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].getPdInt();
  }

  /**
   * Set PD.QUT.
   *
   * @param vport virtual port
   * @param pdQut PD.QUT
   * @throws DWPortNotValidException invalid port
   */
  public void setPdQut(final int vport, final byte pdQut)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].setPdQut(pdQut);
  }

  /**
   * Get PD.QUT.
   *
   * @param vport virtual port
   * @return PD.QUT
   * @throws DWPortNotValidException invalid port
   */
  public byte getPdQut(final int vport) throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].getPdQut();
  }

  /**
   * Send utility fail response.
   *
   * @param vport virtual port
   * @param code  response code
   * @param txt   fail message
   * @throws DWPortNotValidException invalid port
   */
  public void sendUtilityFailResponse(final int vport,
                                      final byte code,
                                      final String txt)
      throws DWPortNotValidException {
    validateport(vport);
    LOGGER.info("API FAIL: port " + vport + " code " + code + ": " + txt);
    vserialPorts[vport].sendUtilityFailResponse(code, txt);
  }

  /**
   * Send utility OK response message.
   *
   * @param vport virtual port
   * @param txt   message text
   * @throws DWPortNotValidException invalid port
   */
  public void sendUtilityOKResponse(final int vport, final String txt)
      throws DWPortNotValidException {
    validateport(vport);
    LOGGER.debug("API OK: port " + vport + ": command successful");
    vserialPorts[vport].sendUtilityOKResponse("command successful");
    vserialPorts[vport].writeToCoco(txt);
  }

  /**
   * Send utility OK response data.
   *
   * @param vport         virtual port
   * @param responseBytes ok response
   * @throws DWPortNotValidException invalid port
   */
  public void sendUtilityOKResponse(final int vport,
                                    final byte[] responseBytes)
      throws DWPortNotValidException {
    validateport(vport);
    LOGGER.debug("API OK: port " + vport + ": command successful (byte mode)");
    vserialPorts[vport].sendUtilityOKResponse("command successful");
    vserialPorts[vport].writeToCoco(responseBytes);
  }

  /**
   * Get count of bytes waiting on port.
   *
   * @param vport virtual port
   * @return bytes waiting
   * @throws DWPortNotValidException invalid port
   */
  public int bytesWaiting(final int vport) throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].bytesWaiting();
  }

  /**
   * Set port DD.
   *
   * @param vport    virtual port
   * @param devdescr device description
   * @throws DWPortNotValidException invalid port
   */
  public void setDD(final byte vport, final byte[] devdescr)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].setDD(devdescr);
  }

  /**
   * Reset all virtual ports.
   */
  public void resetAllPorts() {
    LOGGER.debug(
        "Resetting all virtual serial ports - part 1, close all sockets"
    );

    for (int i = 0; i < this.maxports; i++) {
      this.listenerpool.closePortConnectionSockets(i);
      this.listenerpool.closePortServerSockets(i);
    }

    LOGGER.debug("Resetting all virtual serial ports - part 2, init all ports");

    //vserialPorts = new DWVSerialPort[MAX_PORTS];
    for (int i = 0; i < this.maxports; i++) {
      // dont reset term
      if (i != this.nTermPort) {
        try {
          resetPort(i);
        } catch (DWPortNotValidException e) {
          LOGGER.warn(e.getMessage());
        }
      }
    }
    // if term is null, init
    if (this.nTermPort > -1 && this.vserialPorts[this.nTermPort] == null) {
      try {
        resetPort(this.nTermPort);
      } catch (DWPortNotValidException e) {
        LOGGER.warn(e.getMessage());
      }
    }
  }

  /**
   * Reset port.
   *
   * @param vport virtual port
   * @throws DWPortNotValidException invalid port
   */
  public void resetPort(final int vport) throws DWPortNotValidException {
    if (vport >= 0 && vport < vserialPorts.length) {
      vserialPorts[vport] = new DWVSerialPort(this, this.dwProto, vport);
    } else {
      throw new DWPortNotValidException("Invalid port # " + vport);
    }
  }

  /**
   * Is port open.
   *
   * @param vport virtual port
   * @return port open state
   */
  public boolean isOpen(final int vport) {
    if (!isNull(vport)) {
      return vserialPorts[vport].isOpen();
    }
    return false;
  }

  /**
   * Get port opens.
   *
   * @param vport virtual port
   * @return opens
   * @throws DWPortNotValidException
   */
  public int getOpen(final int vport) throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].getOpen();
  }

  /**
   * Get DD from port.
   *
   * @param vport virtual port
   * @return DD byte array
   * @throws DWPortNotValidException invalid port
   */
  public byte[] getDD(final int vport) throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].getDD();
  }

  /**
   * Wrie to CoCo.
   *
   * @param vport    virtual port
   * @param databyte data byte
   * @throws DWPortNotValidException invalid port
   */
  public void writeToCoco(final int vport,
                          final byte databyte) throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].writeToCoco(databyte);
  }

  /**
   * Write to CoCo.
   *
   * @param vport virtual port
   * @param str   message
   * @throws DWPortNotValidException invalid port
   */
  public void writeToCoco(final int vport,
                          final String str) throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].writeToCoco(str);
  }

  /**
   * Write to CoCo.
   *
   * @param vport virtual port
   * @param bytes     byte data array
   * @throws DWPortNotValidException invalid port
   */
  @SuppressWarnings("unused")
  public void writeToCoco(final int vport, final byte[] bytes)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].writeToCoco(bytes);
  }

  /**
   * Write to CoCo.
   *
   * @param vport  virtual port
   * @param bytes      byte data array
   * @param offset data offset
   * @param length bytes to send
   * @throws DWPortNotValidException invalid port
   */
  public void writeToCoco(final int vport,
                          final byte[] bytes,
                          final int offset,
                          final int length) throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].writeToCoco(bytes, offset, length);
  }

  /**
   * Is port null.
   *
   * @param vport virtual port
   * @return is null
   */
  public boolean isNull(final int vport) {
    return vport >= 0
        && vport < vserialPorts.length
        && vserialPorts[vport] == null;
  }

  /**
   * Is port valid.
   *
   * @param vport virtual port
   * @return port valid
   */
  public boolean isValid(final int vport) {
    return vport >= 0 && vport < this.maxports;
  }

  private void validateport(final int vport) throws DWPortNotValidException {
    if (!isValid(vport)) {
      throw new DWPortNotValidException("Invalid port #" + vport);
    }
    if (isNull(vport)) {
      throw new DWPortNotValidException("Null port #" + vport);
    }
  }

  /**
   * Send connection announcement.
   *
   * @param vport     virtual port
   * @param conno     connection number
   * @param localport local port
   * @param hostaddr  host address
   * @throws DWPortNotValidException invalid port
   */
  public void sendConnectionAnnouncement(final int vport,
                                         final int conno,
                                         final int localport,
                                         final String hostaddr)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].sendConnectionAnnouncement(conno, localport, hostaddr);
  }

  /**
   * Set connection.
   *
   * @param vport virtual port
   * @param conno connection number
   * @throws DWPortNotValidException invalid port
   */
  public void setConn(final int vport, final int conno)
      throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].setConn(conno);
  }

  /**
   * Get connection.
   *
   * @param vport virtual port
   * @return connection
   * @throws DWPortNotValidException invalid port
   */
  public int getConn(final int vport) throws DWPortNotValidException {
    validateport(vport);
    return vserialPorts[vport].getConn();
  }

  /**
   * Get Host IP Address.
   *
   * @param vport virtual port
   * @return ip address
   * @throws DWPortNotValidException       invalid port
   * @throws DWConnectionNotValidException invalid connection
   */
  public String getHostIP(final int vport)
      throws DWPortNotValidException, DWConnectionNotValidException {
    validateport(vport);
    return this.listenerpool
        .getConn(vserialPorts[vport].getConn())
        .socket()
        .getInetAddress()
        .getHostAddress();
  }

  /**
   * Get host port.
   *
   * @param vport virtual port
   * @return host port
   * @throws DWPortNotValidException       invalid port
   * @throws DWConnectionNotValidException invalid connection
   */
  public int getHostPort(final int vport)
      throws DWPortNotValidException, DWConnectionNotValidException {
    validateport(vport);
    return this.listenerpool
        .getConn(vserialPorts[vport].getConn())
        .socket()
        .getPort();
  }

  /**
   * Shutdown.
   */
  public void shutdown() {
    LOGGER.debug("shutting down");
    for (int i = 0; i < this.maxports; i++) {
      this.listenerpool.closePortConnectionSockets(i);
      this.listenerpool.closePortServerSockets(i);
      if (this.vserialPorts[i] != null) {
        this.vserialPorts[i].shutdown();
      }
    }
  }

  /**
   * Get MIDI device info.
   *
   * @return MIDI device info
   */
  public MidiDevice.Info getMidiDeviceInfo() {
    if (this.midiDevice != null) {
      return this.midiDevice.getDeviceInfo();
    }
    return null;
  }

  /**
   * Set MIDI device.
   *
   * @param device MIDI device
   * @throws MidiUnavailableException midi not available
   * @throws IllegalArgumentException invalid argument
   */
  public void setMIDIDevice(final MidiDevice device)
      throws MidiUnavailableException, IllegalArgumentException {
    if (this.midiDevice != null) {
      if (this.midiDevice.isOpen()) {
        LOGGER.debug("midi: closing "
            + this.midiDevice.getDeviceInfo().getName());
        this.midiDevice.close();
      }
    }
    device.open();
    this.midiDevice = device;
    DriveWireServer.submitMIDIEvent(
        this.dwProto.getHandlerNo(),
        "device",
        this.midiDevice.getDeviceInfo().getName()
    );
    LOGGER.info("midi: opened " + this.midiDevice.getDeviceInfo().getName());
  }

  /**
   * Send MIDI message.
   *
   * @param mmsg      MIDI message
   * @param timestamp timestamp
   */
  public void sendMIDIMsg(final ShortMessage mmsg, final int timestamp) {
    if (this.midiDevice != null) {
      try {
        this.midiDevice.getReceiver().send(mmsg, timestamp);
      } catch (MidiUnavailableException | IllegalStateException e) {
        LOGGER.warn(e.getMessage());
      }
    } else {
      LOGGER.warn("No MIDI device for MIDI msg");
    }
  }

  /**
   * Get Midi receiver.
   *
   * @return receiver
   * @throws MidiUnavailableException midi not available
   */
  @SuppressWarnings("unused")
  public Receiver getMidiReceiver() throws MidiUnavailableException {
    return this.midiDevice.getReceiver();
  }

  /**
   * Get Midi synth.
   *
   * @return midi synth
   */
  public Synthesizer getMidiSynth() {
    return midiSynth;
  }

  /**
   * Is soundbank supported.
   *
   * @param soundbank soundbank
   * @return soundbank supported
   */
  public boolean isSoundbankSupported(final Soundbank soundbank) {
    return midiSynth.isSoundbankSupported(soundbank);
  }

  /**
   * Set Midi soundbank filename.
   *
   * @param soundbank soundbank
   * @param fname     fllename
   * @return success
   */
  public boolean setupMidiSoundbank(final Soundbank soundbank,
                                    final String fname) {
    if (midiSynth.loadAllInstruments(soundbank)) {
      LOGGER.debug("loaded soundbank file '" + fname + "'");
      this.soundbankfilename = fname;
      return true;
    }
    return false;
  }

  /**
   * Get Midi soundbank filename.
   *
   * @return soundbank filename
   */
  public String getMidiSoundbankFilename() {
    return this.soundbankfilename;
  }

  /**
   * Get Midi voice lock.
   *
   * @return midi voice lock
   */
  public boolean isMidiVoicelock() {
    return this.midiVoicelock;
  }

  /**
   * Set MIDI voice lock.
   *
   * @param lock bool
   */
  public void setMidiVoicelock(final boolean lock) {
    this.midiVoicelock = lock;
    DriveWireServer.submitMIDIEvent(
        this.dwProto.getHandlerNo(), "voicelock", String.valueOf(lock)
    );
    LOGGER.debug("MIDI: synth voicelock = " + lock);
  }

  private void loadSoundbank(final String filename) {
    Soundbank soundbank;
    final File file = new File(filename);
    try {
      soundbank = MidiSystem.getSoundbank(file);
    } catch (InvalidMidiDataException | IOException e) {
      LOGGER.warn("Error loading soundbank: " + e.getMessage());
      return;
    }
    if (isSoundbankSupported(soundbank)) {
      if (!setupMidiSoundbank(soundbank, filename)) {
        LOGGER.warn("Failed to set soundbank '" + filename + "'");
        return;
      }
      DriveWireServer.submitMIDIEvent(
          this.dwProto.getHandlerNo(), "soundbank", filename
      );
    } else {
      LOGGER.warn("Unsupported soundbank '" + filename + "'");
    }
  }

  /**
   * Get midi profile name.
   *
   * @return profile name
   */
  public String getMidiProfileName() {
    if (this.midiProfConf != null) {
      return this.midiProfConf.getString("[@name]", "none");
    } else {
      return "none";
    }
  }

  /**
   * Get Midi profile.
   *
   * @return profile
   */
  @SuppressWarnings("unused")
  public HierarchicalConfiguration getMidiProfile() {
    return this.midiProfConf;
  }

  /**
   * Set midi profile.
   *
   * @param profile profile name
   * @return success
   */
  @SuppressWarnings("unchecked")
  public boolean setupMidiProfile(final String profile) {
    final List<HierarchicalConfiguration> profiles =
        DriveWireServer.getServerConfiguration().configurationsAt(
            "midisynthprofile");
    for (final HierarchicalConfiguration mprof : profiles) {
      if (mprof.containsKey("[@name]")
          && mprof.getString("[@name]").equalsIgnoreCase(profile)) {
        this.midiProfConf = (HierarchicalConfiguration) mprof.clone();
        doMidiTranslateCurrentVoices();
        DriveWireServer.submitMIDIEvent(
            this.dwProto.getHandlerNo(), "profile", profile
        );
        LOGGER.debug("MIDI: set profile to '" + profile + "'");
        return true;
      }
    }
    return false;
  }

  private void doMidiTranslateCurrentVoices() {
    // translate current GM voices to current profile
    if (this.midiSynth != null) {
      final MidiChannel[] chans = this.midiSynth.getChannels();
      for (int i = 0; i < chans.length; i++) {
        if (chans[i] != null) {
          chans[i].programChange(getGMInstrument(this.gmInstrumentCache[i]));
        }
      }
    }
  }

  /**
   * Get GM Instrument.
   *
   * @param voice voice
   * @return translated voice
   */
  @SuppressWarnings("unchecked")
  public int getGMInstrument(final int voice) {
    if (this.midiProfConf == null) {
      return voice;
    }
    int xvoice;
    final List<HierarchicalConfiguration> mappings
        = this.midiProfConf.configurationsAt("mapping");
    for (final HierarchicalConfiguration sub : mappings) {
      if (sub.getInt("[@dev]"
          + this.midiProfConf.getInt("[@dev_adjust]", 0)) == voice) {
        xvoice = sub.getInt("[@gm]")
            + this.midiProfConf.getInt("[@gm_adjust]", 0);
        LOGGER.debug("MIDI: profile '"
            + this.midiProfConf.getString("[@name]")
            + "' translates device inst " + voice + " to GM instr "
            + xvoice);
        return xvoice;
      }
    }
    // no translation match
    return voice;
  }

  /**
   * Set Midi Instrument.
   *
   * @param channel channel
   * @param instr   instrument
   * @return flag
   */
  public boolean setupMIDIInstr(final int channel, final int instr) {
    final MidiChannel[] chans = this.midiSynth.getChannels();
    if (channel < chans.length) {
      if (chans[channel] != null) {
        chans[channel].programChange(instr);
        LOGGER.debug("MIDI: set instrument "
            + instr + " on channel " + channel);
        return true;
      }
    }
    return false;
  }

  /**
   * Clear GM instrument cache on all channels.
   */
  public void clearGMInstrumentCache() {
    this.gmInstrumentCache = new int[MAX_CACHE];
    for (int i = 0; i < MAX_CACHE; i++) {
      this.gmInstrumentCache[i] = 0;
    }
  }

  /**
   * Set GM Instrument cache.
   *
   * @param chan  channel
   * @param instr cache
   */
  public void setGMInstrumentCache(final int chan, final int instr) {
    if (chan >= 0 && chan < this.gmInstrumentCache.length) {
      this.gmInstrumentCache[chan] = instr;
    } else {
      LOGGER.debug("MIDI: channel out of range on program change: " + chan);
    }
  }

  /**
   * Get GM instrument cache.
   *
   * @param chan channel
   * @return channel cache
   */
  public int getGMInstrumentCache(final int chan) {
    return this.gmInstrumentCache[chan];
  }

  /**
   * Get port listener pool.
   *
   * @return listener pool
   */
  public DWVPortListenerPool getListenerPool() {
    return this.listenerpool;
  }

  /**
   * Get utility mode on port.
   *
   * @param index port index
   * @return utility mode
   * @throws DWPortNotValidException invalid port
   */
  public int getUtilMode(final int index) throws DWPortNotValidException {
    validateport(index);
    return this.vserialPorts[index].getUtilMode();
  }

  /**
   * Get total maximum ports.
   *
   * @return max ports
   */
  public int getMaxPorts() {
    return this.maxports;
  }

  /**
   * Get Max N ports.
   *
   * @return max ports
   */
  public int getMaxNPorts() {
    return this.maxNports;
  }

  /**
   * Get max Z ports.
   *
   * @return max ports
   */
  public int getMaxZPorts() {
    return this.maxZports;
  }

  /**
   * Get NTerm port.
   *
   * @return port
   */
  public int getNTermPort() {
    return this.nTermPort;
  }

  /**
   * Get ZTerm port.
   *
   * @return port
   */
  @SuppressWarnings("unused")
  public int getZTermPort() {
    return this.zTermPort;
  }

  /**
   * Get MIDI port.
   *
   * @return port
   */
  public int getMIDIPort() {
    return this.midiPort;
  }

  /**
   * Get Port Virtual Modem.
   *
   * @param port port
   * @return modem
   * @throws DWPortNotValidException
   */
  public DWVModem getPortVModem(final int port)
      throws DWPortNotValidException {
    validateport(port);
    return this.vserialPorts[port].getVModem();
  }

  /**
   * Is reboot request flagged.
   *
   * @return reboot flag
   */
  public boolean isRebootRequested() {
    return rebootRequested;
  }

  /**
   * Ser reboot reauested flag.
   *
   * @param requestFlag reboot flag
   */
  public void setRebootRequested(final boolean requestFlag) {
    this.rebootRequested = requestFlag;
  }
}
