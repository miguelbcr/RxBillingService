package com.miguelbcr.io.rxbillingservice;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import com.miguelbcr.io.rx_billing_service.*;
import com.miguelbcr.io.rx_billing_service.BuildConfig;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import com.miguelbcr.io.rx_billing_service.entities.Purchase;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.Arrays;

public class RxBillingServiceWrapper {
  private static final String LAST_TOKEN = "LAST_TOKEN";
  private final Object ui;
  private final TextView tvLog;

  public static <T extends Activity> RxBillingServiceWrapper getInstance(T activity,
      TextView tvLog) {
    return new RxBillingServiceWrapper(activity, tvLog);
  }

  public static <T extends Fragment> RxBillingServiceWrapper getInstance(T fragment,
      TextView tvLog) {
    return new RxBillingServiceWrapper(fragment, tvLog);
  }

  private RxBillingServiceWrapper(Object ui, TextView tvLog) {
    this.ui = ui;
    this.tvLog = tvLog;
  }

  Context getContext() {
    return (ui instanceof Fragment) ? ((Fragment) ui).getContext() : ((Activity) ui);
  }

  private String getLastToken() {
    SharedPreferences prefs =
        getContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

    return prefs.getString(LAST_TOKEN, "");
  }

  private void saveToken(String token) {
    SharedPreferences prefs =
        getContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

    prefs.edit().putString(LAST_TOKEN, token).apply();
  }

  private RxBillingService getRxBillingServiceInsatance() {
    if (ui instanceof Fragment) {
      return RxBillingService.getInstance((Fragment) ui, true);
    } else {
      return RxBillingService.getInstance((Activity) ui, true);
    }
  }

  void isBillingSupported(ProductType productType) {
    getRxBillingServiceInsatance().isBillingSupported(productType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(supported -> {
          String text = "";
          text += "Billing supported for " + productType.getName() + " = " + supported + "\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  void skuDetails(ProductType productType, String productId) {
    getRxBillingServiceInsatance().getSkuDetails(productType, Arrays.asList(productId))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(skuDetailsList -> {
          String text = "";
          text += skuDetailsList.get(0).toString() + "\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  void purchase(ProductType productType, String productId, String developerPayload) {
    getRxBillingServiceInsatance().purchase(productType, productId, developerPayload)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchase -> {
          saveToken(purchase.token());
          String text = "";
          text += "You own " + purchase.sku() + " token (" + purchase.token() + ")\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  void consumePurchase() {
    getRxBillingServiceInsatance().consumePurchase(getLastToken())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          saveToken("");
          String text = "";
          text += "Product consumed successfully\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  void purchaseAndConsume(ProductType productType, String productId,
      String developerPayload) {
    getRxBillingServiceInsatance().purchaseAndConsume(productType, productId, developerPayload)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchase -> {
          saveToken("");
          String text = "";
          text += "Product "
              + purchase.sku()
              + " with token ("
              + purchase.token()
              + ") was bought and consumed successfully\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  void purchases(ProductType productType) {
    getRxBillingServiceInsatance().getPurchases(productType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchases -> {
          String text = "";

          text += "## START OF YOUR PURCHASES ##\n\n";
          for (Purchase purchase : purchases) {
            text += purchase.toString() + "\n\n";
          }
          text += "## END OF YOUR PURCHASES ##\n\n";

          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  void manageException(Throwable throwable) {
    String text = "";

    if (throwable instanceof RxBillingServiceException) {
      RxBillingServiceException billingException = (RxBillingServiceException) throwable;
      text +=
          "error (" + billingException.getCode() + ") = " + billingException.getMessage() + "\n\n";
    } else {
      text += "error = " + throwable.getMessage() + "\n\n";
    }

    tvLog.setText(text + tvLog.getText().toString());
  }
}
