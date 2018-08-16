package com.dng.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dng.activity.LoginActivity;
import com.dng.model.RequestInfo;
import com.dng.model.User;
import com.google.gson.Gson;

public class Session {

    private Context _context;
    private SharedPreferences mypref;
    private SharedPreferences.Editor editor;
    public static final String PREF_NAME = "DNG";

    private static final String IS_LOGGEDIN = "isLoggedIn";
    private static final String IS_FIrebaseLogin = "isFirebaseLogin";
    private static final String IS_UPDATE_UID = "isUpdateUid";
    public static final String ScreenName = "screenName";
    private static final String RouteStatus = "routeStatus";
    private static final String DEFAULT_MAP = "defaultmap";
    private static final String IS_DIALOG_OPEN = "isDialogOpen";
    private static final String IS_DIALOG_OPEN2 = "isDialogOpen2";
    private static final String PopUpForCustomerArea = "PopUpForCustomerArea";

    private static final String setIsOffRouteLodingDetails = "setIsOffRouteLodingDetails";
    private static final String setIsOffRouteDeliveryDetails = "setIsOffRouteDeliveryDetails";

    public Session(Context context) {
        this._context = context;
        mypref = _context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
        editor = mypref.edit();
        editor.apply();

    }

    public boolean isUpdateUid() {
        return mypref.getBoolean(IS_UPDATE_UID, false);
    }

    public void setUpdateUid(boolean isUpdate) {
        editor.putBoolean(IS_UPDATE_UID, isUpdate);
        editor.commit();
    }

    public void createSession(User user) {
        createSession(user, false);
    }

    public void createSession(User user, boolean isFirebaseLogin) {
        Gson gson = new Gson();
        String json = gson.toJson(user); // myObject - instance of MyObject
        editor.putString("user", json);
        editor.putBoolean(IS_LOGGEDIN, true);
        editor.putBoolean(IS_FIrebaseLogin, isFirebaseLogin);
        editor.putString("authToken", user.data.authToken);
        editor.apply();
    }

    public User getUser() {
        Gson gson = new Gson();
        String string = mypref.getString("user", "");
        if (!string.isEmpty())
            return gson.fromJson(string, User.class);
        else return null;
    }

    public void createRequest(RequestInfo requestInfo) {
        Gson gson = new Gson();
        String json = gson.toJson(requestInfo);
        editor.putString("requestInfo", json);
        editor.commit();
    }

    public void setScreen(String screenName) {
        editor.putString(ScreenName, screenName);
        editor.apply();
    }

    public void setPopUpForCustomerArea(String value) {
        editor.putString(PopUpForCustomerArea, value);
        editor.apply();
    }

    public String getPopUpForCustomerArea() {
        return mypref.getString(PopUpForCustomerArea, "");
    }


    public void setLoadingRouteReceipt(String bitmapString, String paymentMode, String description) {
        editor.putString("loading_route_receipt", bitmapString);
        editor.putString("loading_route_payment_mode", paymentMode);
        editor.putString("loading_route_description", description);
        editor.commit();
    }

    public String getLoadingRouteReceipt() {
        return mypref.getString("loading_route_receipt", "");
    }

    public String getLoadingRoutePaymentMode() {
        return mypref.getString("loading_route_payment_mode", "");
    }

    public String getLoadingRouteDescription() {
        return mypref.getString("loading_route_description", "");
    }

    public void setDeliveryDetailReceipt(String paymentMode, String description) {
        editor.putString("delivery_detail_payment_mode", paymentMode);
        editor.putString("delivery_detail_description", description);
        editor.commit();
    }

    public void setBeforeDeliveryBitmap(String beforeUpload) {
        editor.putString("before_delivery_bitmap", beforeUpload);
        editor.commit();
    }

    public void setAfterDeliveryBitmap(String afterUpload) {
        editor.putString("after_delivery_bitmap", afterUpload);
        editor.commit();
    }

    public void setWaiverReceiptBitmap(String waiverReceipt) {
        editor.putString("waiver_receipt_bitmap", waiverReceipt);
        editor.commit();
    }

    public void setCustomerPaymentReceiptBitmap(String waiverReceipt) {
        editor.putString("customer_payment_receipt_bitmap", waiverReceipt);
        editor.commit();
    }

    public String getBeforeDeliveryBitmap() {
        return mypref.getString("before_delivery_bitmap", "");
    }

    public String getAfterDeliveryBitmap() {
        return mypref.getString("after_delivery_bitmap", "");
    }

    public String getWaiverReceiptBitmap() {
        return mypref.getString("waiver_receipt_bitmap", "");
    }

    public String getCustomerPaymentReceiptBitmap() {
        return mypref.getString("customer_payment_receipt_bitmap", "");
    }


    public String getDeliveryDetailPaymentMode() {
        return mypref.getString("delivery_detail_payment_mode", "");
    }

    public String getDeliveryDetailDescription() {
        return mypref.getString("delivery_detail_description", "");
    }

    public void setLoadingChequeNumber(String chequeNumber) {
        editor.putString("Loading Cheque Number", chequeNumber);
        editor.commit();
    }

    public String getLoadingChequeNumber() {
        return mypref.getString("Loading Cheque Number", "");
    }

    public void setDeliveryChequeNumber(String chequeNumber) {
        editor.putString("Delivery Cheque Number", chequeNumber);
        editor.commit();
    }

    public String getDeliveryChequeNumber() {
        return mypref.getString("Delivery Cheque Number", "");
    }

    public void setDefaultMap(int position) {
        editor.putInt(DEFAULT_MAP, position);
        editor.commit();
    }

    public void setIsDialogOpen(boolean isOpenDialog) {
        editor.putBoolean(IS_DIALOG_OPEN, isOpenDialog);
        editor.commit();
    }

    public void setIsDialogOpen2(boolean isOpenDialog2) {
        editor.putBoolean(IS_DIALOG_OPEN2, isOpenDialog2);
        editor.commit();
    }

    public void setIsOffRouteLodingDetails(String off) {
        editor.putString(setIsOffRouteLodingDetails, off);
        editor.commit();
    }

    public void setIsOffRouteDeliveryDetails(String off) {
        editor.putString(setIsOffRouteDeliveryDetails, off);
        editor.commit();
    }

    public String getIsOffRouteLodingDetails() {
        return mypref.getString(setIsOffRouteLodingDetails, "");
    }

    public String getIsOffRouteDeliveryDetails() {
        return mypref.getString(setIsOffRouteDeliveryDetails, "");
    }

    public boolean getIsDialogOpen() {
        return mypref.getBoolean(IS_DIALOG_OPEN, false);
    }

    public boolean getIsDialogOpen2() {
        return mypref.getBoolean(IS_DIALOG_OPEN2, false);
    }


    public void setRoute(String status) {
        editor.putString(RouteStatus, status);
        editor.commit();
    }

    public int getDefaultMap() {
        return mypref.getInt(DEFAULT_MAP, 0);
    }

    public String getRouteStatus() {
        return mypref.getString(RouteStatus, "");
    }

    public String getScreen() {
        return mypref.getString(ScreenName, "");
    }


   /* public String getRequestInfoString() {

        return mypref.getString("requestInfo", "");
    }*/

    synchronized public RequestInfo getRequestInfo() {
        Gson gson = new Gson();
        String string = mypref.getString("requestInfo", "");

        if (!string.isEmpty())
            return gson.fromJson(string, RequestInfo.class);
        else return null;
    }

    public void setPaymentMode(String paymentMode) {
        editor.putString("PaymentMode", paymentMode);
        editor.commit();
    }

    public String getPaymentMode() {
        return mypref.getString("PaymentMode", "");
    }

    public String getAuthToken() {
        return mypref.getString("authToken", "");
    }

    public boolean getIsFirebaseLogin() {
        return mypref.getBoolean(IS_FIrebaseLogin, false);
    }


    public void logout() {
        Intent showLogin = new Intent(_context, LoginActivity.class);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(showLogin);

        editor.putBoolean(IS_LOGGEDIN, false);
        editor.apply();
    }

    public void clearAllSession() {
        editor.clear();
        editor.apply();
    }

    public void sessionExpireLogout() {
        editor.clear();
        editor.apply();

        Intent showLogin = new Intent(_context, LoginActivity.class);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(showLogin);
    }

    public void clearSessionNewUser() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return mypref.getBoolean(IS_LOGGEDIN, false);
    }

    public void setUserPassword(String password) {
        editor.putString("user_password", password);
        editor.apply();
    }

    public String getUserPassword() {
        return mypref.getString("user_password", "");
    }
}
