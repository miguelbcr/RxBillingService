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

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Created by miguel on 25/08/2016.<br/>
 * https://developer.android.com/google/play/billing/billing_reference.html#getSkuDetails
 */
@AutoValue
public abstract class SkuDetails {
    /**
     * The product ID for the product.
     */
    @SerializedName("productId")
    public abstract String sku();

    /**
     *  "inapp" for an in-app product or "subs" for subscriptions.
     */
    public abstract String type();

    /**
     * Formatted price of the item, including its currency sign. The price does not include tax
     */
    public abstract String price();

    /**
     * Price in micro-units, where 1,000,000 micro-units equal one unit of the currency.
     * For example, if price is "€7.99", price_amount_micros is "7990000".
     * This value represents the localized, rounded price for a particular currency.
     */
    @SerializedName("price_amount_micros")
    public abstract long priceAmountMicros();

    /**
     * ISO 4217 currency code for price. For example,
     * if price is specified in British pounds sterling, price_currency_code is "GBP".
     */
    @SerializedName("price_currency_code")
    public abstract String priceCurrencyCode();

    /**
     * Title of the product.
     */
    public abstract String title();

    /**
     * Description of the product
     */
    public abstract String description();
    
    
    public static SkuDetails create(String sku, String type, String price,
                                    long priceAmountMicros, String priceCurrencyCode,
                                    String title, String description) {
       return new AutoValue_SkuDetails(sku, type, price,
        priceAmountMicros, priceCurrencyCode, title, description);
    }

    public static TypeAdapter<SkuDetails> typeAdapter(Gson gson) {
        return new AutoValue_SkuDetails.GsonTypeAdapter(gson);
    }
}
