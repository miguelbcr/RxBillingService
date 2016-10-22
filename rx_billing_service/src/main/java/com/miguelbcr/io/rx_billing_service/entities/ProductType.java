package com.miguelbcr.io.rx_billing_service.entities;

/**
 * Created by miguel on 16/10/2016.
 */

public enum ProductType {
  /**
   * For an in-app product
   */
  IN_APP("inapp"),
  /**
   *  For subscriptions
   */
  SUBS("subs");

  private String name;

  ProductType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
