# RxBillingService

RxJava extension for Android [In-App Billing](https://developer.android.com/google/play/billing/index.html) [version 3](https://developer.android.com/google/play/billing/api.html) .

Currently is only tested for `inapp` products, not `subs`

## Features:
 
- Works in activities and fragments.
- Reactive calls, so you don't need `onActivityResult()` anymore.
- You will get the proper object (`Purchase`, `SkuDetails`, `Boolean`) or `RxBillingServiceException` if something went wrong.


## Setup
Add the JitPack repository in your build.gradle (top level module):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

And add next dependencies in the build.gradle of the module:
```gradle
dependencies {
    compile "com.github.miguelbcr:RxBillingService:0.0.1"
    compile "io.reactivex:rxjava:1.1.10"
    compile 'io.reactivex.rxjava2:rxandroid:2.0.0-RC1'
}
```


## Usage
Before attempting to use RxBillingService, you need to call `RxBillingService.register()` in your Android `Application` class, supplying as parameter the current instance.
        
```java
public class SampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxBillingService.register(this);
    }
}
```

**Limitations:** Your fragments need to extend from `android.support.v4.app.Fragment` instead of `android.app.Fragment`. 

In order to avoid recreate the `activity` or `fragment` by rotating the screen during the transaction, which will result a malfunction, you will have to add this to your `activity` in the `Manifest.xml`:

> `android:configChanges="layoutDirection|orientation|screenLayout|screenSize"`

or you could define the screen orientation to your `activity` in the `Manifest.xml` by adding, for instance:

> `android:screenOrientation="portrait"`

## Methods available
### Checking whether the billing service is available

> `Single<Boolean> isBillingSupported(ProductType productType)`

```java
RxBillingService.getInstance(this, BuildConfig.DEBUG)
        .isBillingSupported(ProductType.IN_APP)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(supported -> {
          // Do your stuffs
        }, throwable -> {
          if (throwable instanceof RxBillingServiceException) {
            // You can handle the response code defined in RxBillingServiceError
          }
        });
``` 

### Getting product details

> `Single<List<SkuDetails>> getSkuDetails(ProductType productType, List<String> productIds)`

```java
RxBillingService.getInstance(this, BuildConfig.DEBUG)
        .getSkuDetails(ProductType.IN_APP, Arrays.asList("my-sku", "my-product_id"))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(skuDetailsList -> {
          // Do your stuffs
        }, throwable -> {
          if (throwable instanceof RxBillingServiceException) {
            // You can handle the response code defined in RxBillingServiceError
          }
        });
``` 

### Purchasing a product

> `Single<Purchase> purchase(ProductType productType, String productId, String developerPayload)`

```java
    final String developerPayload = String.valueOf(System.currentTimeMillis());
    
    RxBillingService.getInstance(this, BuildConfig.DEBUG)
        .purchase(ProductType.IN_APP, "my-sku", developerPayload)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchase -> {
          // Do your stuffs
        }, throwable -> {
          if (throwable instanceof RxBillingServiceException) {
            // You can handle the response code defined in RxBillingServiceError
          }
        });
``` 

### Consuming a purchase

> `Single<Boolean> consumePurchase(String purchaseToken)`

```java
    final String token = purchase.token();
    ...
    RxBillingService.getInstance(this, BuildConfig.DEBUG)
        .consumePurchase(token)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          // Do your stuffs
        }, throwable -> {
          if (throwable instanceof RxBillingServiceException) {
            // You can handle the response code defined in RxBillingServiceError
          }
        });
``` 

### Purchasing and consuming a product at once

> `Single<Purchase> purchaseAndConsume(ProductType productType, String productId, String developerPayload)`

```java
    final String developerPayload = String.valueOf(System.currentTimeMillis());
    
    RxBillingService.getInstance(this, BuildConfig.DEBUG)
        .purchaseAndConsume(ProductType.IN_APP, "my-sku", developerPayload)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchase -> {
          // Do your stuffs
        }, throwable -> {
          if (throwable instanceof RxBillingServiceException) {
            // You can handle the response code defined in RxBillingServiceError
          }
        });
``` 

### Getting your purchases

> `Single<List<Purchase>> getPurchases(final ProductType productType)`

```java
    RxBillingService.getInstance(this, BuildConfig.DEBUG)
        .getPurchases(ProductType.IN_APP)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(purchaseList -> {
          // Do your stuffs
        }, throwable -> {
          if (throwable instanceof RxBillingServiceException) {
            // You can handle the response code defined in RxBillingServiceError
          }
        });
```

## Error handling

If an error happens during the transaction you can check the exception thrown in the `subscribe()`. 

You can handle [Google Play errors](https://developer.android.com/google/play/billing/billing_reference.html#billing-codes) by checking the Throwable instance and getting the code defined in `RxBillingServiceError`:

```java
    ...
    .subscribe(purchase -> {
      // Do your stuffs
    }, throwable -> {
      if (throwable instanceof RxBillingServiceException) {     
        RxBillingServiceException billingException = (RxBillingServiceException) throwable;
        if (billingException.getCode() == RxBillingServiceError.ITEM_ALREADY_OWNED) {
            // Notify the user
        }
      }
    });
```


## Credits
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain.

## Author

**Miguel García**

* <https://es.linkedin.com/in/miguelbcr>
* <https://github.com/miguelbcr>


## Another author's libraries using RxJava:
* [RxPaparazzo](https://github.com/miguelbcr/RxPaparazzo): RxJava extension for Android to take images using camera and gallery.
* [RxGpsService](https://github.com/miguelbcr/RxGpsService): An Android service to retrieve GPS locations and route stats using RxJava
* [OkAdapters](https://github.com/miguelbcr/OkAdapters): Wrappers for Android adapters to simply its api at a minimum
