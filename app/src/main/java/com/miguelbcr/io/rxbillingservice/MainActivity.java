package com.miguelbcr.io.rxbillingservice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.miguelbcr.io.rx_billing_service.RxBillingService;
import com.miguelbcr.io.rx_billing_service.RxBillingServiceException;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import com.miguelbcr.io.rx_billing_service.entities.Purchase;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
  private TextView tvLog;
  private String productId, token;  // TODO guardar token en preferences
  private ProductType productType;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    View btSkuDetails = findViewById(R.id.bt_sku_details);
    View btBuy = findViewById(R.id.bt_buy);
    View btConsume = findViewById(R.id.bt_consume);
    View btBuyConsume = findViewById(R.id.bt_buy_consume);
    View btMyPurchases = findViewById(R.id.bt_my_purchases);
    View btSupported = findViewById(R.id.bt_billing_supported);
    View btClear = findViewById(R.id.bt_clear);
    Spinner spProductsTypes = (Spinner) findViewById(R.id.spinner_product_types);
    Spinner spProductsIds = (Spinner) findViewById(R.id.spinner_product_ids);
    tvLog = (TextView) findViewById(R.id.tv_log);

    initProductTypes(spProductsTypes);
    initProductIds(spProductsIds);

    btClear.setOnClickListener(v -> tvLog.setText(""));

    btSkuDetails.setOnClickListener(v -> {
      skuDetails(productType, productId);
    });

    btBuy.setOnClickListener(v -> {
      final String developerPayload = String.valueOf(System.currentTimeMillis());
      purchase(productType, productId, developerPayload);
    });

    btConsume.setOnClickListener(v -> consumePurchase());

    btBuyConsume.setOnClickListener(v -> {
      final String developerPayload = String.valueOf(System.currentTimeMillis());
      purchaseAndConsume(productType, productId, developerPayload);
    });

    btMyPurchases.setOnClickListener(v -> purchases(productType));

    btSupported.setOnClickListener(v -> isBillingSupported(productType));
  }

  private void initProductTypes(Spinner spinner) {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.product_types, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        productType = position == 0 ? ProductType.IN_APP : ProductType.SUBS;
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    productType = ProductType.IN_APP;
  }

  private void initProductIds(Spinner spinner) {
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.product_ids, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        productId = adapter.getItem(position).toString();
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  private void isBillingSupported(ProductType productType) {
    RxBillingService.getInstance(this)
        .isBillingSupported(productType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(supported -> {
          String text = "";
          text += "Billing supported for " + productType.getName() + " = " + supported + "\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  private void skuDetails(ProductType productType, String productId) {
    RxBillingService.getInstance(this)
        .getSkuDetails(productType, Arrays.asList(productId))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(skuDetailsList -> {
          String text = "";
          text += skuDetailsList.get(0).toString() + "\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  private void purchase(ProductType productType, String productId, String developerPayload) {
    RxBillingService.getInstance(this)
        .purchase(productType, productId, developerPayload)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchase -> {
          token = purchase.token();
          String text = "";
          text += "You own " + purchase.sku() + " token (" + purchase.token() + ")\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  private void consumePurchase() {
    RxBillingService.getInstance(this)
        .consumePurchase(token)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          token = "";
          String text = "";
          text += "Product consumed successfully\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  private void purchaseAndConsume(ProductType productType, String productId, String developerPayload) {
    RxBillingService.getInstance(this)
        .purchaseAndConsume(productType, productId, developerPayload)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchase -> {
          token = "";
          String text = "";
          text += "Product " + purchase.sku() + " with token (" + purchase.token() + ") was bought and consumed successfully\n\n";
          tvLog.setText(text + tvLog.getText().toString());
        }, this::manageException);
  }

  private void purchases(ProductType productType) {
    RxBillingService.getInstance(this)
        .getPurchases(productType)
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

  private void manageException(Throwable throwable) {
    String text = "";

    if (throwable instanceof RxBillingServiceException) {
      RxBillingServiceException billingException = (RxBillingServiceException) throwable;
      text += "error (" + billingException.getCode() + ") = " + billingException.getMessage() + "\n\n";
    } else {
      text += "error = " + throwable.getMessage() + "\n\n";
    }

    tvLog.setText(text + tvLog.getText().toString());

  }
}
