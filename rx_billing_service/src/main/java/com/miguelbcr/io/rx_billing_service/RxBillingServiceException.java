/*
 * Copyright 2016 Miguel Garcia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
