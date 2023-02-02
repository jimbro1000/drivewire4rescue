package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

import static com.groupunix.drivewireserver.DWDefs.BYTE_MASK;

public class DWSerialDevice implements DWProtocolDevice {
  /**
   * Log appender.
   */
  private static final Logger LOGGER
      = Logger.getLogger("DWServer.DWSerialDevice");
  /**
   * DATURBO data bits.
   */
  public static final int DATURBO_DATA_BITS = 8;
  /**
   * DATURBO stop bits.
   */
  public static final int DATURBO_STOP_BITS = 2;
  /**
   * DATURBO parity bit.
   */
  public static final int DATURBO_PARITY = 0;
  /**
   * Baud rate.
   */
  public static final int BAUD_RATE = 115200;
  /**
   * Default data bits.
   */
  public static final int DEFAULT_DATA_BITS = 8;
  /**
   * Default wait time.
   */
  public static final int DEFAULT_WAIT_TIME = 200;
  /**
   * Default write delay.
   */
  public static final int DEFAULT_WRITE_DELAY = 0;
  /**
   * Connection timeout.
   */
  public static final int CONNECT_TIMEOUT = 2000;
  /**
   * queue capacity.
   */
  public static final int QUEUE_CAPACITY = 512;
  /**
   * Prefix byte.
   */
  public static final int PREFIX_BYTE = 0xC0;
  /**
   * Serial port.
   */
  private SerialPort serialPort = null;
  /**
   * Log byte flag.
   */
  private boolean bytelog = false;
  /**
   * Device name.
   */
  private String device;
  /**
   * Protocol.
   */
  private final DWProtocol dwProtocol;
  /**
   * DATURBO mode flag.
   */
  private boolean daTurboMode = false;
  /**
   * XOR input.
   */
  private boolean xorInput = false;
  /**
   * Delay on write byte.
   */
  private long writeByteDelay = 0;
  /**
   * Wait on read byte.
   */
  private long readByteWait = DEFAULT_WAIT_TIME;
  /**
   * Prefix.
   */
  private final byte[] prefix;
  /**
   * Read time.
   */
  private long readtime;
  /**
   * Queue.
   */
  private ArrayBlockingQueue<Byte> queue;
  /**
   * Serial reader.
   */
  private DWSerialReader evtlistener;
  /**
   * Flip output bits flag.
   */
  private boolean protocolFlipOutputBits;
  /**
   * Response prefix flag.
   */
  private boolean protocolResponsePrefix;

  /**
   * Serial device.
   *
   * @param protocol drivewire protocol
   * @throws NoSuchPortException invalid port
   * @throws PortInUseException port in use
   * @throws UnsupportedCommOperationException invalid comm operation
   * @throws IOException read/write failure
   * @throws TooManyListenersException too many listeners
   */
  public DWSerialDevice(final DWProtocol protocol)
      throws NoSuchPortException,
      PortInUseException,
      UnsupportedCommOperationException,
      IOException,
      TooManyListenersException {
    this.dwProtocol = protocol;
    this.device = protocol.getConfig().getString("SerialDevice");
    prefix = new byte[1];
    prefix[0] = (byte) PREFIX_BYTE;
    LOGGER.debug("init " + device + " for handler #" + protocol.getHandlerNo()
        + " (logging bytes: " + bytelog + "  xorinput: " + xorInput + ")");
    connect(device);
  }

  /**
   * Is port connected.
   *
   * @return true if connected
   */
  public boolean connected() {
    return this.serialPort != null;
  }

  /**
   * Close port.
   */
  public void close() {
    if (this.serialPort != null) {
      LOGGER.debug("closing serial device " + device + " in handler #"
          + dwProtocol.getHandlerNo());
      serialPort.notifyOnDataAvailable(false);
      if (this.evtlistener != null) {
        this.evtlistener.shutdown();
        serialPort.removeEventListener();
      }
      serialPort.close();
      serialPort = null;
    }
  }

  /**
   * Shutdown port.
   */
  public void shutdown() {
    this.close();
  }

  /**
   * Reconnect port.
   *
   * @throws UnsupportedCommOperationException invalid port operation
   * @throws IOException read/write failure
   * @throws TooManyListenersException too many port listeners
   */
  public void reconnect()
      throws UnsupportedCommOperationException,
      IOException,
      TooManyListenersException {
    if (this.serialPort != null) {
      setSerialParams(serialPort);
      if (this.evtlistener != null) {
        this.serialPort.removeEventListener();
      }
      this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
      this.evtlistener = new DWSerialReader(serialPort.getInputStream(), queue);
      serialPort.addEventListener(this.evtlistener);
      serialPort.notifyOnDataAvailable(true);
    }
  }

  /**
   * Connect to port.
   *
   * @param portName port name
   * @throws NoSuchPortException non-existent port
   * @throws PortInUseException port already in use
   * @throws UnsupportedCommOperationException invalid comm operation
   * @throws IOException read/write failed
   * @throws TooManyListenersException too many port listeners
   */
  private void connect(final String portName)
      throws NoSuchPortException,
      PortInUseException,
      UnsupportedCommOperationException,
      IOException,
      TooManyListenersException {
    LOGGER.debug("attempting to open device '" + portName + "'");
    CommPortIdentifier portIdentifier
        = CommPortIdentifier.getPortIdentifier(portName);
    CommPort commPort = portIdentifier.open("DriveWire", CONNECT_TIMEOUT);
    if (commPort instanceof SerialPort) {
      serialPort = (SerialPort) commPort;
      reconnect();
      LOGGER.info("opened serial device " + portName);
    } else {
      LOGGER.error("The operating system says '" + portName
          + "' is not a serial port!");
      throw new NoSuchPortException();
    }
  }

  private int getParity() {
    if (dwProtocol.getConfig().containsKey("SerialParity")) {
      switch (dwProtocol.getConfig().getString("SerialParity")) {
        case "none" -> {
          return SerialPort.PARITY_NONE;
        }
        case "even" -> {
          return SerialPort.PARITY_EVEN;
        }
        case "odd" -> {
          return SerialPort.PARITY_ODD;
        }
        case "mark" -> {
          return SerialPort.PARITY_MARK;
        }
        case "space" -> {
          return SerialPort.PARITY_SPACE;
        }
        default -> {
          return 0;
        }
      }
    }
    return 0;
  }

  private int getStopBits() {
    if (dwProtocol.getConfig().containsKey("SerialStopbits")) {
      switch (dwProtocol.getConfig().getString("SerialStopbits")) {
        case "1" -> {
          return SerialPort.STOPBITS_1;
        }
        case "1.5" -> {
          return SerialPort.STOPBITS_1_5;
        }
        case "2" -> {
          return SerialPort.STOPBITS_2;
        }
        default -> {
          return 1;
        }
      }
    }
    return 1;
  }
  /**
   * Set serial port parameters.
   *
   * @param port serial port
   * @throws UnsupportedCommOperationException invalid comm operation
   */
  private void setSerialParams(final SerialPort port)
      throws UnsupportedCommOperationException {
    int rate;
    int parity = getParity();
    int stopBits = getStopBits();
    int dataBits = DEFAULT_DATA_BITS;

    // mode vars
    this.writeByteDelay = this.dwProtocol.getConfig()
        .getLong("WriteByteDelay", DEFAULT_WRITE_DELAY);
    this.readByteWait = this.dwProtocol.getConfig()
        .getLong("ReadByteWait", DEFAULT_WAIT_TIME);
    this.protocolFlipOutputBits = this.dwProtocol.getConfig()
        .getBoolean("ProtocolFlipOutputBits", false);
    this.protocolResponsePrefix = dwProtocol.getConfig()
        .getBoolean("ProtocolResponsePrefix", false);
    this.xorInput = dwProtocol.getConfig()
        .getBoolean("ProtocolXORInputBits", false);
    this.bytelog = dwProtocol.getConfig()
        .getBoolean("LogDeviceBytes", false);

    // serial port tweaks
    this.serialPort.enableReceiveThreshold(1);

    // serial params
    rate = dwProtocol.getConfig().getInt("SerialRate", BAUD_RATE);

    int flow = SerialPort.FLOWCONTROL_NONE;
    if (dwProtocol.getConfig()
        .getBoolean("SerialFlowControl_RTSCTS_IN", false)) {
      flow = flow | SerialPort.FLOWCONTROL_RTSCTS_IN;
    }

    if (dwProtocol.getConfig()
        .getBoolean("SerialFlowControl_RTSCTS_OUT", false)) {
      flow = flow | SerialPort.FLOWCONTROL_RTSCTS_OUT;
    }

    if (dwProtocol.getConfig()
        .getBoolean("SerialFlowControl_XONXOFF_IN", false)) {
      flow = flow | SerialPort.FLOWCONTROL_XONXOFF_IN;
    }

    if (dwProtocol.getConfig()
        .getBoolean("SerialFlowControl_XONXOFF_OUT", false)) {
      flow = flow | SerialPort.FLOWCONTROL_XONXOFF_OUT;
    }

    this.serialPort.setFlowControlMode(flow);

    LOGGER.debug("setting port params to " + rate + " " + dataBits + ":"
        + parity + ":" + stopBits);
    port.setSerialPortParams(rate, dataBits, stopBits, parity);

    if (dwProtocol.getConfig().containsKey("SerialDTR")) {
      port.setDTR(dwProtocol.getConfig().getBoolean("SerialDTR", false));
      LOGGER.debug("setting port DTR to "
          + dwProtocol.getConfig().getBoolean("SerialDTR", false));
    }

    if (dwProtocol.getConfig().containsKey("SerialRTS")) {
      port.setRTS(dwProtocol.getConfig().getBoolean("SerialRTS", false));
      LOGGER.debug("setting port RTS to "
          + dwProtocol.getConfig().getBoolean("SerialRTS", false));
    }
  }

  /**
   * Get serial port baud rate.
   *
   * @return baud rate
   */
  public int getRate() {
    if (this.serialPort != null) {
      return (this.serialPort.getBaudRate());
    }
    return -1;
  }

  /**
   * Write N bytes to serial port.
   *
   * @param byteData byte array
   * @param len      number of bytes to write
   * @param prefixFlag   require response prefix
   */
  public void comWrite(
      final byte[] byteData, final int len, final boolean prefixFlag
  ) {
    byte[] data = new byte[byteData.length];
    System.arraycopy(byteData, 0, data, 0, byteData.length);
    try {
      if (this.protocolFlipOutputBits || this.daTurboMode) {
        data = DWUtils.reverseByteArray(data);
      }
      if (this.writeByteDelay > 0) {
        for (int i = 0; i < len; i++) {
          comWrite1(data[i], prefixFlag);
        }
      } else {
        if (prefixFlag && (this.protocolResponsePrefix || this.daTurboMode)) {
          byte[] out = new byte[this.prefix.length + len];
          System.arraycopy(this.prefix, 0, out, 0, this.prefix.length);
          System.arraycopy(data, 0, out, this.prefix.length, len);
          serialPort.getOutputStream().write(out);
        } else {
          serialPort.getOutputStream().write(data, 0, len);
        }
        // extreme cases only
        if (bytelog) {
          StringBuilder tmps = new StringBuilder();
          for (int i = 0; i < len; i++) {
            tmps.append(" ").append(data[i] & BYTE_MASK);
          }
          LOGGER.debug("WRITE " + len + ":" + tmps);
        }
      }
    } catch (IOException e) {
      // problem with comm port, bail out
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Write single byte to serial port.
   *
   * @param dataByte byte
   * @param prefixFlag   require response prefix
   */
  public void comWrite1(final int dataByte, final boolean prefixFlag) {
    int data = dataByte;
    try {
      if (this.protocolFlipOutputBits || this.daTurboMode) {
        data = DWUtils.reverseByte(data);
      }
      if (this.writeByteDelay > 0) {
        try {
          Thread.sleep(this.writeByteDelay);
        } catch (InterruptedException e) {
          LOGGER.warn("interrupted during writebytedelay");
        }
      }
      if (prefixFlag && (this.protocolResponsePrefix || this.daTurboMode)) {
        byte[] out = new byte[this.prefix.length + 1];
        out[out.length - 1] = (byte) data;
        System.arraycopy(this.prefix, 0, out, 0, this.prefix.length);
        serialPort.getOutputStream().write(out);
      } else {
        serialPort.getOutputStream().write((byte) data);
      }
      if (bytelog) {
        LOGGER.debug("WRITE1: " + (BYTE_MASK & data));
      }
    } catch (IOException e) {
      // problem with comm port, bail out
      LOGGER.error(e.getMessage());
    }
  }

  /**
   * Read N bytes from serial port.
   *
   * @param len number of bytes to read
   * @return byte array
   * @throws DWCommTimeOutException serial port timeout
   */
  public byte[] comRead(final int len) throws DWCommTimeOutException {
    byte[] buf = new byte[len];
    for (int i = 0; i < len; i++) {
      buf[i] = (byte) comRead1(true, false);
    }
    if (this.bytelog) {
      StringBuilder tmp = new StringBuilder();
      for (byte b : buf) {
        tmp.append(" ").append(b & BYTE_MASK);
      }
      LOGGER.debug("READ " + len + ": " + tmp);
    }
    return buf;
  }

  /**
   * Read one byte from serial port.
   * <p>
   * if timeout flag set throw exception on reading null
   * </p>
   *
   * @param timeout timeout flag
   * @return read byte
   * @throws DWCommTimeOutException serial port timeout
   */
  public int comRead1(final boolean timeout) throws DWCommTimeOutException {
    return comRead1(timeout, true);
  }

  /**
   * Read one byte from serial port.
   * <p>
   * if timeout flag set throw exception on reading null
   * </p>
   *
   * @param timeout timeout flag
   * @param blog    log byte flag
   * @return read byte
   * @throws DWCommTimeOutException serial port timeout
   */
  public int comRead1(final boolean timeout, final boolean blog)
      throws DWCommTimeOutException {
    int res = -1;
    try {
      while ((res == -1) && (this.serialPort != null)) {
        long startTime = System.currentTimeMillis();
        Byte read = queue.poll(this.readByteWait, TimeUnit.MILLISECONDS);
        this.readtime += System.currentTimeMillis() - startTime;
        if (read != null) {
          res = BYTE_MASK & read;
        } else if (timeout) {
          throw (new DWCommTimeOutException(
              "No data in " + this.readByteWait + " ms")
          );
        }
      }
    } catch (InterruptedException e) {
      LOGGER.debug("interrupted in serial read");
    }
    if (this.xorInput) {
      res = res ^ BYTE_MASK;
    }
    if (blog && this.bytelog) {
      LOGGER.debug("READ1: " + res);
    }
    return res;
  }

  /**
   * Get device name.
   *
   * @return device name
   */
  @Override
  public String getDeviceName() {
    if (this.serialPort != null) {
      return this.serialPort.getName();
    }
    return null;
  }

  /**
   * Get device type.
   *
   * @return serial
   */
  @Override
  public String getDeviceType() {
    return "serial";
  }

  /**
   * Enable DA turbo mode.
   *
   * @throws UnsupportedCommOperationException invalid comm operation
   */
  public void enableDATurbo() throws UnsupportedCommOperationException {
    // valid port, not already turbo
    if ((this.serialPort != null) && !this.daTurboMode) {
      // change to 2x instead of hardcoded
      if ((this.serialPort.getBaudRate() >= DWDefs.COM_MIN_DATURBO_RATE)
          && ((this.serialPort.getBaudRate() <= DWDefs.COM_MAX_DATURBO_RATE))) {
        this.serialPort.setSerialPortParams(
            this.serialPort.getBaudRate() * 2,
            DATURBO_DATA_BITS,
            DATURBO_STOP_BITS,
            DATURBO_PARITY
        );
        this.daTurboMode = true;
      }
    }
  }

  /**
   * Get read time.
   *
   * @return read time
   */
  public long getReadTime() {
    return this.readtime;
  }

  /**
   * Reset read time to 0.
   */
  public void resetReadTime() {
    this.readtime = 0;
  }

  /**
   * Get serial device port.
   *
   * @return serial port
   */
  @SuppressWarnings("unused")
  public SerialPort getSerialPort() {
    return this.serialPort;
  }

  /**
   * Get client.
   * <p>
   * not implemented
   * </p>
   *
   * @return null
   */
  @Override
  public String getClient() {
    return null;
  }

  /**
   * Get input serial stream.
   *
   * @return input stream
   */
  @Override
  public InputStream getInputStream() {
    return new InputStream() {
      private boolean endReached = false;

      @Override
      public int read() throws IOException {
        if (endReached) {
          return -1;
        }
        try {
          Byte value = queue.take();
          if (value == null) {
            throw new IOException(
                "Timeout while reading from the queue-based input stream");
          }
          endReached = (value.intValue() == -1);
          return value;
        } catch (InterruptedException ie) {
          throw new IOException(
              "Interruption occurred while writing in the queue");
        }
      }
    };
  }
}
