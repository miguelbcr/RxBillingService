package com.miguelbcr.io.rx_billing_service;

/**
 * Created by miguel on 20/10/2016.
 */

public class RxBillingServiceException extends Throwable {
  private final int code;
  private final String message;

  RxBillingServiceException(int errorCode) {
    this.code = errorCode;
    this.message = RxBillingServiceError.getMessage(code);
  }

  /**
   * See {@link RxBillingServiceError}
   * @return
   */
  public int getCode() {
    return code;
  }

  @Override public String getMessage() {
    return message;
  }
}
