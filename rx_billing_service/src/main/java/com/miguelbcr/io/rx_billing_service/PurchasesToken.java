package com.miguelbcr.io.rx_billing_service;

import com.miguelbcr.io.rx_billing_service.entities.Purchase;
import java.util.List;

class PurchasesToken {
  private final List<Purchase> purchases;
  private final String continuationToken;

  PurchasesToken(List<Purchase> purchases, String continuationToken) {
    this.purchases = purchases;
    this.continuationToken = continuationToken;
  }

  public List<Purchase> getPurchases() {
    return purchases;
  }

  public String getContinuationToken() {
    return continuationToken;
  }
}