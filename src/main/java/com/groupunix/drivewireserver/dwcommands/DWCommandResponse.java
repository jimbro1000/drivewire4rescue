package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;

public class DWCommandResponse {
  /**
   * Success status.
   */
  private final boolean successStatus;
  /**
   * Response code.
   */
  private final byte responseCodeByte;
  /**
   * Response text.
   */
  private String responseAsText;
  /**
   * Response byte array.
   */
  private byte[] responseAsBytes;
  /**
   * Byte array used.
   */
  private boolean useBytes = false;

  /**
   * Command response constructor.
   *
   * <p>
   *   Create response with custom
   *   success, code and text
   * </p>
   * @param success success status
   * @param responseCode response code
   * @param responseText response text
   */
  public DWCommandResponse(
      final boolean success,
      final byte responseCode,
      final String responseText
  ) {
    this.successStatus = success;
    this.responseCodeByte = responseCode;
    this.responseAsText = responseText;
  }

  /**
   * Command response constructor.
   *
   * <p>
   *   Create response with custom
   *   text, assumes success is true
   * </p>
   * @param responseText response text
   */
  public DWCommandResponse(final String responseText) {
    this.successStatus = true;
    this.responseCodeByte = DWDefs.RC_SUCCESS;
    this.responseAsText = responseText;
  }

  /**
   * Command response constructor.
   *
   * <p>
   *   Create response with byte
   *   array, assumes success is true
   * </p>
   * @param responseBytes response byte array
   */
  public DWCommandResponse(final byte[] responseBytes) {
    this.successStatus = true;
    this.responseCodeByte = DWDefs.RC_SUCCESS;
    this.responseAsBytes = responseBytes;
    this.useBytes = true;
  }

  /**
   * Returns success status.
   *
   * @return success
   */
  public boolean getSuccess() {
    return this.successStatus;
  }

  /**
   * Returns response code.
   *
   * @return response code
   */
  public byte getResponseCode() {
    return this.responseCodeByte;
  }

  /**
   * Returns string response.
   *
   * @return response string
   */
  public String getResponseText() {
    return this.responseAsText;
  }

  /**
   * Returns byte array response.
   *
   * @return byte array
   */
  public byte[] getResponseBytes() {
    return this.responseAsBytes;
  }

  /**
   * Returns true if the response is provided as a byte array.
   *
   * @return using bytes
   */
  public boolean isUseBytes() {
    return useBytes;
  }
}
