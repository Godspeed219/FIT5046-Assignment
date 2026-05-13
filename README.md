# FIT5046 Assignment 2 — VolunteerLink Android App

## Project Overview

**VolunteerLink** is a dual-role Android application built for FIT5046 (Mobile Application Development) at Monash University. The app connects **Volunteers** with **NGOs** (Non-Governmental Organisations) by letting NGOs post volunteer drives and volunteers discover, filter, and apply for them.

The app is a **working prototype** using Jetpack Compose with static dummy data. The Firebase, Room, Retrofit, and ViewModel layers are scaffolded but mostly empty stubs — they exist as file placeholders for the next implementation phase.

---

## Tech Stack

| Layer              | Technology                                          |
| ------------------ | --------------------------------------------------- |
| Language           | Kotlin                                              |
| UI Framework       | Jetpack Compose + Material3                         |
| Navigation         | Navigation Compose 2.9.7                            |
| Local DB           | Room 2.8.4 (entity models defined, DAOs are stubs)  |
| Remote API         | Retrofit 3.0.0 + Gson (interface files are stubs)   |
| Image Loading      | Coil 2.7.0                                          |
| Backend / Auth     | Firebase BOM 34.12.0 (Auth + Firestore + Analytics) |
| Architecture       | MVVM (ViewModels are stubs)                         |
| Build Tool         | Gradle with KSP (Kotlin Symbol Processing)          |
| Min SDK            | 24 (Android 7.0)                                    |
| Target/Compile SDK | 36                                                  |

---

## Application Package

```
com.example.assignment_fit5046
```

---

## Project Directory Structure

```
FIT5046-Assignment/
├── app/
│   ├── build.gradle.kts                          # App-level build config (all dependencies)
│   ├── google-services.json                      # Firebase project config
│   └── src/main/
│       ├── AndroidManifest.xml                   # Single activity, launcher intent
│       ├── java/com/example/assignment_fit5046/
│       │   ├── MainActivity.kt
│       │   ├── components/
│       │   │   ├── common/
│       │   │   │   └── AppNavigation.kt
│       │   │   ├── volunteer/
│       │   │   │   ├── DriveCard.kt
│       │   │   │   ├── QuoteCard.kt
│       │   │   │   ├── VolunteerNavBar.kt
│       │   │   │   └── WeatherCard.kt
│       │   │   └── ngo/
│       │   │       ├── DriveManageCard.kt
│       │   │       └── NgoNavBar.kt
│       │   ├── datamodels/
│       │   │   ├── Application.kt
│       │   │   ├── Drive.kt
│       │   │   ├── DummyData.kt
│       │   │   ├── GeocodingResponse.kt
│       │   │   ├── NgoSearchResponse.kt
│       │   │   ├── Quote.kt
│       │   │   ├── User.kt
│       │   │   └── WeatherResponse.kt
│       │   ├── screens/
│       │   │   ├── common/
│       │   │   │   ├── LoginScreen.kt
│       │   │   │   └── RegisterScreen.kt
│       │   │   ├── company/
│       │   │   │   ├── AboutUsScreen.kt
│       │   │   │   ├── ContactUsScreen.kt
│       │   │   │   └── TermsConditionsScreen.kt
│       │   │   ├── volunteer/
│       │   │   │   ├── DriveDetailScreen.kt
│       │   │   │   ├── EditVolunteerProfileScreen.kt
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   ├── MyApplicationsScreen.kt
│       │   │   │   ├── ProfileScreen.kt
│       │   │   │   └── SearchScreen.kt
│       │   │   └── ngo/
│       │   │       ├── CreateDriveScreen.kt
│       │   │       ├── DriveApplicationsScreen.kt
│       │   │       ├── DriveConfirmationScreen.kt
│       │   │       ├── EditDriveScreen.kt
│       │   │       ├── EditNgoProfileScreen.kt
│       │   │       ├── ManageDrivesScreen.kt
│       │   │       ├── NgoDashboardScreen.kt
│       │   │       └── NgoProfileScreen.kt
│       │   ├── services/
│       │   │   ├── local/
│       │   │   │   ├── AppDatabase.kt
│       │   │   │   ├── TypeConverters.kt
│       │   │   │   └── dao/
│       │   │   │       ├── ApplicationDao.kt
│       │   │   │       ├── DriveDao.kt
│       │   │   │       └── UserDao.kt
│       │   │   ├── remote/
│       │   │   │   ├── RetrofitClient.kt
│       │   │   │   ├── api/
│       │   │   │   │   ├── GeocodingApi.kt
│       │   │   │   │   ├── GlobalGivingApi.kt
│       │   │   │   │   ├── QuotableApi.kt
│       │   │   │   │   └── WeatherApi.kt
│       │   │   │   └── firebase/
│       │   │   │       ├── ApplicationService.kt
│       │   │   │       ├── DriveService.kt
│       │   │   │       ├── FirebaseService.kt
│       │   │   │       ├── StorageService.kt
│       │   │   │       └── UserService.kt
│       │   │   └── viewmodel/
│       │   │       ├── AuthViewModel.kt
│       │   │       └── MainViewModel.kt
│       │   └── ui/
│       │       ├── Color.kt
│       │       ├── Theme.kt
│       │       └── Type.kt
│       └── res/
│           ├── drawable/
│           │   ├── ic_empty_drives.xml
│           │   ├── ic_launcher_background.xml
│           │   └── ic_launcher_foreground.xml
│           ├── font/
│           │   ├── opensans_bold.ttf
│           │   ├── opensans_medium.ttf
│           │   ├── opensans_regular.ttf
│           │   └── opensans_semibold.ttf
│           ├── mipmap-*/                          # App icon (all densities)
│           ├── values/
│           │   ├── colors.xml
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── xml/
│               ├── backup_rules.xml
│               └── data_extraction_rules.xml
├── build.gradle.kts                              # Root build config
├── settings.gradle.kts                           # Module registration
├── gradle.properties                             # JVM args, AndroidX flag
├── gradlew / gradlew.bat                         # Gradle wrapper scripts
├── gradle/
│   ├── libs.versions.toml                        # Version catalog
│   ├── gradle-daemon-jvm.properties
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── my-release-key.keystore                       # Release signing key
├── firebase.json                                 # Firebase hosting/emulator config
├── firestore.rules                               # Firestore security rules
├── firestore.indexes.json                        # Firestore composite indexes
└── .firebaserc                                   # Firebase project alias
```

---

## Navigation & Routing

All routes are defined in `AppNavigation.kt` as a sealed class `Screen`:

| Route Constant            | Path                         | Notes                     |
| ------------------------- | ---------------------------- | ------------------------- |
| `Screen.Login`            | `login`                      | Start destination         |
| `Screen.Register`         | `register`                   |                           |
| `Screen.VolunteerHome`    | `volunteer_home`             |                           |
| `Screen.DriveDetail`      | `drive_detail/{driveId}`     | driveId = String path arg |
| `Screen.Search`           | `search`                     |                           |
| `Screen.MyApplications`   | `my_applications`            |                           |
| `Screen.VolunteerProfile` | `volunteer_profile`          |                           |
| `Screen.NgoDashboard`     | `ngo_dashboard`              |                           |
| `Screen.CreateDrive`      | `create_drive`               |                           |
| `Screen.ManageDrives`     | `manage_drives`              |                           |
| `Screen.NgoProfile`       | `ngo_profile`                |                           |
| `Screen.NgoApplications`  | `ngo_applications/{driveId}` | driveId = String path arg |

The `AppNavigation` composable uses a `Scaffold` with a `NavHost`. The `currentRole` state (either `"VOLUNTEER"` or `"NGO"`) drives which bottom nav bar (`VolunteerNavBar` or `NgoNavBar`) is shown.

---

## Prototype Login Credentials

Authentication is currently **hardcoded** (no Firebase Auth call yet):

| Email                | Password      | Role navigated to           |
| -------------------- | ------------- | --------------------------- |
| `volunteer@test.com` | `password123` | VOLUNTEER → `VolunteerHome` |
| `ngo@test.com`       | `password123` | NGO → `NgoDashboard`        |

"Forgot Password?" is a clickable text with a `// TODO` comment. Registration simply navigates by the selected radio button without any backend call.

---

## Screens — Status & Detail

### Common Screens (both roles)

#### `LoginScreen.kt` — COMPLETE (prototype)

- Email and password `OutlinedTextField`
- Password show/hide toggle using `Visibility`/`VisibilityOff` icons
- "Forgot Password?" clickable text (TODO stub)
- Inline error message displayed in red on bad credentials
- Navigates to `VolunteerHome` or `NgoDashboard` based on email; clears back stack

#### `RegisterScreen.kt` — COMPLETE (prototype)

- Name, Email, Password fields
- Radio buttons to select role: **Volunteer** or **NGO**
- On submit navigates to the appropriate home screen; clears entire back stack
- No backend registration call yet

---

### Volunteer Screens

#### `HomeScreen.kt` — COMPLETE

- `TopAppBar` with title "VolunteerLink"
- `LazyColumn` containing:
  - A `QuoteCard` at the top (static quote from DummyData)
  - Section header "Upcoming Drives Near You"
  - List of `DriveCard` composables for each drive in `DummyData.DRIVES`
- Tapping any `DriveCard` navigates to `DriveDetailScreen` passing `drive.driveId`

#### `SearchScreen.kt` — COMPLETE

- `OutlinedTextField` for free-text search (filters by drive title, case-insensitive)
- `ExposedDropdownMenuBox` for category filter with options: `All`, `Environment`, `Education`, `Health`, `Animal Welfare`, `Community`
- Filters `DummyData.DRIVES` by both criteria simultaneously
- Empty-state message "No drives found" shown when no results match
- Results rendered as `DriveCard` list; tapping navigates to `DriveDetailScreen`

#### `DriveDetailScreen.kt` — COMPLETE

- Back arrow `IconButton` in `TopAppBar`
- Displays: NGO name, category `SuggestionChip`, description
- Event Details section with icons: date (`CalendarToday`), location (`LocationOn`), indoor/outdoor (`Home`/`Park` based on category), volunteer spots remaining (`Group`)
- `WeatherCard` component embedded in the scroll (uses `DummyData.WEATHER`)
- "Getting There" section with hardcoded distance (5.2 km) and travel time (12 mins by car)
- "Apply Now" button triggers a `SnackbarHostState` showing "Application submitted successfully!"
- Indoor classification: categories `Education` and `Health` are indoor; all others outdoor

#### `MyApplicationsScreen.kt` — COMPLETE

- Lists `DummyData.APPLICATIONS` (3 entries for the prototype volunteer)
- Each item is an `ElevatedCard` (`ApplicationItem`) showing:
  - Drive title (bold)
  - NGO name (looked up via `DummyData.DRIVES`)
  - Applied date formatted as "dd MMM yyyy"
  - Status `AssistChip` color-coded:
    - APPROVED → green `0xFF2E7D32`
    - PENDING → orange `0xFFE65100`
    - REJECTED → red `0xFFC62828`

#### `ProfileScreen.kt` — COMPLETE

- Pre-filled from `DummyData.VOLUNTEER_USER` (name: "Alex Johnson", bio: "Passionate about giving back")
- Editable fields: Name, Bio (3-line min), City
- "Set Availability Date" `OutlinedButton` opens a Material3 `DatePickerDialog`
- Date formatted and displayed as "Availability: dd MMM yyyy"
- "Save Profile" button shows Snackbar "Profile saved successfully"
- Large `AccountCircle` icon as avatar placeholder

---

### NGO Screens

#### `NgoDashboardScreen.kt` — COMPLETE

- "Welcome back, [NGO Name]" greeting from `DummyData.NGO_USER.ngoName`
- Three stat cards in a row (using private `StatCard` composable):
  - **Drives** — count of `NGO_OWN_DRIVES`
  - **Applicants** — total count of `NGO_RECEIVED_APPLICATIONS`
  - **Pending** — count where status == PENDING
- Section "Your Active Drives" lists drives using `DriveManageCard`
- Tapping "View Applications" on a card navigates to `DriveApplicationsScreen/{driveId}`

#### `CreateDriveScreen.kt` — COMPLETE

- Scrollable form with:
  - Drive Title (single line)
  - Description (3–5 lines)
  - Location/Address (single line)
  - Max Volunteers (numeric keyboard)
  - Category dropdown: `Environment`, `Education`, `Health`, `Animal Welfare`, `Community`
  - Drive Date via Material3 `DatePickerDialog` (formatted "dd MMM yyyy")
- "Post Drive" button shows Snackbar "Drive posted successfully!" (no backend call yet)

#### `ManageDrivesScreen.kt` — COMPLETE

- Status filter dropdown: `All`, `Active`, `Closed` — filters `DummyData.NGO_OWN_DRIVES` by `DriveStatus` enum
- Empty-state message when filter yields no results
- Results shown via `DriveManageCard` with application counts
- "View Applications" navigates to `DriveApplicationsScreen/{driveId}`

#### `DriveApplicationsScreen.kt` — COMPLETE

- Shows count header "X applications received"
- Filters `DummyData.NGO_RECEIVED_APPLICATIONS` by the passed `driveId`
- Each applicant is an `ApplicantCard` (private composable) with:
  - Person icon + volunteer name
  - Applied date formatted
  - Application message shown in quotes (if non-empty)
  - Status `AssistChip` (color-coded same as volunteer side)
  - For PENDING applications: **Approve** (green) and **Reject** (red outlined) buttons
- Status changes are tracked in local `mutableStateOf(Map)` — updates persist for the session but are not saved to backend
- Snackbar feedback on approve/reject: "[Name] approved!" or "[Name] rejected."

#### `NgoProfileScreen.kt` — COMPLETE

- Large `Business` icon as avatar placeholder
- Pre-filled from `DummyData.NGO_USER`: orgName, description, phone, email
- Editable fields: Organisation Name, Mission/Description (3–5 lines), City, Website (optional), Phone
- Email field is **read-only** (cannot be changed)
- "Save Profile" shows Snackbar "Profile saved successfully"

---

## Reusable Components

### Volunteer Components

#### `DriveCard.kt`

- `ElevatedCard` — tappable (`onClick` lambda)
- Shows: title, category `SuggestionChip` (primaryContainer), NGO name, date with `CalendarToday` icon, location with `LocationOn` icon, indoor/outdoor icon, spots remaining in primary color

#### `QuoteCard.kt`

- `Card` with `primaryContainer` background
- Large `"` character (52sp), italic quote content, right-aligned bold author attribution

#### `WeatherCard.kt`

- `OutlinedCard` titled "Weather on Drive Day"
- Row 1: `WbSunny` icon, temperature in °C, weather description (capitalised)
- Row 2: `Air` icon, wind speed in km/h

#### `VolunteerNavBar.kt`

- `NavigationBar` with 4 items: **Home** (`Home`), **Search** (`Search`), **Applications** (`List`), **Profile** (`Person`)
- Highlights active item by comparing `currentRoute` to each route
- Uses `popUpTo(VolunteerHome)` + `saveState`/`restoreState` for proper back-stack management

### NGO Components

#### `DriveManageCard.kt`

- `ElevatedCard` showing: title, category chip, date, location, application count (People icon + primary color), status chip (`Active`=green / `Closed`=grey)
- Full-width "View Applications (N)" `OutlinedButton`

#### `NgoNavBar.kt`

- `NavigationBar` with 4 items: **Dashboard** (`Home`), **Create** (`Add`), **Manage** (`Edit`), **Profile** (`Person`)
- Same active-highlight and back-stack logic as `VolunteerNavBar`

---

## Data Models

### `Drive.kt` — Room `@Entity(tableName = "drives")`

| Field               | Type        | Notes                                                         |
| ------------------- | ----------- | ------------------------------------------------------------- |
| `driveId`           | String      | `@PrimaryKey`                                                 |
| `ngoId`             | String      |                                                               |
| `ngoName`           | String      | Denormalised for display                                      |
| `title`             | String      |                                                               |
| `description`       | String      |                                                               |
| `location`          | String      | Full address string                                           |
| `date`              | String      | "YYYY-MM-DD"                                                  |
| `maxVolunteers`     | Int         |                                                               |
| `currentVolunteers` | Int         |                                                               |
| `category`          | String      | Environment / Education / Health / Animal Welfare / Community |
| `status`            | DriveStatus | `ACTIVE` or `CLOSED`                                          |
| `createdAt`         | Long        | Unix epoch milliseconds                                       |

### `Application.kt` — Room `@Entity(tableName = "applications")`

| Field           | Type              | Notes                             |
| --------------- | ----------------- | --------------------------------- |
| `applicationId` | String            | `@PrimaryKey`                     |
| `driveId`       | String            | FK to drives                      |
| `driveTitle`    | String            | Denormalised                      |
| `volunteerId`   | String            |                                   |
| `volunteerName` | String            | Denormalised                      |
| `status`        | ApplicationStatus | `PENDING`, `APPROVED`, `REJECTED` |
| `appliedAt`     | Long              | Unix epoch milliseconds           |
| `message`       | String            | Optional cover message            |

### `User.kt` — Plain data class (no Room annotation)

| Field             | Type     | Notes                          |
| ----------------- | -------- | ------------------------------ |
| `uid`             | String   | Firebase Auth UID              |
| `email`           | String   |                                |
| `name`            | String   | Display name                   |
| `role`            | UserRole | `VOLUNTEER` or `NGO`           |
| `phoneNumber`     | String   |                                |
| `bio`             | String   |                                |
| `profileImageUrl` | String   |                                |
| `ngoName`         | String   | NGO-only, empty for volunteers |
| `ngoDescription`  | String   | NGO-only                       |

### `WeatherResponse.kt` — Retrofit Gson model

Maps to OpenWeatherMap API response. Fields: `cityName`, `main` (temp, feelsLike, humidity), `weather` (list of id/main/description/icon), `wind` (speed).

### `Quote.kt` — Retrofit Gson model

Maps to Quotable API (`https://api.quotable.io/random`). Fields: `id` (`_id`), `content`, `author`, `tags`, `length`.

### `GeocodingResponse.kt` — Retrofit Gson model

Maps to OpenWeatherMap Geocoding API. Fields: `name`, `lat`, `lon`, `country`, `state`.

### `NgoSearchResponse.kt` — Retrofit Gson model

Maps to GlobalGiving API. Nested structure: `NgoSearchResponse → OrganizationWrapper → List<NgoOrganization>`. Each `NgoOrganization` has: id, name, mission, logoUrl, projectLink, `ThemeWrapper` (list of `NgoTheme`), `CountryWrapper` (list of `NgoCountry`).

---

## DummyData Object (`DummyData.kt`)

All prototype data lives in this singleton object:

| Property                    | Content                                                                                                                                                                                                         |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `VOLUNTEER_USER`            | Alex Johnson, volunteer@test.com, role=VOLUNTEER                                                                                                                                                                |
| `NGO_USER`                  | Green Earth Australia, ngo@test.com, role=NGO                                                                                                                                                                   |
| `DRIVES`                    | 5 drives across Melbourne: Yarra River Clean-Up (Environment, d1), Tutoring for Kids (Education, d2), Community Health Fair (Health, d3), Animal Shelter (Animal Welfare, d4), Community Garden (Community, d5) |
| `APPLICATIONS`              | 3 volunteer applications: a1=PENDING (d1), a2=APPROVED (d2), a3=REJECTED (d3)                                                                                                                                   |
| `QUOTE`                     | Gandhi quote on service                                                                                                                                                                                         |
| `WEATHER`                   | Melbourne, 22°C, Clear Sky, 12 km/h wind                                                                                                                                                                        |
| `GEOCODING`                 | Federation Square, lat=-37.8179, lon=144.9691                                                                                                                                                                   |
| `NGO_OWN_DRIVES`            | Filtered from DRIVES where ngoId=="n1" (only d1: Yarra River Clean-Up)                                                                                                                                          |
| `NGO_RECEIVED_APPLICATIONS` | 4 applications for d1: na1=PENDING (Alex Johnson), na2=PENDING (Sarah Chen), na3=APPROVED (Marcus Williams), na4=REJECTED (Emma Rodriguez)                                                                      |
| `NGO_RESULTS`               | 3 GlobalGiving org results: OzHarvest, Beyond Blue, Australian Red Cross                                                                                                                                        |

---

## Service / Backend Layer — ALL STUBS

These files exist as placeholders (1 line / package declaration only). None have implementations yet.

| File                  | Intended Purpose                                             |
| --------------------- | ------------------------------------------------------------ |
| `FirebaseServices.kt` | Firestore read/write helpers for drives, applications, users |
| `AuthViewModel.kt`    | Firebase Auth login, register, logout state                  |
| `MainViewModel.kt`    | Drive listing, application submission, search/filter logic   |
| `ApplicationDao.kt`   | Room DAO — query/insert/update applications locally          |
| `DriveDao.kt`         | Room DAO — query/insert/update drives locally                |
| `RetrofitClient.kt`   | Singleton Retrofit instances for each API base URL           |
| `WeatherApi.kt`       | OpenWeatherMap current weather endpoint                      |
| `QuotableApi.kt`      | Quotable random quote endpoint                               |
| `GeocodingApi.kt`     | OpenWeatherMap geocoding (address → lat/lon)                 |
| `GlobalGivingApi.kt`  | GlobalGiving NGO search endpoint                             |

---

## UI Theme — ALL STUBS

`Color.kt`, `Theme.kt`, `Type.kt` are all empty (package declaration only). The app currently uses Material3 default theming. The theme name in `AndroidManifest.xml` and `themes.xml` is `Theme.AssignmentFIT5046`.

---

## Firebase Configuration

- **Project alias**: defined in `.firebaserc`
- **Services integrated**: Firebase Auth, Cloud Firestore, Firebase Analytics
- **BOM version**: 34.12.0
- **Firestore rules**: Open read/write (no auth required) until `2026-05-10`. After that date all requests will be denied unless rules are updated.
- **Firestore indexes**: None defined (`firestore.indexes.json` is empty template)

---

## What Is Complete vs. What Is A Stub

### COMPLETE (fully implemented, composes and renders)

- `MainActivity.kt`
- `AppNavigation.kt` (all routes, role-based nav bar switching)
- All 5 Volunteer screens
- All 5 NGO screens
- All 6 reusable UI components
- All 7 data model files (including Room annotations on Drive and Application)
- `DummyData.kt` (full static dataset)

### STUB (file exists, package declaration only, no implementation)

- `FirebaseServices.kt`
- `AuthViewModel.kt`
- `MainViewModel.kt`
- `ApplicationDao.kt`
- `DriveDao.kt`
- `RetrofitClient.kt`
- `WeatherApi.kt`
- `QuotableApi.kt`
- `GeocodingApi.kt`
- `GlobalGivingApi.kt`
- `Color.kt`
- `Theme.kt`
- `Type.kt`

### KNOWN TODOS IN COMPLETE FILES

- `LoginScreen.kt`: "Forgot Password?" click has `// TODO: trigger Firebase password reset`
- `LoginScreen.kt`: Login button uses hardcoded credential matching instead of `AuthViewModel`
- `RegisterScreen.kt`: Register button navigates directly without any `AuthViewModel` call
- `DriveDetailScreen.kt`: "Apply Now" only shows a Snackbar — no Firestore write
- `ProfileScreen.kt`: "Save Profile" only shows a Snackbar — no Firestore write
- `CreateDriveScreen.kt`: "Post Drive" only shows a Snackbar — no Firestore write
- `NgoProfileScreen.kt`: "Save Profile" only shows a Snackbar — no Firestore write
- `DriveApplicationsScreen.kt`: Approve/Reject updates in-memory state only, no Firestore write
- `DriveDetailScreen.kt`: Distance and travel time are hardcoded strings, not from GeocodingApi

---

## Running the Project

1. Open in Android Studio (Hedgehog or newer)
2. Ensure a `google-services.json` is placed in `app/` (Firebase project config)
3. Build with Gradle; min device API 24
4. Use the hardcoded credentials above to log in as either role
5. A debug APK is present at `app/build/intermediates/apk/debug/app-debug.apk`

---

## Git Branches

| Branch              | Purpose                                           |
| ------------------- | ------------------------------------------------- |
| `main`              | Current merged state (prototype complete)         |
| `prototype-version` | Feature branch where prototype was built (merged) |

Recent commits: Volunteer side complete → NGO screens complete → Version change → Few file removal → Merge.

<!-- keytool -list -v \
 -keystore /Users/godspeed/Desktop/Monash/SEM-3/FIT5046-Mobile-App/Ass2/FIT5046-Assignment/my-release-key.keystore \
 -alias my-key-alias \
 -storepass 123456 \
 -keypass 123456 -->
