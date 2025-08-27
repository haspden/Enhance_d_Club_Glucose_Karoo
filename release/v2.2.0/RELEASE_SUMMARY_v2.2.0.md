# Enhance-d Club Glucose - Karoo v2.2.0 Release

## 📦 Release Files

- **`Enhance-d_Club_Glucose_Karoo_v2.2.0.apk`** (8.8 MB) - Signed APK with automatic update functionality
- **`RELEASE_NOTES_v2.2.0.md`** - Comprehensive release notes
- **`INSTALLATION_GUIDE_v2.2.0.md`** - Quick installation guide
- **`SHA256_CHECKSUM_v2.2.0.txt`** - SHA256 checksum for file verification

## 🎯 What's New in v2.2.0

### ✨ Major Features

- **Automatic Update System** - App checks for updates when opened and prompts users to install new versions
- **GitHub Integration** - Seamless updates directly from GitHub releases
- **One-Click Updates** - Download and install new versions with a single tap
- **Smart Update Prompts** - Only shows update dialog once per app session to avoid annoying users
- **Release Notes Preview** - See what's new before updating
- **Manual Update Check** - Still available in Settings for users who want to check manually

### 🔧 Improvements

- **Better User Experience** - Automatic update detection without manual intervention
- **Seamless Installation** - Automatic APK installation after download
- **Progress Tracking** - Shows download progress during updates
- **Error Handling** - Comprehensive error handling for network issues and failed downloads
- **Version Management** - Smart version comparison and update detection

### 🐛 Bug Fixes

- Fixed extension data feeding issues (extension now reads settings correctly)
- Improved network connectivity handling
- Enhanced error messages and user feedback
- Better API token management and visibility

## 📱 Installation

### Recommended: Hammerhead App Method

1. Download the APK
2. Use the Hammerhead app to transfer to your Karoo device
3. Follow the on-screen installation instructions

### Alternative: USB Debugging

1. Enable USB debugging on your Karoo
2. Use `adb install` to install the APK

## 🔗 Supported Nightscout Sources

1. **Cloud-based Nightscout servers** (with optional API token auth)
2. **Phone hotspot with xDrip+ broadcast**
3. **Local xDrip+ on Karoo device**

## 🔄 Automatic Updates

### How It Works

1. **App Launch**: When you open the app, it automatically checks for updates after 2 seconds
2. **Update Detection**: Compares your current version with the latest GitHub release
3. **Update Prompt**: If a newer version is available, shows an update dialog
4. **One-Click Update**: Tap "Update Now" to download and install automatically
5. **Session Management**: Only prompts once per app session to avoid annoyance

### Update Features

- **Automatic Detection**: No manual checking required
- **Release Notes**: See what's new before updating
- **Progress Tracking**: Shows download progress
- **Error Recovery**: Handles network issues gracefully
- **Manual Override**: Still available in Settings menu

## 🔐 Enhanced API Token Features

### New Capabilities:

- **Default Visibility** - Tokens shown clearly by default
- **Privacy Toggle** - Optional switch to hide tokens
- **Real-time Display** - See actual token values in Settings
- **Better Editing** - Clear visibility during configuration

### Setup:

1. **Open Settings** → **Configure API Token**
2. **Enter your token** (visible by default)
3. **Use the toggle** to hide token if needed for privacy
4. **Save configuration**
5. **Restart app** to apply changes

## 📊 Available Data Fields

- **Glu. mg** - Blood glucose in mg/dL
- **Glu. mmol** - Blood glucose in mmol/L
- **Time Since (s)** - Seconds since last reading
- **Time Since** - Formatted time (h:m:s) since last reading
- **Direction** - Glucose trend with arrow indicators
- **15m Δ mg** - 15-minute glucose change in mg/dL
- **15m Δ mmol** - 15-minute glucose change in mmol/L
- **5m Δ mg** - 5-minute glucose change in mg/dL
- **5m Δ mmol** - 5-minute glucose change in mmol/L

## ⚠️ Important Notes

- **Medical Disclaimer**: This app is for informational purposes only
- **System Requirements**: Android 7.0+ (API 24)
- **Data Usage**: ~1MB per hour of ride time + minimal update check data
- **Background Operation**: Glucose data fields work automatically in background

## 🔄 Updating from Previous Versions

1. **Automatic**: The app will prompt you to update when opened
2. **Manual**: Use "Check for Updates" in Settings
3. **Manual Installation**: Download and install APK manually
4. **Your settings will be preserved** during updates

## 📞 Support

- **GitHub Issues**: [Report bugs and request features](https://github.com/haspden/Enhance_d_Club_Glucose_Karoo/issues)
- **Email**: uk@club.enhance-d.com
- **Documentation**: Check the README.md file for detailed setup instructions

## 💪 About Enhance-d Club UK

**Enhance-d Club UK** is a not-for-profit community built by and for people living with type 1 diabetes who refuse to let it hold them back. From cyclists and runners to swimmers and weekend adventurers, we support each other to stay active, healthy, and connected.

### Our Mission:

- **💪 Empower** people with type 1 diabetes to live active, fulfilling lives
- **🚴 Create safe spaces** to share experiences, challenges, and wins
- **🤝 Build community** through meetups, events, and online support
- **🌍 Break down barriers** to access, so everyone can enjoy the benefits of sport and activity

### Support the Community:

Your support helps us keep the community thriving – from hosting meetups, to creating resources, to backing initiatives that make active living with type 1 diabetes more accessible to all.

**☕ Every coffee fuels community, connection, and action.**

_Together, we're proving that diabetes doesn't define limits._

---

**Version**: 2.2.0  
**Release Date**: August 27, 2025  
**File Size**: 8.8 MB  
**Supported Devices**: Karoo, Karoo 2, Karoo 3

_Brought to you by Enhance-d Club_
