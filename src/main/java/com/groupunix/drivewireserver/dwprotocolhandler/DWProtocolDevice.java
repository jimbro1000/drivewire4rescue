package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;
import java.io.InputStream;

import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

public interface DWProtocolDevice {
  /**
   * Is device connected.
   *
   * @return true if connected
   */
  boolean connected();

  /**
   * Close device.
   */
  void close();

  /**
   * Shutdown device.
   */
  void shutdown();

  /**
   * Write N bytes.
   *
   * @param data   byte array
   * @param len    number of bytes to write
   * @param prefix require response prefix
   */
  void comWrite(byte[] data, int len, boolean prefix);

  /**
   * Write a single byte.
   *
   * @param data   byte
   * @param prefix require response prefix
   */
  void comWrite1(int data, boolean prefix);

  /**
   * Read N bytes.
   *
   * @param len number of bytes to read
   * @return byte array
   * @throws IOException            failed to read data
   * @throws DWCommTimeOutException read timeout
   */
  byte[] comRead(int len) throws IOException, DWCommTimeOutException;

  /**
   * Read a single byte.
   *
   * @param timeout timeout flag
   * @return single byte
   * @throws IOException            failed to read data
   * @throws DWCommTimeOutException read timeout
   */
  int comRead1(boolean timeout) throws IOException, DWCommTimeOutException;

  /**
   * Get rate.
   *
   * @return rate
   */
  int getRate();

  /**
   * Get device type.
   *
   * @return device type
   */
  String getDeviceType();

  /**
   * Get device name.
   *
   * @return device name
   */
  String getDeviceName();

  /**
   * Get device client.
   *
   * @return client name
   */
  String getClient();

  /**
   * Get device input stream.
   *
   * @return input stream
   */
  InputStream getInputStream();
}
