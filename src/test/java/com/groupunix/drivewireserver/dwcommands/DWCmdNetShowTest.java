package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DWCmdNetShowTest {
  private DWCmdNetShow cmdNetShow;
  @Mock
  private DWVSerialProtocol protocol;
  @Mock
  private DWCommand parent;

  @BeforeEach
  public void setup() {
    cmdNetShow = new DWCmdNetShow(protocol, parent);
  }

  @Test
  public void getCommandTakesChildDetails() {
    assertEquals(cmdNetShow.getCommand(), "show");
  }

  @Test
  public void getUsageTakesChildDetails() {
    assertEquals(cmdNetShow.getUsage(), "dw net show");
  }

  @Test
  public void getShortHelpTakesChildDetails() {
    assertEquals(cmdNetShow.getShortHelp(), "Show networking status");
  }
}
