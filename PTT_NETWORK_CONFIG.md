# PTT Walkie-Talkie Network Configuration

## Current Setup

The walkie-talkie server is running on **r510** and can be accessed via two different IP addresses depending on your network setup.

---

## Option 1: Tailscale VPN (Recommended - Works Anywhere)

**Server IP:** `100.103.3.35:8090`  
**Advantages:**
- Works from anywhere (home, cellular, other WiFi networks)
- Encrypted mesh VPN connection
- No port forwarding needed
- More secure

**Requirements:**
- Tailscale app installed on Siyata device ✅ (just installed)
- Device connected to Tailscale network

**Feed URLs:**
```
PTT Channel 1 | Team Radio | ptt://100.103.3.35:8090/1
PTT Channel 2 | Emergency Net | ptt://100.103.3.35:8090/2
```

**Setup Steps:**
1. Open Tailscale app on Siyata device
2. Log in with your Tailscale account
3. Enable VPN connection
4. Verify connection: `ping 100.103.3.35` should work
5. Update scanner_feeds.txt to use 100.103.3.35

---

## Option 2: Local Network (Current Setup)

**Server IP:** `192.168.0.169:8090`  
**Advantages:**
- No VPN needed
- Slightly lower latency
- Works immediately on home WiFi

**Limitations:**
- Only works when both devices are on same WiFi network
- Won't work on cellular or other WiFi networks

**Feed URLs (currently active):**
```
PTT Channel 1 | Team Radio | ptt://192.168.0.169:8090/1
PTT Channel 2 | Emergency Net | ptt://192.168.0.169:8090/2
```

---

## Switching Between Networks

### To Switch to Tailscale:

1. **Connect Tailscale on Siyata:**
   ```bash
   # Launch Tailscale app
   adb shell am start -n com.tailscale.ipn/.MainActivity
   ```

2. **Test connectivity:**
   ```bash
   adb shell ping -c 3 100.103.3.35
   ```

3. **Update feeds file:**
   Edit `scanner_feeds.txt` and replace `192.168.0.169` with `100.103.3.35`

4. **Push to device:**
   ```bash
   adb push scanner_feeds.txt /sdcard/Android/data/com.siyata.scanner/files/
   adb shell am force-stop com.siyata.scanner
   adb shell am start -n com.siyata.scanner/.MainActivity
   ```

### To Switch to Local Network:

Replace `100.103.3.35` with `192.168.0.169` in the above steps.

---

## Current Status

- ✅ Tailscale installed on Siyata device
- ✅ Server running on r510 (port 8090)
- ✅ Feeds currently using local IP: `192.168.0.169`
- ⏳ **Next:** Connect Tailscale on device, then switch feeds to Tailscale IP

---

## Troubleshooting

### Test server connectivity:
```bash
# From Mac:
nc -zv 100.103.3.35 8090  # Tailscale
nc -zv 192.168.0.169 8090 # Local

# From Siyata:
adb shell ping -c 3 100.103.3.35   # Tailscale
adb shell ping -c 3 192.168.0.169  # Local
```

### Check Tailscale status on device:
```bash
adb shell dumpsys package com.tailscale.ipn | grep -A 5 "state"
```

### View server logs:
```bash
ssh r510 "tail -50 ~/walkie-talkie/walkie-talkie.log"
```

### Check which IP is in feeds:
```bash
adb shell cat /sdcard/Android/data/com.siyata.scanner/files/scanner_feeds.txt | grep ptt://
```

---

## Network Topology

```
[Siyata Device]  --WiFi-->  [Router]  --Ethernet-->  [r510 Server]
   192.168.0.22                           192.168.0.169
        |                                        |
        +------ Tailscale VPN ------+-----------|
                100.103.3.x          100.103.3.35
```

With Tailscale: Both devices get a 100.103.3.x IP and can communicate through encrypted VPN tunnel, regardless of their physical network location.

Without Tailscale: Both devices must be on same 192.168.0.x network to communicate.
