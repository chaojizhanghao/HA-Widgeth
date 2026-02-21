package com.example.hawidget;

import android.content.Context;
import android.content.SharedPreferences;

public class HAPreferences {
    
    private static final String PREFS_NAME = "HAWidgetPrefs";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ENTITY_ID = "entity_id";
    private static final String KEY_LAST_VALUE = "last_value_";
    private static final String KEY_LAST_UPDATE = "last_update_";
    
    private final SharedPreferences prefs;
    
    public HAPreferences(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void saveConfig(String serverUrl, String accessToken, String entityId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SERVER_URL, serverUrl);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_ENTITY_ID, entityId);
        editor.apply();
    }
    
    public String getServerUrl() {
        return prefs.getString(KEY_SERVER_URL, "");
    }
    
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, "");
    }
    
    public String getEntityId() {
        return prefs.getString(KEY_ENTITY_ID, "");
    }
    
    public void saveWidgetData(int appWidgetId, String value, String updateTime) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LAST_VALUE + appWidgetId, value);
        editor.putString(KEY_LAST_UPDATE + appWidgetId, updateTime);
        editor.apply();
    }
    
    public String getLastValue(int appWidgetId) {
        return prefs.getString(KEY_LAST_VALUE + appWidgetId, "--");
    }
    
    public String getLastUpdate(int appWidgetId) {
        return prefs.getString(KEY_LAST_UPDATE + appWidgetId, "--:--");
    }
    
    public boolean isConfigured() {
        return !getServerUrl().isEmpty() && 
               !getAccessToken().isEmpty() && 
               !getEntityId().isEmpty();
    }
    
    public void clearWidgetData(int appWidgetId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_LAST_VALUE + appWidgetId);
        editor.remove(KEY_LAST_UPDATE + appWidgetId);
        editor.apply();
    }
}