package com.miguelbcr.io.rx_billing_service;

import android.app.Activity;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.TestSubscriber;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ExampleUnitTest {
  @Mock RxBillingService rxBillingService;
  @Mock RxBillingServiceImpl rxBillingServiceImpl;
  @Mock Activity activity;

  @Before public void init() {
    activity = new Activity();
    rxBillingService = RxBillingService.getInstance(activity, false);
  }

  @Test public void Is_Billing_Supported() {
    TestSubscriber<Boolean> ts = new TestSubscriber<>();
    rxBillingService.isBillingSupported(ProductType.IN_APP).toFlowable().subscribe(ts);
    ts.assertValue(true);
  }

  @Test public void Is_Billing_Supported_2() {
    final AtomicReference<Boolean> v = new AtomicReference<>();
    rxBillingService.isBillingSupported(ProductType.IN_APP)
        .subscribe(new SingleObserver<Boolean>() {
          @Override public void onSubscribe(Disposable d) {

          }

          @Override public void onSuccess(Boolean value) {
            v.set(value);
          }

          @Override public void onError(Throwable e) {

          }
        });
    assertEquals(true, v.get());
  }
}