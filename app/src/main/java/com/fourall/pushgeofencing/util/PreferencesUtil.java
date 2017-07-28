package com.fourall.pushgeofencing.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.fourall.pushgeofencing.BuildConfig;

import java.util.Set;

/**
 * Created by Gustavo on 07/03/2017.
 */

public class PreferencesUtil {

    private static final String PREFERENCES_GROUP = BuildConfig.APPLICATION_ID;


    private static SharedPreferences getPreferences(Context context) {
        return  context.getSharedPreferences(PREFERENCES_GROUP, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getPreferencesEditor(Context context) {
        return getPreferences(context).edit();
    }


    public static String getPreferenceValue(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static int getPreferenceValue(Context context, String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    public static boolean getPreferenceValue(Context context, String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static Set<String> getPreferenceValue(Context context, String key, Set<String> defValue) {
        return getPreferences(context).getStringSet(key, defValue);
    }

    public static void setPreferenceValue(Context context, String key, String prefsValue) {
        getPreferencesEditor(context).putString(key, prefsValue).apply();
    }

    public static void setPreferenceValue(Context context, String key, int prefsValue) {
        getPreferencesEditor(context).putInt(key, prefsValue).apply();
    }

    public static void setPreferenceValue(Context context, String key, boolean prefsValue) {
        getPreferencesEditor(context).putBoolean(key, prefsValue).apply();
    }

    public static void setPreferenceValue(Context context, String key, Set<String> prefsValue) {
        getPreferencesEditor(context).putStringSet(key, prefsValue).apply();
    }

    public static boolean containsPreferenceKey(Context context, String key) {
        return getPreferences(context).contains(key);
    }

    public static void removePreferenceValue(Context context, String key) {
        getPreferencesEditor(context).remove(key).apply();
    }

}
