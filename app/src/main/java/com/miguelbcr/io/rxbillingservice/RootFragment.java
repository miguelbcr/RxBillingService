package com.miguelbcr.io.rxbillingservice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.miguelbcr.io.rx_billing_service.RxBillingService;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RootFragment extends Fragment {

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_root, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    TextView textView = (TextView) view.findViewById(R.id.textView);

    RxBillingService.getInstance(this)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(RxBillingService.isBillingSupported(ProductType.IN_APP))
        //.flatMap(rxBillingService -> rxBillingService.isBillingSupported_2(ProductType.IN_APP))
        .subscribe(supported -> {
              Log.e("XXXXXXXXXXXXXXX", "RootFragment: Billing supported = " + supported);
              textView.setText("RootFragment: Billing supported = " + supported);
            },
            throwable -> {
              throwable.printStackTrace();
              textView.setText("RootFragment: error = " + throwable.getMessage());
            }
        );
  }
}
