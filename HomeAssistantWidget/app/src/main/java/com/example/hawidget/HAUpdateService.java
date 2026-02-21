package com.example.hawidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

public class HAUpdateService extends Service {
    
    private Handler handler;
    private Runnable updateRunnable;
    private static final long UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(5); // 5分钟更新一次
    
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateAllWidgets();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(updateRunnable);
        handler.post(updateRunnable);
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void updateAllWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, HAWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        
        for (int appWidgetId : appWidgetIds) {
            HAWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
        }
    }
}