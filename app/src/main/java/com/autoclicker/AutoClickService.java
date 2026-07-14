package com.autoclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * 无障碍服务 - 用于执行自动点击
 */
public class AutoClickService extends AccessibilityService {

    private static final String TAG = "AutoClickService";
    private static AutoClickService instance;

    public static AutoClickService getInstance() {
        return instance;
    }

    public static boolean isServiceRunning() {
        return instance != null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 不需要处理事件
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "服务被中断");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "无障碍服务已连接");

        // 通知主界面更新状态
        sendBroadcast(new Intent("com.autoclicker.ACCESSIBILITY_STATUS_CHANGED"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "无障碍服务已销毁");

        // 通知主界面更新状态
        sendBroadcast(new Intent("com.autoclicker.ACCESSIBILITY_STATUS_CHANGED"));
    }

    /**
     * 执行点击操作
     */
    public void performClick(int x, int y, ClickCallback callback) {
        // 显示点击效果
        showClickEffect(x, y);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);

            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gesture = builder
                    .addStroke(new GestureDescription.StrokeDescription(path, 0, 100))
                    .build();

            dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    if (callback != null) {
                        callback.onFailed("点击被取消");
                    }
                }
            }, null);
        } else {
            if (callback != null) {
                callback.onFailed("系统版本不支持");
            }
        }
    }

    /**
     * 显示点击效果
     */
    private void showClickEffect(int x, int y) {
        try {
            Intent intent = new Intent(this, ClickEffectService.class);
            intent.putExtra("x", x);
            intent.putExtra("y", y);
            startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "显示点击效果失败: " + e.getMessage());
        }
    }

    /**
     * 点击回调接口
     */
    public interface ClickCallback {
        void onSuccess();
        void onFailed(String reason);
    }
}
