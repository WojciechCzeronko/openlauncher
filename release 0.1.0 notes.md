# RetroLauncher v0.1.0

First stable RetroLauncher release.

RetroLauncher is a retro-inspired Android automotive launcher designed for dedicated in-car tablets and head units.  
This release introduces the new AW11 / 1980s LCD-inspired interface together with integrated navigation, trip data, media controls and voice destination search.

## Highlights

### 🗺️ Integrated Navigation
- HERE-based map and routing
- Destination search
- Voice destination search
- Turn-by-turn route guidance
- Maneuver information
- Automatic rerouting
- Traffic-aware route updates
- Route progress visualization
- Remaining distance and ETA
- Route line progressively disappears as the route is completed

### 🎙️ Voice Destination Search
- Dedicated microphone mode built directly into the destination search control
- Uses Android speech recognition
- Automatically searches the recognized destination
- Supports the device's configured language
- Keyboard voice input remains available as a fallback

### 🚗 Automotive Dashboard
- GPS speedometer
- Trip distance
- Trip duration
- Average speed
- Maximum speed
- Compass heading
- Vehicle-oriented landscape layout

### 🎵 Media Integration
- Active media source detection
- Track and artist information
- Play / pause
- Previous / next track
- Playback progress
- Direct access to the active media application

### 🟢 AW11 Retro Display Interface
Complete visual redesign inspired by late-1970s and 1980s automotive LCD displays:

- Dark automotive display background
- Yellow-green monochrome palette
- Dot-matrix / pixel-display styling
- Scanlines
- Subtle display glow
- Screen interference effects
- Pixel-mask rendering
- Retro typography
- Custom HERE map styling matching the dashboard

### ⚙️ Settings
AW11-styled settings interface with controls for:

- Vehicle name
- Text scale
- Compass calibration
- Automatic rerouting
- Traffic refresh interval
- Off-route detection distance
- Reroute delay
- Minimum route improvement
- A-GPS assistance data reset

### 🚀 First-run Onboarding
New four-step onboarding flow:

1. Introduction
2. Location Services
3. Media Integration
4. System Ready

The onboarding explains required permissions and provides direct access to the appropriate Android settings.

### 📱 App Library
- Access installed applications directly from RetroLauncher
- Internal Apps and Settings screens no longer destroy an active navigation session

### 🧭 Navigation Session Improvements
Active navigation now survives switching between:

- Home
- Settings
- App Library

Returning to the dashboard restores the existing navigation session without rebuilding the route.

## Stability

The `v0.1.0` build has been smoke-tested for:

- Application startup
- Destination search
- Voice search
- Route calculation
- Active guidance
- Rerouting
- Route progress
- Settings navigation
- App Library navigation
- Media controls
- Settings persistence
- First-run onboarding

Route progress and navigation behavior have also been verified during real driving.

## Known Limitations

- App Library still needs additional visual refinement to fully match the AW11 interface.
- Opening an external media application moves RetroLauncher to the background.
- Location updates currently stop while RetroLauncher is in the background.
- There is currently no dedicated floating NAV return control over external applications.

These areas are planned for future releases.

## Planned Next Steps

- AW11 redesign of the App Library
- Quick-return NAV overlay for external applications
- Background navigation using a foreground service
- Improved media application workflow
- Additional hardware and head-unit compatibility testing