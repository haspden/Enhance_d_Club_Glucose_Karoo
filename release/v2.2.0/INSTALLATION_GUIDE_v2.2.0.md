# Quick Installation Guide - v2.2.0

## 🚀 Fast Installation (Recommended)

### Using Hammerhead App (Easiest)

1. **Download** `Enhance-d_Club_Glucose_Karoo_v2.2.0.apk` from this release
2. **Install** the Hammerhead app on your phone:
   - [Google Play Store](https://play.google.com/store/apps/details?id=io.hammerhead.karoo)
   - [App Store](https://apps.apple.com/us/app/hammerhead-karoo/id1441752191)
3. **Connect** your Karoo to the same WiFi as your phone
4. **Open** Hammerhead app and sign in
5. **Go to** Device Settings → Install APK
6. **Upload** the downloaded APK file
7. **Follow** the on-screen instructions

### Using USB Debugging

1. **Download** `Enhance-d_Club_Glucose_Karoo_v2.2.0.apk`
2. **Enable USB debugging** on your Karoo:
   - Settings → About → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"
3. **Connect** via USB to your computer
4. **Install**: `adb install Enhance-d_Club_Glucose_Karoo_v2.2.0.apk`

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
- **Local**: `http://127.0.0.1:17580/sgv.json`

## 🔄 Automatic Updates

### New in v2.2.0:

- **Auto-check on app launch** - App checks for updates when opened
- **One-click updates** - Download and install with a single tap
- **Smart prompts** - Only shows once per session to avoid annoyance
- **Release notes preview** - See what's new before updating

### How it works:

1. **Open the app** - Update check happens automatically after 2 seconds
2. **See update dialog** - If a newer version is available
3. **Tap "Update Now"** - Download and install automatically
4. **Done!** - App restarts with new version

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

- **Glu. mg** - Blood glucose in mg/dL
- **Glu. mmol** - Blood glucose in mmol/L
- **Time Since (s)** - Seconds since last reading
- **Time Since** - Formatted time (h:m:s) since last reading
- **Direction** - Glucose trend with arrow indicators
- **15m Δ mg** - 15-minute glucose change in mg/dL
- **15m Δ mmol** - 15-minute glucose change in mmol/L
- **5m Δ mg** - 5-minute glucose change in mg/dL
- **5m Δ mmol** - 5-minute glucose change in mmol/L

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
**Version**: 2.2.0  
**Release Date**: August 20, 2025  
**Requires**: Android 7.0+ (API 24)
