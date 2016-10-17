package com.miguelbcr.io.rxbillingservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.miguelbcr.io.rx_billing_service.RxBillingService;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RootFragment extends Fragment {
  private TextView textView;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_root, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    textView = (TextView) view.findViewById(R.id.textView);

    isBillingSupported(ProductType.IN_APP);
    isBillingSupported(ProductType.SUBS);
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
          throwable.printStackTrace();
          String text = textView.getText().toString();
          text += "error = " + throwable.getMessage() + "\n";
          textView.setText(text);
        });
  }
}