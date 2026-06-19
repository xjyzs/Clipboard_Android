<div align="center">

# 网络剪贴板

**跨设备剪贴板无缝同步**

[![Android](https://img.shields.io/badge/Android-8.0%2B-green?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=google&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Release](https://img.shields.io/github/v/release/xjyzs/Clipboard_Android?color=orange)](https://github.com/xjyzs/Clipboard/releases)

[English](README.md) · [报告 Bug](https://github.com/xjyzs/Clipboard/issues) · [功能建议](https://github.com/xjyzs/Clipboard/issues)

---

</div>

## ✨ 为什么选择网络剪贴板？

手机上复制，电脑上粘贴。电脑上复制，平板上粘贴。**零操作，即时同步。**

网络剪贴板是一款**免费开源**的 Android 客户端，能自动将剪贴板内容实时同步到其他设备和 Web 端。基于 [Xposed](https://github.com/nicholaskemery/XposedBridge) 框架，直接 Hook 系统剪贴板服务，实现**真正的无缝集成** —— 配置完成后无需任何手动操作。

## 🚀 核心特性

| 特性 | 说明 |
|------|------|
| **自动同步** | 任意设备复制，所有已连接设备即时可用 |
| **后台常驻** | 前台服务 + Wake Lock，确保稳定后台运行 |
| **Web 兼容** | 基于 Socket.IO 协议，可对接 Web、桌面端等 |
| **深度集成** | Xposed 模块 Hook 系统剪贴板，零延迟检测 |
| **现代 UI** | Material 3 Expressive 设计 + 毛玻璃模糊效果 |
| **深色模式** | 完整的亮/暗色主题，跟随系统设置 |
| **多架构** | 支持 arm64、arm、x86、x86_64 及通用版本 |
| **省电优化** | 智能电池优化处理，确保后台同步可靠 |

## 服务端下载
[Clipboard_Server](https://github.com/xjyzs/Clipboard_Server)

## 其他客户端下载
[Windows)](https://github.com/xjyzs/Clipboard_Windows)

## 📦 工作原理

```
┌──────────────┐     Socket.IO       ┌──────────────┐
│   Android    │ ◄─────────────────► │   服务端      │
│  (Xposed)    │                     │  (Python)    │
└──────┬───────┘                     └──────┬───────┘
       │                                    │
  系统级 Hook                       ┌────────┴────────┐
  剪贴板检测                        │   Web 客户端      │
                                  │   桌面客户端       │
                                  │   其他设备         │
                                  └──────────────────┘
```

## 📲 安装指南

### 前置条件

- Android 8.0+（API 26）
- [LSPosed](https://github.com/nicholaskemery/LSPosed) 或兼容的 Xposed 框架
- Socket.IO 兼容的剪贴板服务端

### 安装步骤

1. 从 [Releases](https://github.com/xjyzs/Clipboard/releases) 下载适配你设备架构的 APK
2. 安装 APK
3. 在 LSPosed 中启用模块，作用域勾选 `系统框架`
4. 重启设备
5. 打开应用，配置服务器 URL
6. 根据提示授予通知权限

### 构建变体

| 变体 | 架构 | 最低版本 | 推荐设备 |
|------|------|---------|---------|
| `arm64Minsdk35` | arm64-v8a | 35 | 新设备 (Android 15+) |
| `arm64Minsdk29` | arm64-v8a | 29 | 大部分设备 (Android 10+) |
| `arm64Minsdk26` | arm64-v8a | 26 | 旧款 arm64 设备 |
| `arm` | armeabi-v7a | 26 | 32 位 ARM 设备 |
| `x86_64` | x86_64 | 26 | 模拟器 / Chromebook |
| `x86` | x86 | 26 | 旧版模拟器 |
| `universal` | 全部 | 26 | 通用版（体积较大） |

## 🛠️ 从源码构建

```bash
git clone https://github.com/xjyzs/Clipboard.git
cd Clipboard
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/`

## 🤝 参与贡献

欢迎参与项目改进！

- 🐛 通过 [Issues](https://github.com/xjyzs/Clipboard/issues) 报告 Bug
- 💡 提出功能建议
- 🔧 提交 Pull Request

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源。

---

<div align="center">

**如果觉得这个项目有用，请点个 ⭐ 支持一下吧**

Made with ❤️ by [雪霁银装素](https://github.com/xjyzs)

</div>
