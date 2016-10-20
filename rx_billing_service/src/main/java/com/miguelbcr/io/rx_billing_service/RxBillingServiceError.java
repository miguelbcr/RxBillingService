package com.miguelbcr.io.rx_billing_service;

import com.miguelbcr.io.rx_billing_service.entities.Purchase;
import com.miguelbcr.io.rx_billing_service.entities.SkuDetails;

/**
 * Created by miguel on 25/08/2016.
 * <br/><br/>
 * See https://developer.android.com/google/play/billing/billing_reference.html
 */

public class RxBillingServiceError {
    /**
     * Success
     */
    public static  final int OK = 0;
    /**
     * User pressed back or canceled a dialog
     */
    public static  final int USER_CANCELED = 1;
    /**
     * Network connection is down
     */
    public static  final int SERVICE_UNAVAILABLE = 2;
    /**
     * Billing API version is not supported for the type requested
     */
    public static  final int BILLING_UNAVAILABLE = 3;
    /**
     * Requested product is not available for purchase
     */
    public static  final int ITEM_UNAVAILABLE = 4;
    /**
     * Invalid arguments provided to the API. This error can also indicate that the application
     * was not correctly signed or properly set up for In-app Billing in Google Play, or does not
     * have the necessary permissions in its manifest
     */
    public static  final int DEVELOPER_ERROR = 5;
    /**
     * Fatal error during the API action
     */
    public static  final int ERROR = 6;
    /**
     * Failure to purchase since item is already owned
     */
    public static  final int ITEM_ALREADY_OWNED = 7;
    /**
     * Failure to consume since item is not owned
     */
    public static  final int ITEM_NOT_OWNED = 8;

    // Custom errors
    /**
     * Billing service disconnected
     */
    public static  final int SERVICE_DISCONNECTED = -1;
    /**
     * Error parsing {@link SkuDetails} object
     */
    public static  final int PARSING_SKUDETAILS = -2;
    /**
     * Error parsing {@link Purchase} object
     */
    public static  final int PARSING_PURCHASE = -3;
    
    static String getMessage(int errorCode) {
        switch (errorCode) {
            case OK:
                return "Success";
            case USER_CANCELED:
                return "User pressed back or canceled a dialog";
            case SERVICE_UNAVAILABLE:
                return "Network connection is down";
            case BILLING_UNAVAILABLE:
                return "Billing API version is not supported for the type requested";
            case ITEM_UNAVAILABLE:
                return "Requested product is not available for purchase";
            case DEVELOPER_ERROR:
                return "Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest";
            case ERROR:
                return "Fatal error during the API action";
            case ITEM_ALREADY_OWNED:
                return "Failure to purchase since item is already owned";
            case ITEM_NOT_OWNED:
                return "Failure to consume since item is not owned";
            case SERVICE_DISCONNECTED:
                return "Billing service disconnected";
            case PARSING_SKUDETAILS:
                return "Error parsing SkuDetails object";
            case PARSING_PURCHASE:
                return "Error parsing Purchase object";
            default:
                return "";
        }
    }
}
