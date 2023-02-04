package com.groupunix.drivewireserver.uicommands;

import java.util.List;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class UICmdServerShowErrors extends DWCommand {


  private DWProtocol dwProto;

  public UICmdServerShowErrors(DWUIClientThread dwuiClientThread) {
    this.dwProto = DriveWireServer.getHandler(dwuiClientThread.getInstance());
    setHelp();
  }


  public UICmdServerShowErrors(DWProtocol dwProto) {
    this.dwProto = dwProto;
    setHelp();
  }

  private void setHelp() {
    setCommand("errors");
    setShortHelp("show error descriptions");
    setUsage("ui server show errors");
  }

  @Override
  public DWCommandResponse parse(String cmdline) {
    String res = "";

    if (dwProto != null) {
      List<String> rconfs = dwProto.getHelp().getSectionTopics("resultcode");

      if (rconfs != null) {
        for (String rc : rconfs) {
          try {
            res += rc.substring(1) + "|" + dwProto.getHelp().getTopicText("resultcode." + rc).trim() + "\r\n";
          } catch (DWHelpTopicNotFoundException e) {
            // whatever
          }

        }

        return (new DWCommandResponse(res));
      }
    }

    return (new DWCommandResponse(false, DWDefs.RC_HELP_TOPIC_NOT_FOUND, "No error descriptions available from server"));

  }

  public boolean validate(String cmdline) {
    return (true);
  }
}
