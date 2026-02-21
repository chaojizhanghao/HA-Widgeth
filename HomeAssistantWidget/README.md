# Home Assistant 桌面小部件

一个轻量级的Android应用，用于从Home Assistant服务器拉取设备状态并在桌面小部件上显示。

## 功能特性

- 从Home Assistant API获取设备状态
- 在桌面小部件上实时显示数值
- 支持手动刷新和自动更新
- 可配置多个小部件显示不同设备
- 离线时显示上次成功获取的数据

## 项目结构

```
HomeAssistantWidget/
├── app/
│   ├── build.gradle              # 应用级构建配置
│   ├── proguard-rules.pro        # ProGuard混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml   # 应用清单
│       ├── java/com/example/hawidget/
│       │   ├── MainActivity.java       # 配置页面
│       │   ├── HAPreferences.java      # 配置存储
│       │   ├── HAService.java          # HA API服务
│       │   ├── HAWidgetProvider.java   # 小部件Provider
│       │   └── HAUpdateService.java    # 后台更新服务
│       └── res/
│           ├── layout/           # 布局文件
│           ├── xml/              # 小部件配置
│           ├── drawable/         # 图形资源
│           └── values/           # 字符串、颜色等
├── build.gradle                  # 项目级构建配置
├── settings.gradle
└── gradle.properties
```

## 使用方法

### 1. 配置Home Assistant

1. 打开应用
2. 输入Home Assistant服务器地址 (如: `http://homeassistant.local:8123`)
3. 在用户资料中创建**长期访问令牌 (Long-Lived Access Token)**
4. 输入要监控的实体ID (如: `sensor.living_room_temperature`)
5. 点击"测试连接"验证配置
6. 点击"保存配置"

### 2. 添加桌面小部件

1. 长按桌面空白处
2. 选择"小部件"或"Widgets"
3. 找到"HA 设备监控"
4. 拖动到桌面合适位置

### 3. 获取访问令牌

在Home Assistant中:
1. 点击左下角用户名
2. 滚动到底部"长期访问令牌"
3. 点击"创建令牌"
4. 复制令牌到应用中使用

## 编译方法

### 使用 Android Studio

1. 克隆或下载项目
2. 用 Android Studio 打开项目
3. 同步Gradle
4. 构建 APK: Build → Build Bundle(s) / APK(s) → Build APK(s)

### 使用命令行

```bash
# Linux/Mac
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

APK文件将生成在 `app/build/outputs/apk/debug/app-debug.apk`

## 技术栈

- **语言**: Java
- **最小SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)
- **网络库**: OkHttp 4.11.0
- **UI组件**: Material Design 3

## 权限说明

- `INTERNET`: 连接Home Assistant服务器
- `ACCESS_NETWORK_STATE`: 检查网络状态

## 注意事项

1. 确保手机和Home Assistant服务器在同一网络，或服务器有公网访问
2. 如使用HTTPS，确保证书有效
3. 令牌请妥善保管，不要分享给他人
4. 小部件默认每30分钟自动更新一次

## 项目体积

- 源代码: < 100KB
- 编译后APK: ~2-3MB
- 完全符合GitHub上传要求

## License

MIT License