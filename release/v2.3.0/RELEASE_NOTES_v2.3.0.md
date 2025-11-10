# Enhance-d Club Glucose - Karoo v2.3.0

## 🎉 Release Highlights

**Critical fix for glucose widget data field display issues**

This release fixes a critical bug where the glucose mg/dL and mmol/L data fields would incorrectly show "no sensor" even when valid glucose data was available in the app.

![Enhance-d Club Glucose - Karoo](app/src/main/res/drawable/ic_enhanced_club_glucose.png)

## 📋 What's New

### 🐛 Critical Bug Fixes

- **Fixed "No Sensor" Display Issue**: Glucose mg/dL and mmol/L data fields now correctly check for stale data before displaying values
- **Improved Data State Management**: Data fields now properly emit `StreamState.NotAvailable` when data is stale (older than 10 minutes) and `StreamState.Streaming` when data is fresh
- **Consistent Behavior**: All glucose data types now use consistent stale data checking logic

### 🔧 Improvements

- **Better Widget Reliability**: Widgets now accurately reflect the actual sensor state
- **Enhanced Data Validation**: Improved handling of stale glucose readings
- **Consistent State Management**: All data types now follow the same stale data validation pattern

## 📊 Technical Details

### What Was Fixed

The `GlucoseDataType` and `GlucoseMmolDataType` classes were missing stale data checks before emitting `StreamState.Streaming`. This caused Karoo to display "no sensor" even when fresh data was available, because:

1. The extension was emitting `StreamState.Streaming` without validating data freshness
2. Karoo expects `StreamState.NotAvailable` when data is stale (older than 10 minutes)
3. Without proper state management, the widget couldn't determine the correct display state

### How It Works Now

- **Fresh Data (< 10 minutes old)**: Widget displays the glucose value correctly
- **Stale Data (> 10 minutes old)**: Widget correctly shows "no sensor" or "not available"
- **No Data**: Widget shows "no sensor" appropriately

## 📱 Supported Devices

- **Hammerhead Karoo**
- **Hammerhead Karoo 2**
- **Hammerhead Karoo 3**

## 🔗 Nightscout API Sources

The app supports multiple Nightscout data sources:

### 1. **Nightscout Server** (Cloud-based)

- Standard cloud-hosted Nightscout instances
- Requires internet connection
- Optional API token authentication for secure access
- Example: `https://mybloodglucose.herokuapp.com/api/v1/entries/sgv.json`

### 2. **Phone Local Broadcast via Hotspot**

- xDrip+ or similar apps broadcasting glucose data
- Auto-detects gateway IP when connected to phone hotspot
- Example: `http://192.168.1.1:17580/sgv.json`

### 3. **Local Karoo Device** (xDrip+ Local)

- xDrip+ running locally on the Karoo device
- No internet required, direct local access
- Example: `http://127.0.0.1:17580/sgv.json`

## 📊 Available Data Fields

- **Glu. mg** - Blood glucose in mg/dL (Fixed in this release)
- **Glu. mmol** - Blood glucose in mmol/L (Fixed in this release)
- **Time Since (s)** - Seconds since last reading
- **Time Since** - Formatted time (h:m:s) since last reading
- **Direction** - Glucose trend with arrow indicators
- **15m Δ mg** - 15-minute glucose change in mg/dL
- **15m Δ mmol** - 15-minute glucose change in mmol/L
- **5m Δ mg** - 5-minute glucose change in mg/dL
- **5m Δ mmol** - 5-minute glucose change in mmol/L

## 🛠️ Installation

### Method 1: Hammerhead App Installation (Recommended)

1. Download the APK from this release
2. Install the Hammerhead app on your phone
3. Connect your Karoo device to the same WiFi network as your phone
4. Open the Hammerhead app and sign in
5. Navigate to Device Settings → Install APK
6. Upload the downloaded APK file
7. Follow the on-screen instructions

### Method 2: USB Debugging Installation

1. Download the APK from this release
2. Enable USB debugging on your Karoo device
3. Connect via USB to your computer
4. Install using: `adb install Enhance-d_Club_Glucose_Karoo_v2.3.0.apk`

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

### Configuration

The app automatically connects to the official GitHub repository to check for updates. No additional configuration required.

## ⚙️ Configuration

### API Token Setup (Enhanced)

If your Nightscout server requires authentication:

1. **Open Settings** in the app
2. **Tap "Configure API Token"**
3. **Enter your API token** (visible by default for easy verification)
4. **Use the toggle** to hide the token if needed for privacy
5. **Save the configuration**
6. **Restart the app** to apply changes

The API token supports both:

- **api-secret header**: Traditional Nightscout authentication
- **Authorization header**: Bearer token authentication

### Enhanced Token Features

- **Default Visibility**: Tokens are shown by default for easier verification
- **Privacy Toggle**: Optional switch to hide tokens when needed
- **Real-time Display**: See actual token values in Settings
- **Secure Storage**: Tokens stored securely in app preferences

### Creating Nightscout Tokens

#### **What are Nightscout Tokens?**

Nightscout tokens are authentication credentials that allow secure access to your Nightscout site. They act like a "key" that grants specific permissions to access your glucose data.

#### **When Do You Need a Token?**

**You need a token if:**

- Your Nightscout site is **not publicly accessible** (most common)
- Your site requires authentication to view data
- You want to restrict access to specific users
- Your site uses `AUTH_DEFAULT_ROLES` set to `denied`

**You don't need a token if:**

- Your Nightscout site is publicly accessible
- Your site allows anonymous read access
- You're using local xDrip+ without authentication

#### **Creating Tokens:**

To create authentication tokens for your Nightscout site, follow the official Nightscout documentation:

1. **Access Admin Tools**: Click the settings panel (three horizontal bars) in your Nightscout website and select "Admin Tools"
2. **Create Token**: Click "Add New Subject" and choose the appropriate role:
   - **`readable`**: Read-only access (recommended for this app)
   - **`careportal`**: Can view and make treatment entries
   - **`admin`**: Full access
3. **Copy Token**: Use the generated token string for authentication

_Note: If you change your API_SECRET, all tokens will change and need to be updated._

## ⚠️ Important Notes

### Medical Disclaimer

This app is for **informational purposes only**. It is NOT intended for:

- Medical diagnosis or treatment
- Emergency medical situations
- Life-critical diabetes management decisions

**Always consult qualified healthcare professionals for medical decisions.**

### System Requirements

- Android 7.0+ (API level 24)
- Active data connection (WiFi or cellular) for updates
- Nightscout server with glucose data

### Data Usage

- Estimated usage: ~1MB per hour of ride time
- Update checks: Minimal data usage (~10KB per check)
- Data charges may apply depending on your cellular plan
- WiFi recommended when available

## 🔄 Updating from Previous Versions

1. **Automatic**: The app will prompt you to update when opened
2. **Manual**: Use "Check for Updates" in Settings
3. **Manual Installation**: Download and install APK manually
4. **Your settings will be preserved** during updates

## 🐛 Known Issues

- Some deprecation warnings in the code (non-functional)
- Requires "Install from Unknown Sources" to be enabled for first-time installation
- Update checks require internet connection

## 📞 Support

- **GitHub Issues**: [Report bugs and request features](https://github.com/haspden/Enhance_d_Club_Glucose_Karoo/issues)
- **Email**: uk@club.enhance-d.com
- **Documentation**: Check the README.md file for detailed setup instructions

## 🙏 Acknowledgments

- **Enhance-d Club UK** - Community support and inspiration for active living with type 1 diabetes
- **CGM in the Cloud** - Community-driven diabetes technology
- **xDrip+** - Open source diabetes management
- **Hammerhead** - Karoo platform and extension framework
- **Nightscout** - Open source CGM data platform

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

**Version**: 2.3.0  
**Release Date**: January 2025  
**File**: `Enhance-d_Club_Glucose_Karoo_v2.3.0.apk`  
**Size**: See file details below

---

_Brought to you by Enhance-d Club_

