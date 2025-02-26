# 微信红包助手 (WeChat Red Packet Assistant)

一个基于 Android 无障碍服务的微信红包自动抢取工具。

## 功能特点

- 自动检测并抢取微信红包
- 支持指定群聊/联系人监控
- 完全中文界面
- 基于 Android 辅助功能服务实现，无需 Root
- 使用 Jetpack Compose 构建现代化 UI

## 技术栈

- Kotlin
- Jetpack Compose
- Android Accessibility Service
- Material3 Design
- SharedPreferences 数据持久化

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

## 构建说明

1. **环境要求**
   ```
   Android Studio Hedgehog | 2023.1.1
   Kotlin 1.9.0
   Gradle 8.2
   ```

2. **构建步骤**
   ```bash
   # 克隆项目
   git clone https://github.com/Ming-H/RedPacketAssistant.git
   
   # 使用 Android Studio 打开项目
   # 等待 Gradle 同步完成
   
   # 构建 Debug 版本
   ./gradlew assembleDebug
   
   # 构建 Release 版本
   ./gradlew assembleRelease
   ```

## 项目结构 
app/src/main/
├── java/com/example/redpacketassistant/
│ ├── MainActivity.kt # 主界面
│ ├── service/
│ │ └── RedPacketService.kt # 辅助功能服务实现
│ └── utils/
│ └── PreferenceHelper.kt # SharedPreferences 工具类
└── res/
├── values/
│ └── strings.xml # 英文字符串资源
└── values-zh/
└── strings.xml # 中文字符串资源
```

## 注意事项

1. **权限说明**
   - 应用需要辅助功能权限才能正常工作
   - 某些系统可能会自动关闭辅助功能服务，需要手动重新开启

2. **兼容性**
   - 不同版本的微信可能会导致识别失败
   - 不同品牌手机的系统可能会有差异

3. **使用建议**
   - 建议将应用加入电池优化白名单
   - 建议定期检查服务是否正常运行

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进项目。

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 免责声明

本项目仅供学习研究使用，请勿用于商业用途。对于使用本项目造成的任何问题，作者不承担任何责任。