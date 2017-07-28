package com.fourall.pushgeofencing.network;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Gustavo Terras on 30/05/2017.
 */

public interface IPushNotificationService {

    @POST("/identities")
    Call<JsonObject> sendPushIdentities(@Body Map<String, Object> body);

    @POST("/notifications")
    Call<JsonObject> sendPushNotifications(@Body Map<String, Object> body);

    @GET("/locations")
    Call<JsonObject> getLocations();
}
