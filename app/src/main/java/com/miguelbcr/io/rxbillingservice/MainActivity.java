package com.miguelbcr.io.rxbillingservice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.miguelbcr.io.rx_billing_service.RxBillingService;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    TextView textView = (TextView) findViewById(R.id.textView);

    RxBillingService.getInstance(this)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(RxBillingService.isBillingSupported(ProductType.IN_APP))
        .flatMap(supported -> {
          Log.e("XXXXXXXXXXXXXXX", "MainActivity: Billing supported = " + supported);
          textView.setText("MainActivity: Billing supported = " + supported);

          if (supported) {
            return RxBillingService.getInstance(this)
                .compose(RxBillingService.isBillingSupported(ProductType.IN_APP))
                .flatMap(Observable::just);
          } else {
            return Observable.just(false);
          }
        })
        .subscribe(supported -> {
              Log.e("XXXXXXXXXXXXXXX", "MainActivity2: Billing supported = " + supported);
              textView.setText("MainActivity2: Billing supported = " + supported);
            },
              throwable -> {
                throwable.printStackTrace();
                textView.setText("MainActivity: error = " + throwable.getMessage());
              }
        );
  }
}
