package com.miguelbcr.io.rx_billing_service.entities;

/**
 * Created by miguel on 16/10/2016.
 */

public enum ProductType {
  IN_APP("inapp"), SUBS("subs");

  private String name;

  ProductType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
