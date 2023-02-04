package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerTerminate extends DWCommand {

  @SuppressWarnings("unused")
  private DWUIClientThread dwuiref;

  @SuppressWarnings("unused")
  private DWProtocol dwProto;

  public UICmdServerTerminate(DWUIClientThread dwuiClientThread) {
    this.dwuiref = dwuiClientThread;
    setHelp();
  }

  public UICmdServerTerminate(DWProtocol dwProto) {
    this.dwProto = dwProto;
    setHelp();
  }

  private void setHelp() {
    setCommand("terminate");
    setShortHelp("Terminate the server");
    setUsage("ui server terminate");
  }

  public DWCommandResponse parse(String cmdline) {
    DriveWireServer.shutdown();
    return (new DWCommandResponse("Server shutdown requested."));
  }

  public boolean validate(String cmdline) {
    return (true);
  }

}