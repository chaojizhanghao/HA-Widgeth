package com.example.hawidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class HAWidgetProvider extends AppWidgetProvider {
    
    public static final String ACTION_REFRESH = "com.example.hawidget.ACTION_REFRESH";
    public static final String EXTRA_APPWIDGET_ID = "appWidgetId";
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        HAPreferences preferences = new HAPreferences(context);
        for (int appWidgetId : appWidgetIds) {
            preferences.clearWidgetData(appWidgetId);
        }
    }
    
    @Override
    public void onEnabled(Context context) {
        // 第一个小部件被添加
    }
    
    @Override
    public void onDisabled(Context context) {
        // 最后一个小部件被删除
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        if (ACTION_REFRESH.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, 
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }
    
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, 
                                       int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        HAPreferences preferences = new HAPreferences(context);
        HAService haService = new HAService(context);
        
        // 设置刷新按钮点击事件
        Intent refreshIntent = new Intent(context, HAWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        refreshIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent);
        
        // 设置整个widget点击打开配置
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(
                context, appWidgetId, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent);
        
        // 检查是否已配置
        if (!preferences.isConfigured()) {
            views.setTextViewText(R.id.tv_entity_name, "未配置");
            views.setTextViewText(R.id.tv_entity_value, "点击配置");
            views.setTextViewText(R.id.tv_last_update, "--:--");
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }
        
        // 显示加载状态
        views.setTextViewText(R.id.tv_entity_value, "加载中...");
        appWidgetManager.updateAppWidget(appWidgetId, views);
        
        // 异步获取数据
        haService.fetchEntityState(new HAService.HACallback() {
            @Override
            public void onSuccess(String value, String friendlyName, String unit) {
                views.setTextViewText(R.id.tv_entity_name, friendlyName);
                views.setTextViewText(R.id.tv_entity_value, value);
                views.setTextViewText(R.id.tv_last_update, haService.getCurrentTime());
                
                // 保存数据
                preferences.saveWidgetData(appWidgetId, value, haService.getCurrentTime());
                
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            
            @Override
            public void onError(String error) {
                // 使用上次保存的数据
                String lastValue = preferences.getLastValue(appWidgetId);
                String lastUpdate = preferences.getLastUpdate(appWidgetId);
                
                views.setTextViewText(R.id.tv_entity_name, preferences.getEntityId());
                views.setTextViewText(R.id.tv_entity_value, lastValue);
                views.setTextViewText(R.id.tv_last_update, lastUpdate);
                
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }
    
    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, HAWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}