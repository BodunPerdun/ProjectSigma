# LocaPop — UI/UX Design System & Specification

## 1. Brand Identity & Overview
- **Application Name**: LocaPop
- **Tagline**: Local-First Interactive Social Event Map
- **Target Aesthetic**: Modern, vibrant, minimal, and social (inspired by Google Maps, TikTok Profiles, and Material 3 design systems).
- **Core Experience**:
  - Discovering nearby events, meetups, food markets, parties, and culture exhibitions on a full-screen interactive vector map.
  - Creating and publishing custom event pins with time intervals, categories, and cover photos.
  - Interacting with attendees, sending friend requests, and viewing user profiles with Instagram & Telegram deep links.

---

## 2. Color Palette & Theming (Light Mode Priority)

| Element / Surface | Hex / Token | Description |
| :--- | :--- | :--- |
| **Primary Brand Accent** | `#1976D2` / `#2196F3` | Vibrant Blue for active states, pins, and primary buttons |
| **Secondary Accent** | `#3F51B5` / `#3949AB` | Indigo for event clusters and secondary badges |
| **Canvas Background** | `#FFFFFF` | Pure White background for bottom sheets and containers |
| **Card Containers** | `#F8F9FA` / `#F0F4F8` | Light Slate Gray for list items, profile cards, and inputs |
| **Active / Success** | `#4CAF50` / `#2E7D32` | Soft Green for "Verified" pills, friend badges, and status |
| **Warning / Pending** | `#FFF3CD` / `#856404` | Warm Amber for pending requests and "Your Event" badge |
| **Error / Action** | `#E53935` / `#C2185B` | Crimson Red for delete actions, sign out, and Instagram pill |

---

## 3. Typography Scale

| Style Name | Size / Weight | Usage |
| :--- | :--- | :--- |
| **Display Large** | `24sp` / `Bold` | Main sheet titles (`LocaPop`, `Friends & Co-Attendees`) |
| **Title Medium** | `18sp` / `Bold` | Event card titles, user display names |
| **Body Large** | `14sp` / `Regular` | Event descriptions, bio text, form inputs |
| **Body Small** | `12sp` / `Medium` | Subtitles, handle texts (`@username`), dates |
| **Badge Label** | `11sp` / `Bold` | Distance tags (`📍 350m away`), status pills |

---

## 4. Key Screens & Component Hierarchy

### A. Full-Bleed Map & Floating Controls (`MainScreen`)
- **Map Canvas**: Full-screen MapLibre GL Native vector map with OpenFreeMap Positron tiles.
- **Top Header Bar**:
  - Floating 56dp rounded pill (`RoundedCornerShape(28.dp)`), white (`0.96f` opacity), `8dp` shadow elevation.
  - Left: Settings Circle Button (`Icons.Default.Settings`)
  - Center: LocaPop Logo + Brand Name
  - Right: Bell Notifications (`Icons.Default.Notifications`) with Red Unread Badge + Profile Circle Avatar.
- **Top Category Filter Bar**:
  - Horizontal scrollable chip row: All (`🥳`), Party (`🎉`), Meetup (`💼`), Food (`🍔`), Culture (`🎨`), Sports (`⚽`).
- **Bottom Right FAB Column**:
  - `52dp` circular floating action buttons:
    - **My Location FAB** (`Icons.Default.LocationOn`): Smoothly animates camera to user GPS coordinates.
    - **Friends FAB** (`Icons.Default.Person`): Opens Friends & Co-Attendees sheet.

### B. TikTok / Instagram Style Profile (`ProfileBottomSheet` & `ParticipantProfileSheet`)
- **Header Block**:
  - Large Centered Avatar Circle (`104dp`) with camera edit icon overlay.
  - Display Name (`22sp` Bold) + Handle (`@username` in gray).
  - Verified Status Pill (`CheckCircle` icon + `Active Status` text in `#E3F2FD`).
- **Centered Stats Row**:
  - 3-column row with vertical dividers (`VerticalDivider`):
    - `Created Events` count
    - `Friends` count (clickable, opens Friends sheet)
    - `Total Attendees` across created events
- **Action Buttons Row**:
  - Primary filled `Edit Bio` button + Secondary `Edit Socials` button.
- **Centered Bio & Social Links**:
  - Centered bio description text.
  - Social Media Chips:
    - **Instagram**: `📸 @username` (deep-link to `https://instagram.com/_u/username`)
    - **Telegram**: `✈️ @username` (deep-link to `https://t.me/username`)

### C. Interactive Event Details (`EventDetailsBottomSheet`)
- **Header**: Category Badge + "Your Event" owner badge + System Share Button (`Icons.Default.Share`).
- **Event Information**:
  - Title (HeadlineSmall) + Formatted Timezone-aware Date String (e.g. `08 Oct, 21:02 - 23:00`).
  - Cover Photo Card (180dp height, rounded 16dp).
  - Distance Badge (`"📍 350 m away"`).
  - Clickable Organizer Card + Horizontal Participant Avatar List.

### D. Settings & App Configuration (`SettingsBottomSheet`)
- **Privacy & Discovery Controls**:
  - `Visible to People Nearby` switch (`isLocationVisible`)
  - `Public Social Links` switch (`isSocialsPublic`)
- **Language Selection**:
  - Radio button options for English (`🇺🇸`), Russian (`🇷🇺`), Ukrainian (`🇺🇦`), Polish (`🇵🇱`).

---

## 5. Icons & Visual Assets Policy
- **Material Vector Icons Only**: All functional UI controls, buttons, cards, and navigation items strictly use `androidx.compose.material.icons` vector drawables.
- **Emojis Restricted to Content**: Emojis are reserved solely for Event Category icons (🥳, 💼, 🍔, 🎨, ⚽).

---

## 6. Network & Real-Time Sync
- **Timezone Management**: Event timestamps stored in UTC Epoch Milliseconds (`startTimestampMs`, `endTimestampMs`) and formatted in viewer's local device timezone (`TimeZone.currentSystemDefault()`).
- **Real-Time WebSockets**: Live broadcast of `EVENT_CREATED`, `EVENT_UPDATED`, `EVENT_DELETED` updating pins across all connected devices in real-time.
