package com.miguelbcr.io.rxbillingservice;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.miguelbcr.io.rx_billing_service.RxBillingService;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
  private TextView textView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    View btBuy = findViewById(R.id.bt_buy);
    textView = (TextView) findViewById(R.id.textView);

    btBuy.setOnClickListener(v -> {
      final String developerPayload = String.valueOf(System.currentTimeMillis());
      purchaseAndConsume(ProductType.IN_APP, "android.test.purchased", developerPayload);
    });

    //isBillingSupported(ProductType.IN_APP);
    //isBillingSupported(ProductType.SUBS);

  }

  private void isBillingSupported(ProductType productType) {
    RxBillingService.getInstance(this)
        .isBillingSupported(productType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(supported -> {
          String text = textView.getText().toString();
          text += "Billing supported (" + productType.getName() + ") = " + supported + "\n";
          textView.setText(text);
        }, throwable -> {
          String text = textView.getText().toString();
          text += "error = " + throwable.getMessage() + "\n";
          textView.setText(text);
        });
  }

  private void purchaseAndConsume(ProductType productType, String productId, String developerPayload) {
    RxBillingService.getInstance(this)
        .purchaseAndConsume(productType, productId, developerPayload)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchase -> {
          String text = textView.getText().toString();
          text += purchase.sku() + " consumed successfully\n";
          textView.setText(text);
        }, throwable -> {
          String text = textView.getText().toString();
          text += "error = " + throwable.getMessage() + "\n";
          textView.setText(text);
        });
  }

  private void consumePurchase(String token) {
    RxBillingService.getInstance(this)
        .consumePurchase(token)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          String text = textView.getText().toString();
          text += "Product consumed successfully = " + success + "\n";
          textView.setText(text);
        }, throwable -> {
          String text = textView.getText().toString();
          text += "error = " + throwable.getMessage() + "\n";
          textView.setText(text);
        });
  }
}
