/*
 * Copyright 2016 Miguel Garcia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.miguelbcr.io.rx_billing_service.entities;

import android.support.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Created by miguel on 25/08/2016.<br/>
 * https://developer.android.com/google/play/billing/billing_reference.html#getBuyIntent
 */
@AutoValue
public abstract class Purchase {
    /**
     * Indicates whether the subscription renews automatically.
     * If true, the subscription is active, and will automatically renew on the next billing date.
     * If false, indicates that the user has canceled the subscription.
     * The user has access to subscription content until the next billing date and will lose access
     * at that time unless they re-enable automatic renewal (or manually renew, as described in Manual Renewal).
     * If you offer a grace period, this value remains set to true for all subscriptions,
     * as long as the grace period has not lapsed. The next billing date is extended dynamically
     * every day until the end of the grace period or until the user fixes their payment method.
     */
    public abstract boolean autoRenewing();

    /**
     * A unique order identifier for the transaction.
     * This identifier corresponds to the Google payments order ID.
     * If the order is a test purchase made through the In-app Billing Sandbox, orderId is blank.
     */
    @Nullable   /* Nullable for testing */
    public abstract String orderId();

    /**
     * The application package from which the purchase originated.
     */
    public abstract String packageName();

    /**
     * The item's product identifier.
     * Every item has a product ID, which you must specify in the application's product list
     * on the Google Play Developer Console.
     */
    @SerializedName("productId")
    public abstract String sku();

    /**
     * The time the product was purchased, in milliseconds since the epoch (Jan 1, 1970).
     */
    public abstract long purchaseTime();

    /**
     * The purchase state of the order.
     * Possible values are 0 (purchased), 1 (canceled), or 2 (refunded).
     */
    public abstract int purchaseState();

    /**
     * A developer-specified string that contains supplemental information about an order.
     * You can specify a value for this field when you make a getBuyIntent request.<br/><br/>
     *
     * Security Recommendation: When you send a purchase request, create a String token that
     * uniquely identifies this purchase request and include this token in the developerPayload.
     * You can use a randomly generated string as the token. When you receive the purchase response
     * from Google Play, make sure to check the returned data signature, the orderId, and the
     * developerPayload String. For added security, you should perform the checking on your own
     * secure server. Make sure to verify that the orderId is a unique value that you have not
     * previously processed, and the developerPayload String matches the token that you sent
     * previously with the purchase request.
     */
    public abstract String developerPayload();

    /**
     * A token that uniquely identifies a purchase for a given item and user pair.<br/><br/>
     *
     * Note: Google Play generates a token for the purchase. This token is an opaque character
     * sequence that may be up to 1,000 characters long. Pass this entire token to other methods,
     * such as when you consume the purchase, as described in Consume a Purchase. Do not abbreviate
     * or truncate this token; you must save and return the entire token.
     */
    @SerializedName("purchaseToken")
    public abstract String token();

    private String signature;
    
    
    public static Purchase create(boolean autoRenewing, String orderId, String packageName,
                                  String sku, long purchaseTime, int purchaseState,
                                  String developerPayload, String token) {
       return new AutoValue_Purchase(autoRenewing, orderId, packageName, sku, purchaseTime,
               purchaseState, developerPayload, token);
    }

    public static TypeAdapter<Purchase> typeAdapter(Gson gson) {
        return new AutoValue_Purchase.GsonTypeAdapter(gson);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
