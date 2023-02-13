package com.groupunix.drivewireserver.virtualserial.api;

import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class DWAPISerialPortDef {
  /**
   * baud.
   */
  private int baudRate = -1;
  /**
   * Data bits.
   */
  private int dataBits = -1;
  /**
   * Stop bits.
   */
  private int stopBits = -1;
  /**
   * Parity.
   */
  private int parityType = -1;
  /**
   * Flow control.
   */
  private int flowControl = -1;

  /**
   * Get baud rate.
   *
   * @return baud
   */
  @SuppressWarnings("unused")
  public int getRate() {
    return baudRate;
  }

  /**
   * Set baud rate.
   *
   * @param rate baud
   */
  public void setRate(final int rate) {
    this.baudRate = rate;
  }

  /**
   * Get data bits.
   *
   * @return data bits
   */
  @SuppressWarnings("unused")
  public int getDatabits() {
    return dataBits;
  }

  /**
   * Set data bits.
   *
   * @param databits data bits
   */
  public void setDatabits(final int databits) {
    this.dataBits = databits;
  }

  /**
   * Get stop bits.
   *
   * @return stop bits
   */
  @SuppressWarnings("unused")
  public int getStopbits() {
    return stopBits;
  }

  /**
   * Set stop bits.
   *
   * @param stopbits stop bits
   */
  public void setStopbits(final int stopbits) {
    this.stopBits = stopbits;
  }

  /**
   * Get parity.
   *
   * @return parity bits.
   */
  @SuppressWarnings("unused")
  public int getParity() {
    return parityType;
  }

  /**
   * Set parity.
   *
   * @param parity parity bits
   */
  public void setParity(final int parity) {
    this.parityType = parity;
  }

  /**
   * Get flow control.
   *
   * @return flow control
   */
  @SuppressWarnings("unused")
  public int getFlowcontrol() {
    return flowControl;
  }

  /**
   * Set flow control.
   *
   * @param flowcontrol flow control
   */
  public void setFlowcontrol(final int flowcontrol) {
    this.flowControl = flowcontrol;
  }

  /**
   * Set parameters.
   *
   * @param port serial port
   * @throws UnsupportedCommOperationException invalid operation
   */
  public void setParams(final SerialPort port)
      throws UnsupportedCommOperationException {
    if (this.baudRate == -1) {
      this.baudRate = port.getBaudRate();
    }
    if (this.dataBits == -1) {
      this.dataBits = port.getDataBits();
    }
    if (this.stopBits == -1) {
      this.stopBits = port.getStopBits();
    }
    if (this.parityType == -1) {
      this.parityType = port.getParity();
    }
    port.setSerialPortParams(baudRate, dataBits, stopBits, parityType);
    if (this.flowControl > -1) {
      port.setFlowControlMode(this.flowControl);
    }
  }
}
