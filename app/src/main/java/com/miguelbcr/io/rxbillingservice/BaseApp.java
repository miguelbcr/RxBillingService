package com.miguelbcr.io.rxbillingservice;

import android.app.Application;
import com.miguelbcr.io.rx_billing_service.RxBillingService;

/**
 * Created by miguel on 16/10/2016.
 */

public class BaseApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    RxBillingService.register(this);
  }
}
