package com.groupunix.drivewireserver.uicommands;

import java.io.StringWriter;

import org.apache.commons.configuration.ConfigurationException;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigWrite extends DWCommand {

  public UICmdServerConfigWrite() {
    setCommand("write");
    setShortHelp("Write config xml");
    setUsage("ui server config write");
  }

  public DWCommandResponse parse(String cmdline) {
    String res = new String();

    StringWriter sw = new StringWriter();

    try {
      DriveWireServer.getServerConfiguration().save(sw);
    } catch (ConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    res = sw.getBuffer().toString();

    return (new DWCommandResponse(res));
  }

  public boolean validate(String cmdline) {
    return (true);
  }

}