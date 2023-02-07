package com.groupunix.drivewireserver.dwcommands;

import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

import static com.groupunix.drivewireserver.DriveWireServer.OPEN_PORT_TIMEOUT_MILLIS;


public class DWCmdServerShowSerial extends DWCommand {
  /**
   * Protocol.
   */
  @SuppressWarnings("unused")
  private final DWProtocol dwProtocol;

  /**
   * Server show serial command constructor.
   *
   * @param protocol protocol
   * @param parent parent command
   */
  DWCmdServerShowSerial(final DWProtocol protocol, final DWCommand parent) {
    setParentCmd(parent);
    this.dwProtocol = protocol;
    this.setCommand("serial");
    this.setShortHelp("Show serial device information");
    this.setUsage("dw server show serial");
  }

  /**
   * Parse command line.
   *
   * @param cmdline command line
   * @return command response
   */
  public DWCommandResponse parse(final String cmdline) {
    StringBuilder text = new StringBuilder();

    text.append("Server serial devices:\r\n\r\n");

    @SuppressWarnings("unchecked")
    java.util.Enumeration<gnu.io.CommPortIdentifier> thePorts
        = gnu.io.CommPortIdentifier.getPortIdentifiers();

    while (thePorts.hasMoreElements()) {
      try {
        gnu.io.CommPortIdentifier com = thePorts.nextElement();
        if (com.getPortType() == gnu.io.CommPortIdentifier.PORT_SERIAL) {
          text.append(com.getName()).append("  ");

          try {
            SerialPort serialPort = (SerialPort) com.open(
                "DWList", OPEN_PORT_TIMEOUT_MILLIS
            );

            text.append(serialPort.getBaudRate()).append(" bps  ");
            text.append(serialPort.getDataBits());
            switch (serialPort.getParity()) {
              case SerialPort.PARITY_NONE -> text.append("N");
              case SerialPort.PARITY_EVEN -> text.append("E");
              case SerialPort.PARITY_MARK -> text.append("M");
              case SerialPort.PARITY_ODD -> text.append("O");
              case SerialPort.PARITY_SPACE -> text.append("S");
              default ->
                  throw new IllegalStateException(
                      "Unexpected value: " + serialPort.getParity()
                  );
            }
            text.append(serialPort.getStopBits());

            if (serialPort.getFlowControlMode()
                == SerialPort.FLOWCONTROL_NONE) {
              text.append("  No flow control  ");
            } else {
              text.append("  ");

              if (
                  (serialPort.getFlowControlMode()
                      & SerialPort.FLOWCONTROL_RTSCTS_IN)
                      == SerialPort.FLOWCONTROL_RTSCTS_IN
              ) {
                text.append("In: RTS/CTS  ");
              }

              if (
                  (serialPort.getFlowControlMode()
                      & SerialPort.FLOWCONTROL_RTSCTS_OUT)
                      == SerialPort.FLOWCONTROL_RTSCTS_OUT
              ) {
                text.append("Out: RTS/CTS  ");
              }

              if (
                  (serialPort.getFlowControlMode()
                      & SerialPort.FLOWCONTROL_XONXOFF_IN)
                      == SerialPort.FLOWCONTROL_XONXOFF_IN
              ) {
                text.append("In: XOn/XOff  ");
              }

              if (
                  (serialPort.getFlowControlMode()
                      & SerialPort.FLOWCONTROL_XONXOFF_OUT)
                      == SerialPort.FLOWCONTROL_XONXOFF_OUT
              ) {
                text.append("Out: XOn/XOff  ");
              }
            }
            text.append(" CD:").append(yn(serialPort.isCD()));
            text.append(" CTS:").append(yn(serialPort.isCTS()));
            text.append(" DSR:").append(yn(serialPort.isDSR()));
            text.append(" DTR:").append(yn(serialPort.isDTR()));
            text.append(" RTS:").append(yn(serialPort.isRTS()));
            text.append("\r\n");
            serialPort.close();
          } catch (PortInUseException e1) {
            text.append("In use by ").append(e1.getMessage()).append("\r\n");
          }
        }
      } catch (Exception e) {
        return new DWCommandResponse(
            false,
            DWDefs.RC_SERVER_IO_EXCEPTION,
            "While gathering serial port info: " + e.getMessage()
        );
      }
    }
    return new DWCommandResponse(text.toString());
  }

  private String yn(final boolean cd) {
    if (cd) {
      return "Y";
    }
    return "n";
  }

  /**
   * Validate command line.
   *
   * @param cmdline command line
   * @return true if valid
   */
  public boolean validate(final String cmdline) {
    return true;
  }
}
