# VolunteerLink — Implementation Audit

_Audited: 2026-05-15. Read-only. No code changed._

---

## Summary

| | Count |
|---|---|
| Total items audited | 64 |
| DONE | 29 |
| PARTIAL | 9 |
| MISSING | 26 |
| **Completion (DONE only)** | **45%** |
| **Completion (DONE + PARTIAL)** | **59%** |

---

## Screens

### Common Screens

- [x] MISSING — **Onboarding screen** — No file exists. No swipeable first-launch flow anywhere in codebase.
- [~] PARTIAL — **Login screen** — Firebase Auth email/password fully wired via `AuthViewModel.login → UserService.loginUser`. Google Sign-In button absent. Password reset UI exists (`TODO` comment only, `sendPasswordReset` in `UserService` is implemented but unreachable from UI).
- [ ] DONE — **Register screen** — Fully implemented. `SegmentedButton` for role selection, Firebase Auth `createUserWithEmailAndPassword`, Firestore write, Room cache. Inline password validation present.

### Volunteer Screens

- [~] PARTIAL — **HomeScreen** — Drive feed loads from Firestore ✓. Inline search + category filter chips ✓. Pull to refresh ✓. Quote card renders but quote is **hardcoded** in `MainViewModel.loadVolunteerHome` (Gandhi quote) — `QuotableApi.kt` is a 1-line stub, never called.
- [~] PARTIAL — **DriveDetailScreen** — Apply Now calls `mainViewModel.applyToDrive` → writes to Firestore and Room ✓. Withdraw functionality ✓. **No WeatherCard** — `WeatherApi.kt` is a 1-line stub and `WeatherCard` component is never used here. **No distance or travel time** — `GeocodingApi.kt` is also a 1-line stub. Banner image loads via Coil ✓.
- [ ] DONE — **MyApplicationsScreen** — Colour-coded status chips (PENDING/APPROVED/REJECTED/WITHDRAWN) ✓. Withdraw dialog ✓. Pull to refresh ✓. Data from Firestore/Room via `volunteerApplications` StateFlow ✓.
- [x] MISSING — **MapScreen** — No file. No Google Maps dependency or screen.
- [x] MISSING — **SavedDrivesScreen** — No file. No saved/bookmarked drives feature anywhere.
- [x] MISSING — **VolunteerStatsScreen** — No file. No charts library, no stats data model.
- [x] MISSING — **ChatsHistoryScreen** — No file.
- [x] MISSING — **ChatScreen** — No file. No real-time Firestore listener anywhere.
- [ ] DONE — **ProfileScreen** — Displays name, email, bio from auth state (Firestore-backed). Pull to refresh reloads from Firestore via `refreshCurrentUser`. Edit Profile, Company submenu, Logout ✓.
- [~] PARTIAL — **EditVolunteerProfileScreen** — Name, bio, phone editable. Saves to Firestore (`UserService.updateUser`) and Room (`userDao.insertUser`) ✓. Propagates to auth state via `authViewModel.updateCurrentUser` ✓. **Profile image upload absent** — placeholder text "Profile photo coming soon", no Cloudinary call.
- [x] MISSING — **SettingsScreen** — No file. No notification preferences or settings UI.
- [x] MISSING — **VolunteerDetailScreen** — No file. NGO cannot view a volunteer's profile.
- [~] PARTIAL — **SearchScreen** — File exists (`SearchScreen.kt`) but is **deprecated** (comment at line 1). Search/filter functionality was merged into `HomeScreen`. `SearchScreen` is **not registered** in `AppNavigation` and the `VolunteerNavBar` has only 3 items (Home, Applications, Profile) — no Search tab. Route `Screen.Search` defined but unreachable.

### NGO Screens

- [ ] DONE — **NgoDashboardScreen** — Stat tiles (Drives, Applicants, Pending) derived from live Firestore data via `ngoDrives` and `ngoApplications` StateFlows ✓. Pull to refresh ✓. Drive cards navigate to applications and edit ✓.
- [ ] DONE — **CreateDriveScreen** — 3-step wizard (Basic Info / Details / Banner). Cloudinary banner upload via `MainViewModel.uploadDriveBanner` ✓. Writes to Firestore via `DriveService.createDrive` and Room via `driveDao.insertDrive` ✓. Navigates to `DriveConfirmationScreen` on success ✓.
- [ ] DONE — **EditDriveScreen** — Pre-filled from `ngoDrives` StateFlow ✓. DatePicker ✓. Banner re-upload supported ✓. Saves via `MainViewModel.updateDrive` → `DriveService.updateDrive` + `driveDao.insertDrive` ✓.
- [ ] DONE — **ManageDrivesScreen** — Current/Expired tab filter ✓. Close confirmation dialog with `AlertDialog` ✓. Auto-expire past drives on open, throttled 24h via `SharedPreferences` ✓. Pull to refresh ✓. FAB to create drive ✓. Empty state with illustration ✓.
- [ ] DONE — **DriveApplicationsScreen** — Approve/Reject buttons with confirmation dialog ✓. Writes to Firestore via `ApplicationService.updateApplicationStatus` ✓. Summary counts (Total/Pending/Approved/Rejected) ✓. Pull to refresh ✓. Applicant cover message shown ✓.
- [x] MISSING — **NgoAnalyticsScreen** — No file. No charts library, no analytics data.
- [~] PARTIAL — **NgoProfileScreen** — Displays name, email, bio from auth state ✓. Edit Profile, Company submenu, Logout ✓. **No pull to refresh** — does not call `refreshCurrentUser` on swipe; unlike `ProfileScreen`, no reload from Firestore on visit.
- [ ] DONE — **EditNgoProfileScreen** — Name, ngoName, ngoDescription, bio, phone editable. Saves via `mainViewModel.saveUserProfile` → Firestore + Room ✓. Propagates via `authViewModel.updateCurrentUser` ✓.
- [x] MISSING — **NgoSettingsScreen** — No file.

---

## Features

### Authentication

- [ ] DONE — **Firebase email/password login** — `UserService.loginUser` calls `auth.signInWithEmailAndPassword` then fetches Firestore user document. Full end-to-end.
- [ ] DONE — **Firebase registration writing user to Firestore** — `UserService.registerUser` creates Firebase Auth user, then `firestore.collection("users").document(uid).set(user)`. User also cached in Room.
- [x] MISSING — **Google Sign-In via Firebase OAuth** — No Google Sign-In button, no `GoogleSignInClient`, no OAuth flow anywhere.
- [ ] DONE — **Auth persistence (skip login on relaunch)** — `AuthViewModel.checkAuthState` checks `FirebaseService.isLoggedIn()`, loads cached Room user, falls back to Firestore. `AppNavigation` shows `AppLoader` during `AuthState.Loading` to prevent login flash.
- [ ] DONE — **Role-based routing after login** — `AppNavigation` routes to `VolunteerHome` or `NgoDashboard` based on `user.role`. `LaunchedEffect(authState)` drives navigation on login/logout.

### Data & Backend

- [ ] DONE — **All drives loaded from Firestore** — `DriveService.getAllActiveDrives()` in `loadVolunteerHome`. Room cache pre-populated then updated from Firestore.
- [ ] DONE — **All applications loaded from Firestore** — `ApplicationService.getApplicationsByVolunteer` and `getApplicationsByDrive` both implemented and called.
- [ ] DONE — **Room caching for drives, applications, user** — `driveDao.insertDrives`, `applicationDao.insertApplications`, `userDao.insertUser` all called after successful Firestore reads.
- [ ] DONE — **Profile save writes to Firestore and Room** — `saveUserProfile` calls `UserService.updateUser` (Firestore) then `userDao.insertUser` (Room).
- [ ] DONE — **Drive create writes to Firestore and Room** — `createDrive` calls `DriveService.createDrive` then `driveDao.insertDrive`.
- [~] PARTIAL — **Application status update writes to Firestore and Room** — `updateApplicationStatus` calls `ApplicationService.updateApplicationStatus` (Firestore ✓) and updates `_ngoApplications` StateFlow in-memory, but **does not call `applicationDao` directly**. Room is only updated indirectly on next full refresh.
- [ ] DONE — **Auto-expire past drives (throttled 24hr)** — `ManageDrivesScreen` `LaunchedEffect` reads `SharedPreferences` `last_expire_check`, calls `mainViewModel.expirePassedDrives(ngoId)` if >24h since last check.
- [ ] DONE — **Drive close confirmation dialog** — `AlertDialog` in `ManageDrivesScreen` shows before calling `mainViewModel.closeDrive`. Closes drive in Firestore and Room.
- [ ] DONE — **Profile update propagates to all screens via StateFlow** — `saveUserProfile` sets `_currentUser`; `EditVolunteerProfileScreen` and `EditNgoProfileScreen` call `authViewModel.updateCurrentUser(it)` on success, updating the shared `authState` StateFlow.

### APIs

- [~] PARTIAL — **Quotable API** — `Quote.kt` data model exists. `QuotableApi.kt` is a **1-line stub** (package declaration only). `RetrofitClient.kt` is a **1-line stub**. `MainViewModel.loadVolunteerHome` sets a hardcoded Gandhi quote instead of calling the API. Quote card renders but never fetches real quotes.
- [x] MISSING — **OpenWeatherMap API** — `WeatherApi.kt` is a **1-line stub**. `WeatherResponse.kt` data model exists. `WeatherCard` component exists but is **never used** in `DriveDetailScreen`. No API call anywhere in the codebase.
- [~] PARTIAL — **Geocoding API** — `GeocodingApi.kt` is a **1-line stub**. `GeocodingResponse.kt` data model exists. Not called anywhere. `DriveDetailScreen` shows no distance/travel time.
- [~] PARTIAL — **GlobalGiving API** — `GlobalGivingApi.kt` is a 1-line stub. `NgoSearchResponse.kt` data model exists. Not integrated in any screen. `NgoProfileScreen` has no NGO search UI.

### Context Awareness

- [x] MISSING — **LocationSimulator** — No file, no class, no GeoLife dataset integration.
- [x] MISSING — **ActivitySimulator** — No file, no HAR dataset integration.
- [x] MISSING — **ContextEngine** — No file, no rules engine.
- [x] MISSING — **WorkManager background GPS + weather refresh** — No `Worker` class, no `WorkManager` dependency called in any meaningful way.
- [x] MISSING — **AlarmManager drive reminders (24hr + 1hr before)** — No `BroadcastReceiver`, no alarm scheduling.
- [x] MISSING — **Push notifications for volunteers within 5km** — No FCM integration, no proximity check.
- [x] MISSING — **Weather warning badge on outdoor drives** — No weather fetch, no badge logic.
- [x] MISSING — **Time-of-day drive prioritisation** — No ordering logic based on time of day.

### UI/UX

- [ ] DONE — **Edge-to-edge rendering** — `enableEdgeToEdge()` called in `MainActivity.onCreate`.
- [ ] DONE — **Bottom nav hidden on nested screens** — `volunteerTopRoutes` and `ngoTopRoutes` sets in `AppNavigation` gate bottom bar rendering. Nested routes (DriveDetail, EditDrive, etc.) correctly show no bottom nav.
- [~] PARTIAL — **Pull to refresh on all main screens** — Present on: `HomeScreen`, `MyApplicationsScreen`, `ProfileScreen` (volunteer), `NgoDashboardScreen`, `ManageDrivesScreen`, `DriveApplicationsScreen`. **Missing** on `NgoProfileScreen`.
- [ ] DONE — **AppToast** — Floating pill toast, role-aware background colour, auto-dismisses after 1500ms. Used consistently across all screens. No raw `Toast.makeText` calls.
- [ ] DONE — **AppLoader** — Full-screen overlay with role-aware progress indicator. Used on all loading states. No inline `CircularProgressIndicator`.
- [ ] DONE — **Auth check loader before login/home** — `AppNavigation` returns `AppLoader` during `AuthState.Loading`, preventing login/home flash on relaunch.
- [ ] DONE — **Custom OpenSans font** — `Type.kt` defines `OpenSansFamily` from `res/font/` (regular, medium, semibold, bold). `AppTypography` applied via `AppTheme`.
- [ ] DONE — **Custom theme** — `Color.kt` defines Volunteer (green) and NGO (blue) palettes. `Theme.kt` switches `MaterialTheme` colour scheme by role. Not default Material3. _(Note: CLAUDE.md TODO "still default Material3" is now outdated — theme IS customised.)_

### Advanced Features

- [x] MISSING — **Google Maps screen with drive pins** — No Maps dependency, no `MapScreen` file.
- [x] MISSING — **Real-time chat via Firestore listeners** — No `addSnapshotListener`, no chat screen, no message data model.
- [x] MISSING — **Volunteer stats charts** — No charting library, no `VolunteerStatsScreen`.
- [x] MISSING — **NGO analytics charts** — No charting library, no `NgoAnalyticsScreen`.
- [x] MISSING — **Saved drives / bookmarking** — No saved drives field in data model, no UI, no `SavedDrivesScreen`.
- [x] MISSING — **Google OAuth Sign-In** — No Google Sign-In dependency or button.

---

## Priority Order

### Core Functionality (fix before demo)

1. **Quotable API integration** — Remove hardcoded quote in `MainViewModel.loadVolunteerHome`. Implement `QuotableApi` Retrofit interface, wire `RetrofitClient`, call on home load. Data model `Quote.kt` ready.
2. **OpenWeatherMap API + WeatherCard in DriveDetailScreen** — `WeatherApi.kt` needs Retrofit interface, `RetrofitClient` needs base URL. Add `WeatherCard` to `DriveDetailScreen` item list. Data model `WeatherResponse.kt` ready.
3. **Geocoding API for distance in DriveDetailScreen** — Implement `GeocodingApi` Retrofit interface, call with drive location string, display distance (even estimated) in `DriveDetailScreen`. Data model `GeocodingResponse.kt` ready.
4. **Application status Room sync** — In `MainViewModel.updateApplicationStatus`, add `applicationDao.updateStatus(applicationId, status)` call so Room stays consistent without requiring a full refresh.
5. **NgoProfileScreen pull-to-refresh** — Add `PullToRefreshBox` wrapping the scroll column, calling `mainViewModel.refreshCurrentUser`. Mirror what `ProfileScreen` does.
6. **SearchScreen route wired into nav** — Either remove `SearchScreen` entirely or re-add `composable(Screen.Search.route)` in `AppNavigation` and add Search tab back to `VolunteerNavBar`. Current state leaves a dead route.
7. **Profile image upload (volunteer + NGO)** — Hook Cloudinary upload into `EditVolunteerProfileScreen` and `EditNgoProfileScreen` (same flow already built for drive banners in `MainViewModel.uploadDriveBanner`).

### Marking Criteria (required assignment features)

8. **WorkManager background refresh** — Create a `Worker` subclass for periodic Firestore sync of drives/weather. Register in `MainActivity`.
9. **LocationSimulator** — Read GeoLife dataset, emit simulated GPS coordinates on a timer. Wire into `ContextEngine`.
10. **ContextEngine** — Apply proximity rule (5km) to filter/prioritise drives. Feed into `HomeScreen` ordering.
11. **Google Maps screen** — Add Maps dependency, create `MapScreen`, add drive pins from `allActiveDrives`. Add nav route.
12. **Volunteer stats charts** — Add charting library (e.g. Vico or MPAndroidChart). `VolunteerStatsScreen` with hours and category participation.
13. **NGO analytics charts** — `NgoAnalyticsScreen` with applicant trends and approval rates from `ngoApplications` data.
14. **Push notifications (proximity)** — FCM token registration + trigger notification when new drive posted within 5km of simulated location.
15. **AlarmManager drive reminders** — Schedule 24hr and 1hr-before alarms when a drive application is approved.
16. **Google Sign-In** — Add `play-services-auth` dependency, `GoogleSignInClient` flow, Firebase credential linking.

### Nice to Have

17. **Onboarding screen** — Swipeable slides on first launch, stored `SharedPreferences` flag.
18. **ActivitySimulator** — HAR dataset integration for context-awareness bonus marks.
19. **Weather warning badge** — Once weather API is live, add badge overlay on `DriveCard` for outdoor drives with bad weather.
20. **Time-of-day drive prioritisation** — Sort `allActiveDrives` differently by morning/afternoon/evening in `ContextEngine`.
21. **Saved drives / bookmarking** — Add `savedDriveIds: List<String>` field to `User`, bookmark button on `DriveCard`.
22. **Chat / messaging** — `ChatsHistoryScreen` + `ChatScreen` with Firestore `addSnapshotListener` on a `messages` collection.
23. **VolunteerDetailScreen** — Read-only volunteer profile accessible from `DriveApplicationsScreen` applicant card.
24. **SettingsScreen / NgoSettingsScreen** — Notification toggles, account deletion.
25. **Password reset UI** — Wire existing `UserService.sendPasswordReset` to the "Forgot Password?" clickable in `LoginScreen`.
