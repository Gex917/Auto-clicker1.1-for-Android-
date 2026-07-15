package com.autoclicker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 屏幕选点界面 - 全屏透明，可拖动设置点击位置
 */
public class ScreenPickerActivity extends AppCompatActivity {

    private FrameLayout container;
    private List<DraggablePointView> pointViews = new ArrayList<>();
    private List<int[]> positions = new ArrayList<>(); // [x, y]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏透明
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // 创建根布局
        container = new FrameLayout(this);
        container.setBackgroundColor(Color.parseColor("#40000000")); // 半透明背景

        // 从 Intent 获取已有的点击位置
        ArrayList<Integer> pointsX = getIntent().getIntegerArrayListExtra("points_x");
        ArrayList<Integer> pointsY = getIntent().getIntegerArrayListExtra("points_y");

        if (pointsX != null && pointsY != null) {
            for (int i = 0; i < pointsX.size(); i++) {
                positions.add(new int[]{pointsX.get(i), pointsY.get(i)});
            }
        }

        // 如果没有点，添加一个默认点
        if (positions.isEmpty()) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            positions.add(new int[]{metrics.widthPixels / 2, metrics.heightPixels / 2});
        }

        // 创建所有可拖动的点
        for (int i = 0; i < positions.size(); i++) {
            addPointView(i, positions.get(i)[0], positions.get(i)[1]);
        }

        // 添加底部按钮
        addButtons();

        setContentView(container);
    }

    /**
     * 添加一个可拖动的点
     */
    private void addPointView(int index, int x, int y) {
        DraggablePointView pointView = new DraggablePointView(this, index, x, y);
        pointView.setOnPositionChangedListener((pointIndex, newX, newY) -> {
            positions.get(pointIndex)[0] = newX;
            positions.get(pointIndex)[1] = newY;
        });

        // 长按删除
        pointView.setOnLongClickListener(v -> {
            if (positions.size() <= 1) {
                Toast.makeText(this, "至少保留一个点击位置", Toast.LENGTH_SHORT).show();
                return true;
            }

            new AlertDialog.Builder(this)
                    .setTitle("删除点击位置")
                    .setMessage("确定要删除这个点击位置吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        int pos = pointViews.indexOf(pointView);
                        if (pos >= 0) {
                            positions.remove(pos);
                            pointViews.remove(pos);
                            container.removeView(pointView);
                            refreshPointIndices();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

        // 设置初始位置
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        int size = 80;
        params.leftMargin = x - size / 2;
        params.topMargin = y - size / 2;
        params.width = size;
        params.height = size;

        container.addView(pointView, params);
        pointViews.add(pointView);
    }

    /**
     * 刷新所有点的序号
     */
    private void refreshPointIndices() {
        for (int i = 0; i < pointViews.size(); i++) {
            pointViews.get(i).setIndex(i);
        }
    }

    /**
     * 添加底部按钮
     */
    private void addButtons() {
        FrameLayout buttonContainer = new FrameLayout(this);

        // 添加点按钮
        Button btnAdd = new Button(this);
        btnAdd.setText("＋ 添加点");
        btnAdd.setBackgroundColor(Color.parseColor("#FF4081"));
        btnAdd.setTextColor(Color.WHITE);
        btnAdd.setTextSize(14);
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        addParams.gravity = Gravity.BOTTOM | Gravity.START;
        addParams.leftMargin = 40;
        addParams.bottomMargin = 80;
        btnAdd.setLayoutParams(addParams);
        btnAdd.setOnClickListener(v -> {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int newX = metrics.widthPixels / 2;
            int newY = metrics.heightPixels / 2;
            positions.add(new int[]{newX, newY});
            addPointView(positions.size() - 1, newX, newY);
        });

        // 完成按钮
        Button btnDone = new Button(this);
        btnDone.setText("✓ 完成");
        btnDone.setBackgroundColor(Color.parseColor("#4CAF50"));
        btnDone.setTextColor(Color.WHITE);
        btnDone.setTextSize(14);
        FrameLayout.LayoutParams doneParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        doneParams.gravity = Gravity.BOTTOM | Gravity.END;
        doneParams.rightMargin = 40;
        doneParams.bottomMargin = 80;
        btnDone.setLayoutParams(doneParams);
        btnDone.setOnClickListener(v -> finishWithResult());

        buttonContainer.addView(btnAdd);
        buttonContainer.addView(btnDone);

        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.BOTTOM;
        container.addView(buttonContainer, containerParams);
    }

    /**
     * 返回结果给 MainActivity
     */
    private void finishWithResult() {
        Intent resultIntent = new Intent();
        ArrayList<Integer> resultX = new ArrayList<>();
        ArrayList<Integer> resultY = new ArrayList<>();

        for (int[] pos : positions) {
            resultX.add(pos[0]);
            resultY.add(pos[1]);
        }

        resultIntent.putIntegerArrayListExtra("result_x", resultX);
        resultIntent.putIntegerArrayListExtra("result_y", resultY);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
    }
}
