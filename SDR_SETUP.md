# Self-Hosted Scanner with Your FlightRadar24 ADS-B Receiver

## Good News!

Your **FlightRadar24 ADS-B receiver** uses an **RTL-SDR dongle** that can be repurposed for police/fire scanner frequencies!

### What You Already Have

FlightRadar24 receivers typically include:
- **RTL-SDR USB dongle** (RTL2832U chipset) - can tune 24-1766 MHz
- **1090 MHz antenna** (for aircraft ADS-B)
- **Raspberry Pi or PC** running feeder software
- **GPS module** (for timing)

### What You Need to Add

**Just an antenna!**

Police/Fire frequencies:
- **VHF**: 150-174 MHz (most common - police, fire, EMS)
- **UHF**: 450-470 MHz (some agencies)
- **800 MHz**: 806-824 MHz (trunked systems)

**Recommended Antenna**: Discone or wideband scanner antenna (~$40-80)
- Covers 25-1300 MHz
- One antenna for VHF, UHF, and keeps your ADS-B working
- Examples: Diamond D130J, Tram 1410

## Setup Options

### Option 1: Dual Purpose (Recommended)

Keep FlightRadar24 running AND add scanner capability:

1. **Add second RTL-SDR dongle** (~$35)
   - Keep one for ADS-B (1090 MHz)
   - Use second for scanner frequencies

2. **Add scanner antenna**
   - Discone antenna for VHF/UHF scanner
   - Keep existing 1090 MHz antenna for ADS-B

3. **Run both simultaneously**
   - FlightRadar24 feeder keeps running
   - Scanner streams local police/fire

### Option 2: Shared RTL-SDR (Time Division)

Use the SAME RTL-SDR hardware for both:

1. **Install scanner software alongside FR24 feeder**
2. **Time-share the dongle**:
   - FR24 runs most of the time
   - Periodically tune to scanner freqs
   - Record scanner audio when active

**Drawback**: Can't do both simultaneously

### Option 3: Scanner Only Mode (Not Recommended)

Stop FR24 and dedicate hardware to scanner only.

## Software Setup

### 1. Find Your Local Frequencies

Visit **RadioReference.com**:
- Search for your city/county
- Get frequency list for police/fire/EMS
- Note if they use trunked or conventional system

**Omaha Example**:
- Douglas County Sheriff: 155.475 MHz (VHF)
- Omaha Police: 800 MHz trunked system
- Omaha Fire: 154.190 MHz (VHF)

### 2. Install Scanner Software

**For Trunked Systems** (Omaha Police):
```bash
# Install trunk-recorder (best for P25/trunked)
sudo apt-get install cmake libboost-all-dev libusb-1.0-0-dev
git clone https://github.com/robotastic/trunk-recorder.git
cd trunk-recorder
./build.sh
```

**For Conventional Systems** (simple frequencies):
```bash
# Install rtl_fm (simpler, works for non-trunked)
sudo apt-get install rtl-sdr
```

### 3. Configure Frequencies

Create `config.json` for trunk-recorder:
```json
{
  "sources": [{
    "driver": "rtlsdr",
    "device": "0",
    "gain": 40
  }],
  "systems": [{
    "type": "p25",
    "systemName": "Douglas County",
    "talkgroupsFile": "talkgroups.csv",
    "frequencies": [
      851.0125, 852.4875, 853.9625
    ]
  }]
}
```

### 4. Stream Audio

**Option A: Icecast2 Server**
```bash
# Install Icecast
sudo apt-get install icecast2

# Configure stream
# trunk-recorder outputs to Icecast
# Your scanner app connects to http://YOUR_IP:8000/stream.mp3
```

**Option B: Simple HTTP Stream**
```bash
# Use ffmpeg to stream
rtl_fm -f 155.475M -s 48k | \
  ffmpeg -f s16le -ar 48000 -ac 1 -i - \
    -acodec libmp3lame -b:a 128k -f mp3 \
    http://YOUR_IP:8000/scanner.mp3
```

### 5. Add to Scanner App

Edit `scanner_feeds.txt`:
```
Local Police | Douglas County SO | http://YOUR_HOME_IP:8000/police.mp3
Local Fire | Omaha Fire Dept | http://YOUR_HOME_IP:8000/fire.mp3
```

## Hardware Shopping List

### Minimal ($40)

- **Discone Antenna**: $40-80
  - Diamond D130J (~$70)
  - Tram 1410 (~$55)
  - Generic discone (~$40)

**Total**: Just antenna! (You have everything else)

### Recommended ($75)

- **Discone Antenna**: $70
- **Second RTL-SDR dongle**: $35
- **Antenna splitter/combiner**: $15 (if needed)

**Total**: ~$120

### Full Setup ($150)

- **Discone Antenna**: $70
- **RTL-SDR V3**: $35
- **LNA (Low Noise Amplifier)**: $25
- **Antenna coax/connectors**: $20

**Total**: ~$150

## Network Access

### From SD7 to Home Network

Your SD7 needs to reach your home network:

**Option 1: Home WiFi**
- SD7 connects to home WiFi when in range
- Streams directly from local server

**Option 2: VPN**
- Set up VPN server at home (WireGuard/OpenVPN)
- SD7 connects via VPN from anywhere
- Access local streams remotely

**Option 3: Public Stream**
- Port forward Icecast through your router
- Use dynamic DNS (DuckDNS, No-IP)
- Access from anywhere: `http://your-domain.duckdns.org:8000/stream.mp3`

## Quick Start Guide

### 1. Check Your RTL-SDR

```bash
# See if it's detected
rtl_test

# Should show: "Found Rafael Micro R820T tuner"
```

### 2. Test Scanner Reception

```bash
# Listen to local police frequency (example: 155.475 MHz)
rtl_fm -f 155.475M -M fm -s 48k | aplay -r 48k -f S16_LE
```

If you hear audio - it works!

### 3. Set Up Permanent Stream

```bash
# Install Icecast
sudo apt-get install icecast2

# Configure in /etc/icecast2/icecast.xml
# Start streaming with trunk-recorder or rtl_fm
```

### 4. Test from SD7

```bash
# Test stream URL
curl -I http://YOUR_HOME_IP:8000/stream.mp3

# Should return: HTTP/1.1 200 OK
```

### 5. Add to Scanner App

Update `scanner_feeds.txt` with your local stream URLs.

## Frequency Resources

- **RadioReference.com** - Frequency database
- **Broadcastify.com** - See what others stream
- **SignalWiki** - Technical info on trunked systems
- **rtl-sdr.com** - Hardware and software guides

## Next Steps

1. **Identify antenna** - Check what you need for local frequencies
2. **Order antenna** - Discone recommended for wide coverage
3. **Test with rtl_fm** - Verify reception before setting up streaming
4. **Configure trunk-recorder** - For trunked systems
5. **Set up Icecast** - Stream to your SD7

## Questions?

Common issues:
- **No audio**: Check antenna, frequency accuracy, squelch level
- **Weak signal**: Add LNA, improve antenna placement
- **Choppy audio**: Check CPU usage, buffer sizes
- **Can't connect**: Check firewall, VPN configuration

---

**Your FlightRadar24 hardware is 90% of what you need for a scanner!** Just add an antenna and software.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
