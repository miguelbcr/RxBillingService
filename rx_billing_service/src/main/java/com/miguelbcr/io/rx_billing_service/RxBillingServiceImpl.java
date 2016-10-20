package com.miguelbcr.io.rx_billing_service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miguelbcr.io.rx_billing_service.entities.Ignore;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import com.miguelbcr.io.rx_billing_service.entities.Purchase;
import com.miguelbcr.io.rx_billing_service.entities.SkuDetails;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import rx_activity_result2.Result;
import rx_activity_result2.RxActivityResult;

class RxBillingServiceImpl {
  private final int VERSION = 5;
  private final TargetUi targetUi;
  private Context context;
  private final PublishSubject<Purchase> purchaseSubject = PublishSubject.create();
  private IInAppBillingService appBillingService;
  private ServiceConnection serviceConnection;

  RxBillingServiceImpl(Object targetUiObject) {
    this.targetUi = new TargetUi(targetUiObject);
    this.context = targetUi.getContext();
  }

  private String getTargetClassName() {
    return targetUi.fragment() == null ? targetUi.activity().getClass().getSimpleName()
        : targetUi.fragment().getClass().getSimpleName();
  }

  private Single<Boolean> connectService() {
    return Single.create(new SingleOnSubscribe<Boolean>() {
      @Override public void subscribe(final SingleEmitter<Boolean> emitter) throws Exception {
        if (serviceConnection == null) {
          serviceConnection = new ServiceConnection() {
            @Override public void onServiceDisconnected(ComponentName name) {
              RxBillingServiceLogger.log(getTargetClassName(), "Service Disconnected");
              appBillingService = null;
              emitter.onError(
                  new RxBillingServiceException(RxBillingServiceError.SERVICE_DISCONNECTED));
            }

            @Override public void onServiceConnected(ComponentName name, final IBinder service) {
              RxBillingServiceLogger.log(getTargetClassName(), "Service Connected");
              appBillingService = IInAppBillingService.Stub.asInterface(service);
              emitter.onSuccess(true);
            }
          };

          bindService();
        } else {
          emitter.onSuccess(true);
        }
      }
    });
  }

  private void bindService() {
    RxBillingServiceLogger.log(getTargetClassName(), "Bind Service");
    Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
    serviceIntent.setPackage("com.android.vending");
    context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
  }

  private void unbindService() {
    if (appBillingService != null) {
      RxBillingServiceLogger.log(getTargetClassName(), "Unbind Service");
      context.unbindService(serviceConnection);
      appBillingService = null;
      context = null;
      targetUi.setUi(null);
    }
  }

  Single<Boolean> isBillingSupported(final ProductType productType) {
    return connectService().flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
      @Override public SingleSource<? extends Boolean> apply(Boolean isServiceConnected)
          throws Exception {
        if (isServiceConnected) {
          boolean isBillingSupported =
              appBillingService.isBillingSupported(VERSION, context.getPackageName(),
                  productType.getName()) == RxBillingServiceError.OK;

          RxBillingServiceLogger.log(getTargetClassName(),
              "is Billing Supported = " + isBillingSupported);
          return Single.just(isBillingSupported);
        } else {
          return Single.error(
              new RxBillingServiceException(RxBillingServiceError.SERVICE_DISCONNECTED));
        }
      }
    }).doOnSuccess(new Consumer<Boolean>() {
      @Override public void accept(Boolean _I) throws Exception {
        unbindService();
      }
    }).doOnError(new Consumer<Throwable>() {
      @Override public void accept(Throwable throwable) throws Exception {
        unbindService();
      }
    });
  }

  Single<List<SkuDetails>> getSkuDetails(final ProductType productType, List<String> productIds) {
    final Bundle querySkus = new Bundle();
    querySkus.putStringArrayList("ITEM_ID_LIST", (ArrayList<String>) productIds);

    return Single.create(new SingleOnSubscribe<Bundle>() {
      @Override public void subscribe(SingleEmitter<Bundle> emitter) {
        try {
          emitter.onSuccess(appBillingService.getSkuDetails(VERSION, context.getPackageName(),
              productType.getName(), querySkus));
        } catch (RemoteException e) {
          e.printStackTrace();
          emitter.onError(e);
        }
      }
    }).flatMap(new Function<Bundle, SingleSource<? extends List<SkuDetails>>>() {
      @Override public SingleSource<? extends List<SkuDetails>> apply(Bundle skuDetailsBundle)
          throws Exception {
        int response = skuDetailsBundle.getInt(SkuDetails.RESPONSE_CODE);

        if (response == RxBillingServiceError.OK) {
          List<SkuDetails> skuDetails = new ArrayList<>();
          List<String> skuDetailsStrings =
              skuDetailsBundle.getStringArrayList(SkuDetails.DETAILS_LIST);

          for (String skuDetailString : skuDetailsStrings) {
            Gson gson =
                new GsonBuilder().registerTypeAdapterFactory(GsonAdapterFactory.create()).create();
            try {
              skuDetails.add(SkuDetails.typeAdapter(gson).fromJson(skuDetailString));
            } catch (IOException e) {
              e.printStackTrace();
              return Single.error(
                  new RxBillingServiceException(RxBillingServiceError.PARSING_SKUDETAILS));
            }
          }

          return Single.just(skuDetails);
        } else {
          return Single.error(new RxBillingServiceException(response));
        }
      }
    });
  }

  Single<Purchase> purchase(ProductType productType, String productId, String developerPayload) {
    return connectService().compose(purchaseTransformer(productType, productId, developerPayload))
        .doOnSuccess(new Consumer<Purchase>() {
          @Override public void accept(Purchase _I) throws Exception {
            unbindService();
          }
        })
        .doOnError(new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) throws Exception {
            unbindService();
          }
        });
  }

  private SingleTransformer<Boolean, Purchase> purchaseTransformer(final ProductType productType,
      final String productId, final String developerPayload) {
    return new SingleTransformer<Boolean, Purchase>() {
      @Override public SingleSource<Purchase> apply(Single<Boolean> loader) throws Exception {
        return loader.map(new Function<Boolean, Bundle>() {
          @Override public Bundle apply(Boolean isServiceConnected) throws Exception {
            Bundle buyBundle = null;

            if (isServiceConnected) {
              buyBundle =
                  appBillingService.getBuyIntent(VERSION, context.getPackageName(), productId,
                      productType.getName(), developerPayload);
              RxBillingServiceLogger.log(getTargetClassName(),
                  "getBuyIntent() returns = " + buyBundle);
            } else {
              Single.error(
                  new RxBillingServiceException(RxBillingServiceError.SERVICE_DISCONNECTED));
            }

            return buyBundle;
          }
        }).flatMap(new Function<Bundle, SingleSource<Purchase>>() {
          @Override public SingleSource<Purchase> apply(Bundle buyBundle) throws Exception {
            int response = buyBundle.getInt(Purchase.RESPONSE_CODE);
            RxBillingServiceLogger.log(getTargetClassName(),
                "getBuyIntent() bundle response code(" + response + ") OK = " + (response
                    == RxBillingServiceError.OK));

            if (response == RxBillingServiceError.OK) {
              PendingIntent pendingIntent = buyBundle.getParcelable(Purchase.BUY_INTENT);

              RxBillingServiceLogger.log(getTargetClassName(), "launch getBuyIntent()");
              if (targetUi.fragment() == null) {
                RxActivityResult.on(targetUi.activity())
                    .startIntentSender(pendingIntent.getIntentSender(), new Intent(), 0, 0, 0)
                    .flatMap(new Function<Result<Activity>, ObservableSource<Ignore>>() {
                      @Override public ObservableSource<Ignore> apply(Result<Activity> result)
                          throws Exception {
                        return processActivityResult(result);
                      }
                    })
                    .subscribe();
              } else {
                RxActivityResult.on(targetUi.fragment())
                    .startIntentSender(pendingIntent.getIntentSender(), new Intent(), 0, 0, 0)
                    .flatMap(new Function<Result<Fragment>, ObservableSource<?>>() {
                      @Override public ObservableSource<?> apply(Result<Fragment> result)
                          throws Exception {
                        return processActivityResult(result);
                      }
                    })
                    .subscribe();
              }
            } else {
              purchaseSubject.onError(new RxBillingServiceException(response));
            }

            return purchaseSubject.toSingle();
          }
        });
      }
    };
  }

  private Observable<Ignore> processActivityResult(Result result) {
    RxBillingServiceLogger.log(getTargetClassName(),
        "getBuyIntent() result code(" + result.resultCode() + ") OK = " + (result.resultCode()
            == Activity.RESULT_OK));

    if (result.resultCode() == Activity.RESULT_OK) {
      String purchaseString = result.data().getStringExtra(Purchase.INAPP_PURCHASE_DATA);
      String signature = result.data().getStringExtra(Purchase.INAPP_DATA_SIGNATURE);
      Purchase purchase;
      Gson gson =
          new GsonBuilder().registerTypeAdapterFactory(GsonAdapterFactory.create()).create();
      try {
        purchase = Purchase.typeAdapter(gson).fromJson(purchaseString);
        purchase.setSignature(signature);
        purchaseSubject.onNext(purchase);
        purchaseSubject.onComplete();
      } catch (IOException e) {
        e.printStackTrace();
        purchaseSubject.onError(
            new RxBillingServiceException(RxBillingServiceError.PARSING_PURCHASE));
      }
    } else {
      purchaseSubject.onError(new RxBillingServiceException(RxBillingServiceError.USER_CANCELED));
    }

    return Observable.just(Ignore.Get);
  }

  /**
   * Once an in-app product is purchased, it is considered to be "owned" and cannot be purchased
   * from Google Play. You must send a consumption request for the in-app product before Google
   * Play
   * makes it available for purchase again.<br/><br/>
   *
   * Important: Managed in-app products are consumable, but subscriptions are not.
   *
   * @param token The purchaseToken is part of the data returned in the INAPP_PURCHASE_DATA String
   * by the Google Play service following a successful purchase request
   * @return true if success, false otherwise
   */
  Single<Boolean> consumePurchase(final String token) {
    return connectService().flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
      @Override public SingleSource<? extends Boolean> apply(Boolean isServiceConnected)
          throws Exception {
        if (isServiceConnected) {
          int response =
              appBillingService.consumePurchase(VERSION, context.getPackageName(), token);

          if (response == RxBillingServiceError.OK) {
            RxBillingServiceLogger.log(getTargetClassName(), "token consumed: " + token);
            return Single.just(true);
          } else {
            return Single.error(new RxBillingServiceException(response));
          }
        } else {
          return Single.error(
              new RxBillingServiceException(RxBillingServiceError.SERVICE_DISCONNECTED));
        }
      }
    }).doOnSuccess(new Consumer<Boolean>() {
      @Override public void accept(Boolean _I) throws Exception {
        unbindService();
      }
    }).doOnError(new Consumer<Throwable>() {
      @Override public void accept(Throwable throwable) throws Exception {
        unbindService();
      }
    });
  }

  Single<Purchase> purchaseAndConsume(ProductType productType, String productId,
      String developerPayload) {
    return connectService().compose(purchaseTransformer(productType, productId, developerPayload))
        .flatMap(new Function<Purchase, SingleSource<? extends Purchase>>() {
          @Override public SingleSource<? extends Purchase> apply(Purchase purchase)
              throws Exception {
            int response = appBillingService.consumePurchase(VERSION, context.getPackageName(),
                purchase.token());

            if (response == RxBillingServiceError.OK) {
              RxBillingServiceLogger.log(getTargetClassName(),
                  "token consumed: " + purchase.token());
              return Single.just(purchase);
            } else {
              return Single.error(new RxBillingServiceException(response));
            }
          }
        })
        .doOnSuccess(new Consumer<Purchase>() {
          @Override public void accept(Purchase _I) throws Exception {
            unbindService();
          }
        })
        .doOnError(new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) throws Exception {
            unbindService();
          }
        });
  }
}
