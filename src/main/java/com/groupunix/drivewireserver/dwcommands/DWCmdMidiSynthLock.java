package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynthLock extends DWCommand {

  private DWProtocolHandler dwProto;

  public DWCmdMidiSynthLock(DWProtocolHandler dwProto, DWCommand parent) {
    setParentCmd(parent);
    this.dwProto = dwProto;
    this.setCommand("lock");
    this.setShortHelp("Toggle instrument lock");
    this.setUsage("dw midi synth lock");
  }

  public DWCommandResponse parse(String cmdline) {
    return (doMidiSynthLock());
  }

  private DWCommandResponse doMidiSynthLock() {
    if (dwProto.getVPorts().getMidiVoicelock()) {
      dwProto.getVPorts().setMidiVoicelock(false);
      return (new DWCommandResponse("Unlocked MIDI instruments, program changes will be processed"));
    } else {
      dwProto.getVPorts().setMidiVoicelock(true);
      return (new DWCommandResponse("Locked MIDI instruments, progam changes will be ignored"));
    }
  }

  public boolean validate(String cmdline) {
    return (true);
  }
}
