package com.miguelbcr.io.rx_billing_service.entities;

/**
 * Created by miguel on 25/08/2016.
 */

public class BillingResponseCodes {
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
}
