# Siyata SDK Access Request

## Email Template

---

**To**: support@siyata.com  
**Subject**: SDK Access Request - SD7 OLED Display Integration for Scanner App

---

Dear Siyata Support Team,

I am developing a police/fire scanner application for the **Siyata SD7** device and would like to request SDK access to properly integrate with the device's OLED display.

## Project Details

**Application**: Voice-controlled Police/Fire Scanner  
**Device**: Siyata SD7 (Model SD7)  
**Purpose**: Emergency services monitoring with hardware rotary knob navigation

## Current Implementation Status

I have successfully implemented:
- ✅ Hardware rotary knob integration via broadcast receivers (`com.br.intent.action.ROTARY_KNOB`)
- ✅ Audio streaming (Broadcastify, NOAA Weather)
- ✅ Text-to-speech voice announcements
- ✅ Dynamic feed configuration
- ✅ Auto-start on boot

## SDK Access Needed

I am requesting SDK access to integrate with the **small LCD (OLED display)** on the SD7 device.

**Current Attempts**:
- Tried broadcasting to `com.br.smallcd` package
- Attempted MCX PTT protocol broadcasts (`com.mcx.intent.action.PTT__Kodiak`)
- Need proper API to communicate with PocService for OLED updates

**Desired Functionality**:
- Display current scanner feed name on OLED
- Show playback status (Playing, Stopped, Loading)
- Update display when user rotates knob

## Use Case

The app provides emergency services personnel and radio enthusiasts with hands-free scanner operation using the SD7's physical controls. The OLED display would show feed information without requiring users to look at the main screen while driving or in the field.

## Developer Information

**Name**: Matt Hagen  
**Email**: mattjhagen@gmail.com  
**GitHub**: https://github.com/Mattjhagen/Siyata-Police-Scanner

## Technical Questions

1. What is the proper API/broadcast intent to update the SD7's OLED display?
2. Is there SDK documentation for integrating with PocService?
3. Are there example apps demonstrating OLED display integration?
4. What are the requirements for signing/system-level access?

## Additional Notes

The application currently works fully with voice feedback compensating for the lack of OLED integration, but native display support would significantly improve the user experience.

I am happy to sign any necessary developer agreements or NDAs to access the SDK documentation.

Thank you for your time and consideration. I look forward to hearing from you.

Best regards,

**Matt Hagen**  
Email: mattjhagen@gmail.com  
GitHub: https://github.com/Mattjhagen/Siyata-Police-Scanner

---

## Alternative Contact Methods

If email does not receive a response:

1. **Siyata Website Contact Form**  
   https://www.siyata.com/contact/

2. **LinkedIn**  
   Search for Siyata Mobile employees in engineering/developer relations

3. **Developer Forums**  
   Check if Siyata has developer community or forums

4. **Sales Team**  
   Contact sales and ask for developer relations contact

## What to Include When Sending

Attach or reference:
- GitHub repository: https://github.com/Mattjhagen/Siyata-Police-Scanner
- Screenshots of working app
- Video demonstration of rotary knob navigation
- Explanation of emergency services use case

## Expected Response

- SDK documentation
- Example code for OLED integration
- Signing requirements
- Timeline for access approval

---

**Send this email to**: support@siyata.com or appropriate developer relations contact
