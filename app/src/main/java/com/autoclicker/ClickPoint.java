package com.autoclicker;

/**
 * 点击位置数据类
 */
public class ClickPoint {
    private int x;
    private int y;
    private int waitTime;      // 点击后等待时间（毫秒）
    private int repeatCount;   // 重复次数（0=无限）
    private int clickInterval; // 点击间隔（毫秒）
    private int roundInterval; // 轮次间隔（毫秒）

    public ClickPoint(int x, int y, int waitTime) {
        this.x = x;
        this.y = y;
        this.waitTime = waitTime;
        this.repeatCount = 10;
        this.clickInterval = 500;
        this.roundInterval = 2000;
    }

    public ClickPoint(int x, int y, int waitTime, int repeatCount, int clickInterval, int roundInterval) {
        this.x = x;
        this.y = y;
        this.waitTime = waitTime;
        this.repeatCount = repeatCount;
        this.clickInterval = clickInterval;
        this.roundInterval = roundInterval;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getClickInterval() {
        return clickInterval;
    }

    public void setClickInterval(int clickInterval) {
        this.clickInterval = clickInterval;
    }

    public int getRoundInterval() {
        return roundInterval;
    }

    public void setRoundInterval(int roundInterval) {
        this.roundInterval = roundInterval;
    }
}
