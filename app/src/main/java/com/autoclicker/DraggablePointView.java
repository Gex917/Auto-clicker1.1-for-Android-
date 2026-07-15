package com.autoclicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * 可拖动的点击位置标记
 */
public class DraggablePointView extends View {

    private Paint circlePaint;
    private Paint textPaint;
    private Paint borderPaint;
    private Paint coordPaint;
    private Paint coordBgPaint;
    private int pointIndex;
    private int centerX, centerY;
    private int radius = 30; // 半径（像素）

    private OnPositionChangedListener listener;

    public interface OnPositionChangedListener {
        void onPositionChanged(int index, int newX, int newY);
    }

    public DraggablePointView(Context context, int index, int x, int y) {
        super(context);
        this.pointIndex = index;
        this.centerX = x;
        this.centerY = y;

        initPaints();
    }

    private void initPaints() {
        // 圆圈填充
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#80FF4081"));
        circlePaint.setStyle(Paint.Style.FILL);

        // 圆圈边框
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#FF4081"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);

        // 序号文字
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // 坐标背景
        coordBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordBgPaint.setColor(Color.parseColor("#CC333333"));
        coordBgPaint.setStyle(Paint.Style.FILL);

        // 坐标文字
        coordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordPaint.setColor(Color.WHITE);
        coordPaint.setTextSize(20);
        coordPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.listener = listener;
    }

    public void setPosition(int x, int y) {
        this.centerX = x;
        this.centerY = y;
        updateLayout();
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setIndex(int index) {
        this.pointIndex = index;
        invalidate();
    }

    private void updateLayout() {
        int width = radius * 2 + 20;
        int height = radius * 2 + 50; // 额外空间显示坐标
        int left = centerX - width / 2;
        int top = centerY - radius - 10;
        layout(left, top, left + width, top + height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = radius + 10; // 圆心位置，留出上方空间

        // 画圆圈
        canvas.drawCircle(cx, cy, radius, circlePaint);
        canvas.drawCircle(cx, cy, radius, borderPaint);

        // 画序号
        String indexText = String.valueOf(pointIndex + 1);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2;
        canvas.drawText(indexText, cx, textY, textPaint);

        // 画坐标标签
        String coordText = "(" + centerX + ", " + centerY + ")";
        float coordWidth = coordPaint.measureText(coordText);
        float coordHeight = 24;
        float coordY = cy + radius + coordHeight + 4;

        // 坐标背景
        canvas.drawRoundRect(
                cx - coordWidth / 2 - 6,
                coordY - coordHeight,
                cx + coordWidth / 2 + 6,
                coordY + 4,
                4, 4, coordBgPaint
        );

        // 坐标文字
        Paint.FontMetrics coordFm = coordPaint.getFontMetrics();
        float coordTextY = coordY - (coordFm.ascent + coordFm.descent) / 2;
        canvas.drawText(coordText, cx, coordTextY, coordPaint);
    }

    private float lastTouchX, lastTouchY;
    private boolean isDragging = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                isDragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float dx = event.getRawX() - lastTouchX;
                    float dy = event.getRawY() - lastTouchY;

                    centerX += (int) dx;
                    centerY += (int) dy;

                    // 限制在屏幕范围内
                    if (centerX < 0) centerX = 0;
                    if (centerY < 0) centerY = 0;

                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();

                    updateLayout();

                    if (listener != null) {
                        listener.onPositionChanged(pointIndex, centerX, centerY);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }
}
