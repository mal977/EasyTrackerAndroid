package com.ntu.staizen.EasyTracker;

import android.content.Context;
import android.content.SharedPreferences;

import javax.annotation.Nullable;

import androidx.preference.PreferenceManager;

/**
 * This class helps facilitate the adding/deleting of shared preference
 * Used to store contractor details, and running job info
 */
public class SharedPreferenceHelper {

    public static String KEY_USERNAME = "username";
    public static String KEY_PHONE_NUMBER = "phone_number";
    public static String KEY_RUNNING_JOB = "running_job";
    public static String PREFERENCE_FILE_KEY = "com.ntu.staizen.EasyTracker";

    public static void setPreferences(String key, String value, Context mContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Nullable
    public static String getPreference(String key, Context mContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPreferences.getString(key, null);
    }
    @Nullable
    public static boolean getPreference(String key, Context mContext, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        value = sharedPreferences.getBoolean(key, false);
        return value;
    }

    public static boolean doesValueExist(String key, Context mContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPreferences.contains(key);
    }

    public static void removeValue(String key, Context mContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(sharedPreferences.contains(key)){
            editor.remove(key);
            editor.apply();
        }
    }
}
