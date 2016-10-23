package com.miguelbcr.io.rx_billing_service;

import android.util.Log;

class RxBillingServiceLogger {
  private final boolean debug;

  public RxBillingServiceLogger(boolean debug) {
    this.debug = debug;
  }

  void log(String message) {
    if (debug) {
      Log.d("RxBillingService", message);
    }
  }

  void log(String className, String message) {
    if (debug) {
      Log.d("RxBillingService", className + " > " + message);
    }
  }
}
