package com.miguelbcr.io.rx_billing_service;

import android.util.Log;

class RxBillingServiceLogger {
  private String message;

  static void log(String message) {
    if (BuildConfig.DEBUG) {
      Log.d("RxBillingService", message);
    }
  }

  static void log(String className, String message) {
    //if (BuildConfig.DEBUG) {
    if (true) {
      Log.d("RxBillingService", className + ": " + message);
    }
  }
}
