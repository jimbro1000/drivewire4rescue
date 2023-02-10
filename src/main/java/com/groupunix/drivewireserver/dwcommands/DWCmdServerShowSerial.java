package com.groupunix.drivewireserver.dwcommands;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

import java.util.Enumeration;

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
   * @param parent   parent command
   */
  public DWCmdServerShowSerial(final DWProtocol protocol,
                               final DWCommand parent) {
    super();
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
    final StringBuilder text = new StringBuilder();
    text.append("Server serial devices:\r\n\r\n");

    @SuppressWarnings("unchecked")
    final Enumeration<CommPortIdentifier> thePorts
        = CommPortIdentifier.getPortIdentifiers();

    while (thePorts.hasMoreElements()) {
      try {
        final CommPortIdentifier com = thePorts.nextElement();
        if (com.getPortType() == CommPortIdentifier.PORT_SERIAL) {
          text.append(com.getName()).append("  ");

          try {
            final SerialPort serialPort = (SerialPort) com.open(
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
            text.append(" CD:").append(yesOrNo(serialPort.isCD()));
            text.append(" CTS:").append(yesOrNo(serialPort.isCTS()));
            text.append(" DSR:").append(yesOrNo(serialPort.isDSR()));
            text.append(" DTR:").append(yesOrNo(serialPort.isDTR()));
            text.append(" RTS:").append(yesOrNo(serialPort.isRTS()));
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

  private String yesOrNo(final boolean bool) {
    return bool ? "Y" : "n";
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
