# 微信红包助手 (WeChat Red Packet Assistant)

一个基于 Android 无障碍服务的微信红包自动抢取工具。

## 功能特点

- 自动检测并抢取微信红包
- 支持指定群聊/联系人监控
- 完全中文界面
- 基于 Android 辅助功能服务实现，无需 Root
- 使用 Jetpack Compose 构建现代化 UI

## 系统要求

- Android 7.0 (API 24) 及以上
- 需要开启辅助功能权限
- 已安装微信应用

## 使用说明

1. **安装应用**
   - 从 Release 下载最新版本 APK
   - 或从源码构建安装

2. **授予权限**
   - 打开应用
   - 点击"前往辅助功能设置"
   - 在系统设置中找到并启用"红包助手"服务

3. **配置监控**
   - 添加需要监控的群聊/联系人名称
   - 如不添加则监控所有聊天
   - 启用服务开关

## 开发环境

- Android Studio Hedgehog | 2023.1.1
- Kotlin 1.9.0
- Gradle 8.2
- Jetpack Compose
- minSdk 24
- targetSdk 34

## 构建说明

```bash
# 克隆项目
git clone https://github.com/yourusername/RedPacketAssistant.git

# 使用 Android Studio 打开项目
# 等待 Gradle 同步完成

# 构建 Debug 版本
./gradlew assembleDebug

# 构建 Release 版本
./gradlew assembleRelease
```

## 项目结构

```
app/src/main/
├── java/com/example/redpacketassistant/
│   ├── MainActivity.kt              # 主界面
│   ├── service/
│   │   └── RedPacketService.kt      # 辅助功能服务实现
│   └── utils/
│       └── PreferenceHelper.kt      # SharedPreferences 工具类
└── res/
    ├── values/
    │   └── strings.xml              # 英文字符串资源
    └── values-zh/
        └── strings.xml              # 中文字符串资源
```

## 注意事项

- 本项目仅供学习研究使用
- 请遵守相关法律法规和微信用户协议
- 不保证在所有设备和微信版本上都能正常工作

## License

```
MIT License

Copyright (c) 2024 Your Name

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.