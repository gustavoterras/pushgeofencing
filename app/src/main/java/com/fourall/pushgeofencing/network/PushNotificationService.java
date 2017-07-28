package com.fourall.pushgeofencing.network;

import android.util.Log;

import com.fourall.marketplace.module.core.network.ServiceGenerator;
import com.fourall.pushgeofencing.BuildConfig;
import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Gustavo Terras on 30/05/2017.
 */

@SuppressWarnings("unchecked")
public class PushNotificationService {

    private static final String TAG = PushNotificationService.class.getSimpleName();
    private IPushNotificationService service;
    private OnTaskCompleted listener;

    public interface OnTaskCompleted<T> {
        void onSuccess(T response, int code, int requestCode);
        void onFailure(Throwable error, int requestCode);
    }

    public PushNotificationService() {
        service = ServiceGenerator.createService(BuildConfig.DEBUG, BuildConfig.PUSH_URL, BuildConfig.PUSH_TOKEN, IPushNotificationService.class);
    }

    public void setOnTaskCompleted(OnTaskCompleted onTaskCompleted) {
        listener = onTaskCompleted;
    }

    /**
     * Envia o indentificador do push para o server
     * @param body body
     */
    public void sendPushIdentities(Map<String, Object> body) {
        service.sendPushIdentities(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: success");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                Log.d(TAG, "onFailure: failure");
            }
        });
    }

    public void getLocations(final int requestCode) {
        service.getLocations().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                listener.onSuccess(response.body(), response.code(), requestCode);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                listener.onFailure(throwable, requestCode);
            }
        });
    }

    public void sendPushNotifications(Map<String, Object> body, final int requestCode) {
        service.sendPushNotifications(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                listener.onSuccess(response.body(), response.code(), requestCode);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                listener.onFailure(throwable, requestCode);
            }
        });
    }
}
