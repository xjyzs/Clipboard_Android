<div align="center">

# Network Clipboard

**Seamless clipboard sync across all your devices**

[![Android](https://img.shields.io/badge/Android-8.0%2B-green?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=google&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Release](https://img.shields.io/github/v/release/xjyzs/Clipboard_Android?color=orange)](https://github.com/xjyzs/Clipboard/releases)

[中文](README_CN.md) · [Report Bug](https://github.com/xjyzs/Clipboard/issues) · [Request Feature](https://github.com/xjyzs/Clipboard/issues)

---

</div>

## ✨ Why Network Clipboard?

Copy on your phone, paste on your PC. Copy on your PC, paste on your tablet. **Zero friction, instant sync.**

Network Clipboard is a **free & open-source** Android client that automatically syncs your clipboard content across devices and web in real-time. Powered by [Xposed](https://github.com/nicholaskemery/XposedBridge) framework, it hooks directly into Android's clipboard system for **true seamless integration** — no manual actions needed after setup.

## 🚀 Features

| Feature | Description |
|---------|-------------|
| **Auto Sync** | Copy anywhere, instantly available on all connected devices |
| **Background Service** | Keeps running reliably with foreground service + wake lock |
| **Web Compatible** | Connects to any Socket.IO server — works with web, desktop, and more |
| **Deep Integration** | Xposed module hooks system clipboard for zero-delay detection |
| **Modern UI** | Material 3 Expressive design with glassmorphism blur effects |
| **Light & Dark** | Full theme support following system settings |
| **Multi-Arch** | Builds for arm64, arm, x86, x86_64, and universal |
| **Battery Aware** | Smart battery optimization handling for reliable background sync |

## Server Download
Get the self-hosted server component here:
[Clipboard_Server](https://github.com/xjyzs/Clipboard_Server)

## Other Clients
- [Windows](https://github.com/xjyzs/Clipboard_Windows)

## 📦 Architecture

```
┌──────────────┐     Socket.IO       ┌──────────────┐
│   Android    │ ◄─────────────────► │   Server     │
│   (Xposed)   │                     │  (Python)    │
└──────┬───────┘                     └──────┬───────┘
       │                                    │
  System Hook                     ┌─────────┴───────┐
  Clipboard                       │   Web Client    │
  Detection                       │   Desktop App   │
                                  │   Other Devices │
                                  └─────────────────┘
```

## 📲 Installation

### Prerequisites

- Android 8.0+ (API 26)
- [LSPosed](https://github.com/nicholaskemery/LSPosed) or compatible Xposed framework
- A Socket.IO compatible clipboard server (e.g., [Clipboard-Server](https://github.com/nicholaskemery/Clipboard-Server))

### Steps

1. Download the latest APK for your device architecture from [Releases](https://github.com/xjyzs/Clipboard/releases)
2. Install the APK
3. Enable the module in LSPosed and set scope to `System Framework`
4. Reboot your device
5. Open the app and configure your server URL
6. Grant notification permission when prompted

### Build Variants

| Variant | Arch | Min SDK | Recommended For |
|---------|------|---------|-----------------|
| `arm64Minsdk35` | arm64-v8a | 35 | Modern devices (Android 15+) |
| `arm64Minsdk29` | arm64-v8a | 29 | Most devices (Android 10+) |
| `arm64Minsdk26` | arm64-v8a | 26 | Older arm64 devices |
| `arm` | armeabi-v7a | 26 | 32-bit ARM devices |
| `x86_64` | x86_64 | 26 | Emulators / Chromebooks |
| `x86` | x86 | 26 | Older emulators |
| `universal` | All | 26 | Universal (larger size) |

## 🛠️ Build from Source

```bash
git clone https://github.com/xjyzs/Clipboard.git
cd Clipboard
./gradlew assembleRelease
```

APKs will be in `app/build/outputs/apk/`.

## 🤝 Contributing

Contributions are welcome! Feel free to:

- 🐛 Report bugs via [Issues](https://github.com/xjyzs/Clipboard/issues)
- 💡 Suggest features
- 🔧 Submit Pull Requests

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

**If you find this project useful, please consider giving it a ⭐**

Made with ❤️ by [雪霁银装素](https://github.com/xjyzs)

</div>
