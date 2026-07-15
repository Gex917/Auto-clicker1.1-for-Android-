# 自动点击器 / AutoClicker

[中文](#中文) | [English](#english)

---

## 中文

### 📖 简介

一款 Android 自动点击工具，适用于游戏和重复性任务。支持多点点击、可视化拖动选点、配置保存/加载等功能。无需 Root 权限。

### ✨ 功能特性

- 🎯 **多点点击** - 支持设置多个点击位置，每个位置独立配置
- 📍 **屏幕选点** - 可视化拖动设置点击位置，实时显示坐标
- ⚙️ **灵活配置** - 每个点可独立设置重复次数、点击间隔、轮次间隔
- 💾 **配置管理** - 保存/加载点击配置，自动记忆上次设置
- 🪟 **悬浮窗控制** - 运行时显示悬浮窗，支持拖拽、开始/停止/退出
- 🔒 **无需 Root** - 使用无障碍服务实现点击，无需 Root 权限

### 📋 系统要求

- Android 7.0 (API 24) 及以上
- 需要开启无障碍服务权限
- 需要开启悬浮窗权限

### 🚀 使用方法

1. **安装应用** - 将 APK 安装到手机
2. **开启权限**
   - 无障碍服务：设置 → 无障碍 → 自动点击器 → 开启
   - 悬浮窗权限：设置 → 应用 → 自动点击器 → 悬浮窗 → 开启
3. **设置点击位置**
   - 手动输入坐标，或
   - 点击「屏幕拖动选点」在屏幕上可视化拖动设置
4. **配置参数**
   - 重复次数：0 表示无限循环
   - 点击间隔：每次点击后等待时间（毫秒）
   - 轮次间隔：每轮结束后等待时间（毫秒）
5. **保存配置**（可选）- 点击「保存配置」以便下次使用
6. **启动运行** - 点击「启动自动点击」按钮
7. **控制运行** - 使用悬浮窗控制开始/停止

### 📁 项目结构

```
AutoClicker/
├── app/
│   └── src/main/
│       ├── java/com/autoclicker/
│       │   ├── MainActivity.java          # 主界面
│       │   ├── AutoClickService.java      # 无障碍服务
│       │   ├── FloatingWindowService.java # 悬浮窗服务
│       │   ├── ScreenPickerActivity.java  # 屏幕选点界面
│       │   ├── DraggablePointView.java    # 可拖动点视图
│       │   ├── ClickPoint.java            # 点击位置数据类
│       │   ├── ClickPointAdapter.java     # 列表适配器
│       │   ├── ClickEffectService.java    # 点击效果服务
│       │   └── ConfigManager.java         # 配置管理器
│       └── res/
│           ├── layout/                    # 布局文件
│           └── drawable/                  # 图形资源
└── README.md
```

### 🔧 构建方法

1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 点击 Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`

### ⚠️ 注意事项

- 部分游戏可能检测无障碍服务并禁止使用
- 建议点击间隔不要设置过小（≥200ms），避免被检测
- 首次使用需要手动开启无障碍服务和悬浮窗权限

---

## English

### 📖 Introduction

An Android auto-clicking tool for games and repetitive tasks. Supports multi-point clicking, visual drag-to-position, configuration save/load, and more. No Root required.

### ✨ Features

- 🎯 **Multi-point Clicking** - Set multiple click positions with independent configurations
- 📍 **Screen Picking** - Visually drag to set click positions with real-time coordinate display
- ⚙️ **Flexible Configuration** - Each point can have independent repeat count, click interval, and round interval
- 💾 **Config Management** - Save/load click configurations, auto-remembers last settings
- 🪟 **Floating Window** - Control panel with drag support, start/stop/exit buttons
- 🔒 **No Root Required** - Uses Accessibility Service for clicking

### 📋 Requirements

- Android 7.0 (API 24) or higher
- Accessibility Service permission required
- Overlay (floating window) permission required

### 🚀 How to Use

1. **Install** - Install the APK on your phone
2. **Enable Permissions**
   - Accessibility: Settings → Accessibility → AutoClicker → Enable
   - Overlay: Settings → Apps → AutoClicker → Display over other apps → Enable
3. **Set Click Positions**
   - Enter coordinates manually, or
   - Tap "Screen Drag Picking" to visually drag and set positions
4. **Configure Parameters**
   - Repeat Count: 0 for infinite loop
   - Click Interval: Wait time after each click (milliseconds)
   - Round Interval: Wait time after each round (milliseconds)
5. **Save Configuration** (optional) - Tap "Save Config" for future use
6. **Start** - Tap the "Start Auto Click" button
7. **Control** - Use the floating window to start/stop

### 📁 Project Structure

```
AutoClicker/
├── app/
│   └── src/main/
│       ├── java/com/autoclicker/
│       │   ├── MainActivity.java          # Main activity
│       │   ├── AutoClickService.java      # Accessibility service
│       │   ├── FloatingWindowService.java # Floating window service
│       │   ├── ScreenPickerActivity.java  # Screen picker activity
│       │   ├── DraggablePointView.java    # Draggable point view
│       │   ├── ClickPoint.java            # Click point data class
│       │   ├── ClickPointAdapter.java     # List adapter
│       │   ├── ClickEffectService.java    # Click effect service
│       │   └── ConfigManager.java         # Configuration manager
│       └── res/
│           ├── layout/                    # Layout files
│           └── drawable/                  # Drawable resources
└── README.md
```

### 🔧 Build Instructions

1. Open the project with Android Studio
2. Wait for Gradle sync to complete
3. Click Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK output location: `app/build/outputs/apk/debug/app-debug.apk`

### ⚠️ Notes

- Some games may detect Accessibility Service and block usage
- Recommended click interval: ≥200ms to avoid detection
- First use requires manually enabling Accessibility Service and Overlay permissions

---

## 📄 License

MIT License

## 🤝 Contributing

Welcome to submit Issues and Pull Requests!

## 📧 Contact

如有问题请提交 Issue / For questions, please submit an Issue
