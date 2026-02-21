package com.example.hawidget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HAService {
    
    private final OkHttpClient httpClient;
    private final HAPreferences preferences;
    private final Handler mainHandler;
    
    public interface HACallback {
        void onSuccess(String value, String friendlyName, String unit);
        void onError(String error);
    }
    
    public HAService(Context context) {
        this.preferences = new HAPreferences(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }
    
    public void fetchEntityState(final HACallback callback) {
        if (!preferences.isConfigured()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError("未配置"));
            }
            return;
        }
        
        String serverUrl = preferences.getServerUrl();
        String token = preferences.getAccessToken();
        String entityId = preferences.getEntityId();
        
        String url = serverUrl + "/api/states/" + entityId;
        
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError("HTTP " + response.code()));
                    }
                    return;
                }
                
                String responseBody = response.body() != null ? response.body().string() : "";
                
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String state = json.optString("state", "unknown");
                    String friendlyName = entityId;
                    String unit = "";
                    
                    JSONObject attributes = json.optJSONObject("attributes");
                    if (attributes != null) {
                        friendlyName = attributes.optString("friendly_name", entityId);
                        unit = attributes.optString("unit_of_measurement", "");
                    }
                    
                    final String finalValue = state + (unit.isEmpty() ? "" : " " + unit);
                    final String finalName = friendlyName;
                    final String finalUnit = unit;
                    
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(finalValue, finalName, finalUnit));
                    }
                    
                } catch (JSONException e) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onError("解析错误"));
                    }
                }
            }
        });
    }
    
    public String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}