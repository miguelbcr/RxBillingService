package com.miguelbcr.io.rx_billing_service;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.miguelbcr.io.rx_billing_service.entities.ProductType;
import com.miguelbcr.io.rx_billing_service.entities.Purchase;
import com.miguelbcr.io.rx_billing_service.entities.SkuDetails;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.w3c.dom.Text;
import rx_activity_result2.RxActivityResult;

/**
 * https://developer.android.com/google/play/billing/billing_integrate.html<br/>
 * https://developer.android.com/google/play/billing/billing_testing.html <br/><br/>
 *
 * <b>Make sure you complete these steps in order to test purchases witouth charges:</b>
 *
 * <ul>
 *
 * <li>Make sure it's actually published in alpha, beta, or production. If it appears, for example,
 * as "Draft in Alpha" or beta then you haven't published in. Publishing means that you also need
 * to
 * upload the minimum required assets for Google Play, descriptions, content ratings, etc (See the
 * APK, Store Listing, and Content Rating tabs in the developer console).</li>
 *
 * <li>At least for alpha and beta testing, there is a list of testers that you must create and add
 * each tester's email. Look for the "Manage Testers" section in the Alpha Testing or Beta Testing
 * tab.</li>
 *
 * <li>Add tester's email on Google Play Developer Console in Settings > Account Data > Testing
 * License</li>
 *
 * <li>Each tester must also accept to be a tester. There is an opt-in URL link which you must send
 * to your testers, and they must click on the link and accepts the conditions.</li>
 *
 * <li>After all that is done, you still have to wait about 1 hour before the purchase details
 * start
 * to appear.</li>
 *
 * </ul>
 * <b>Errors:</b> *
 * <ul>
 *
 * <li>This version of the application is not configured for billing through Google Play<br/>
 * http://stackoverflow.com/questions/11068686/this-version-of-the-application-is-not-configured-for-billing-through-google-pla
 *
 * <br/>
 *
 * - Google takes a while to process applications and update them to their servers, for me it
 * takes about half a day. So after saving the apk as a draft on Google Play, you must wait a few
 * hours before the in-app products will respond normally and allow for regular purchases.
 *
 * <br/>
 *
 * - Export and sign APK. Unsigned APK trying to make purchases will get error. </li>
 *
 * </ul>
 */
public class RxBillingService {
  private final RxBillingServiceImpl rxBillingServiceImpl;

  public static void register(Application application) {
    RxActivityResult.register(application);
  }

  public static <T extends Activity> RxBillingService getInstance(T activity) {
    return new RxBillingService(activity);
  }

  public static <T extends Fragment> RxBillingService getInstance(T fragment) {
    return new RxBillingService(fragment);
  }

  private RxBillingService(Object targetUiObject) {
    this.rxBillingServiceImpl = new RxBillingServiceImpl(targetUiObject);
  }

  /**
   * Checks support for the requested billing.
   * <br/>
   * See https://developer.android.com/google/play/billing/billing_reference.html
   *
   * @param productType {@link ProductType#IN_APP} or  {@link ProductType#SUBS}
   * @return A boolean {@link Single}
   */
  public Single<Boolean> isBillingSupported(ProductType productType) {
    return rxBillingServiceImpl.isBillingSupported(productType);
  }

  /**
   * Provides details of a list of SKUs (product ids).
   * <br/>
   * See https://developer.android.com/google/play/billing/billing_reference.html
   *
   * @param productType {@link ProductType#IN_APP} or  {@link ProductType#SUBS}
   * @param productIds A list of SKUs. This API can be called with a maximum of 20 SKUs.
   * @return A list of {@link SkuDetails} {@link Single}
   */
  public Single<List<SkuDetails>> getSkuDetails(ProductType productType, List<String> productIds) {
    ArrayList<String> productIdsList = new ArrayList<>(productIds.size());
    productIdsList.addAll(productIds);
    return rxBillingServiceImpl.getSkuDetails(productType, productIdsList);
  }

  /**
   * Purchase for a {@code productId}
   * <br/>
   * See https://developer.android.com/google/play/billing/billing_reference.html
   *
   * @param productType {@link ProductType#IN_APP} or  {@link ProductType#SUBS}
   * @param productId A SKU.
   * @param developerPayload A developer-specified string that contains supplemental information
   * about an order.
   * @return A {@link Purchase} {@link Single}
   */
  public Single<Purchase> purchase(ProductType productType, String productId,
      String developerPayload) {
    return rxBillingServiceImpl.purchase(productType, productId, developerPayload);
  }

  /**
   * Consume the last purchase of the given SKU. This will result in this item being removed
   * from all subsequent responses to getPurchases() and allow re-purchase of this item.
   * <br/><br/>
   * Important: in-app products are consumable, but subscriptions are not.
   * <br/>
   * See https://developer.android.com/google/play/billing/billing_reference.html
   *
   * @param purchaseToken Token in the {@link Purchase} object that identifies the purchase
   * to be consumed
   * @return A boolean {@link Single}
   */
  public Single<Boolean> consumePurchase(String purchaseToken) {
    return rxBillingServiceImpl.consumePurchase(purchaseToken);
  }

  /**
   * Purchase for a {@code productId} and then consume it.
   * <br/><br/>
   * Important: Managed in-app products are consumable, but subscriptions are not.
   * <br/>
   * See https://developer.android.com/google/play/billing/billing_reference.html
   *
   * @param productType {@link ProductType#IN_APP} or  {@link ProductType#SUBS}
   * @param productId A SKU.
   * @param developerPayload A developer-specified string that contains supplemental information
   * about an order.
   * @return A {@link Purchase} {@link Single}
   */
  public Single<Purchase> purchaseAndConsume(ProductType productType, String productId,
      String developerPayload) {
    return rxBillingServiceImpl.purchaseAndConsume(productType, productId, developerPayload);
  }

  /**
   * Returns the current SKUs (product Ids) owned by the user, including both purchased items and
   * items acquired by redeeming a promo code.
   * <br/>
   * This will return all SKUs that have been purchased in V3 and managed items purchased using
   * V1 and V2 that have not been consumed.
   *
   * @param productType {@link ProductType#IN_APP} or  {@link ProductType#SUBS}
   * @return A list of {@link Purchase} {@link Single}
   */
  public Single<List<Purchase>> getPurchases(final ProductType productType) {
    return rxBillingServiceImpl.getPurchases(productType);
  }
}
