package com.miguelbcr.io.rx_billing_service;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

/**
 * Created by miguel on 17/08/2016.
 */
@GsonTypeAdapterFactory
abstract class GsonAdapterFactory implements TypeAdapterFactory {

    static GsonAdapterFactory create() {
        return new AutoValueGson_GsonAdapterFactory();
    }
}
