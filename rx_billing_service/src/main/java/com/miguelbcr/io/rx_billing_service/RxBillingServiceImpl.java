package com.miguelbcr.io.rx_billing_service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import rx_activity_result2.Result;
import rx_activity_result2.RxActivityResult;

class RxBillingServiceImpl {
  private static final String RESPONSE_CODE = "RESPONSE_CODE";
  private static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
  private static final String INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";
  private static final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
  private static final String INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
  private static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";
  private static final String BUY_INTENT = "BUY_INTENT";
  private static final String DETAILS_LIST = "DETAILS_LIST";

  private final int VERSION = 5;
  private final TargetUi targetUi;
  private Context context;
  private final RxBillingServiceLogger rxBillingServiceLogger;
  private final PublishSubject<Purchase> purchaseSubject = PublishSubject.create();
  private IInAppBillingService appBillingService;
  private ServiceConnection serviceConnection;

  RxBillingServiceImpl(Object targetUiObject, boolean debug) {
    this.targetUi = new TargetUi(targetUiObject);
    this.context = targetUi.getContext();
    this.rxBillingServiceLogger = new RxBillingServiceLogger(debug);
  }

  private String getTargetClassName() {
    return targetUi.fragment() == null ? targetUi.activity().getClass().getSimpleName()
        : targetUi.fragment().getClass().getSimpleName();
  }

  private Single<Ignore> connectService() {
    return Single.create(new SingleOnSubscribe<Ignore>() {
      @Override public void subscribe(final SingleEmitter<Ignore> emitter) throws Exception {
        if (serviceConnection == null) {
          serviceConnection = new ServiceConnection() {
            @Override public void onServiceDisconnected(ComponentName name) {
              rxBillingServiceLogger.log(getTargetClassName(), "Service disconnected");
              appBillingService = null;
              emitter.onError(
                  new RxBillingServiceException(RxBillingServiceError.SERVICE_DISCONNECTED));
            }

            @Override public void onServiceConnected(ComponentName name, final IBinder service) {
              rxBillingServiceLogger.log(getTargetClassName(), "Service connected");
              appBillingService = IInAppBillingService.Stub.asInterface(service);
              emitter.onSuccess(Ignore.Get);
            }
          };

          bindService();
        } else {
          emitter.onSuccess(Ignore.Get);
        }
      }
    });
  }

  private void bindService() {
    rxBillingServiceLogger.log(getTargetClassName(), "Bind service");
    Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
    serviceIntent.setPackage("com.android.vending");
    context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
  }

  private void unbindService() {
    if (appBillingService != null) {
      rxBillingServiceLogger.log(getTargetClassName(), "Unbind service");
      context.unbindService(serviceConnection);
      appBillingService = null;
      context = null;
      targetUi.setUi(null);
    }
  }

  Single<Boolean> isBillingSupported(final ProductType productType) {
    return connectService().flatMap(new Function<Ignore, SingleSource<? extends Boolean>>() {
      @Override public SingleSource<? extends Boolean> apply(Ignore _I) throws Exception {
        boolean isBillingSupported =
            appBillingService.isBillingSupported(VERSION, context.getPackageName(),
                productType.getName()) == RxBillingServiceError.OK;

        rxBillingServiceLogger.log(getTargetClassName(),
            "Is billing (" + productType.getName() + ") supported = " + isBillingSupported);
        return Single.just(isBillingSupported);
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

  Single<List<SkuDetails>> getSkuDetails(final ProductType productType,
      final ArrayList<String> productIds) {
    return connectService().flatMap(new Function<Ignore, SingleSource<? extends Bundle>>() {
      @Override public SingleSource<? extends Bundle> apply(Ignore _I) throws Exception {
        final Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", productIds);

        rxBillingServiceLogger.log(getTargetClassName(),
            "Getting SkuDetails (" + productType.getName() + "): " + TextUtils.join(", ",
                productIds));

        return Single.just(appBillingService.getSkuDetails(VERSION, context.getPackageName(),
            productType.getName(), querySkus));
      }
    }).flatMap(new Function<Bundle, SingleSource<? extends List<SkuDetails>>>() {
      @Override public SingleSource<? extends List<SkuDetails>> apply(Bundle skuDetailsBundle)
          throws Exception {
        int response = skuDetailsBundle.getInt(RESPONSE_CODE, RxBillingServiceError.OK);

        rxBillingServiceLogger.log(getTargetClassName(), "Getting SkuDetails ("
            + productType.getName()
            + ") "
            + RESPONSE_CODE
            + "="
            + response
            + " OK="
            + (response == RxBillingServiceError.OK));

        if (response != RxBillingServiceError.OK) {
          return Single.error(new RxBillingServiceException(response));
        }

        List<SkuDetails> skuDetails = new ArrayList<>();
        List<String> skuDetailsStrings = skuDetailsBundle.getStringArrayList(DETAILS_LIST);

        Gson gson =
            new GsonBuilder().registerTypeAdapterFactory(GsonAdapterFactory.create()).create();

        if (skuDetailsStrings == null) {
          rxBillingServiceLogger.log(getTargetClassName(), "Getting SkuDetails ("
              + productType.getName()
              + ") '"
              + DETAILS_LIST
              + "' bundle arg is null");
          return Single.error(
              new RxBillingServiceException(RxBillingServiceError.PARSING_SKUDETAILS));
        }

        for (String skuDetailString : skuDetailsStrings) {
          try {
            skuDetails.add(SkuDetails.typeAdapter(gson).fromJson(skuDetailString));
          } catch (IOException e) {
            rxBillingServiceLogger.log(getTargetClassName(), "Getting SkuDetails ("
                + productType.getName()
                + ") error parsing SkuDetails object");
            e.printStackTrace();
            return Single.error(
                new RxBillingServiceException(RxBillingServiceError.PARSING_SKUDETAILS));
          }
        }

        return Single.just(skuDetails);
      }
    }).doOnSuccess(new Consumer<List<SkuDetails>>() {
      @Override public void accept(List<SkuDetails> _I) throws Exception {
        unbindService();
      }
    }).doOnError(new Consumer<Throwable>() {
      @Override public void accept(Throwable throwable) throws Exception {
        unbindService();
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

  private SingleTransformer<Ignore, Purchase> purchaseTransformer(final ProductType productType,
      final String productId, final String developerPayload) {
    return new SingleTransformer<Ignore, Purchase>() {
      @Override public SingleSource<Purchase> apply(Single<Ignore> loader) throws Exception {
        return loader.map(new Function<Ignore, Bundle>() {
          @Override public Bundle apply(Ignore _I) throws Exception {
            rxBillingServiceLogger.log(getTargetClassName(),
                "Starting getBuyIntent() (" + productType.getName() + ")");

            return appBillingService.getBuyIntent(VERSION, context.getPackageName(), productId,
                productType.getName(), developerPayload);
          }
        }).flatMap(new Function<Bundle, SingleSource<Purchase>>() {
          @Override public SingleSource<Purchase> apply(Bundle buyBundle) throws Exception {
            int response = buyBundle.getInt(RESPONSE_CODE);

            rxBillingServiceLogger.log(getTargetClassName(), "getBuyIntent() ("
                + productType.getName()
                + ") bundle "
                + RESPONSE_CODE
                + "="
                + response
                + " OK="
                + (response == RxBillingServiceError.OK));

            if (response != RxBillingServiceError.OK) {
              return Single.error(new RxBillingServiceException(response));
            }

            PendingIntent pendingIntent = buyBundle.getParcelable(BUY_INTENT);

            rxBillingServiceLogger.log(getTargetClassName(),
                "Starting pending intent (" + productType.getName() + ")");
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

            return purchaseSubject.toSingle();
          }
        });
      }
    };
  }

  private Observable<Ignore> processActivityResult(Result result) {
    int response = result.data().getIntExtra(RESPONSE_CODE, RxBillingServiceError.OK);

    rxBillingServiceLogger.log(getTargetClassName(),
        "Pending intent bundle result code(" + result.resultCode() + ") OK=" + (result.resultCode()
            == Activity.RESULT_OK) + ", " + RESPONSE_CODE + "=" + response + " OK=" + (response
            == RxBillingServiceError.OK));

    if (result.resultCode() != Activity.RESULT_OK || response != RxBillingServiceError.OK) {
      purchaseSubject.onError(new RxBillingServiceException(response));
      return Observable.just(Ignore.Get);
    }

    String purchaseString = result.data().getStringExtra(INAPP_PURCHASE_DATA);
    String signature = result.data().getStringExtra(INAPP_DATA_SIGNATURE);
    Purchase purchase;
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GsonAdapterFactory.create()).create();

    if (TextUtils.isEmpty(purchaseString)) {
      rxBillingServiceLogger.log(getTargetClassName(),
          "getBuyIntent() '" + INAPP_PURCHASE_DATA + "' bundle arg is null");

      purchaseSubject.onError(
          new RxBillingServiceException(RxBillingServiceError.ITEM_UNAVAILABLE));

      return Observable.just(Ignore.Get);
    }

    try {
      purchase = Purchase.typeAdapter(gson).fromJson(purchaseString);
      purchase.setSignature(signature);
      purchaseSubject.onNext(purchase);
      purchaseSubject.onComplete();
    } catch (IOException e) {
      rxBillingServiceLogger.log(getTargetClassName(),
          "getBuyIntent() error parsing Purchase object");
      e.printStackTrace();
      purchaseSubject.onError(
          new RxBillingServiceException(RxBillingServiceError.PARSING_PURCHASE));
    }

    return Observable.just(Ignore.Get);
  }

  Single<Boolean> consumePurchase(final String purchaseToken) {
    return connectService().flatMap(new Function<Ignore, SingleSource<? extends Boolean>>() {
      @Override public SingleSource<? extends Boolean> apply(Ignore _I) throws Exception {
        int response =
            appBillingService.consumePurchase(VERSION, context.getPackageName(), purchaseToken);

        rxBillingServiceLogger.log(getTargetClassName(),
            "Consume purchase " + RESPONSE_CODE + "=" + response + " OK=" + (response
                == RxBillingServiceError.OK) + " token=" + purchaseToken);

        if (response != RxBillingServiceError.OK) {
          return Single.error(new RxBillingServiceException(response));
        }

        return Single.just(true);
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

  Single<Purchase> purchaseAndConsume(final ProductType productType, String productId,
      String developerPayload) {
    return connectService().compose(purchaseTransformer(productType, productId, developerPayload))
        .flatMap(new Function<Purchase, SingleSource<? extends Purchase>>() {
          @Override public SingleSource<? extends Purchase> apply(Purchase purchase)
              throws Exception {
            int response = appBillingService.consumePurchase(VERSION, context.getPackageName(),
                purchase.token());

            rxBillingServiceLogger.log(getTargetClassName(),
                "Consume purchase " + RESPONSE_CODE + "=" + response + " OK=" + (response
                    == RxBillingServiceError.OK) + " token=" + purchase.token());

            if (response != RxBillingServiceError.OK) {
              return Single.error(new RxBillingServiceException(response));
            }

            return Single.just(purchase);
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

  private SingleTransformer<Ignore, PurchasesToken> getPurchasesTransformer(
      final ProductType productType, final String continuationToken) {
    return new SingleTransformer<Ignore, PurchasesToken>() {
      @Override public SingleSource<PurchasesToken> apply(Single<Ignore> loader) throws Exception {
        return loader.flatMap(new Function<Ignore, SingleSource<? extends Bundle>>() {
          @Override public SingleSource<? extends Bundle> apply(Ignore _I) throws Exception {
            rxBillingServiceLogger.log(getTargetClassName(), "Getting purchases ("
                + productType.getName()
                + ") continuation token="
                + continuationToken);

            return Single.just(appBillingService.getPurchases(VERSION, context.getPackageName(),
                productType.getName(), continuationToken));
          }
        }).flatMap(new Function<Bundle, SingleSource<? extends PurchasesToken>>() {
          @Override public SingleSource<? extends PurchasesToken> apply(Bundle purchasesBundle)
              throws Exception {
            int response = purchasesBundle.getInt(RESPONSE_CODE);

            rxBillingServiceLogger.log(getTargetClassName(), "Getting purchases ("
                + productType.getName()
                + ") "
                + RESPONSE_CODE
                + "="
                + response
                + " OK="
                + (response == RxBillingServiceError.OK));

            if (response != RxBillingServiceError.OK) {
              return Single.error(new RxBillingServiceException(response));
            }

            List<String> purchasesStrings =
                purchasesBundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST);
            List<String> signatures = purchasesBundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST);
            String continuationToken = purchasesBundle.getString(INAPP_CONTINUATION_TOKEN, null);

            Gson gson =
                new GsonBuilder().registerTypeAdapterFactory(GsonAdapterFactory.create()).create();

            if (purchasesStrings == null || signatures == null) {
              rxBillingServiceLogger.log(getTargetClassName(), "Getting purchases ("
                  + productType.getName()
                  + ") '"
                  + INAPP_PURCHASE_DATA_LIST
                  + "' bundle arg is null");
              return Single.error(
                  new RxBillingServiceException(RxBillingServiceError.PARSING_PURCHASE));
            }

            List<Purchase> purchases = new ArrayList<>();
            for (int i = 0; i < purchasesStrings.size(); i++) {
              try {
                String purchaseString = purchasesStrings.get(i);
                String signature = signatures.get(i);
                Purchase purchase = Purchase.typeAdapter(gson).fromJson(purchaseString);
                purchase.setSignature(signature);
                purchases.add(purchase);
              } catch (IOException e) {
                rxBillingServiceLogger.log(getTargetClassName(), "Getting purchases ("
                    + productType.getName()
                    + ") error parsing Purchase object");
                e.printStackTrace();
                return Single.error(
                    new RxBillingServiceException(RxBillingServiceError.PARSING_PURCHASE));
              }
            }

            return Single.just(new PurchasesToken(purchases, continuationToken));
          }
        });
      }
    };
  }

  Single<List<Purchase>> getPurchases(final ProductType productType) {
    final List<Purchase> purchases = new ArrayList<>();
    return connectService().compose(getPurchasesTransformer(productType, null))
        .toObservable()
        .repeat()
        .takeUntil(new Predicate<PurchasesToken>() {
          @Override public boolean test(PurchasesToken purchasesToken) throws Exception {
            return purchasesToken.getContinuationToken() == null;
          }
        })
        .doOnNext(new Consumer<PurchasesToken>() {
          @Override public void accept(PurchasesToken purchasesToken) throws Exception {
            purchases.addAll(purchasesToken.getPurchases());
          }
        })
        .map(new Function<PurchasesToken, List<Purchase>>() {
          @Override public List<Purchase> apply(PurchasesToken purchasesToken) throws Exception {
            rxBillingServiceLogger.log(getTargetClassName(),
                "Getting purchases (" + productType.getName() + ") size = " + purchases.size());
            return purchases;
          }
        })
        .toSingle()
        .doOnSuccess(new Consumer<List<Purchase>>() {
          @Override public void accept(List<Purchase> _I) throws Exception {
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
