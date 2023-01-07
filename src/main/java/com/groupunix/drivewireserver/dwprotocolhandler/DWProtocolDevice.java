package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;
import java.io.InputStream;

import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

public interface DWProtocolDevice {
  boolean connected();

  void close();

  void shutdown();

  void comWrite(byte[] data, int len, boolean prefix);

  void comWrite1(int data, boolean prefix);

  byte[] comRead(int len) throws IOException, DWCommTimeOutException;

  int comRead1(boolean timeout) throws IOException, DWCommTimeOutException;

  int getRate();

  String getDeviceType();

  String getDeviceName();

  String getClient();

  InputStream getInputStream();
}
