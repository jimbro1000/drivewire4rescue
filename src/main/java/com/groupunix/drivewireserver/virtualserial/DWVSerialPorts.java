package com.groupunix.drivewireserver.virtualserial;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
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

public class DWVSerialPorts {

  public static final int MODE_TERM = 3;
  private static final Logger logger = Logger.getLogger("DWServer.DWVSerialPorts");
  public static final int MAX_CACHE = 16;
  private DWVSerialProtocol dwProto;
  private boolean bytelog = false;


  private DWVSerialPort[] vserialPorts;
  private DWVPortListenerPool listenerpool = new DWVPortListenerPool();

  private int[] dataWait;

  // midi stuff
  private MidiDevice midiDevice;
  private Synthesizer midiSynth;
  private String soundbankfilename = null;
  private boolean midiVoicelock = false;
  private HierarchicalConfiguration midiProfConf = null;
  private int[] GMInstrumentCache;
  private int maxNports = 0;
  private int maxZports = 0;
  private int maxports = 0;
  private int nTermPort = 0;
  private int zTermPort = 0;
  private int MIDIPort = 0;
  private int multiReadLimit = 0;
  private boolean rebootRequested = false;

  public DWVSerialPorts(DWVSerialProtocol dwProto) {
    this.dwProto = dwProto;
    bytelog = dwProto.getConfig().getBoolean("LogVPortBytes", false);

    maxNports = dwProto.getConfig().getInt("VSerial_MaxNDevPorts", 16);
    maxZports = dwProto.getConfig().getInt("VSerial_MaxZDevPorts", 16);
    nTermPort = dwProto.getConfig().getInt("VSerial_NTermPort", 0);
    zTermPort = dwProto.getConfig().getInt("VSerial_ZTermPort", 16);
    MIDIPort = dwProto.getConfig().getInt("VSerial_MIDIPort", 14);
    this.multiReadLimit = dwProto.getConfig().getInt("VSerial_MultiReadLimit", 3);

    maxports = maxNports + maxZports;

    dataWait = new int[maxports];
    vserialPorts = new DWVSerialPort[maxports];


    if (dwProto.getConfig().getBoolean("UseMIDI", false) && !DriveWireServer.getNoMIDI()) {

      clearGMInstrumentCache();

      try {

        // set default output
        if (dwProto.getConfig().containsKey("MIDIDefaultOutput")) {
          int devno = dwProto.getConfig().getInt("MIDIDefaultOutput", -1);

          MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

          if ((devno < 0) || (devno > infos.length)) {
            logger.warn("Invalid MIDI output device # " + devno + " specified in MIDIDefaultOutput setting");
          } else {
            setMIDIDevice(MidiSystem.getMidiDevice(infos[devno]));
          }

        } else {
          midiSynth = MidiSystem.getSynthesizer();
          setMIDIDevice(midiSynth);
        }

        // soundbank
        if (dwProto.getConfig().containsKey("MIDISynthDefaultSoundbank")) {
          loadSoundbank(dwProto.getConfig().getString("MIDISynthDefaultSoundbank"));
        }


        // default translation profile
        if (dwProto.getConfig().containsKey("MIDISynthDefaultProfile")) {
          if (!setMidiProfile(dwProto.getConfig().getString("MIDISynthDefaultProfile"))) {
            logger.warn("Invalid MIDI profile specified in config file.");
          }
        }

      } catch (MidiUnavailableException e) {
        logger.warn("MIDI is not available");
      }
    }
  }


  public void openPort(int port) throws DWPortNotValidException {
    this.validateport(port);
    if (vserialPorts[port] == null) {
      resetPort(port);
    }

    vserialPorts[port].open();
  }


  public String prettyPort(int port) {
    if (port == this.nTermPort) {
      return ("NTerm");
    } else if (port == this.zTermPort) {
      return ("ZTerm");
    } else if (port == this.MIDIPort) {
      return ("MIDI");
    } else if (port < this.maxNports) {
      return ("N" + port);
    } else if (port < this.maxNports + this.maxZports) {
      return ("Z" + (port - this.maxNports));
    } else {
      return ("?" + port);
    }
  }


  public void closePort(int port) throws DWPortNotValidException {
    if (port < vserialPorts.length) {
      if (vserialPorts[port] != null) {
        vserialPorts[port].close();
        //vserialPorts[port] = null;
      }
    } else {
      throw new DWPortNotValidException("Valid port range is 0 - " + (vserialPorts.length - 1));
    }
  }


  public byte[] serRead() {
    byte[] response = new byte[2];

    // reboot req takes absolute priority

    if (this.isRebootRequested()) {
      response[0] = (byte) 16;
      response[1] = (byte) 255;

      logger.debug("reboot request pending, sending response " + response[0] + "," + response[1]);

      this.setRebootRequested(false);
      return (response);
    }


    // Z devices go first...

    for (int i = this.maxNports; i < this.maxNports + this.maxZports; i++) {
      if (vserialPorts[i] != null) {
        if (vserialPorts[i].bytesWaiting() > 0) {
          // increment wait count
          dataWait[i]++;

          logger.debug("waiting Z " + i + ": " + vserialPorts[i].bytesWaiting());
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
      response[0] = (byte) ((DWDefs.POLL_RESP_MODE_WINDOW << 6) + (oldestZport) - this.maxNports);
      response[1] = vserialPorts[oldestZport].read1();

      logger.debug("Z poll response " + response[0] + "," + response[1]);

      return (response);
    }


    // N devices


    // first look for termed ports
    for (int i = 0; i < this.maxNports; i++) {
      if (vserialPorts[i] != null) {
        if (vserialPorts[i].isTerm()) {
          response[0] = (byte) 16;  // port status
          response[1] = (byte) i;   // 000 portnumber

          logger.debug("sending terminated status to coco for port " + i);

          vserialPorts[i] = new DWVSerialPort(this, this.dwProto, i);

          return (response);
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
      response[0] = (byte) (oldest1port + 1);     // add one
      response[1] = vserialPorts[oldest1port].read1();  // send data byte
    } else if (oldestMport > -1) {
      // send serream for oldest bulk

      dataWait[oldestMport] = 0;
      response[0] = (byte) (oldestMport + 16 + 1);     // add one and 16 for serreadm
      response[1] = (byte) vserialPorts[oldestMport].bytesWaiting(); //send data size
      // logger.debug("SERREADM RESPONSE: " + Integer.toBinaryString(response[0]) + " " + Integer.toBinaryString(response[1]));

    } else {
      // no waiting ports

      response[0] = (byte) 0;
      response[1] = (byte) 0;
    }

    // logger.debug("SERREAD RESPONSE: " + Integer.toBinaryString(response[0]) + " " + Integer.toBinaryString(response[1]));

    return (response);
  }


  public void serWriteM(int port, byte[] data) throws DWPortNotOpenException, DWPortNotValidException {
    for (int i = 0; i < data.length; i++) {
      // inefficient as hell, but serwriteM isn't even implemented in driver anyway
      serWrite(port, data[i]);
    }
  }

  public void serWriteM(int port, byte[] data, int bread) throws DWPortNotOpenException, DWPortNotValidException {
    for (int i = 0; (i < data.length) && (i < bread); i++) {
      // inefficient as hell, but serwriteM isn't even implemented in driver anyway
      serWrite(port, data[i]);
    }
  }


  public void serWrite(int port, int databyte) throws DWPortNotOpenException, DWPortNotValidException {


    if ((port < this.maxports) && (port >= 0)) {
      if (vserialPorts[port] != null) {
        if (vserialPorts[port].isOpen()) {
          if (bytelog) {

            logger.debug("write to port " + port + ": " + databyte + " (" + (char) databyte + ")");
          }

          // normal write
          vserialPorts[port].write(databyte);
        } else {
          throw new DWPortNotOpenException("Port " + port + " is not open (but coco sent us a byte: " + (0xff & databyte) + " '" + Character.toString((char) databyte) + "')");
        }
      } else {
        // should port not initialized be different than port not open?
        throw new DWPortNotOpenException("Port " + port + " is not open (but coco sent us a byte: " + (0xff & databyte) + " '" + Character.toString((char) databyte) + "')");
      }
    } else {
      throw new DWPortNotValidException(port + " is not a valid port number");
    }

  }


  public byte[] serReadM(int port, int len) throws DWPortNotOpenException, DWPortNotValidException {


    if ((port < this.maxports) && (port >= 0)) {
      if (vserialPorts[port].isOpen()) {
        byte[] data = new byte[len];
        data = vserialPorts[port].readM(len);
        return (data);
      } else {
        throw new DWPortNotOpenException("Port " + port + " is not open");
      }
    } else {
      throw new DWPortNotValidException(port + " is not a valid port number");
    }

  }


  public OutputStream getPortInput(int vport) throws DWPortNotValidException {
    validateport(vport);
    return (vserialPorts[vport].getPortInput());
  }

  public InputStream getPortOutput(int vport) throws DWPortNotValidException {
    validateport(vport);
    return (vserialPorts[vport].getPortOutput());
  }
	
	
	/*
	
	public InputStream getPortOutput(int vport) throws DWPortNotValidException 
	{
		validateport(vport);
		return (vserialPorts[vport].getPortOutput());
	}
	
	public void setPortOutput(int vport, OutputStream output)
	{
		if (isNull(vport))
		{
			logger.debug("attempt to set output on null port " + vport);
		}
		else
		{
			vserialPorts[vport].setPortOutput(output);
		}
	}
	*/

  public void setPortChannel(int vport, SocketChannel sc) {
    if (isNull(vport)) {
      logger.debug("attempt to set io channel on null port " + vport);
    } else {
      vserialPorts[vport].setPortChannel(sc);
    }
  }


  public void markConnected(int port) {
    if ((port < vserialPorts.length) && (vserialPorts[port] != null)) {
      vserialPorts[port].setConnected(true);
    } else {
      logger.warn("mark connected on invalid port " + port);
    }
  }


  public void markDisconnected(int port) {
    if ((port < vserialPorts.length) && (vserialPorts[port] != null)) {
      vserialPorts[port].setConnected(false);
    } else {
      logger.warn("mark disconnected on invalid port " + port);
    }
  }


  public boolean isConnected(int port) {
    if ((port < vserialPorts.length) && (vserialPorts[port] != null)) {
      return (vserialPorts[port].isConnected());
    }
    return (false);
  }


  public void setUtilMode(int port, int mode) throws DWPortNotValidException {
    validateport(port);
    vserialPorts[port].setUtilMode(mode);
  }


  public void wr3ite1(int port, byte data) throws IOException, DWPortNotValidException {
    validateport(port);
    getPortInput(port).write(data);
  }

  public void write(int port, String str) throws DWPortNotValidException {
    validateport(port);
    vserialPorts[port].writeM(str);

  }


  public void setPD_INT(int port, byte pD_INT) throws DWPortNotValidException {
    validateport(port);
    vserialPorts[port].setPdInt(pD_INT);
  }


  public byte getPD_INT(int port) throws DWPortNotValidException {
    validateport(port);
    return (vserialPorts[port].getPdInt());
  }


  public void setPD_QUT(int port, byte pD_QUT) throws DWPortNotValidException {
    validateport(port);
    vserialPorts[port].setPdQut(pD_QUT);
  }


  public byte getPD_QUT(int port) throws DWPortNotValidException {
    validateport(port);
    return (vserialPorts[port].getPdQut());
  }


  public void sendUtilityFailResponse(int vport, byte code, String txt) throws DWPortNotValidException {
    validateport(vport);
    logger.info("API FAIL: port " + vport + " code " + code + ": " + txt);
    vserialPorts[vport].sendUtilityFailResponse(code, txt);
  }


  public void sendUtilityOKResponse(int vport, String txt) throws DWPortNotValidException {
    validateport(vport);
    logger.debug("API OK: port " + vport + ": command successful");
    vserialPorts[vport].sendUtilityOKResponse("command successful");
    vserialPorts[vport].writeToCoco(txt);
  }


  public void sendUtilityOKResponse(int vport, byte[] responseBytes) throws DWPortNotValidException {
    validateport(vport);
    logger.debug("API OK: port " + vport + ": command successful (byte mode)");
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
    return (vserialPorts[vport].bytesWaiting());
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
    logger.debug("Resetting all virtual serial ports - part 1, close all sockets");

    for (int i = 0; i < this.maxports; i++) {
      this.listenerpool.closePortConnectionSockets(i);
      this.listenerpool.closePortServerSockets(i);
    }

    logger.debug("Resetting all virtual serial ports - part 2, init all ports");

    //vserialPorts = new DWVSerialPort[MAX_PORTS];
    for (int i = 0; i < this.maxports; i++) {
      // dont reset term
      if (i != this.nTermPort) {
        try {
          resetPort(i);
        } catch (DWPortNotValidException e) {
          logger.warn(e.getMessage());
        }
      }
    }
    // if term is null, init
    if ((this.nTermPort > -1) && (this.vserialPorts[this.nTermPort] == null)) {
      try {
        resetPort(this.nTermPort);
      } catch (DWPortNotValidException e) {
        logger.warn(e.getMessage());
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
    if ((vport >= 0) && (vport < vserialPorts.length)) {
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
    return (vserialPorts[vport].getOpen());
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
   * @param b     byte data array
   * @throws DWPortNotValidException invalid port
   */
  @SuppressWarnings("unused")
  public void writeToCoco(int vport, byte[] b) throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].writeToCoco(b);
  }

  /**
   * Write to CoCo.
   *
   * @param vport  virtual port
   * @param b      byte data array
   * @param offset data offset
   * @param length bytes to send
   * @throws DWPortNotValidException invalid port
   */
  public void writeToCoco(final int vport,
                          final byte[] b,
                          final int offset,
                          final int length) throws DWPortNotValidException {
    validateport(vport);
    vserialPorts[vport].writeToCoco(b, offset, length);
  }

  /**
   * Is port null.
   *
   * @param vport virtual port
   * @return is null
   */
  public boolean isNull(final int vport) {
    return (vport >= 0)
        && (vport < vserialPorts.length)
        && (vserialPorts[vport] == null);
  }

  /**
   * Is port valid.
   *
   * @param vport virtual port
   * @return port valid
   */
  public boolean isValid(final int vport) {
    return (vport >= 0) && (vport < this.maxports);
  }

  private void validateport(final int vport) throws DWPortNotValidException {
    if (!isValid(vport)) {
      throw (new DWPortNotValidException("Invalid port #" + vport));
    }
    if (isNull(vport)) {
      throw (new DWPortNotValidException("Null port #" + vport));
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
    logger.debug("shutting down");
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
        logger.debug("midi: closing " + this.midiDevice.getDeviceInfo().getName());
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
    logger.info("midi: opened " + this.midiDevice.getDeviceInfo().getName());
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
        logger.warn(e.getMessage());
      }
    } else {
      logger.warn("No MIDI device for MIDI msg");
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
    return (this.midiDevice.getReceiver());
  }

  /**
   * Get Midi synth.
   *
   * @return midi synth
   */
  public Synthesizer getMidiSynth() {
    return (midiSynth);
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
  public boolean setMidiSoundbank(final Soundbank soundbank,
                                  final String fname) {
    if (midiSynth.loadAllInstruments(soundbank)) {
      logger.debug("loaded soundbank file '" + fname + "'");
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
  public boolean getMidiVoicelock() {
    return this.midiVoicelock;
  }

  public void setMidiVoicelock(final boolean lock) {
    this.midiVoicelock = lock;
    DriveWireServer.submitMIDIEvent(
        this.dwProto.getHandlerNo(), "voicelock", String.valueOf(lock)
    );
    logger.debug("MIDI: synth voicelock = " + lock);
  }

  private void loadSoundbank(final String filename) {
    Soundbank soundbank;
    File file = new File(filename);
    try {
      soundbank = MidiSystem.getSoundbank(file);
    } catch (InvalidMidiDataException | IOException e) {
      logger.warn("Error loading soundbank: " + e.getMessage());
      return;
    }
    if (isSoundbankSupported(soundbank)) {
      if (!setMidiSoundbank(soundbank, filename)) {
        logger.warn("Failed to set soundbank '" + filename + "'");
        return;
      }
      DriveWireServer.submitMIDIEvent(
          this.dwProto.getHandlerNo(), "soundbank", filename
      );
    } else {
      logger.warn("Unsupported soundbank '" + filename + "'");
      return;
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
  public boolean setMidiProfile(final String profile) {
    List<HierarchicalConfiguration> profiles =
        DriveWireServer.getServerConfiguration().configurationsAt(
            "midisynthprofile");
    for (HierarchicalConfiguration mprof : profiles) {
      if (mprof.containsKey("[@name]")
          && mprof.getString("[@name]").equalsIgnoreCase(profile)) {
        this.midiProfConf = (HierarchicalConfiguration) mprof.clone();
        doMidiTranslateCurrentVoices();
        DriveWireServer.submitMIDIEvent(
            this.dwProto.getHandlerNo(), "profile", profile
        );
        logger.debug("MIDI: set profile to '" + profile + "'");
        return true;
      }
    }
    return false;
  }

  private void doMidiTranslateCurrentVoices() {
    // translate current GM voices to current profile
    if (this.midiSynth != null) {
      MidiChannel[] chans = this.midiSynth.getChannels();
      for (int i = 0; i < chans.length; i++) {
        if (chans[i] != null) {
          chans[i].programChange(getGMInstrument(this.GMInstrumentCache[i]));
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
    List<HierarchicalConfiguration> mappings
        = this.midiProfConf.configurationsAt("mapping");
    for (HierarchicalConfiguration sub : mappings) {
      if ((sub.getInt("[@dev]")
          + this.midiProfConf.getInt("[@dev_adjust]", 0)) == voice) {
        xvoice = sub.getInt("[@gm]")
            + this.midiProfConf.getInt("[@gm_adjust]", 0);
        logger.debug("MIDI: profile '"
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
  public boolean setMIDIInstr(final int channel, final int instr) {
    MidiChannel[] chans = this.midiSynth.getChannels();
    if (channel < chans.length) {
      if (chans[channel] != null) {
        chans[channel].programChange(instr);
        logger.debug("MIDI: set instrument " + instr + " on channel " + channel);
        return true;
      }
    }
    return false;
  }

  /**
   * Clear GM instrument cache on all channels.
   */
  public void clearGMInstrumentCache() {
    this.GMInstrumentCache = new int[MAX_CACHE];
    for (int i = 0; i < MAX_CACHE; i++) {
      this.GMInstrumentCache[i] = 0;
    }
  }

  /**
   * Set GM Instrument cache.
   *
   * @param chan  channel
   * @param instr cache
   */
  public void setGMInstrumentCache(final int chan, final int instr) {
    if ((chan >= 0) && (chan < this.GMInstrumentCache.length)) {
      this.GMInstrumentCache[chan] = instr;
    } else {
      logger.debug("MIDI: channel out of range on program change: " + chan);
    }
  }

  /**
   * Get GM instrument cache.
   *
   * @param chan channel
   * @return channel cache
   */
  public int getGMInstrumentCache(final int chan) {
    return this.GMInstrumentCache[chan];
  }

  /**
   * Get port listener pool
   *
   * @return listener pool
   */
  public DWVPortListenerPool getListenerPool() {
    return this.listenerpool;
  }

  /**
   * Get utility mode on port.
   *
   * @param i port index
   * @return utility mode
   * @throws DWPortNotValidException invalid port
   */
  public int getUtilMode(final int i) throws DWPortNotValidException {
    validateport(i);
    return this.vserialPorts[i].getUtilMode();
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
    return this.MIDIPort;
  }

  /**
   * Get Port Virtual Modem.
   *
   * @param port port
   * @return
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
