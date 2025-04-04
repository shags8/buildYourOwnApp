# AutoZen - Smart Mode Switching

AutoZen is an Android app that automatically switches your phone's sound mode (Silent/Vibrate) based on your location. Users can save specific locations with a defined radius, and the app will adjust the mode when they enter or leave the area. This helps in maintaining a distraction‑free environment in places like offices, libraries, or meetings.

## Tech Stack & Challenges

AutoZen is built using:
- **Kotlin**
- **Room Database**
- **Google Location Services**
- **Android MVVM Architecture**
- **Coroutines**

Coroutines are used for efficient background operations, ensuring smooth database access and location updates without blocking the UI.  
A key challenge was optimizing location tracking while minimizing battery consumption.

**Note:** We couldn't use WorkManager for the background task scheduling due to its minimum periodic interval of 15 minutes. Instead, AutoZen uses a persistent foreground service to achieve near‑real‑time mode switching.

## Future Plans

- **Calendar Integration:** Sync with calendars to automatically switch modes during meetings or scheduled events.
- **Geofencing APIs:** Improve accuracy and reduce battery consumption compared to continuous location tracking.
- **Custom Scheduling:** Allow users to set time‑based rules for mode switching.
