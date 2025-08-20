# Quick Installation Guide - v2.1.0

## 🚀 Fast Installation (Recommended)

### Using Hammerhead App (Easiest)

1. **Download** `Enhance-d_Club_Glucose_Karoo_v2.1.0.apk` from this release
2. **Install** the Hammerhead app on your phone:
   - [Google Play Store](https://play.google.com/store/apps/details?id=io.hammerhead.karoo)
   - [App Store](https://apps.apple.com/us/app/hammerhead-karoo/id1441752191)
3. **Connect** your Karoo to the same WiFi as your phone
4. **Open** Hammerhead app and sign in
5. **Go to** Device Settings → Install APK
6. **Upload** the downloaded APK file
7. **Follow** the on-screen instructions

### Using USB Debugging

1. **Download** `Enhance-d_Club_Glucose_Karoo_v2.1.0.apk`
2. **Enable USB debugging** on your Karoo:
   - Settings → About → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"
3. **Connect** via USB to your computer
4. **Install**: `adb install Enhance-d_Club_Glucose_Karoo_v2.1.0.apk`

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

## 🔐 Enhanced API Token Features

### New in v2.1.0:

- **Visible by default** - tokens are shown clearly for easy verification
- **Show/Hide toggle** - optional privacy switch to hide tokens
- **Real-time display** - see actual token values in Settings
- **Better editing** - clear visibility during configuration

### Setup:

1. **Open Settings** → **Configure API Token**
2. **Enter your token** (visible by default)
3. **Use toggle** to hide if needed for privacy
4. **Save** and restart app

### Creating Nightscout Tokens:

#### **What are Tokens?**

Nightscout tokens are authentication "keys" that allow secure access to your glucose data when your site isn't publicly accessible.

#### **When Do You Need One?**

- **Need token**: Site requires authentication, not publicly accessible
- **No token needed**: Publicly accessible site or local xDrip+

#### **How to Create:**

Follow the [official Nightscout documentation](https://nightscout.github.io/nightscout/admin_tools/) to create tokens:

1. **Access Admin Tools** in your Nightscout website
2. **Add New Subject** with appropriate role (`readable` recommended)
3. **Copy the token** for use in this app

## ⚠️ Important

- **Medical Disclaimer**: This app is for informational purposes only
- **Permissions**: Grant necessary permissions when prompted
- **Updates**: Uninstall previous version before installing new one

## 📞 Need Help?

- Check the full [README.md](../README.md) for detailed instructions
- Report issues on [GitHub](https://github.com/henryaspden/Enhance_d_Club_Glucose_Karoo/issues)

---

**File Size**: 8.6 MB  
**Version**: 2.1.0  
**Release Date**: August 20, 2025  
**Requires**: Android 7.0+ (API 24)
