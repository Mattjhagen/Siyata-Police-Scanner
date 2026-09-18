# Siyata SD7 Public Radio Scanner

Voice-controlled public radio scanner application designed for the **Siyata SD7** rugged Android device with hardware rotary knob navigation. Listen to walkie-talkie channels, internet radio, weather broadcasts, and more!

## Features

- **Hardware Rotary Knob Navigation**
  - Counter-clockwise: Next feed
  - Clockwise: Previous feed  
  - Press: Play/Stop toggle

- **Voice Announcements**
  - Text-to-speech feed names when navigating
  - Status announcements (playing, stopped, errors)
  - Only speaks when app is in foreground

- **Live Audio Streaming**
  - HTTP/HTTPS audio stream support
  - Internet radio integration
  - NOAA Weather Radio support
  - WebSocket walkie-talkie (future)
  - Auto-reconnect on network changes

- **Dynamic Feed Configuration**
  - Edit feeds without rebuilding APK
  - File-based configuration at `/sdcard/Android/data/com.siyata.scanner/files/scanner_feeds.txt`
  - Simple pipe-separated format

- **Auto-Start on Boot**
  - Automatically launches when device boots
  - Ready to use immediately

## Feed Configuration

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

### TTS Speaking in Other Apps

- App only registers for rotary events when in foreground
- TTS only plays when MainActivity is active
- Use `onResume`/`onPause` lifecycle properly

## SDK Access Request

To enable full OLED display integration, SDK access from Siyata is required. See `SIYATA_SDK_REQUEST.md` for email template.

## Project Structure

```
app/src/main/
├── java/com/siyata/scanner/
│   ├── MainActivity.java      # Main activity, audio, TTS
│   ├── RotaryReceiver.java    # Hardware knob integration
│   ├── BootReceiver.java      # Auto-start on boot
│   └── RadioFeed.java         # Feed data model
├── res/
│   └── layout/
│       └── activity_main.xml  # UI layout
└── AndroidManifest.xml        # Permissions, receivers
```

## License

This project was developed for use on Siyata SD7 devices for emergency services and radio enthusiasts.

## Credits

Developed with assistance from Claude (Anthropic).

**Note**: This app requires Android device with physical rotary knob hardware. Tested on Siyata SD7.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
