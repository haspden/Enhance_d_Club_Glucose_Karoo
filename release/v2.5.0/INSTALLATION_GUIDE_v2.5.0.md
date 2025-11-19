# Quick Installation Guide - v2.5.0

## 🚀 Fast Installation (Recommended)

### Using Hammerhead App (Easiest)

1. **Download** `Enhance-d_Club_Glucose_Karoo_v2.5.0.apk` from this release
2. **Install** the Hammerhead app on your phone:
   - [Google Play Store](https://play.google.com/store/apps/details?id=io.hammerhead.karoo)
   - [App Store](https://apps.apple.com/us/app/hammerhead-karoo/id1441752191)
3. **Connect** your Karoo to the same WiFi as your phone
4. **Open** Hammerhead app and sign in
5. **Go to** Device Settings → Install APK
6. **Upload** the downloaded APK file
7. **Follow** the on-screen instructions

### Using USB Debugging

1. **Download** `Enhance-d_Club_Glucose_Karoo_v2.5.0.apk`
2. **Enable USB debugging** on your Karoo:
   - Settings → About → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"
3. **Connect** via USB to your computer
4. **Install**: `adb install Enhance-d_Club_Glucose_Karoo_v2.5.0.apk`

## ⚙️ First Time Setup

1. **Launch** the app from your Karoo's app drawer
2. **Accept** the medical disclaimer
3. **Configure** your Nightscout URL in Settings
4. **Configure API Token** (optional) - now with enhanced visibility
5. **Add data fields** to your Karoo ride profile

## 📱 Supported Devices

- ✅ Hammerhead Karoo
- ✅ Hammerhead Karoo 2
- ✅ Hammerhead Karoo 3

## 🔗 Nightscout Sources

- **Cloud**: `https://yournightscout.com/api/v1/entries/sgv.json`
- **Hotspot**: `http://[GATEWAY_IP]:17580/sgv.json` (auto-detected)
  - ⚠️ **Note**: May not work on recent Android devices due to Layer 2 Firewalls
- **Local**: `http://127.0.0.1:17580/sgv.json` (e.g. xDrip+ installed on Karoo device)

## 🎉 What's New in v2.5.0

### New Features:

- **Improved Update Detection**: Fixed version comparison to correctly handle GitHub release tags with suffixes
  - Now properly detects updates even when releases are marked as pre-releases or have version suffixes (e.g., `v2.4.1-beta`)
  - Enhanced version parsing ensures users always get notified of available updates

### Improvements:

- **Better Version Parsing**: More robust update detection across different release naming conventions
- **Improved Compatibility**: Correctly detects updates from pre-release and beta tags

### Features (from previous versions):

- **8 New Combo Fields**: Space-efficient fields combining time since, delta, and direction with glucose value
  - Combo 1: Time since (s) + 15m delta + arrow | Value
  - Combo 2: Time since (s) + 5m delta + arrow | Value
  - Combo 3: Time since (m) + 15m delta + arrow | Value
  - Combo 4: Time since (m) + 5m delta + arrow | Value
  - Available in both mg/dL and mmol/L units

### Previous Improvements:

- **Direction Arrows**: Now display actual arrow characters (↑↑, ↑, ↗, →, ↘, ↓, ↓↓) instead of numbers
- **mmol/L Precision**: All mmol/L values consistently show 1 decimal place
- **Better Formatting**: Improved combo field layout and display

## 🔄 Automatic Updates

### How it works:

1. **Open the app** - Update check happens automatically after 2 seconds
2. **See update dialog** - If a newer version is available
3. **Tap "Update Now"** - Download and install automatically
4. **Done!** - App restarts with new version

### Features:

- **Auto-check on app launch** - App checks for updates when opened
- **One-click updates** - Download and install with a single tap
- **Smart prompts** - Only shows once per session to avoid annoyance
- **Release notes preview** - See what's new before updating
- **Improved detection** - Now correctly handles pre-release and beta tags

## 🔐 Enhanced API Token Features

### Setup:

1. **Open Settings** → **Configure API Token**
2. **Enter your token** (visible by default)
3. **Use toggle** to hide token if needed for privacy
4. **Save configuration**
5. **Restart app** to apply changes

### Features:

- **Default visibility** - tokens shown clearly for easy verification
- **Privacy toggle** - optional switch to hide tokens
- **Real-time display** - see actual token values in Settings
- **Better editing** - clear visibility during configuration

## 📊 Available Data Fields

### Basic Fields (9 total)
- **Glu. mg** - Blood glucose in mg/dL
- **Glu. mmol** - Blood glucose in mmol/L (1 decimal place)
- **Time Since (s)** - Seconds since last reading
- **Time Since (m)** - Formatted time (h:m:s) since last reading
- **Direction** - Glucose trend with arrow indicators (↑↑, ↑, ↗, →, ↘, ↓, ↓↓)
- **15m Δ mg** - 15-minute glucose change in mg/dL
- **15m Δ mmol** - 15-minute glucose change in mmol/L
- **5m Δ mg** - 5-minute glucose change in mg/dL
- **5m Δ mmol** - 5-minute glucose change in mmol/L

### Combo Fields (NEW - 8 total, Graphical)
- **Combo 1 mg & mmol** - Time since (s) + 15m delta + arrow | Value
- **Combo 2 mg & mmol** - Time since (s) + 5m delta + arrow | Value
- **Combo 3 mg & mmol** - Time since (m) + 15m delta + arrow | Value
- **Combo 4 mg & mmol** - Time since (m) + 5m delta + arrow | Value

**Total: 17 data fields available**

## ⚠️ Important

- **Medical Disclaimer**: This app is for informational purposes only
- **Permissions**: Grant necessary permissions when prompted
- **Updates**: App will automatically check for updates when opened
- **Background Operation**: Glucose data fields work automatically in background

## 📞 Need Help?

- Check the full [README.md](../../README.md) for detailed instructions
- Report issues on [GitHub](https://github.com/haspden/Enhance_d_Club_Glucose_Karoo/issues)
- Email: uk@club.enhance-d.com

---

**File Size**: See APK details  
**Version**: 2.5.0  
**Release Date**: January 2025  
**Requires**: Android 7.0+ (API 24)

