package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;
import static com.groupunix.drivewireserver.DWDefs.CARRIAGE_RETURN;
import static com.groupunix.drivewireserver.DWDefs.NEWLINE;
import static com.groupunix.drivewireserver.dwprotocolhandler.DWUtils.MIDI_CHAN_PRESS;
import static com.groupunix.drivewireserver.dwprotocolhandler.DWUtils.MIDI_CHAN_PRESS_MAX;
import static com.groupunix.drivewireserver.dwprotocolhandler.DWUtils.MIDI_NOTE_CHANNEL;
import static com.groupunix.drivewireserver.dwprotocolhandler.DWUtils.MIDI_PITCH_BEND_CHANNEL_MAX;
import static com.groupunix.drivewireserver.dwprotocolhandler.DWUtils.MIDI_PRG_CHANGE;

public class DWVSerialPort {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWVSerialPort");
  /**
   * Input buffer size (-1 = unlimited).
   */
  private static final int INPUT_BUFFER_SIZE = -1;
  /**
   * Output buffer size.
   */
  private static final int OUTPUT_BUFFER_SIZE = 10240;
  /**
   * Default port number.
   */
  private static final int DEFAULT_PORT_VALUE = 6309;
  // ASCII codes
  /**
   * DEL.
   */
  public static final int ASCII_DEL = 0x7f;
  /**
   * Start Of Header.
   */
  public static final int ASCII_SOH = 0x01;
  /**
   * ESC.
   */
  public static final int ASCII_ESC = 0x1b;
  /**
   * Device command len (bytes).
   */
  public static final int DEVICE_CMD_LEN = 8;
  /**
   * Device command data offset.
   */
  public static final int DEVICE_CMD_OFFSET = 3;
  /**
   * Poll delay (millis).
   */
  public static final int BUFFER_POLL_DELAY = 100;
  /**
   * Midi msg type base value.
   */
  public static final int MIDI_MSG_BASE = 192;
  /**
   * Midi sysex msg.
   */
  public static final int MIDI_SYSEX_MSG = 247;
  /**
   * Midi in sysex msg.
   */
  public static final int MIDI_IN_SYSEX = 240;
  /**
   * Send midi msg.
   */
  public static final int SEND_MIDI = 248;
  /**
   * DD array length.
   */
  public static final int DD_LEN = 26;
  /**
   * Port.
   */
  private int vPort = -1;
  /**
   * Serial protocol.
   */
  private final DWVSerialProtocol dwvSerialProtocol;
  /**
   * Connected flag.
   */
  private boolean connected = false;
  /**
   * Open count.
   */
  private int opens = 0;
  /**
   * Port handler.
   */
  private DWVPortHandler portHandler = null;
  /**
   * Serial ports.
   */
  private final DWVSerialPorts vSerialPorts;
  /**
   * PD_INT.
   */
  private byte pdInt = 0;
  /**
   * PD_QUT.
   */
  private byte pdQut = 0;
  /**
   * DD byte array.
   */
  private byte[] dD = new byte[DD_LEN];
  /**
   * Input buffer.
   */
  private final DWVSerialCircularBuffer inputBuffer
      = new DWVSerialCircularBuffer(INPUT_BUFFER_SIZE, true);
  /**
   * Output buffer byte array.
   */
  private final byte[] outBuffer = new byte[OUTPUT_BUFFER_SIZE];
  /**
   * Output buffer.
   */
  private final ByteBuffer outputBuffer = ByteBuffer.wrap(outBuffer);
  /**
   * Shutdown flag.
   */
  private boolean wanttodie = false;
  /**
   * Connection number.
   */
  private int connNo = -1;

  // midi message stuff
  /**
   * Midi message.
   */
  @SuppressWarnings("unused")
  private ShortMessage midiMsg;
  /**
   * Midi message position.
   */
  private int mmsgPos = 0;
  /**
   * Midi message data.
   */
  private int mmsgData1;
  /**
   * Midi message status.
   */
  private int mmsgStatus;
  /**
   * Last Midi message status.
   */
  @SuppressWarnings("unused")
  private int lastMmsgStatus;
  /**
   * Midi message data bytes.
   */
  private int mmsgDatabytes = 2;
  /**
   * Midi seen.
   */
  private boolean midiSeen = false;
  /**
   * Log midi bytes.
   */
  private boolean logMidiBytes = false;
  /**
   * Midi in sysex.
   */
  private boolean midiInSysex = false;
  /**
   * Midi sysex.
   */
  private String midiSysex = "";
  /**
   * Utility mode.
   */
  private int utilMode = 0;
  /**
   * Socket channel.
   */
  private SocketChannel socketChannel;

  /**
   * Virtual Serial Port.
   *
   * @param serialPorts virtual serial ports
   * @param protocol serial protocol
   * @param port port
   */
  public DWVSerialPort(final DWVSerialPorts serialPorts,
                       final DWVSerialProtocol protocol,
                       final int port) {
    LOGGER.debug("New DWVSerialPort for port " + port
        + " in handler #" + protocol.getHandlerNo());
    this.vSerialPorts = serialPorts;
    this.vPort = port;
    this.dwvSerialProtocol = protocol;

    if ((port != serialPorts.getNTermPort())
        && (port < (serialPorts.getMaxPorts()))) {
      this.portHandler = new DWVPortHandler(protocol, port);
      if (protocol.getConfig().getBoolean("LogMIDIBytes", false)) {
        this.logMidiBytes = true;
      }
    }
  }

  /**
   * Bytes waiting in buffer.
   *
   * @return bytes waiting to read
   */
  public int bytesWaiting() {
    int bytes = inputBuffer.getAvailable();
    // never admit to having more than 255 bytes
    return Math.min(bytes, BYTE_MASK);
  }

  /**
   * Write byte.
   *
   * @param data byte data
   */
  public void write(final int data) {
    int databyte = data;
    if (this.vPort == vSerialPorts.getMIDIPort()) {
      if (!midiSeen) {
        LOGGER.debug("MIDI data on port " + this.vPort);
        midiSeen = true;
      }
      // incomplete, but enough to make most things work for now
      databyte = databyte & BYTE_MASK;
      if (midiInSysex) {
        if (databyte == MIDI_SYSEX_MSG) {
          midiInSysex = false;
          if (logMidiBytes) {
            LOGGER.info("midi sysex: " + midiSysex);
          }
          midiSysex = "";
        } else {
          midiSysex = midiSysex + " " + databyte;
        }
      } else {
        if (databyte == MIDI_IN_SYSEX) {
          midiInSysex = true;
        } else if (databyte == SEND_MIDI) {
          // We ignore other status stuff for now
          sendMIDI(databyte);
        } else if ((databyte >= MIDI_PRG_CHANGE)
            && (databyte <= MIDI_CHAN_PRESS_MAX)) {
          // Program change and channel pressure have 1 data byte
          mmsgDatabytes = 1;
          lastMmsgStatus = mmsgStatus;
          mmsgStatus = databyte;
          mmsgPos = 0;
        } else if ((databyte >= MIDI_NOTE_CHANNEL)
            && (databyte <= MIDI_PITCH_BEND_CHANNEL_MAX)) {
          // Note on/off, key pressure, controller change,
          // pitch bend have 2 data bytes
          mmsgDatabytes = 2;
          lastMmsgStatus = mmsgStatus;
          mmsgStatus = databyte;
          mmsgPos = 0;
        } else {
          // data bytes
          if (mmsgPos == 0) {
            //data1
            if (mmsgDatabytes == 2) {
              // store databyte 1
              mmsgData1 = databyte;
              mmsgPos = 1;
            } else {
              // send midimsg with 1 data byte
              if ((mmsgStatus >= MIDI_MSG_BASE)
                  && (databyte < MIDI_CHAN_PRESS)) {
                if (dwvSerialProtocol.getVPorts().getMidiVoicelock()) {
                  // ignore program change
                  LOGGER.debug(
                      "MIDI: ignored program change due to instrument lock."
                  );
                } else {
                  // translate program changes
                  int xinstr = dwvSerialProtocol.getVPorts()
                      .getGMInstrument(databyte);
                  sendMIDI(mmsgStatus, xinstr, 0);
                  // set cache
                  dwvSerialProtocol.getVPorts().setGMInstrumentCache(
                      mmsgStatus - MIDI_MSG_BASE,
                      databyte
                  );
                }
              } else {
                sendMIDI(mmsgStatus, databyte, 0);
              }
              mmsgPos = 0;
            }
          } else {
            //data2
            sendMIDI(mmsgStatus, mmsgData1, databyte);
            mmsgPos = 0;
          }
        }
      }
    } else {
      // if we are connected, pass the data
      if ((this.connected) || (this.vPort == vSerialPorts.getNTermPort())
          || ((this.vPort >= vSerialPorts.getMaxNPorts())
          && (this.vPort < vSerialPorts.getMaxPorts()))) {
        if (socketChannel == null) {
          LOGGER.debug("write to null io channel on port " + this.vPort);
        } else {
          try {
            while (outputBuffer.remaining() < 1) {
              System.out.println("FULL buffer " + outputBuffer.position()
                  + " " + outputBuffer.remaining() + " "
                  + outputBuffer.limit() + " " + outputBuffer.capacity());
              outputBuffer.flip();
              int wrote = socketChannel.write(outputBuffer);
              outputBuffer.compact();
              System.out.println("full wrote " + wrote);
              if (wrote == 0) {
                try {
                  Thread.sleep(BUFFER_POLL_DELAY);
                } catch (InterruptedException ignored) {
                }
              }
            }
            outputBuffer.put((byte) databyte);
            outputBuffer.flip();
            socketChannel.write(outputBuffer);
            outputBuffer.compact();
          } catch (IOException e) {
            LOGGER.error("in write: " + e.getMessage());
          }
        }
      } else {
        // otherwise process as command
        this.portHandler.takeInput(databyte);
      }
    }
  }

  private void sendMIDI(final int statusByte) {
    ShortMessage mmsg = new ShortMessage();
    try {
      mmsg.setMessage(statusByte);
      dwvSerialProtocol.getVPorts().sendMIDIMsg(mmsg, -1);
    } catch (InvalidMidiDataException e) {
      LOGGER.warn("MIDI: " + e.getMessage());
    }
    if (logMidiBytes) {
      byte[] tmpb = {(byte) statusByte};
      LOGGER.info("midimsg: " + DWUtils.byteArrayToHexString(tmpb));
    }
  }

  private void sendMIDI(final int statusByte,
                        final int data1,
                        final int data2
  ) {
    ShortMessage mmsg = new ShortMessage();
    try {
      mmsg.setMessage(statusByte, data1, data2);
      dwvSerialProtocol.getVPorts().sendMIDIMsg(mmsg, -1);
    } catch (InvalidMidiDataException e) {
      LOGGER.warn("MIDI: " + e.getMessage());
    }
    if (logMidiBytes) {
      LOGGER.info("midimsg: "
          + DWUtils.midiMsgToText(statusByte, data1, data2));
    }
  }

  /**
   * Write string.
   *
   * @param str data string
   */
  public void writeM(final String str) {
    for (int i = 0; i < str.length(); i++) {
      write(str.charAt(i));
    }
  }

  /**
   * Write string to CoCo.
   *
   * @param str data string
   */
  public void writeToCoco(final String str) {
    try {
      inputBuffer.getOutputStream().write(str.getBytes());
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
  }

  /**
   * Write byte array to CoCo.
   *
   * @param dataBytes byte array
   */
  public void writeToCoco(final byte[] dataBytes) {
    try {
      inputBuffer.getOutputStream().write(dataBytes);
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
  }

  /**
   * Write byte array to CoCo.
   *
   * @param dataBytes byte array
   * @param offset start offset
   * @param length bytes to send
   */
  public void writeToCoco(
      final byte[] dataBytes,
      final int offset,
      final int length
  ) {
    try {
      inputBuffer.getOutputStream().write(dataBytes, offset, length);
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
  }

  /**
   * Write byte to CoCo.
   *
   * @param dataByte byte
   */
  public void writeToCoco(final byte dataByte) {
    try {
      inputBuffer.getOutputStream().write(dataByte);
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
  }

  /**
   * Get port input.
   *
   * @return output stream
   */
  public OutputStream getPortInput() {
    return (inputBuffer.getOutputStream());
  }

  /**
   * Get port output.
   *
   * @return input stream
   */
  public InputStream getPortOutput() {
    return (inputBuffer.getInputStream());
  }

  /**
   * Read single byte.
   *
   * @return data byte
   */
  public byte read1() {
    try {
      int dataByte = inputBuffer.getInputStream().read();
      return (byte) dataByte;
    } catch (IOException e) {
      LOGGER.error("in read1: " + e.getMessage());
    }
    return -1;
  }

  /**
   * Read bytes.
   *
   * @param tmplen bytes to read
   * @return byte array
   */
  public byte[] readM(final int tmplen) {
    byte[] buf = new byte[tmplen];
    try {
      inputBuffer.getInputStream().read(buf, 0, tmplen);
      return buf;
    } catch (IOException e) {
      e.printStackTrace();
      LOGGER.error("Failed to read " + tmplen
          + " bytes in SERREADM... not good");
    }
    return null;
  }

  /**
   * Is connected.
   *
   * @return connected flag
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Set connected flag.
   *
   * @param boolFlag bool
   */
  public void setConnected(final boolean boolFlag) {
    this.connected = boolFlag;
  }

  /**
   * Is port open.
   *
   * @return open state
   */
  public boolean isOpen() {
    return (this.opens > 0) || (this.utilMode == DWDefs.UTILMODE_NINESERVER);
  }

  /**
   * Open.
   */
  public void open() {
    this.opens++;
    LOGGER.debug("open port " + this.vPort + ", total opens: " + this.opens);
    // fire off NineServer thread if we are a window device
    if ((this.vPort >= vSerialPorts.getMaxNPorts())
        && (this.vPort < vSerialPorts.getMaxPorts())) {
      String tcphost = this.dwvSerialProtocol.getConfig().getString(
          "NineServer" + this.vPort,
          this.dwvSerialProtocol.getConfig()
              .getString("NineServer", "127.0.0.1")
      );
      int tcpport = this.dwvSerialProtocol.getConfig().getInt(
          "NineServerPort" + this.vPort,
          this.dwvSerialProtocol.getConfig().getInt(
              "NineServerPort",
              DEFAULT_PORT_VALUE
          )
      );
      this.setUtilMode(DWDefs.UTILMODE_NINESERVER);
      // device id cmd
      byte[] wcdata = new byte[DEVICE_CMD_LEN];
      wcdata[0] = ASCII_ESC;
      wcdata[1] = ASCII_DEL;
      wcdata[2] = ASCII_SOH;
      String pname = this.dwvSerialProtocol.getVPorts().prettyPort(this.vPort);
      for (int i = DEVICE_CMD_OFFSET; i < wcdata.length; i++) {
        if (i - DEVICE_CMD_OFFSET < pname.length()) {
          wcdata[i] = (byte) pname.charAt(i - DEVICE_CMD_OFFSET);
        } else {
          wcdata[i] = ' ';
        }
      }
      // start TCP thread
      Thread utilthread = new Thread(new DWVPortTCPConnectionThread(
          this.dwvSerialProtocol, this.vPort, tcphost, tcpport, false, wcdata
      ));
      utilthread.setDaemon(true);
      utilthread.start();
      LOGGER.debug("Started NineServer comm thread for port " + vPort);
    }
  }

  /**
   * Close.
   */
  public void close() {
    if (this.opens > 0) {
      this.opens--;
      LOGGER.debug("close port " + this.vPort + ", total opens: "
          + this.opens + " data in buffer: "
          + this.inputBuffer.getAvailable());
      // send term if last open and not window
      if ((this.opens == 0)
          && (this.getUtilMode() != DWDefs.UTILMODE_NINESERVER)) {
        LOGGER.debug("setting term on port " + this.vPort);
        this.wanttodie = true;
        // close socket channel if connected
        if ((this.socketChannel != null) && (this.socketChannel.isOpen())) {
          LOGGER.debug("closing io channel on port " + this.vPort);
          try {
            this.socketChannel.close();
          } catch (IOException e) {
            LOGGER.warn(e.getMessage());
          }
          this.socketChannel = null;
        }
        // close listeners if this was their control port
        this.dwvSerialProtocol.getVPorts()
            .getListenerPool().closePortServerSockets(this.vPort);
      }
    }
  }

  /**
   * Is terminating.
   *
   * @return shutdown flag
   */
  public boolean isTerm() {
    return (wanttodie);
  }

  /**
   * Set port channel.
   *
   * @param sc socket channel
   */
  public void setPortChannel(final SocketChannel sc) {
    this.socketChannel = sc;
  }

  /**
   * Get Util Mode.
   *
   * @return mode
   */
  public int getUtilMode() {
    return (this.utilMode);
  }

  /**
   * Set Util Mode.
   *
   * @param mode mode
   */
  public void setUtilMode(final int mode) {
    this.utilMode = mode;
  }

  /**
   * Get PD INT.
   *
   * @return byte value
   */
  public byte getPdInt() {
    return pdInt;
  }

  /**
   * Set PD INT.
   *
   * @param value byte value
   */
  public void setPdInt(final byte value) {
    pdInt = value;
    this.inputBuffer.setDwPdInt(pdInt);
  }

  /**
   * Get PD QUT.
   *
   * @return byte value
   */
  public byte getPdQut() {
    return pdQut;
  }

  /**
   * Set PD QUT.
   *
   * @param value byte value
   */
  public void setPdQut(final byte value) {
    pdQut = value;
    this.inputBuffer.setDwPdQut(pdQut);
  }

  /**
   * Send utility fail response.
   *
   * @param errno error code
   * @param txt response message
   */
  public void sendUtilityFailResponse(final byte errno, final String txt) {
    String sErrNo = String.format("%03d", (errno & BYTE_MASK));
    LOGGER.debug("command failed: " + sErrNo + " " + txt);
    try {
      inputBuffer.getOutputStream().write(
          ("FAIL " + sErrNo + " " + txt
              + (char) NEWLINE + (char) CARRIAGE_RETURN).getBytes()
      );
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
  }

  /**
   * Send utility OK response.
   *
   * @param txt response message
   */
  public void sendUtilityOKResponse(final String txt) {
    try {
      inputBuffer.getOutputStream().write(
          ("OK " + txt + (char) NEWLINE + (char) CARRIAGE_RETURN).getBytes()
      );
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
  }

  /**
   * Get device description.
   *
   * @return device description bye array
   */
  public byte[] getDD() {
    return (this.dD);
  }

  /**
   * Set device description.
   *
   * @param devDescr byte array
   */
  public void setDD(final byte[] devDescr) {
    this.dD = devDescr;
  }

  /**
   * Get total open.
   *
   * @return total open count
   */
  public int getOpen() {
    return (this.opens);
  }

  /**
   * Send connection announcement.
   *
   * @param conno connection number
   * @param localport local port
   * @param hostaddr host address
   */
  public void sendConnectionAnnouncement(final int conno,
                                         final int localport,
                                         final String hostaddr) {
    this.portHandler.announceConnection(conno, localport, hostaddr);
  }

  /**
   * Get connection.
   *
   * @return connection number
   */
  public int getConn() {
    return (this.connNo);
  }

  /**
   * Set connection.
   *
   * @param connectionNo connection number
   */
  public void setConn(final int connectionNo) {
    this.connNo = connectionNo;
  }

  /**
   * Shutdown port.
   */
  public void shutdown() {
    // close this port
    this.connected = false;
    this.opens = 0;
    this.socketChannel = null;
    this.portHandler = null;
    this.wanttodie = true;
  }

  /**
   * Get virtual modem.
   *
   * @return modem
   */
  public DWVModem getVModem() {
    return this.portHandler.getVModem();
  }
}
