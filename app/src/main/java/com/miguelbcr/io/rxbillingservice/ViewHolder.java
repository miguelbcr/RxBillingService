package com.miguelbcr.io.rxbillingservice;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;

public class ViewHolder {
  private final Object ui;
  private String productId;
  private ProductType productType;

  public static <T extends Activity> ViewHolder getInstance(T activity) {
    return new ViewHolder(activity);
  }

  public static <T extends Fragment> ViewHolder getInstance(T fragment) {
    return new ViewHolder(fragment);
  }

  private ViewHolder(Object ui) {
    this.ui = ui;
  }

  private Context getContext() {
    return (ui instanceof Fragment) ? ((Fragment) ui).getContext() : ((Activity) ui);
  }

  private RxBillingServiceFactory getRxBillingServiceInsatance(TextView tvLog) {
    if (ui instanceof Fragment) {
      return RxBillingServiceFactory.getInstance((Fragment) ui, tvLog);
    } else {
      return RxBillingServiceFactory.getInstance((Activity) ui, tvLog);
    }
  }

  public void bind(View view) {
    View btSkuDetails = view.findViewById(R.id.bt_sku_details);
    View btBuy = view.findViewById(R.id.bt_buy);
    View btConsume = view.findViewById(R.id.bt_consume);
    View btBuyConsume = view.findViewById(R.id.bt_buy_consume);
    View btMyPurchases = view.findViewById(R.id.bt_my_purchases);
    View btSupported = view.findViewById(R.id.bt_billing_supported);
    View btClear = view.findViewById(R.id.bt_clear);
    Spinner spProductsTypes = (Spinner) view.findViewById(R.id.spinner_product_types);
    Spinner spProductsIds = (Spinner) view.findViewById(R.id.spinner_product_ids);
    TextView tvLog = (TextView) view.findViewById(R.id.tv_log);

    final RxBillingServiceFactory rxBillingServiceFactory = getRxBillingServiceInsatance(tvLog);

    initProductTypes(spProductsTypes);
    initProductIds(spProductsIds);

    btClear.setOnClickListener(v -> tvLog.setText(""));

    btSkuDetails.setOnClickListener(v -> {
      rxBillingServiceFactory.skuDetails(productType, productId);
    });

    btBuy.setOnClickListener(v -> {
      final String developerPayload = String.valueOf(System.currentTimeMillis());
      rxBillingServiceFactory.purchase(productType, productId, developerPayload);
    });

    btConsume.setOnClickListener(v -> rxBillingServiceFactory.consumePurchase());

    btBuyConsume.setOnClickListener(v -> {
      final String developerPayload = String.valueOf(System.currentTimeMillis());
      rxBillingServiceFactory.purchaseAndConsume(productType, productId, developerPayload);
    });

    btMyPurchases.setOnClickListener(v -> rxBillingServiceFactory.purchases(productType));

    btSupported.setOnClickListener(v -> rxBillingServiceFactory.isBillingSupported(productType));
  }

  private void initProductTypes(Spinner spinner) {
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getContext(), R.array.product_types,
            android.R.layout.simple_spinner_item);
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
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getContext(), R.array.product_ids,
            android.R.layout.simple_spinner_item);
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
}
