# Siyata SD7 Bootloader Unlock & Root Guide

## Prerequisites ✅

- [x] Personal device (not work)
- [x] Don't care about warranty
- [x] No important data on device
- [x] Have backup SD7 device
- [x] Developer options enabled
- [x] OEM unlocking enabled

## ⚠️ WARNINGS

This process will:
- **WIPE ALL DATA** on the device
- **VOID WARRANTY** with Siyata
- Potentially **BRICK DEVICE** if done wrong
- Take 30-60 minutes

**BACKUP ANY DATA YOU NEED NOW!**

---

## Step 1: Enable OEM Unlocking

On device:
1. Settings → About phone
2. Tap "Build number" 7 times (enable Developer options)
3. Settings → System → Developer options
4. Enable **"OEM unlocking"** toggle
5. Accept the warning

---

## Step 2: Reboot to Bootloader

```bash
adb reboot bootloader
```

Device will reboot to **fastboot mode** (white screen with small text).

---

## Step 3: Check Fastboot Connection

```bash
fastboot devices
```

Should show: `182d9958    fastboot`

If not connected:
- Try different USB cable
- Try different USB port
- Install fastboot drivers (Mac should work automatically)

---

## Step 4: Unlock Bootloader

### Try Method 1 (Standard):

```bash
fastboot flashing unlock
```

**OR** if that fails:

### Try Method 2 (OEM):

```bash
fastboot oem unlock
```

### What Happens:

**On device screen**:
- Warning message about unlocking
- **Use volume buttons** to navigate
- **Press power button** to confirm

**Device will**:
1. Show unlock warning
2. Wipe all data (factory reset)
3. Reboot automatically
4. Take 5-10 minutes to boot first time

---

## Step 5: Verify Unlock

After device reboots and completes setup:

```bash
adb shell getprop ro.boot.verifiedbootstate
```

Should show: `orange` (unlocked)

**OR**:

```bash
adb shell getprop sys.oem_unlock_allowed
```

Should show: `1`

---

## Step 6: Install Magisk (Root)

### Download Magisk

```bash
cd ~/Siyata-Police-Scanner
curl -L -o magisk.apk https://github.com/topjohnwu/Magisk/releases/latest/download/Magisk-v27.0.apk
```

### Install Magisk Manager

```bash
adb install magisk.apk
```

### Get Boot Image

```bash
# Dump current boot partition
adb shell su -c "dd if=/dev/block/by-name/boot of=/sdcard/boot.img"

# Pull to computer
adb pull /sdcard/boot.img

# OR find boot.img from stock ROM
```

### Patch Boot Image

1. Open Magisk app on device
2. Tap "Install" → "Select and Patch a File"
3. Select `boot.img`
4. Magisk creates `magisk_patched.img` in Downloads

### Pull Patched Boot

```bash
adb pull /sdcard/Download/magisk_patched_*.img ~/magisk_patched.img
```

### Flash Patched Boot

```bash
adb reboot bootloader

fastboot flash boot ~/magisk_patched.img

fastboot reboot
```

### Verify Root

```bash
adb shell su -c "id"
```

Should show: `uid=0(root) gid=0(root)`

---

## Step 7: Install Scanner as System App

### Remount System as Writable

```bash
adb shell su -c "mount -o rw,remount /"
```

### Push Scanner APK to System

```bash
adb push app/build/outputs/apk/debug/app-debug.apk /sdcard/scanner.apk

adb shell su -c "cp /sdcard/scanner.apk /system/priv-app/Scanner.apk"
adb shell su -c "chmod 644 /system/priv-app/Scanner.apk"
adb shell su -c "chown root:root /system/priv-app/Scanner.apk"
```

### Reboot

```bash
adb reboot
```

Scanner now runs with **system privileges**!

---

## Step 8: Enable OLED Display Access

With system privileges, scanner can now:

### Access PocService

```java
// In MainActivity.java - now works with system access
Intent oledIntent = new Intent("com.siyata.poc.intent.action.DISPLAY");
oledIntent.setPackage("com.br.smallcd");
oledIntent.putExtra("line1", feedName);
oledIntent.putExtra("line2", status);
sendBroadcast(oledIntent);
```

### System-Level Broadcasts

```bash
# Test OLED update
adb shell am broadcast -a com.siyata.poc.intent.action.DISPLAY \
  --es line1 "Test Scanner" \
  --es line2 "System Access"
```

---

## Troubleshooting

### "fastboot: command not found"

**Mac**:
```bash
brew install android-platform-tools
```

### Device won't enter bootloader

Try:
```bash
adb reboot bootloader
```

OR physically:
1. Power off device
2. Hold Volume Down + Power
3. Release when fastboot screen appears

### "FAILED (remote: 'Not allowed in Lock State')"

OEM unlocking not enabled:
1. Boot to Android
2. Developer options → Enable "OEM unlocking"
3. Try again

### "FAILED (remote: 'Command not allowed')"

Device locked by carrier or OEM:
- Contact Siyata for unlock code
- Or device doesn't support unlocking

### Boot loop after unlocking

Normal first boot takes 10+ minutes:
- Wait 15 minutes
- If still boot looping, flash stock ROM

### Lost root after update

Re-patch boot image with Magisk:
1. Get new boot.img from update
2. Patch with Magisk
3. Flash patched boot

---

## Recovery (If Something Goes Wrong)

### Restore Stock ROM

1. Download Siyata SD7 stock firmware
2. Flash with SP Flash Tool (MediaTek devices)
3. Or use fastboot to flash stock images

### Re-lock Bootloader (Emergency)

**⚠️ WARNING**: Only do this with stock ROM installed!

```bash
fastboot flashing lock
```

Device will wipe again and re-lock.

---

## After Unlock Benefits

### What You Gain:

✅ **OLED Display Control** - Direct hardware access  
✅ **System-Level Broadcasts** - All intents work  
✅ **Root Access** - Full device control  
✅ **Custom Boot** - Scanner auto-start with priority  
✅ **Remove Bloat** - Delete Siyata apps you don't need  
✅ **Better Performance** - Disable unnecessary services  

### Scanner App Improvements:

- OLED shows feed name when rotating knob
- OLED shows playing status
- System priority (always runs)
- Can intercept all hardware events
- Access to proprietary Siyata APIs

---

## Files Created

- `magisk.apk` - Magisk Manager installer
- `boot.img` - Original boot image (BACKUP THIS!)
- `magisk_patched.img` - Patched boot with root
- `scanner.apk` - System app version

---

## Commands Reference

```bash
# Reboot to bootloader
adb reboot bootloader

# Check fastboot connection
fastboot devices

# Unlock bootloader
fastboot flashing unlock

# Flash boot image
fastboot flash boot magisk_patched.img

# Reboot
fastboot reboot

# Check root
adb shell su -c "id"

# Remount system
adb shell su -c "mount -o rw,remount /"
```

---

## Next Steps After Root

1. Install scanner as system app
2. Update scanner code for OLED access
3. Test hardware integrations
4. Set up boot automation
5. Optimize performance

🤖 Generated with [Claude Code](https://claude.com/claude-code)
