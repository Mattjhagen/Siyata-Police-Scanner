# Siyata SD7 Public Radio Scanner

Voice-controlled public radio scanner application designed for the **Siyata SD7** rugged Android device with hardware rotary knob navigation. Listen to walkie-talkie channels, internet radio, weather broadcasts, and more!

## Features

- **Hardware Rotary Knob Navigation**
  - Counter-clockwise: Next feed
  - Clockwise: Previous feed  
  - Press: Play/Stop toggle
  - **Long-press (when stopped): Open Feed Manager**

- **Custom Feed Manager UI** ✨ NEW
  - Add custom feeds with name, description, and URL
  - Delete unwanted feeds
  - No ADB required - manage feeds directly on device
  - Changes take effect immediately

- **Voice Announcements**
  - Text-to-speech feed names when navigating
  - Status announcements (playing, stopped, errors)
  - **Audio ducking**: Lowers media volume during TTS for clarity
  - Only speaks when app is in foreground

- **Live Audio Streaming**
  - HTTP/HTTPS audio stream support
  - HLS (m3u8) streaming with ExoPlayer
  - Internet radio integration
  - Police scanner feeds (Broadcastify, Scanner Radio)
  - NOAA Weather Radio support
  - Auto-reconnect on network changes

- **Dynamic Feed Configuration**
  - Edit feeds via UI (long-press knob) or file
  - File-based configuration at `/sdcard/Android/data/com.siyata.scanner/files/scanner_feeds.txt`
  - Simple pipe-separated format
  - Auto-reload on app resume

- **Auto-Start on Boot**
  - Automatically launches when device boots
  - Ready to use immediately

- **Remote Access** ✨ NEW
  - VNC server support (droidVNC-NG) for unattended access
  - Web-based screen streaming (port 8080)
  - Control device from phone/computer without physical access

## Remote Access Setup

### droidVNC-NG (Recommended)

Provides unattended remote access without approval prompts:

1. **Install on Siyata:**
   ```bash
   adb install droidvnc-ng.apk
   ```

2. **Setup:**
   - Launch droidVNC-NG
   - Grant screen capture and accessibility permissions
   - Set a password
   - Enable "Start on Boot"
   - Tap "Start"

3. **Connect from phone/computer:**
   - Install VNC Viewer app
   - Connect to `192.168.0.26:5900` (use your Siyata's IP)
   - Enter password
   - Full remote control, no prompts!

### Web Screen Streaming

For view-only access from browser:

```bash
# Push screen server script
adb push screen-server.sh /data/local/tmp/
adb shell chmod +x /data/local/tmp/screen-server.sh

# Start server
adb shell "nohup sh /data/local/tmp/screen-server.sh > /dev/null 2>&1 &"

# Access from browser
open http://192.168.0.26:8080
```

### Tested Companion Apps

- **Scanner Radio** ✅ - Browse police/fire feeds, works great
- **VNC Viewer** ✅ - Remote control (with droidVNC-NG server)
- **TeamViewer QuickSupport** ⚠️ - Works but requires approval each time
- **APKPure** ✅ - Alternative app store (no Google Play Services needed)
- **VLC** ✅ - Backup media player for testing streams

## Feed Configuration

### Using the Feed Manager UI (Easiest)

1. **Stop playback** (press rotary knob if playing)
2. **Long-press rotary knob** to open Feed Manager
3. Tap **"+ Add New Feed"**
4. Enter:
   - Feed Name (e.g., "Omaha Police")
   - Description (e.g., "Police Scanner")
   - Stream URL
5. Tap **"Add"**

To delete: Open Feed Manager, tap "Delete" next to any feed.

### Manual Configuration

Create or edit `/sdcard/Android/data/com.siyata.scanner/files/scanner_feeds.txt`:

```
# Public Radio Scanner Feed Configuration
# Format: Name | Description | Stream URL

Test Music | SomaFM Groove | http://ice1.somafm.com/groovesalad-128-mp3
NOAA Weather | Omaha Weather Radio | http://radio.weatherusa.net/NWR/KIH61.mp3
BBC World | BBC World Service | http://stream.live.vc.bbcmedia.co.uk/bbc_world_service
NASA Audio | NASA TV Audio | https://ntd1.akamaized.net/hls/live/2013923/NASA-NTV1-HLS/master.m3u8
```

### Updating Feeds

```bash
# Push new feed file
adb push scanner_feeds.txt /sdcard/Android/data/com.siyata.scanner/files/

# Restart app
adb shell am force-stop com.siyata.scanner
adb shell am start -n com.siyata.scanner/.MainActivity
```

## Building

### Prerequisites

- Java JDK 17
- Android SDK (API level 33)
- Gradle 8.x

### Build Commands

```bash
# Set Java home
export JAVA_HOME=$(/usr/libexec/java_home)

# Build debug APK
gradle assembleDebug

# Install to device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Hardware Integration

### Siyata SD7 Rotary Knob

The app registers a BroadcastReceiver for rotary knob events:

- **Broadcast Action**: `com.br.intent.action.ROTARY_KNOB`
- **KeyCodes**:
  - `KEYCODE_F4` - Counter-clockwise rotation
  - `KEYCODE_F5` - Clockwise rotation
  - `KEYCODE_F8` - Knob press

### OLED Display (Experimental)

Attempted integration with Siyata's small LCD via PocService broadcasts. Currently requires system-level SDK access.

## Audio Sources

### Broadcastify

- **Free**: Requires 15-30 second ads before each stream
- **Premium** ($6.99/month): Ad-free, instant switching
- **Authentication**: Some feeds require logged-in session tokens

Find feed IDs at: https://www.broadcastify.com/

Stream URL format: `https://broadcastify.cdnstream1.com/[FEED_ID]`

### NOAA Weather Radio

Stream URL format: `http://radio.weatherusa.net/NWR/[STATION].mp3`

Example stations:
- `KIH61` - Omaha, NE
- `KEC83` - Lincoln, NE

### Self-Hosted SDR (Future)

Potential setup with RTL-SDR USB dongle:
- Hardware: RTL-SDR V3 (~$35) + antenna
- Software: `trunk-recorder` or `rtl_fm`
- Stream server: Icecast2
- Provides local police/fire feeds without subscription

## Troubleshooting

### No Audio

- Scanner feeds are often silent when no radio traffic
- Try test music feed first to verify streaming works
- Check device volume
- Wait 30 seconds for Broadcastify ad to finish (free accounts)

### Feed Errors

- Broadcastify URLs may require authentication
- Feed IDs change periodically - verify on broadcastify.com
- Some feeds require Premium subscription
- Check internet connectivity

### TTS Not Audible During Playback

- **Fixed in v2.1+**: Audio ducking lowers media volume to 10% during TTS
- TTS uses STREAM_MUSIC for better compatibility
- Force speaker output enabled

### TTS Speaking in Other Apps

- App only registers for rotary events when in foreground
- TTS only plays when MainActivity is active
- Use `onResume`/`onPause` lifecycle properly

### Feed Manager Not Opening

- Make sure playback is **stopped** first (press knob to stop)
- **Long-press** the rotary knob (F8), not short press
- PTT button must not be connected

### Remote Access Issues

- **VNC connection refused**: Check droidVNC-NG is running and started
- **Web stream not loading**: Restart screen-server.sh script
- **TeamViewer needs approval**: Use droidVNC-NG instead for unattended access
- **Wrong IP**: Use `adb shell ip addr show wlan0` to find current IP

### Google Play Services Errors

- Siyata SD7 is not Google-certified
- Google Play Store will crash on launch
- **Solution**: Use APKPure or F-Droid for app downloads
- Scanner Radio works fine without Play Services

## SDK Access Request

To enable full OLED display integration, SDK access from Siyata is required. See `SIYATA_SDK_REQUEST.md` for email template.

## Project Structure

```
app/src/main/
├── java/com/siyata/scanner/
│   ├── MainActivity.java              # Main activity, audio, TTS, audio ducking
│   ├── FeedManagerActivity.java       # Feed management UI (add/delete)
│   ├── RotaryReceiver.java            # Hardware knob integration
│   ├── BootReceiver.java              # Auto-start on boot
│   ├── CredentialsReceiver.java       # Broadcast-based credential setting
│   ├── BroadcastifyAuthManager.java   # Broadcastify auth (experimental)
│   └── RadioFeed.java                 # Feed data model
├── assets/
│   └── feeds.txt                      # Default feed configuration
├── res/
│   └── layout/
│       └── activity_main.xml          # UI layout
└── AndroidManifest.xml                # Permissions, receivers, activities
screen-server.sh                        # Web-based screen streaming server
BROADCASTIFY_AUTH.md                    # Broadcastify authentication guide
```

## Tested Configuration

### Device
- **Model**: Siyata SD7 (toronto_sd7)
- **OS**: Android 12 (SDK 31)
- **Architecture**: arm64-v8a
- **Security Patch**: February 2022

### Working Scanner Apps
1. **Custom Scanner** (this app) - Best for rotary knob control
2. **Scanner Radio** v8.30 - Great for browsing available feeds
3. **Test Music** (SomaFM) - Useful for testing streaming

### Remote Access Tested
- ✅ droidVNC-NG v2.3.1 + VNC Viewer (unattended, recommended)
- ✅ Web screen streaming via screen-server.sh (view-only)
- ✅ scrcpy (from computer, full control)
- ⚠️ TeamViewer QuickSupport (requires approval each time)
- ✅ AnyDesk (installed, not fully tested)

### Network Setup
- ADB over network: `adb connect 192.168.0.26:5555`
- VNC server: `192.168.0.26:5900`
- Web stream: `http://192.168.0.26:8080`

## Known Issues

- Google Play Services crashes on Siyata (device not certified)
- Broadcastify HLS URLs expire and require manual refresh
- Some feeds require premium subscriptions
- TTS may be quiet on first launch (audio ducking initializing)

## Future Enhancements

- [ ] Auto-refresh Broadcastify URLs
- [ ] Widget for home screen control
- [ ] Recording/playback of scanner audio
- [ ] OLED display integration (requires Siyata SDK)
- [ ] Multi-channel monitoring
- [ ] Alert/notification system for specific keywords

## License

This project was developed for use on Siyata SD7 devices for emergency services and radio enthusiasts.

## Credits

Developed with assistance from Claude (Anthropic).

**Note**: This app requires Android device with physical rotary knob hardware. Tested on Siyata SD7.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
