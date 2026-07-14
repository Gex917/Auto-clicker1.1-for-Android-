package com.autoclicker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 悬浮窗服务
 */
public class FloatingWindowService extends Service {

    private static final String TAG = "FloatingWindowService";
    private static final String CHANNEL_ID = "auto_clicker_channel";
    private static final int NOTIFICATION_ID = 1001;

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    private TextView tvStatus;
    private TextView tvProgress;
    private Button btnStart;
    private Button btnStop;
    private Button btnClose;

    private boolean isRunning = false;
    private boolean stopFlag = false;
    private int currentRound = 0;

    private List<ClickPoint> clickPoints = new ArrayList<>();

    private Handler handler;
    private Thread clickThread;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        createFloatingWindow();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // 获取配置
            ArrayList<Integer> pointsX = intent.getIntegerArrayListExtra("click_points_x");
            ArrayList<Integer> pointsY = intent.getIntegerArrayListExtra("click_points_y");
            ArrayList<Integer> pointsWait = intent.getIntegerArrayListExtra("click_points_wait");
            ArrayList<Integer> pointsRepeat = intent.getIntegerArrayListExtra("click_points_repeat");
            ArrayList<Integer> pointsClickInterval = intent.getIntegerArrayListExtra("click_points_click_interval");
            ArrayList<Integer> pointsRoundInterval = intent.getIntegerArrayListExtra("click_points_round_interval");

            if (pointsX != null && pointsY != null && pointsWait != null) {
                clickPoints.clear();
                for (int i = 0; i < pointsX.size(); i++) {
                    int repeatCount = (pointsRepeat != null && i < pointsRepeat.size()) ? pointsRepeat.get(i) : 10;
                    int clickInterval = (pointsClickInterval != null && i < pointsClickInterval.size()) ? pointsClickInterval.get(i) : 500;
                    int roundInterval = (pointsRoundInterval != null && i < pointsRoundInterval.size()) ? pointsRoundInterval.get(i) : 2000;
                    clickPoints.add(new ClickPoint(pointsX.get(i), pointsY.get(i), pointsWait.get(i),
                            repeatCount, clickInterval, roundInterval));
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFlag = true;
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "自动点击器",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("自动点击器运行通知");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 创建通知
     */
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("自动点击器")
                .setContentText("正在运行中...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();
    }

    /**
     * 创建悬浮窗
     */
    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 设置悬浮窗参数
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        // 加载布局
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null);

        // 初始化控件
        tvStatus = floatingView.findViewById(R.id.tvStatus);
        tvProgress = floatingView.findViewById(R.id.tvProgress);
        btnStart = floatingView.findViewById(R.id.btnStart);
        btnStop = floatingView.findViewById(R.id.btnStop);
        btnClose = floatingView.findViewById(R.id.btnClose);

        // 设置按钮点击事件
        btnStart.setOnClickListener(v -> startClicking());
        btnStop.setOnClickListener(v -> stopClicking());
        btnClose.setOnClickListener(v -> stopSelf());

        // 设置拖拽功能
        setupDragFunction();

        // 添加到窗口
        windowManager.addView(floatingView, params);
    }

    /**
     * 设置拖拽功能
     */
    private void setupDragFunction() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isMoving = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isMoving = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initialTouchX;
                        float dy = event.getRawY() - initialTouchY;

                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isMoving = true;
                        }

                        params.x = initialX + (int) dx;
                        params.y = initialY + (int) dy;
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * 开始点击
     */
    private void startClicking() {
        if (isRunning) {
            return;
        }

        if (clickPoints.isEmpty()) {
            Toast.makeText(this, "请先设置点击位置", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!AutoClickService.isServiceRunning()) {
            Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
            return;
        }

        isRunning = true;
        stopFlag = false;
        currentRound = 0;

        updateStatus("运行中", 0);

        clickThread = new Thread(() -> {
            AutoClickService clickService = AutoClickService.getInstance();

            // 计算最大重复次数（取所有点击位置中的最大值）
            int maxRepeatCount = 0;
            for (ClickPoint point : clickPoints) {
                if (point.getRepeatCount() == 0) {
                    maxRepeatCount = 0; // 无限循环
                    break;
                }
                if (point.getRepeatCount() > maxRepeatCount) {
                    maxRepeatCount = point.getRepeatCount();
                }
            }

            while (!stopFlag) {
                currentRound++;

                // 检查是否达到重复次数
                if (maxRepeatCount > 0 && currentRound > maxRepeatCount) {
                    break;
                }

                final int round = currentRound;
                handler.post(() -> updateStatus("运行中", round));

                // 执行所有点击
                for (int i = 0; i < clickPoints.size(); i++) {
                    if (stopFlag) break;

                    ClickPoint point = clickPoints.get(i);

                    // 检查该位置是否已达到重复次数
                    if (point.getRepeatCount() > 0 && currentRound > point.getRepeatCount()) {
                        continue; // 跳过已达到重复次数的位置
                    }

                    // 执行点击
                    final int index = i;
                    handler.post(() -> {
                        Log.d(TAG, "点击位置 " + (index + 1) + ": (" + point.getX() + ", " + point.getY() + ")");
                    });

                    // 使用无障碍服务点击
                    final boolean[] clickDone = {false};
                    clickService.performClick(point.getX(), point.getY(), new AutoClickService.ClickCallback() {
                        @Override
                        public void onSuccess() {
                            clickDone[0] = true;
                            synchronized (clickDone) {
                                clickDone.notify();
                            }
                        }

                        @Override
                        public void onFailed(String reason) {
                            Log.e(TAG, "点击失败: " + reason);
                            clickDone[0] = true;
                            synchronized (clickDone) {
                                clickDone.notify();
                            }
                        }
                    });

                    // 等待点击完成
                    synchronized (clickDone) {
                        try {
                            clickDone.wait(1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    // 点击间隔
                    if (point.getClickInterval() > 0) {
                        try {
                            Thread.sleep(point.getClickInterval());
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                // 轮次间隔（使用所有位置中最大的轮次间隔）
                if (!stopFlag) {
                    int maxRoundInterval = 0;
                    for (ClickPoint point : clickPoints) {
                        if (point.getRoundInterval() > maxRoundInterval) {
                            maxRoundInterval = point.getRoundInterval();
                        }
                    }
                    if (maxRoundInterval > 0) {
                        try {
                            Thread.sleep(maxRoundInterval);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }

            isRunning = false;
            final int finalRound = currentRound - 1;
            handler.post(() -> {
                if (stopFlag) {
                    updateStatus("已停止", finalRound);
                    Toast.makeText(FloatingWindowService.this, "已手动停止", Toast.LENGTH_SHORT).show();
                } else {
                    updateStatus("已完成", finalRound);
                    Toast.makeText(FloatingWindowService.this, "执行完成！共 " + finalRound + " 轮", Toast.LENGTH_SHORT).show();
                }
            });
        });

        clickThread.start();
    }

    /**
     * 停止点击
     */
    private void stopClicking() {
        stopFlag = true;
        if (clickThread != null) {
            clickThread.interrupt();
        }
    }

    /**
     * 更新状态显示
     */
    private void updateStatus(String status, int round) {
        tvStatus.setText("状态：" + status);

        // 计算总重复次数
        int totalRepeat = 0;
        for (ClickPoint point : clickPoints) {
            if (point.getRepeatCount() == 0) {
                totalRepeat = 0; // 无限
                break;
            }
            if (point.getRepeatCount() > totalRepeat) {
                totalRepeat = point.getRepeatCount();
            }
        }

        String progressText = "进度：" + round + "/" + (totalRepeat == 0 ? "∞" : totalRepeat);
        tvProgress.setText(progressText);
    }
}
