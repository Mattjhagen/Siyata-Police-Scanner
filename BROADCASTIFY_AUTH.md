# Broadcastify Authentication Setup

The scanner app now supports automatic URL refresh for Broadcastify feeds. This eliminates the need to manually update expired stream URLs.

## Setup Your Broadcastify Credentials

### Option 1: Via ADB (Quick Setup)

```bash
# Replace YOUR_USERNAME and YOUR_PASSWORD with your Broadcastify credentials
adb shell am broadcast -a com.siyata.scanner.SET_CREDENTIALS \
  --es username "YOUR_USERNAME" \
  --es password "YOUR_PASSWORD"
```

### Option 2: Via App Settings (Future)

A settings screen will be added in a future update.

## How It Works

1. **Feed ID Format**: Use `broadcastify://FEED_ID` in your feeds.txt file
   - Example: `Omaha Police | Police Feed | broadcastify://39790`

2. **Automatic Refresh**: When you select a Broadcastify feed:
   - App logs in with your credentials
   - Fetches fresh stream URL with valid token
   - Starts playback automatically

3. **Token Expiry Handling**: If a stream fails with expired token:
   - App automatically refreshes the URL
   - Retries playback with new token

## Finding Feed IDs

1. Go to https://www.broadcastify.com/
2. Search for your desired feed (e.g., "Omaha Police")
3. Click on the feed to open its page
4. Look at the URL: `https://www.broadcastify.com/listen/feed/39790`
5. The number `39790` is the feed ID
6. Use `broadcastify://39790` in your feeds.txt

## Premium Account Required

Broadcastify Premium ($6.99/month) is required for:
- Ad-free streaming
- Instant playback (no 15-30 second ads)
- Access to most police/fire feeds

Free accounts work but have ads and delays.

## Privacy & Security

- Credentials are stored securely in app private storage
- Only accessible by the scanner app
- Not synced or transmitted anywhere except to Broadcastify for authentication
- Delete credentials: `adb shell rm /data/data/com.siyata.scanner/shared_prefs/broadcastify_auth.xml`
