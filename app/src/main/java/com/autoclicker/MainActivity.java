package com.autoclicker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvAccessibilityStatus;
    private TextView tvOverlayStatus;
    private Button btnAccessibility;
    private Button btnOverlay;
    private Button btnAddPoint;
    private Button btnStart;
    private RecyclerView rvClickPoints;

    private List<ClickPoint> clickPoints = new ArrayList<>();
    private ClickPointAdapter adapter;

    private BroadcastReceiver accessibilityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupListeners();
        updateStatus();

        // 请求通知权限 (Android 13+)
        requestNotificationPermission();

        // 注册广播接收器
        accessibilityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateStatus();
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(accessibilityReceiver,
                    new IntentFilter("com.autoclicker.ACCESSIBILITY_STATUS_CHANGED"),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(accessibilityReceiver,
                    new IntentFilter("com.autoclicker.ACCESSIBILITY_STATUS_CHANGED"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (accessibilityReceiver != null) {
            unregisterReceiver(accessibilityReceiver);
        }
    }

    /**
     * 请求通知权限 (Android 13+)
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "通知权限被拒绝，可能影响后台运行", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus);
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        btnOverlay = findViewById(R.id.btnOverlay);
        btnAddPoint = findViewById(R.id.btnAddPoint);
        btnStart = findViewById(R.id.btnStart);
        rvClickPoints = findViewById(R.id.rvClickPoints);
    }

    /**
     * 设置 RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new ClickPointAdapter(clickPoints, position -> {
            clickPoints.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, clickPoints.size());
        });

        rvClickPoints.setLayoutManager(new LinearLayoutManager(this));
        rvClickPoints.setAdapter(adapter);

        // 添加默认点击位置
        clickPoints.add(new ClickPoint(540, 1800, 500));
        clickPoints.add(new ClickPoint(540, 1200, 500));
        adapter.notifyDataSetChanged();
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 无障碍服务按钮
        btnAccessibility.setOnClickListener(v -> {
            if (AutoClickService.isServiceRunning()) {
                Toast.makeText(this, "无障碍服务已开启", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("开启无障碍服务")
                        .setMessage("需要开启无障碍服务才能执行自动点击。\n\n请在设置中找到「自动点击器」并开启。")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        // 悬浮窗权限按钮
        btnOverlay.setOnClickListener(v -> {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "悬浮窗权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("开启悬浮窗权限")
                        .setMessage("需要悬浮窗权限才能显示控制面板。")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        // 添加点击位置按钮
        btnAddPoint.setOnClickListener(v -> {
            clickPoints.add(new ClickPoint(540, 1200, 500));
            adapter.notifyItemInserted(clickPoints.size() - 1);
            rvClickPoints.smoothScrollToPosition(clickPoints.size() - 1);
        });

        // 启动按钮
        btnStart.setOnClickListener(v -> startAutoClick());
    }

    /**
     * 更新权限状态
     */
    private void updateStatus() {
        // 检查无障碍服务
        if (AutoClickService.isServiceRunning()) {
            tvAccessibilityStatus.setText("✅ 无障碍服务：已开启");
            tvAccessibilityStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnAccessibility.setEnabled(false);
            btnAccessibility.setText("已开启");
        } else {
            tvAccessibilityStatus.setText("❌ 无障碍服务：未开启");
            tvAccessibilityStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnAccessibility.setEnabled(true);
            btnAccessibility.setText("开启");
        }

        // 检查悬浮窗权限
        if (Settings.canDrawOverlays(this)) {
            tvOverlayStatus.setText("✅ 悬浮窗权限：已开启");
            tvOverlayStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnOverlay.setEnabled(false);
            btnOverlay.setText("已开启");
        } else {
            tvOverlayStatus.setText("❌ 悬浮窗权限：未开启");
            tvOverlayStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnOverlay.setEnabled(true);
            btnOverlay.setText("开启");
        }
    }

    /**
     * 启动自动点击
     */
    private void startAutoClick() {
        // 检查权限
        if (!AutoClickService.isServiceRunning()) {
            Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查点击位置
        if (clickPoints.isEmpty()) {
            Toast.makeText(this, "请至少添加一个点击位置", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证点击位置坐标
        for (ClickPoint point : clickPoints) {
            if (point.getX() <= 0 || point.getY() <= 0) {
                Toast.makeText(this, "点击位置坐标必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 启动悬浮窗服务
        Intent serviceIntent = new Intent(this, FloatingWindowService.class);

        // 传递点击位置数据
        ArrayList<Integer> pointsX = new ArrayList<>();
        ArrayList<Integer> pointsY = new ArrayList<>();
        ArrayList<Integer> pointsWait = new ArrayList<>();
        ArrayList<Integer> pointsRepeat = new ArrayList<>();
        ArrayList<Integer> pointsClickInterval = new ArrayList<>();
        ArrayList<Integer> pointsRoundInterval = new ArrayList<>();

        for (ClickPoint point : clickPoints) {
            pointsX.add(point.getX());
            pointsY.add(point.getY());
            pointsWait.add(point.getWaitTime());
            pointsRepeat.add(point.getRepeatCount());
            pointsClickInterval.add(point.getClickInterval());
            pointsRoundInterval.add(point.getRoundInterval());
        }

        serviceIntent.putIntegerArrayListExtra("click_points_x", pointsX);
        serviceIntent.putIntegerArrayListExtra("click_points_y", pointsY);
        serviceIntent.putIntegerArrayListExtra("click_points_wait", pointsWait);
        serviceIntent.putIntegerArrayListExtra("click_points_repeat", pointsRepeat);
        serviceIntent.putIntegerArrayListExtra("click_points_click_interval", pointsClickInterval);
        serviceIntent.putIntegerArrayListExtra("click_points_round_interval", pointsRoundInterval);

        startService(serviceIntent);

        Toast.makeText(this, "自动点击器已启动", Toast.LENGTH_SHORT).show();
    }
}
