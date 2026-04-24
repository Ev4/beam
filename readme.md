# Beam

*Beam* is a real-time battery monitor. It displays live battery metrics as
persistent notifications in your status bar, and shows a full breakdown of
battery metrics in-app.

You can configure which additional metrics appear in the notification body.
Notification permissions are required; open the app once to grant them and
start the monitor service automatically.

This app respects your privacy!
* No unnecessary permissions
* No ads
* No collection of user data of any kind
* No sharing data with third parties

Detailed battery metrics include:
* Power (watts)
* Current (amps)
* Voltage (volts)
* Energy Level (watt-hours and amp-hours)
* Temperature (celsius)
* Charge Level (percent)
* Is Charging (yes/no)
* Charging Since (date/time)
* Time to Full Charge (duration)

The persistent notification is configurable:
* Body entries — additional metrics shown in the notification body

## Screenshots

**Home Screen**

<img src="readme/home.png" alt="Home Screen" width="360" />

**Notification**

<img src="readme/notification.png" alt="Notification" width="360" />

**Settings View**

<img src="readme/settings.png" alt="Settings View" width="360" />

## FAQ

1. Why does my phone always show `0W`?

    Many phones, especially Samsungs, don't follow the BatteryManager spec. Try changing "Power Scalar Workaround" in the settings view.

2. Why does my external power meter show different power numbers than *Beam*?

    *Beam* can only measure power coming into or out of the battery management system. Your external meter is measuring this plus any power your phone is using in addition to that.

3. Why isn't the indicator showing up in my status bar?

    *Beam* needs notification permissions on newer Android phones in order to show the indicator. Make sure to open *Beam* at least once, where it should prompt you to grant it permissions. Otherwise, check the Android app settings to ensure *Beam* has notification permissions.

    Keep in mind, Android can revoke these permissions at any time without telling you, so you may need to re-enable them periodically.
