package com.example.hawidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etServerUrl, etAccessToken, etEntityId;
    private MaterialButton btnSave, btnTest;
    private View tvStatus;
    private HAPreferences preferences;
    private OkHttpClient httpClient;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = new HAPreferences(this);
        httpClient = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        loadSavedConfig();
    }

    private void initViews() {
        etServerUrl = findViewById(R.id.et_server_url);
        etAccessToken = findViewById(R.id.et_access_token);
        etEntityId = findViewById(R.id.et_entity_id);
        btnSave = findViewById(R.id.btn_save);
        btnTest = findViewById(R.id.btn_test);
        tvStatus = findViewById(R.id.tv_status);

        btnTest.setOnClickListener(v -> testConnection());
        btnSave.setOnClickListener(v -> saveConfiguration());
    }

    private void loadSavedConfig() {
        etServerUrl.setText(preferences.getServerUrl());
        etAccessToken.setText(preferences.getAccessToken());
        etEntityId.setText(preferences.getEntityId());
    }

    private void testConnection() {
        String serverUrl = getTextFromEditText(etServerUrl);
        String token = getTextFromEditText(etAccessToken);
        String entityId = getTextFromEditText(etEntityId);

        if (serverUrl.isEmpty() || token.isEmpty() || entityId.isEmpty()) {
            showToast("请填写所有字段");
            return;
        }

        btnTest.setEnabled(false);
        showStatus("正在测试连接...", true);

        String url = serverUrl + "/api/states/" + entityId;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> {
                    btnTest.setEnabled(true);
                    showStatus("连接失败: " + e.getMessage(), false);
                    showToast(getString(R.string.connection_failed));
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final boolean success = response.isSuccessful();
                final String responseBody = response.body() != null ? response.body().string() : "";
                
                mainHandler.post(() -> {
                    btnTest.setEnabled(true);
                    if (success) {
                        showStatus("连接成功! 响应: " + responseBody.substring(0, 
                                Math.min(100, responseBody.length())), true);
                        showToast(getString(R.string.connection_success));
                    } else {
                        showStatus("连接失败，HTTP " + response.code(), false);
                        showToast(getString(R.string.connection_failed));
                    }
                });
            }
        });
    }

    private void saveConfiguration() {
        String serverUrl = getTextFromEditText(etServerUrl);
        String token = getTextFromEditText(etAccessToken);
        String entityId = getTextFromEditText(etEntityId);

        if (serverUrl.isEmpty() || token.isEmpty() || entityId.isEmpty()) {
            showToast("请填写所有字段");
            return;
        }

        preferences.saveConfig(serverUrl, token, entityId);
        showToast("配置已保存");

        // 更新所有小部件
        updateAllWidgets();

        // 如果是从widget配置启动，设置结果
        Intent intent = getIntent();
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
        }

        finish();
    }

    private void updateAllWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, HAWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        
        for (int appWidgetId : appWidgetIds) {
            HAWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
        }
    }

    private String getTextFromEditText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setBackgroundColor(isSuccess ? 0xFF4CAF50 : 0xFFF44336);
        ((android.widget.TextView) tvStatus).setText(message);
    }
}