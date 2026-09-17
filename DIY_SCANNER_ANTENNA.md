# DIY Scanner Antenna - FREE to $20

## You Already Have the SDR!

Your FlightRadar24 RTL-SDR dongle **CAN receive police frequencies**. You just need an antenna tuned to the right frequency.

### The Problem

- **ADS-B antenna**: Tuned for 1090 MHz (aircraft transponders)
- **Police/Fire**: 150-174 MHz (VHF band)
- Your 1090 MHz antenna won't pick up 150 MHz signals efficiently

### The Solution

Build a simple VHF antenna for **FREE** or buy one for $20-40.

---

## Option 1: FREE Dipole Antenna (30 minutes)

### What You Need

- **Wire or coat hanger** (any conductive metal)
- **Coax cable** (if you have spare, or use existing)
- **Tape or zip ties**
- **PVC pipe or wooden dowel** (optional support)

### Dipole Formula

For police/fire VHF (155 MHz typical):

**Each leg length** = 300 ÷ frequency ÷ 4

For **155 MHz**:
- 300 ÷ 155 ÷ 4 = **0.484 meters = 19 inches each leg**
- **Total antenna**: 38 inches

### Build Instructions

1. **Cut two pieces of wire**: 19 inches each
2. **Strip coax end**: Expose center conductor and shield
3. **Connect**:
   - Center conductor → One wire leg
   - Shield → Other wire leg
4. **Mount**: Tape to PVC pipe or window frame in a "T" shape
5. **Position**: Vertical works best, high as possible

```
     Wire leg 1 (19")
          |
    ------+------ Coax connection point
          |
     Wire leg 2 (19")
```

### Multi-Frequency Dipole

Make it work for multiple bands:

- **VHF Police (155 MHz)**: 19" legs
- **VHF Fire (154 MHz)**: 19" legs (same)
- **UHF (460 MHz)**: 6.5" legs

Build two separate dipoles or one compromised at 18-20" that works "okay" for both.

---

## Option 2: Rabbit Ears TV Antenna ($10-20)

**Cheapest commercial solution**:

1. Buy old-style "rabbit ears" TV antenna ($10 at thrift store)
2. Works for VHF band (54-216 MHz)
3. Covers police/fire perfectly
4. Adjustable length

### Hack It

- Remove the coax from rabbit ears
- Adapt for SMA connector on your RTL-SDR
- Extend the "ears" to ~19" each

---

## Option 3: Buy Scanner Antenna ($20-40)

If you want something better:

**Budget Options**:
- **Nagoya NA-771**: $15-20 (handheld scanner antenna)
- **Tram 1089**: $25 (VHF/UHF base antenna)
- **Scanner stick antenna**: $20-30

**Best Value**:
- **Diamond RH77CA**: $30 (VHF/UHF flexible)
- **Comet SMA-24**: $35 (wideband)

---

## Option 4: Ground Plane Antenna (FREE, better performance)

### What You Need

- **Coat hanger or wire**
- **4 radials**: 19" each (ground plane)
- **1 vertical**: 19" (driven element)

### Build

```
        Vertical element (19")
               |
               |
    -----------+----------- Ground plane (4x 19" wires at 45° down)
          /    |    \
         /     |     \
        /      |      \
```

1. **Cut 5 wires**: All 19" long
2. **Mount center vertical**: Points straight up
3. **Mount 4 radials**: Angled 45° downward around base
4. **Connect coax**:
   - Center conductor → Vertical wire
   - Shield → All 4 radials

This antenna **outperforms most commercial $50 antennas**!

---

## Testing Your Antenna

### Step 1: Connect RTL-SDR

```bash
# On your Pi/PC with FR24 feeder
rtl_test
```

Should show: `Found Rafael Micro R820T tuner`

### Step 2: Find Local Police Frequency

Go to **RadioReference.com**:
- Search your city/county
- Find VHF frequencies (usually 150-174 MHz)

**Omaha Examples**:
- Douglas County Sheriff: 155.475 MHz
- Omaha Fire: 154.190 MHz

### Step 3: Test Reception

```bash
# Install if needed
sudo apt-get install rtl-sdr

# Listen to 155.475 MHz
rtl_fm -f 155.475M -M fm -s 48k | aplay -r 48k -f S16_LE
```

**If you hear scratchy voices = SUCCESS!**

### Step 4: Improve Signal

If signal is weak:
- **Move antenna higher** (attic, roof, window)
- **Move away from electronics** (computers, power lines)
- **Point vertically** (VHF radio waves are vertical polarization)
- **Add ground plane** (use Option 4)

---

## Complete Setup Cost Breakdown

### $0 Option (DIY Everything)

- ✅ RTL-SDR dongle: **You have it**
- ✅ Wire/coat hangers: **Free**
- ✅ Coax: **Use existing or salvage**
- ✅ Raspberry Pi: **Already running FR24**
- ✅ Software: **Free (rtl_fm, trunk-recorder)**

**Total: $0**

### $20 Budget Option

- ✅ RTL-SDR dongle: **You have it**
- 🛒 Nagoya NA-771 antenna: **$15**
- 🛒 SMA adapter if needed: **$5**

**Total: $20**

---

## Antenna Placement Tips

### Best Locations

1. **Window sill**: Simple, good line of sight
2. **Attic**: Hidden, decent height
3. **Roof**: Best performance (if you can mount safely)
4. **Outside wall**: Better than inside

### Worst Locations

- Basement (blocked by earth)
- Inside metal building (Faraday cage)
- Near power lines (interference)
- Near WiFi router (interference)

### Quick Test

Move the antenna around while running `rtl_fm`:
- **Clear audio**: Good spot
- **Scratchy**: Marginal
- **Silent**: Bad location or wrong frequency

---

## Software Setup (Free)

Once antenna is working:

### Option A: Simple Single Frequency

```bash
# Stream one frequency to Icecast
rtl_fm -f 155.475M -M fm -s 48k | \
  ffmpeg -f s16le -ar 48k -ac 1 -i - \
    -acodec libmp3lame -b:a 128k -f mp3 \
    icecast://source:password@localhost:8000/police.mp3
```

### Option B: Trunk Recorder (Multiple Channels)

For trunked systems (800 MHz systems):

```bash
git clone https://github.com/robotastic/trunk-recorder.git
cd trunk-recorder
./build.sh
```

Configure for your local system (get details from RadioReference).

---

## Quick Start TODAY

### 1. Build 19" Dipole (10 minutes)

- Cut two 19" wires
- Connect to coax
- Tape to window frame

### 2. Test Reception (5 minutes)

```bash
rtl_fm -f 155.475M -M fm -s 48k | aplay -r 48k -f S16_LE
```

### 3. If It Works...

Set up Icecast streaming (30 minutes):

```bash
sudo apt-get install icecast2
# Edit /etc/icecast2/icecast.xml
# Start streaming
```

### 4. Update Scanner App

```
Local Police | Douglas County | http://YOUR_PI_IP:8000/police.mp3
```

**Total time**: ~1 hour  
**Total cost**: $0

---

## Need Help?

Common issues:

**"No audio at all"**
- Wrong frequency? Check RadioReference.com
- Antenna too short/long? Recalculate for exact frequency
- Scanner system off? Try different frequency

**"Weak scratchy audio"**
- Move antenna higher
- Build ground plane version
- Check for interference sources

**"Digital garbled noise"**
- System is digital (P25/DMR)
- Need trunk-recorder software
- Or find analog frequency

---

## Bottom Line

**You can do this for FREE using what you have!**

1. ✅ RTL-SDR: You have it
2. ✅ Pi/Computer: Already running FR24
3. ✅ Antenna: Build from wire (free)
4. ✅ Software: Free and open source

The only "cost" is an hour of your time to build and test.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
