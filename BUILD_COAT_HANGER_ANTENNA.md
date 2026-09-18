# Build a Police Scanner Antenna from Coat Hangers

## What You Need (All Free)

- **2 metal coat hangers** (wire type, not plastic)
- **Wire cutters** or pliers
- **Tape** (electrical tape, duct tape, or zip ties)
- **Coax cable** from your RTL-SDR (the one connected to 1090 MHz antenna)
- **Something to mount on**: PVC pipe, wooden dowel, cardboard, or just a wall

Optional:
- **Soldering iron** (tape works fine if you don't have one)
- **SMA to coax adapter** (if your RTL-SDR has SMA connector)

**Build time**: 10-15 minutes

---

## Step 1: Calculate Your Antenna Length

### Find Your Local Police Frequency

Go to **RadioReference.com** → Search your city

**Common ranges**:
- Police/Fire: 150-174 MHz
- Example: Douglas County Sheriff = 155.475 MHz

### Calculate Wire Length

**Formula**: 300 ÷ frequency (MHz) ÷ 4 = length in meters

**For 155 MHz**:
- 300 ÷ 155 ÷ 4 = 0.484 meters
- **0.484 × 39.37 = 19 inches per leg**

**Quick reference**:
- **150 MHz**: 19.7 inches (50 cm) each leg
- **155 MHz**: 19.0 inches (48 cm) each leg
- **160 MHz**: 18.5 inches (47 cm) each leg
- **170 MHz**: 17.4 inches (44 cm) each leg

**Use 19 inches as good "middle" for 150-160 MHz**

---

## Step 2: Straighten Coat Hangers

```
Before:                After:
   ___                 _______________
  /   \                
 |     |    ══════>    Long straight wire
  \___/
   |
```

1. **Untwist the hook** at the top
2. **Straighten the wire** as much as possible
3. Don't worry if slightly bent - still works

---

## Step 3: Cut Two 19-Inch Pieces

```
Coat hanger 1:  |-------- 19 inches --------|-------- scrap --------|

Coat hanger 2:  |-------- 19 inches --------|-------- scrap --------|
```

**Measure and cut**:
- Mark 19 inches from end
- Cut with wire cutters
- You need **2 pieces total**, 19 inches each

**Tip**: Cut a bit longer (20") - you can trim later, can't add back!

---

## Step 4: Prepare Your Coax Cable

### Find the Coax

Your RTL-SDR has a coax cable connected to the 1090 MHz antenna. You need to:

**Option A**: Disconnect that antenna temporarily and use this cable  
**Option B**: Get a spare coax cable if you have one

### Strip the Coax End

```
Before stripping:
|===================|
     Coax cable

After stripping (strip back 1 inch):
                    Center conductor (copper wire)
                         |
|================|-------+-------|
                  Shield (braid or foil)
```

**Steps**:
1. **Cut outer jacket**: Strip back 1 inch
2. **Fold back shield**: Push the metal braid/foil back
3. **Strip center insulation**: Expose 1/2 inch of center wire

You should have:
- **Center conductor**: Bare wire sticking out
- **Shield/braid**: Folded back around cable

---

## Step 5: Connect Wires to Coax

### The Connection

```
     Coat hanger wire #1 (19")
              |
              |
     -------(   )------- Coax stripped end
              |
              |
     Coat hanger wire #2 (19")


Close-up of connection:

   Wire #1 ─────┐
                 │
   Center ──────┤ 
   conductor    │  ← Twist/tape/solder together
                │
   Shield ──────┤
   (braid)      │
                │
   Wire #2 ─────┘
```

### How to Connect

**Method 1: Tape (No Soldering)**

1. **Wire #1**: Wrap tightly around center conductor
2. **Wire #2**: Wrap tightly around shield/braid
3. **Tape it**: Wrap entire connection with electrical tape

**Method 2: Solder (Better but optional)**

1. **Tin the wires**: Put solder on coat hanger ends
2. **Wire #1**: Solder to center conductor
3. **Wire #2**: Solder to shield
4. **Tape it**: Wrap with electrical tape for strength

---

## Step 6: Mount the Antenna

### Simple Wall Mount

```
          Window/Wall
              ||
              || ← Tape here
              ||
     Wire #1  ||
              ||
              || 
              ||
    ─────────●●─────────  ← Connection point (coax)
              ||
              ||
     Wire #2  ||
              ||
              || ← Tape here
              ||
```

**Steps**:
1. **Tape wire #1** to wall going UP (19 inches up)
2. **Tape connection point** to wall
3. **Tape wire #2** to wall going DOWN (19 inches down)
4. **Keep straight** and vertical as possible

### PVC Pipe Mount (Better)

```
     1/2" PVC pipe or wooden dowel
              |
     Wire #1  |
              |
              | ← Zip tie or tape wires to pipe
              |
    ─────────●─────────  ← Connection point
              |
              | ← Coax hangs down or runs along pipe
              |
     Wire #2  |
              |
```

**Steps**:
1. **Get PVC pipe**: 1/2" diameter, 4 feet long ($2 at hardware store, or free scrap)
2. **Tape wire #1**: UP along pipe (19 inches)
3. **Tape connection**: Middle of pipe
4. **Tape wire #2**: DOWN along pipe (19 inches)
5. **Mount pipe**: Stand vertically near window or outside

---

## Step 7: Position for Best Reception

### Height

- **Higher is better**: Attic > window > desk
- **Minimum**: 5 feet off ground
- **Ideal**: 10-20 feet (roof, attic)

### Orientation

- **Vertical**: Keep antenna straight up and down (not horizontal)
- **Away from metal**: At least 2 feet from metal objects
- **Near window**: Glass is transparent to radio waves

### Good Locations

✅ Window sill (vertical against glass)  
✅ Attic (high, no obstructions)  
✅ Balcony rail (outside is best)  
✅ Roof mount (best performance)

### Bad Locations

❌ Basement (earth blocks signals)  
❌ Behind TV/computer (interference)  
❌ Inside metal shed (Faraday cage)  
❌ Lying flat (wrong polarization)

---

## Step 8: Test It!

### Connect to RTL-SDR

1. **Disconnect** your 1090 MHz antenna (temporarily)
2. **Connect** your new coat hanger antenna
3. **Keep ADS-B running?** You'll need to stop FR24 feeder to use RTL-SDR for scanner

### Test Reception

On your Raspberry Pi or computer:

```bash
# Install tools if needed
sudo apt-get install rtl-sdr

# Find local police frequency on RadioReference.com
# Example: 155.475 MHz for Douglas County

# Test reception
rtl_fm -f 155.475M -M fm -s 48k | aplay -r 48k -f S16_LE
```

### What You Should Hear

- **SUCCESS**: Scratchy voices, static, radio chatter
- **WEAK**: Faint voices, lots of static → Move antenna higher
- **NOTHING**: Wrong frequency, bad connection, or system is digital

### Troubleshooting

**"No sound at all"**
- Check connections (wire to coax)
- Try different frequency
- Move antenna to window
- System might be digital (P25) - needs special software

**"Weak/scratchy audio"**
- Move antenna higher
- Point straight up (vertical)
- Move away from electronics
- Build ground plane version (see below)

**"Digital garbled noise"**
- System is digital (P25/DMR)
- Need trunk-recorder software
- Or find analog frequency

---

## Upgrade: Ground Plane Antenna (Better Performance)

If basic dipole works but is weak, upgrade to ground plane:

### What You Need

- **4 more coat hangers** (for radials)
- **Cardboard or wood disk** (6-8 inch diameter)

### Build It

```
Top view:                Side view:

       |                      | Vertical (19")
   ----+----                  |
  /    |    \              ---+--- Ground plane
 /     |     \            /   |   \
                         /    |    \
                        19" at 45° angle
```

**Steps**:

1. **Cut 4 more wires**: 19 inches each
2. **Make base disk**: Poke 5 holes in cardboard (1 center, 4 around edge)
3. **Center wire**: Vertical (19" straight up) = connects to center conductor
4. **4 radials**: Angled down 45° = all connect to shield
5. **Mount high**: Window, pole, roof

**Performance**: This beats most $50 commercial antennas!

---

## Complete Setup Photos (Text Diagram)

### Basic Dipole

```
                    Window
                      ||
         Wire #1      ||
         (19")        ||
                      ||
                      ||
                      ||
    Coax ═══════════ ●● ← Connection point (taped to wall)
    to RTL-SDR        ||
                      ||
         Wire #2      ||
         (19")        ||
                      ||
                      ||
```

### Ground Plane on PVC

```
                    Outside/Window
                         |
         Vertical wire   | (19")
         (center)        |
                         |
    ──────────────────── ● ──────────────────
                    /    |    \
                   /     |     \
                  /      |      \
              19" radials at 45°
              (4 wires, shield connection)
                         |
                    Coax to RTL-SDR
```

---

## Cost Breakdown

- Coat hangers: **FREE** (you have them)
- Tape: **FREE** (you have it)
- PVC pipe: **FREE** (optional, scrap) or $2
- Coax: **FREE** (use existing)
- RTL-SDR: **FREE** (you have it)

**Total: $0-2**

---

## Next Steps After Building

Once antenna is working:

### 1. Set Up Streaming

Install Icecast to stream audio to your SD7:

```bash
sudo apt-get install icecast2 ffmpeg

# Stream police frequency
rtl_fm -f 155.475M -M fm -s 48k | \
  ffmpeg -f s16le -ar 48k -ac 1 -i - \
    -acodec libmp3lame -b:a 128k -f mp3 \
    icecast://source:hackme@localhost:8000/police.mp3
```

### 2. Update Scanner App

Edit `scanner_feeds.txt`:

```
Local Police | Douglas County SO | http://YOUR_PI_IP:8000/police.mp3
Local Fire | Omaha Fire | http://YOUR_PI_IP:8000/fire.mp3
```

### 3. Multiple Frequencies

For trunked systems or multiple channels, use **trunk-recorder** (see SDR_SETUP.md).

---

## Quick Build Summary

1. **Cut**: 2 wires, 19 inches each from coat hangers
2. **Strip**: 1 inch of coax, separate center and shield
3. **Connect**: Wire #1 to center, wire #2 to shield
4. **Tape**: Wrap connection tightly
5. **Mount**: Vertical on wall/window
6. **Test**: `rtl_fm -f 155.475M -M fm -s 48k | aplay`

**Time**: 15 minutes  
**Cost**: $0  
**Result**: Free police scanner!

---

## Questions?

**"How do I know it's working?"**
- You'll hear scratchy voices when testing with rtl_fm

**"Can I leave ADS-B running?"**
- Not simultaneously on same RTL-SDR
- Need second RTL-SDR ($35) to run both
- Or time-share (scanner when you want it, ADS-B rest of time)

**"19 inches seems long"**
- That's correct! VHF wavelengths are long
- 155 MHz = 1.9 meter wavelength
- Quarter-wave = 19 inches

**"Can I make it shorter?"**
- Shorter = less efficient
- 19" is already quarter-wave (minimum)
- Full half-wave would be 38" each leg!

**"What if I don't know the exact frequency?"**
- Use 19" as compromise for 150-160 MHz range
- Works "okay" across entire VHF band
- Tune-able by trimming ends (shorter = higher freq)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
