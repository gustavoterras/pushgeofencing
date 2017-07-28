package com.fourall.pushgeofencing.service;

import android.content.Context;
import android.location.Location;

import com.fourall.marketplace.module.core.LocationManager;
import com.fourall.pushgeofencing.network.PushNotificationService;
import com.fourall.pushgeofencing.util.PreferencesUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

import fourall.com.pay_lib.FourAllPayment;
import fourall.com.pay_lib.FourAll_SessionToken;

public class RefreshTokenGCM extends FirebaseInstanceIdService {

    private static final String TAG = "tokenFireBase";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        PreferencesUtil.setPreferenceValue(getApplication().getApplicationContext(), TAG, refreshedToken);
        sendPushIdentities(getApplication().getApplicationContext());
    }

    /**
     * Envia o identificador do push
     * @param context context
     */
    public static void sendPushIdentities(Context context){
        HashMap<String, Object> payload = new HashMap<>();

        payload.put("pushToken", PreferencesUtil.getPreferenceValue(context, TAG, ""));
        payload.put("operatingSystem", 2); //2 = Android

        if(FourAllPayment.isLogged())
            payload.put("sessionToken", FourAll_SessionToken.getSessionToken());

        Location location = LocationManager.getInstance().getLocation();

        if(location != null) {
            payload.put("latitude", location.getLatitude());
            payload.put("longitude", location.getLongitude());
        }

        new PushNotificationService().sendPushIdentities(payload);
    }
}
