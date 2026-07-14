package com.autoclicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * 点击效果服务 - 在点击位置显示波纹动画
 */
public class ClickEffectService extends Service {

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int x = intent.getIntExtra("x", 0);
            int y = intent.getIntExtra("y", 0);
            showClickEffect(x, y);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 在指定位置显示点击效果
     */
    private void showClickEffect(int x, int y) {
        // 创建效果视图
        ImageView effectView = new ImageView(this);
        effectView.setImageResource(R.drawable.click_circle);

        // 设置悬浮窗参数
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 计算1厘米对应的像素值
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.density;
        int size = (int) (63 * density); // 1cm ≈ 63dp

        params = new WindowManager.LayoutParams(
                size,
                size,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x - size / 2;
        params.y = y - size / 2;

        // 添加到窗口
        windowManager.addView(effectView, params);

        // 创建动画 - 圆圈保持大小，逐渐消失
        ObjectAnimator alpha = ObjectAnimator.ofFloat(effectView, "alpha", 0.7f, 0.0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha);
        animatorSet.setDuration(500);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 动画结束后移除视图
                try {
                    windowManager.removeView(effectView);
                } catch (Exception e) {
                    // 忽略
                }
            }
        });

        animatorSet.start();
    }
}
