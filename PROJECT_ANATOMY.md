# PROJECT_ANATOMY.md — VolunteerLink

> Complete anatomy of the VolunteerLink Android application.
> Generated from source code — every function, field, and route documented from actual files.
> Last updated: 2026-05-20.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Screens](#2-screens)
3. [Components](#3-components)
4. [Data Models](#4-data-models)
5. [Services](#5-services)
6. [ViewModels](#6-viewmodels)
7. [Navigation](#7-navigation)
8. [Context-Awareness](#8-context-awareness)
9. [Task Allocation Table](#9-task-allocation-table)

---

## 1. Project Overview

VolunteerLink is a dual-role Android application built for FIT5046 (Mobile Application Development) at Monash University. NGOs can create and manage volunteer drives; volunteers can browse, search, apply for, and track those drives. The app is written in Kotlin with a Jetpack Compose + Material3 UI layer, an MVVM architecture backed by Firebase (Auth, Firestore, Storage, FCM) for the remote source of truth and Room for local caching. Context-awareness is a core feature: a `LocationSimulator` replays real GPS traces from a Melbourne dataset, a `ContextEngine` scores and re-ranks drives by proximity (Haversine), time-of-day, and the volunteer's application history, and a `WorkManager` periodic job advances the simulator every 15 minutes. Drive-day reminders are delivered via `AlarmManager` + `BroadcastReceiver` and persisted through device reboots. The UI applies role-based Material3 colour schemes — green (`VolunteerPrimary`) for volunteers and blue (`NgoPrimary`) for NGOs — and uses OpenSans as the app-wide typeface. External APIs include ZenQuotes (inspirational quote), Open-Meteo (weather), Nominatim (geocoding), and GlobalGiving (NGO search). Maps are rendered with OSMDroid.

---

## 2. Screens

### 2.1 Common Screens

---

#### `LoginScreen`

| Field | Detail |
|---|---|
| **File** | `screens/common/LoginScreen.kt` |
| **Purpose** | Entry point for all users. Supports email/password login and Google Sign-In. |
| **Key Composables** | `LoginScreen` |
| **ViewModels Read** | `AuthViewModel`, `MainViewModel` |
| **StateFlows Collected** | `authState`, `pendingGoogleUser` |
| **Navigation Actions** | On login success → `VolunteerHome` or `NgoDashboard` (handled by `AppNavigation` auth observer); Register link → `Register` |
| **Key UI Elements** | Lottie animation (role-aware), email `OutlinedTextField`, password `OutlinedTextField` with show/hide toggle, `Button` ("Sign In"), `TextButton` ("Forgot Password?" — TODO, no implementation), `OutlinedButton` ("Continue with Google"), `TextButton` ("Don't have an account? Register") |
| **Owner** | |

---

#### `RegisterScreen`

| Field | Detail |
|---|---|
| **File** | `screens/common/RegisterScreen.kt` |
| **Purpose** | New user registration for both roles. Supports manual registration and Google-prefilled registration. |
| **Key Composables** | `RegisterScreen` |
| **ViewModels Read** | `AuthViewModel`, `MainViewModel` |
| **StateFlows Collected** | `authState`, `pendingGoogleUser` |
| **Navigation Actions** | On success → `VolunteerHome` or `NgoDashboard`; Back → `Login` |
| **Key UI Elements** | `SegmentedButton` (Volunteer / NGO role picker), animated Volunteer-only fields (name, email, password), animated NGO-only fields (NGO name, ngoDescription + GlobalGiving search `AlertDialog`), Google-mode pre-fills email/name |
| **Owner** | |

---

#### `NotificationsScreen`

| Field | Detail |
|---|---|
| **File** | `screens/common/NotificationsScreen.kt` |
| **Purpose** | Lists all in-app notifications for the logged-in user; allows marking all as read. |
| **Key Composables** | `NotificationsScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `notifications`, `unreadCount` |
| **Navigation Actions** | Back arrow → `popBackStack()` |
| **Key UI Elements** | `TopAppBar` with back button, `TextButton` ("Mark all read"), `LazyColumn` of `NotificationItem` cards (title, message, timestamp, read/unread styling) |
| **Owner** | |

---

### 2.2 Volunteer Screens

---

#### `HomeScreen`

| Field | Detail |
|---|---|
| **File** | `screens/volunteer/VolunteerHomeScreen.kt` |
| **Purpose** | Primary volunteer landing screen. Displays inspirational quote, drive search/filter, and context-ranked drive list. |
| **Key Composables** | `HomeScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `allActiveDrives`, `contextRankedDrives`, `quote`, `isLoading`, `isRefreshing`, `errorMessage`, `unreadCount` |
| **Navigation Actions** | Drive card tap → `drive_detail/{driveId}`; Notification bell → `notifications` |
| **Key UI Elements** | `TopAppBar` with `BadgedBox` notification icon, user's name as headline, `QuoteCard`, `OutlinedTextField` search with clear icon, `Row` of `FilterChip`s (All / Environment / Education / Health / Animal Welfare / Community), "Upcoming Drives Near You" section header, simulated GPS coordinates label, `PullToRefreshBox`, `LazyColumn` of `DriveCard`s, `LottieEmptyState` (no results), `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `DriveDetailScreen`

| Field | Detail |
|---|---|
| **File** | `screens/volunteer/DriveDetailScreen.kt` |
| **Purpose** | Full details for a single volunteer drive. Shows weather forecast, allows apply/withdraw. |
| **Key Composables** | `DriveDetailScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `driveWeather`, `driveDistance`, `isLoading`, `volunteerApplications`, `allActiveDrives`, `errorMessage`, `successMessage`, `unreadCount` |
| **Navigation Actions** | Back arrow → `popBackStack()`; Notification bell → `notifications` |
| **Key UI Elements** | Coil async hero banner image, stat strip (date, location, category, spots remaining), `WeatherCard`, apply/withdraw `AlertDialog`s, bottom bar sticky CTA (`Button` — "Apply Now" or "Withdraw Application"), `AppLoader` |
| **Owner** | |

---

#### `MapScreen`

| Field | Detail |
|---|---|
| **File** | `screens/volunteer/MapScreen.kt` |
| **Purpose** | OSMDroid-based interactive map showing simulated user location and active drive pins within 5 km. |
| **Key Composables** | `MapScreen` |
| **ViewModels Read** | `MainViewModel` |
| **StateFlows Collected** | `allActiveDrives`, `driveCoordinatesState`, `unreadCount`; also `LocationSimulator.currentLocation` directly |
| **Navigation Actions** | Drive marker tap → `drive_detail/{driveId}`; Notification bell → `notifications` |
| **Key UI Elements** | `TopAppBar` with `BadgedBox`, `AndroidView` wrapping `MapView` (OSMDroid MAPNIK tiles, multi-touch, zoom 13), blue dot `Marker` for simulated GPS, `Polygon` circle (5 km radius, semi-transparent blue fill), red `Marker`s for each drive with info window, legend `Card` (bottom-left overlay) showing GPS coordinates |
| **Owner** | |

---

#### `MyApplicationsScreen`

| Field | Detail |
|---|---|
| **File** | `screens/volunteer/MyApplicationsScreen.kt` |
| **Purpose** | Lists all of the volunteer's drive applications with status chips and withdraw option. |
| **Key Composables** | `MyApplicationsScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `volunteerApplications`, `isLoading`, `isRefreshing`, `errorMessage`, `successMessage`, `unreadCount` |
| **Navigation Actions** | Back arrow; Notification bell → `notifications` |
| **Key UI Elements** | `TopAppBar` with `BadgedBox`, `PullToRefreshBox`, `LazyColumn` of `ApplicationCard`s (drive title, NGO, date, status chip, "Withdraw" button), withdraw confirmation `AlertDialog`, `LottieEmptyState`, `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `ProfileScreen`

| Field | Detail |
|---|---|
| **File** | `screens/volunteer/ProfileScreen.kt` |
| **Purpose** | Volunteer profile page with stats, bio, and company info links. |
| **Key Composables** | `ProfileScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `currentUser`, `volunteerApplications`, `unreadCount` |
| **Navigation Actions** | Notification bell → `notifications`; "Edit Profile" → `edit_volunteer_profile`; About Us → `about_us`; Contact Us → `contact_us`; Terms & Conditions → `terms_conditions`; Logout dialog → navigate to `Login` after sign-out |
| **Key UI Elements** | `TopAppBar` with `BadgedBox`, `ProfileHeaderCard` (stats: Applications / Approved), expandable "Company" `ElevatedCard` with About/Contact/Terms `TextButton`s, logout `AlertDialog` |
| **Owner** | |

---

#### `EditVolunteerProfileScreen`

| Field | Detail |
|---|---|
| **File** | `screens/volunteer/EditVolunteerProfileScreen.kt` |
| **Purpose** | Edit name, bio, and phone number for a volunteer account. |
| **Key Composables** | `EditVolunteerProfileScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `currentUser`, `isLoading`, `profileUpdateSuccess`, `errorMessage` |
| **Navigation Actions** | Back arrow → `popBackStack()`; on `profileUpdateSuccess` → `popBackStack()` |
| **Key UI Elements** | `TopAppBar` with back button, `OutlinedTextField`s (Display Name, Bio, Phone Number), `Button` ("Save Changes"), `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `SearchScreen` *(Deprecated)*

| Field | Detail |
|---|---|
| **File** | `screens/volunteer/SearchScreen.kt` |
| **Purpose** | **Deprecated.** Search and filter functionality has been merged into `HomeScreen`. File remains on disk but the `Screen.Search` route is defined in the sealed class without a registered `composable()` in `AppNavigation`. |
| **Key Composables** | `SearchScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `searchResults`, `ngoSearchResults`, `ngoModalResults`, `ngoModalLoading`, `ngoModalError` |
| **Navigation Actions** | Drive card tap → `drive_detail/{driveId}` |
| **Key UI Elements** | Search `OutlinedTextField`, `FilterChip` row (category), `LazyColumn` of `DriveCard`s (search results), GlobalGiving NGO Partners section with `ngoSearchResults` |
| **Owner** | |

---

### 2.3 NGO Screens

---

#### `NgoDashboardScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/NgoHomeScreen.kt` (composable function: `NgoDashboardScreen`) |
| **Purpose** | NGO home dashboard showing summary stats and manage-drives list. |
| **Key Composables** | `NgoDashboardScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `ngoDrives`, `ngoApplications`, `isLoading`, `isRefreshing`, `unreadCount` |
| **Navigation Actions** | Notification bell → `notifications`; "Manage" FAB → `manage_drives`; `DriveManageCard` callbacks → `ngo_applications/{driveId}`, `edit_drive/{driveId}` |
| **Key UI Elements** | `TopAppBar` with `BadgedBox`, stat row cards (Active Drives / Total Applicants / Pending), `PullToRefreshBox`, `LazyColumn` of `DriveManageCard`s, `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `CreateDriveScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/CreateDriveScreen.kt` |
| **Purpose** | 3-step wizard for creating a new volunteer drive with optional Cloudinary banner upload. |
| **Key Composables** | `CreateDriveScreen`, `StepperHeader` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `isLoading`, `successMessage`, `errorMessage`, `currentUser` |
| **Navigation Actions** | Back arrow → `popBackStack()`; on success → `drive_confirmation` |
| **Key UI Elements** | `StepperHeader` (step indicator), Step 1: title / description / `ExposedDropdownMenuBox` (category); Step 2: location / `DatePicker` dialog / `TimePicker` dialogs (start + end) / max volunteers; Step 3: banner image picker + Cloudinary upload, `Button` ("Post Drive"), `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `ManageDrivesScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/ManageDrivesScreen.kt` |
| **Purpose** | Tabbed list of an NGO's drives (Current / Expired). Supports close, edit, and auto-expire. |
| **Key Composables** | `ManageDrivesScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `ngoDrives`, `ngoApplications`, `isLoading`, `isRefreshing`, `errorMessage`, `successMessage` |
| **Navigation Actions** | Back arrow → `popBackStack()`; FAB → `create_drive`; Edit → `edit_drive/{driveId}`; View Applications → `ngo_applications/{driveId}` |
| **Key UI Elements** | `TopAppBar`, `TabRow` (Current / Expired), `PullToRefreshBox`, `LazyColumn` of `DriveManageCard`s, close-drive confirmation `AlertDialog`, `ExtendedFloatingActionButton` ("New Drive"), auto-expire logic (SharedPreferences once-per-day guard), `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `DriveApplicationsScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/DriveApplicationsScreen.kt` |
| **Purpose** | Lists all applicants for a specific drive; allows approve/reject with confirmation. |
| **Key Composables** | `DriveApplicationsScreen` |
| **ViewModels Read** | `MainViewModel` |
| **StateFlows Collected** | `ngoApplications`, `isLoading`, `successMessage`, `errorMessage` |
| **Navigation Actions** | Back arrow → `popBackStack()` |
| **Key UI Elements** | `TopAppBar` (drive title), summary row chips (Total / Pending / Approved / Rejected counts), `LazyColumn` of `ApplicantCard`s (volunteer name, status chip, message, approve/reject buttons), confirmation `AlertDialog`, `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `EditDriveScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/EditDriveScreen.kt` |
| **Purpose** | Edit all fields of an existing drive including banner image replacement. |
| **Key Composables** | `EditDriveScreen` |
| **ViewModels Read** | `MainViewModel` |
| **StateFlows Collected** | `ngoDrives`, `isLoading`, `successMessage`, `errorMessage` |
| **Navigation Actions** | Back arrow / on save success → `popBackStack()` |
| **Key UI Elements** | Hero banner image picker (Cloudinary re-upload), `OutlinedTextField`s (title, description, location, max volunteers), `DatePicker` dialog, `TimePicker` dialogs (start/end), `ExposedDropdownMenuBox` (category), bottom bar `Button` ("Save Changes"), `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `NgoProfileScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/NgoProfileScreen.kt` |
| **Purpose** | NGO profile page with organisation stats, description, and company links. |
| **Key Composables** | `NgoProfileScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `currentUser`, `ngoDrives`, `ngoApplications`, `unreadCount` |
| **Navigation Actions** | Notification bell → `notifications`; "Edit Profile" → `edit_ngo_profile`; About Us → `about_us`; Contact Us → `contact_us`; Terms → `terms_conditions`; Logout → `Login` |
| **Key UI Elements** | `TopAppBar` with `BadgedBox`, `ProfileHeaderCard` (stats: Drives / Applicants), expandable Company `ElevatedCard`, logout `AlertDialog` |
| **Owner** | |

---

#### `EditNgoProfileScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/EditNgoProfileScreen.kt` |
| **Purpose** | Edit NGO account fields: display name, NGO name, description, bio, and phone. |
| **Key Composables** | `EditNgoProfileScreen` |
| **ViewModels Read** | `MainViewModel`, `AuthViewModel` |
| **StateFlows Collected** | `currentUser`, `isLoading`, `profileUpdateSuccess`, `errorMessage` |
| **Navigation Actions** | Back arrow → `popBackStack()`; on `profileUpdateSuccess` → `popBackStack()` |
| **Key UI Elements** | `TopAppBar` with back button, `OutlinedTextField`s (Display Name, NGO Name, NGO Description, Bio, Phone Number), `Button` ("Save Changes"), `AppLoader`, `AppToast` |
| **Owner** | |

---

#### `DriveConfirmationScreen`

| Field | Detail |
|---|---|
| **File** | `screens/ngo/DriveConfirmationScreen.kt` |
| **Purpose** | Success screen shown after a drive is created. Non-dismissible back navigation to prevent re-creation. |
| **Key Composables** | `DriveConfirmationScreen` |
| **ViewModels Read** | None |
| **StateFlows Collected** | None |
| **Navigation Actions** | "Go to Dashboard" `Button` → `ngo_dashboard` (clears back stack) |
| **Key UI Elements** | Full-screen `Column`, Lottie success animation, "Drive Posted!" headline, subtitle text, `Button` ("Go to Dashboard") |
| **Owner** | |

---

### 2.4 Company Screens

---

#### `AboutUsScreen`

| Field | Detail |
|---|---|
| **File** | `screens/company/AboutUsScreen.kt` |
| **Purpose** | Static scrollable page describing VolunteerLink's mission and values. |
| **Key Composables** | `AboutUsScreen` |
| **ViewModels Read** | None |
| **StateFlows Collected** | None |
| **Navigation Actions** | Back arrow → `popBackStack()` |
| **Key UI Elements** | `TopAppBar` with back button, vertically scrollable `Column`, mission paragraph, `OutlinedCard` for each value (e.g., Community, Impact, Transparency, Innovation) |
| **Owner** | |

---

#### `ContactUsScreen`

| Field | Detail |
|---|---|
| **File** | `screens/company/ContactUsScreen.kt` |
| **Purpose** | Static contact information page with email, phone, address, hours, and social links. |
| **Key Composables** | `ContactUsScreen`, `ContactCard` (private), `SocialChip` (private) |
| **ViewModels Read** | None |
| **StateFlows Collected** | None |
| **Navigation Actions** | Back arrow → `popBackStack()` |
| **Key UI Elements** | `TopAppBar` with back button, scrollable `Column`, four `ContactCard`s (Email, Phone, Office address, Business Hours), social `AssistChip`s (LinkedIn, Instagram, Facebook) |
| **Owner** | |

---

#### `TermsConditionsScreen`

| Field | Detail |
|---|---|
| **File** | `screens/company/TermsConditionsScreen.kt` |
| **Purpose** | Static legal terms and conditions page (9 sections). |
| **Key Composables** | `TermsConditionsScreen`, `TermsSection` (private) |
| **ViewModels Read** | None |
| **StateFlows Collected** | None |
| **Navigation Actions** | Back arrow → `popBackStack()` |
| **Key UI Elements** | `TopAppBar` with back button, scrollable `Column`, nine `TermsSection`s (Acceptance of Terms, Use of Platform, NGO Responsibilities, Volunteer Responsibilities, Privacy, IP, Limitation of Liability, Changes to Terms, Contact) |
| **Owner** | |

---

## 3. Components

### 3.1 Common Components

---

#### `AppNavigation`

| Field | Detail |
|---|---|
| **File** | `components/common/AppNavigation.kt` |
| **Purpose** | Root navigation host. Defines all sealed `Screen` routes, wraps `NavHost` in `Scaffold`, switches bottom nav bar by role, handles auth-state-driven navigation. |
| **Parameters** | `authViewModel: AuthViewModel`, `onRoleChanged: (UserRole) -> Unit` |
| **Used By** | `MainActivity` |

---

#### `AppToast`

| Field | Detail |
|---|---|
| **File** | `components/common/AppToast.kt` |
| **Purpose** | Floating pill toast that slides in from the bottom, auto-dismisses after 1500 ms. Role-aware background colour. |
| **Parameters** | `message: String`, `isVisible: Boolean`, `role: UserRole`, `onDismiss: () -> Unit`, `modifier: Modifier` |
| **Used By** | `HomeScreen`, `DriveDetailScreen`, `MyApplicationsScreen`, `NgoDashboardScreen`, `CreateDriveScreen`, `ManageDrivesScreen`, `DriveApplicationsScreen`, `EditDriveScreen`, `EditVolunteerProfileScreen`, `EditNgoProfileScreen` |

---

#### `AppLoader`

| Field | Detail |
|---|---|
| **File** | `components/common/AppLoader.kt` |
| **Purpose** | Full-screen overlay with semi-transparent white scrim + centred Lottie animation. Role-aware animation asset. Fade-in/out animation. |
| **Parameters** | `isLoading: Boolean`, `role: UserRole`, `modifier: Modifier` |
| **Used By** | `HomeScreen`, `DriveDetailScreen`, `MyApplicationsScreen`, `NgoDashboardScreen`, `CreateDriveScreen`, `ManageDrivesScreen`, `DriveApplicationsScreen`, `EditDriveScreen`, `EditVolunteerProfileScreen`, `EditNgoProfileScreen` |

---

#### `ProfileHeaderCard`

| Field | Detail |
|---|---|
| **File** | `components/common/ProfileHeaderCard.kt` |
| **Purpose** | Shared profile card for both roles. Shows avatar (Coil), name, email, bio/description, and two stat values. Animated entry (fade + slide-up + spring avatar scale). |
| **Parameters** | `user: User`, `role: UserRole`, `statOneLabel: String`, `statOneValue: String`, `statTwoLabel: String`, `statTwoValue: String`, `modifier: Modifier` |
| **Used By** | `ProfileScreen`, `NgoProfileScreen` |

---

#### `LottieEmptyState`

| Field | Detail |
|---|---|
| **File** | `components/common/LottieEmptyState.kt` |
| **Purpose** | Reusable empty-state block with Lottie animation, title, subtitle, and optional action slot. |
| **Parameters** | `rawRes: Int`, `title: String`, `subtitle: String`, `modifier: Modifier`, `animationSize: Dp`, `action: @Composable (() -> Unit)?` |
| **Used By** | `HomeScreen`, `MyApplicationsScreen`, `ManageDrivesScreen`, `DriveApplicationsScreen` |

---

#### `NotificationHelper`

| Field | Detail |
|---|---|
| **File** | `components/common/NotificationHelper.kt` |
| **Purpose** | Creates Android notification channels on first run and posts system notifications. |
| **Parameters / Constants** | `CHANNEL_APPLICATIONS: String`, `CHANNEL_DRIVES: String`, `CHANNEL_REMINDERS: String` |
| **Functions** | `createNotificationChannels(context: Context)` — creates three channels; `showNotification(context, title, message, channelId, notificationId)` — builds and posts `NotificationCompat` notification |
| **Used By** | `MainActivity.onCreate`, `DriveReminderReceiver` |

---

### 3.2 Volunteer Components

---

#### `DriveCard`

| Field | Detail |
|---|---|
| **File** | `components/volunteer/DriveCard.kt` |
| **Purpose** | Tappable `ElevatedCard` summarising a drive in the home/search list. |
| **Parameters** | `drive: Drive`, `onClick: () -> Unit` |
| **Used By** | `HomeScreen`, `SearchScreen` |
| **Key UI Elements** | Left colour accent bar (colour by category), title, NGO name, spots remaining badge, date + time row, location row, indoor/outdoor icon, `SuggestionChip` (category) |

---

#### `QuoteCard`

| Field | Detail |
|---|---|
| **File** | `components/volunteer/QuoteCard.kt` |
| **Purpose** | Displays the daily inspirational quote fetched from the Quotable/ZenQuotes API. |
| **Parameters** | `quote: Quote` |
| **Used By** | `HomeScreen` |
| **Key UI Elements** | `Card` with `primaryContainer` background, large decorative quote mark, italic content (max 2 lines), right-aligned author attribution |

---

#### `WeatherCard`

| Field | Detail |
|---|---|
| **File** | `components/volunteer/WeatherCard.kt` |
| **Purpose** | Displays weather forecast for the drive's date (temperature, description, wind speed). Icon selected from WMO weather code. |
| **Parameters** | `weather: WeatherResponse` |
| **Used By** | `DriveDetailScreen` |
| **Key UI Elements** | `OutlinedCard`, "Weather on Drive Day" title, icon row (WbSunny / Cloud / Thunderstorm / Grain based on `weatherCode`), temperature + description row, wind speed row |

---

#### `VolunteerNavBar`

| Field | Detail |
|---|---|
| **File** | `components/volunteer/VolunteerNavBar.kt` |
| **Purpose** | Bottom navigation bar for the Volunteer role. |
| **Parameters** | `navController: NavController` |
| **Used By** | `AppNavigation` (shown when current route is in `volunteerTopRoutes`) |
| **Items** | Home → `volunteer_home`; Applications → `my_applications`; Map → `drive_map`; Profile → `volunteer_profile` |

---

### 3.3 NGO Components

---

#### `DriveManageCard`

| Field | Detail |
|---|---|
| **File** | `components/ngo/DriveManageCard.kt` |
| **Purpose** | Management card for a single drive shown in the NGO dashboard and manage-drives list. |
| **Parameters** | `drive: Drive`, `applicationCount: Int`, `onViewApplications: () -> Unit`, `onEdit: () -> Unit`, `onToggleStatus: () -> Unit`, `onPreview: () -> Unit` |
| **Used By** | `NgoDashboardScreen`, `ManageDrivesScreen` |
| **Key UI Elements** | Left accent bar (green = ACTIVE, grey = CLOSED), title + category `SuggestionChip` + status chip, date/location/time rows, tappable applications count row, Edit + Close `OutlinedButton`s (only shown when ACTIVE) |

---

#### `NgoNavBar`

| Field | Detail |
|---|---|
| **File** | `components/ngo/NgoNavBar.kt` |
| **Purpose** | Bottom navigation bar for the NGO role. |
| **Parameters** | `navController: NavController` |
| **Used By** | `AppNavigation` (shown when current route is in `ngoTopRoutes`) |
| **Items** | Dashboard → `ngo_dashboard`; Drives → `manage_drives`; Profile → `ngo_profile` |

---

## 4. Data Models

### `Drive.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/Drive.kt` |
| **Room** | `@Entity(tableName = "drives")` |
| **Classes / Enums** | `data class Drive`, `enum class DriveStatus` |

**`Drive` fields:**

| Field | Type | Notes |
|---|---|---|
| `driveId` | `String` | `@PrimaryKey` |
| `ngoId` | `String` | Firestore UID of the owning NGO |
| `ngoName` | `String` | Denormalised |
| `title` | `String` | |
| `description` | `String` | |
| `location` | `String` | Human-readable address |
| `date` | `String` | Format: `YYYY-MM-DD` |
| `maxVolunteers` | `Int` | |
| `currentVolunteers` | `Int` | |
| `category` | `String` | One of: Environment / Education / Health / Animal Welfare / Community |
| `status` | `DriveStatus` | Serialised via `TypeConverters` |
| `createdAt` | `Long` | Unix milliseconds |
| `bannerUrl` | `String` | Cloudinary URL (nullable / empty string) |
| `startTime` | `String` | Added in Room migration v2→3 |
| `endTime` | `String` | Added in Room migration v2→3 |

**`DriveStatus` values:** `ACTIVE`, `CLOSED`

**Usage:** Firestore `drives` collection; Room `drives` table; observed via `MainViewModel.allActiveDrives`, `ngoDrives`; `DriveCard`, `DriveManageCard`, `DriveDetailScreen`, `MapScreen`, `ContextEngine`

---

### `Application.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/Application.kt` |
| **Room** | `@Entity(tableName = "applications")` |
| **Classes / Enums** | `data class Application`, `enum class ApplicationStatus` |

**`Application` fields:**

| Field | Type | Notes |
|---|---|---|
| `applicationId` | `String` | `@PrimaryKey` |
| `driveId` | `String` | FK reference to Drive |
| `driveTitle` | `String` | Denormalised |
| `volunteerId` | `String` | Firebase Auth UID |
| `volunteerName` | `String` | Denormalised |
| `status` | `ApplicationStatus` | Serialised via `TypeConverters` |
| `appliedAt` | `Long` | Unix milliseconds |
| `message` | `String` | Optional cover message |

**`ApplicationStatus` values:** `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`

**Usage:** Firestore `applications` collection; Room `applications` table; `MainViewModel.volunteerApplications`, `ngoApplications`; `MyApplicationsScreen`, `DriveApplicationsScreen`, `ContextEngine` (history for category preference scoring)

---

### `User.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/User.kt` |
| **Room** | `@Entity(tableName = "users")` |
| **Classes / Enums** | `data class User`, `enum class UserRole` |

**`User` fields:**

| Field | Type | Notes |
|---|---|---|
| `uid` | `String` | `@PrimaryKey`; Firebase Auth UID |
| `email` | `String` | |
| `name` | `String` | Display name |
| `role` | `UserRole` | Serialised via `TypeConverters` |
| `phoneNumber` | `String` | |
| `bio` | `String` | Personal bio (Volunteer) or description (NGO) |
| `profileImageUrl` | `String` | Firebase Storage / Cloudinary URL |
| `ngoName` | `String` | NGO role only |
| `ngoDescription` | `String` | NGO role only |
| `fcmToken` | `String` | Added in Room migration v3→4; Firebase Cloud Messaging token |
| `ngoMetadata` | `String` | Added in Room migration v5→6; serialised NGO metadata |
| `ngoAddress` | `String` | Added in Room migration v6→7 |

**`UserRole` values:** `VOLUNTEER`, `NGO`

**Usage:** Firestore `users` collection; Room `users` table; `MainViewModel.currentUser`; `ProfileScreen`, `NgoProfileScreen`, `ProfileHeaderCard`, `EditVolunteerProfileScreen`, `EditNgoProfileScreen`

---

### `AppNotification.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/AppNotification.kt` |
| **Room** | None (plain data class) |
| **Classes** | `data class AppNotification` |

**`AppNotification` fields:**

| Field | Type | Notes |
|---|---|---|
| `notificationId` | `String` | Generated UUID |
| `recipientUid` | `String` | Firebase Auth UID |
| `recipientRole` | `String` | "VOLUNTEER" or "NGO" |
| `type` | `String` | One of the companion constants |
| `title` | `String` | |
| `message` | `String` | |
| `driveId` | `String` | |
| `driveName` | `String` | |
| `read` | `Boolean` | |
| `createdAt` | `Long` | Unix milliseconds |

**Companion constants:** `TYPE_APPLICATION_RECEIVED`, `TYPE_APPLICATION_APPROVED`, `TYPE_APPLICATION_REJECTED`, `TYPE_APPLICATION_WITHDRAWN`, `TYPE_DRIVE_CLOSED`, `TYPE_DRIVE_REMINDER`, `TYPE_DRIVE_CREATED`

**Usage:** Firestore `notifications` collection; `NotificationService`; `MainViewModel.notifications`; `NotificationsScreen`; `DriveReminderReceiver`

---

### `PendingAlarm.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/PendingAlarm.kt` |
| **Room** | `@Entity(tableName = "pending_alarms")` — added in Room migration v4→5 |
| **Classes** | `data class PendingAlarm` |

**`PendingAlarm` fields:**

| Field | Type | Notes |
|---|---|---|
| `alarmId` | `String` | `@PrimaryKey`; also used as PendingIntent request code (hashed) |
| `applicationId` | `String` | |
| `driveId` | `String` | |
| `driveName` | `String` | |
| `recipientUid` | `String` | Firebase Auth UID |
| `recipientRole` | `String` | |
| `triggerTimeMs` | `Long` | Absolute epoch ms when alarm should fire |
| `type` | `String` | |

**Companion:** `TYPE_24HR = "REMINDER_24HR"`

**Usage:** `AlarmScheduler`, `BootReceiver`, `DriveReminderReceiver`, `PendingAlarmDao`, `MainViewModel` (`applyToDrive`, `withdrawApplication`)

---

### `Quote.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/Quote.kt` |
| **Room** | None (plain data class) |
| **Classes** | `data class Quote` |

**`Quote` fields:**

| Field | Type | Serialised Name |
|---|---|---|
| `content` | `String` | `"q"` |
| `author` | `String` | `"a"` |
| `id` | `String` | — |
| `authorSlug` | `String` | — |
| `length` | `Int` | — |
| `tags` | `List<String>` | — |

**Usage:** `QuotableApi`, `MainViewModel.quote`, `QuoteCard`

---

### `WeatherResponse.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/WeatherResponse.kt` |
| **Room** | None (plain data class) |
| **Classes** | `data class WeatherResponse`, `data class CurrentWeather` |

**`WeatherResponse` fields:**

| Field | Type |
|---|---|
| `current` | `CurrentWeather` |

**`CurrentWeather` fields:**

| Field | Type | Notes |
|---|---|---|
| `temperature2m` | `Double` | `@SerializedName("temperature_2m")` |
| `weatherCode` | `Int` | `@SerializedName("weather_code")` — WMO code |
| `windSpeed10m` | `Double` | `@SerializedName("wind_speed_10m")` |

**Computed property:** `description: String` — maps WMO code ranges to human-readable strings (e.g., 0 → "Clear sky", 1–3 → "Partly cloudy", 95–99 → "Thunderstorm")

**Usage:** `WeatherApi`, `MainViewModel.driveWeather`, `WeatherCard`

---

### `GeocodingResponse.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/GeocodingResponse.kt` |
| **Room** | None (plain data class) |
| **Classes** | `data class GeocodingResponse` |

**`GeocodingResponse` fields:**

| Field | Type | Serialised Name |
|---|---|---|
| `name` | `String` | `"display_name"` |
| `lat` | `String` | — |
| `lon` | `String` | — |

**Usage:** `GeocodingApi`, `MainViewModel` (geocodes drive locations for `driveCoordinatesState` and distance calculation)

---

### `NgoSearchResponse.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/NgoSearchResponse.kt` |
| **Room** | None (plain data classes) |
| **Classes** | `ProjectSearchResponse`, `ProjectWrapper`, `GlobalGivingProject`, `GGOrganization`, `SearchProjectsResponse`, `SearchWrapper`, `SearchResponseBody`, `GGThemeWrapper`, `GGTheme` |

**`GlobalGivingProject` fields (key):** `id`, `title`, `summary`, `projectLink`, `organization: GGOrganization`, `themes: GGThemeWrapper`

**`GGOrganization` fields (key):** `id`, `name`, `logoUrl`, `activeProjects`, `totalProjects`

**Usage:** `GlobalGivingApi`, `MainViewModel.ngoSearchResults`, `MainViewModel.ngoModalResults`, `RegisterScreen` (NGO search dialog), `SearchScreen` (deprecated)

---

### `DummyData.kt`

| Field | Detail |
|---|---|
| **File** | `datamodels/DummyData.kt` |
| **Room** | None (static object) |
| **Purpose** | Static prototype data used before Firebase was fully wired. Now superseded by live Firestore data. |

**Constants:**

| Constant | Type | Content |
|---|---|---|
| `VOLUNTEER_USER` | `User` | uid=v1, Alex Johnson, VOLUNTEER |
| `NGO_USER` | `User` | uid=n1, Green Earth Australia, NGO |
| `DRIVES` | `List<Drive>` | 5 drives (d1–d5) across Melbourne areas |
| `APPLICATIONS` | `List<Application>` | 3 applications (a1=PENDING, a2=APPROVED, a3=REJECTED) |
| `QUOTE` | `Quote` | Gandhi quote |
| `NGO_OWN_DRIVES` | `List<Drive>` | `DRIVES.filter { it.ngoId == "n1" }` |
| `NGO_RECEIVED_APPLICATIONS` | `List<Application>` | 4 applications for d1 (na1–na4) |

---

## 5. Services

### 5.1 Local — Room

---

#### `AppDatabase.kt`

| Field | Detail |
|---|---|
| **File** | `services/local/AppDatabase.kt` |
| **Purpose** | Singleton Room database. Version 7. |
| **Entities** | `User`, `Drive`, `Application`, `PendingAlarm` |
| **Version** | 7 |

**Migration chain:**

| Version | Change |
|---|---|
| 2 → 3 | Added `startTime TEXT`, `endTime TEXT` columns to `drives` |
| 3 → 4 | Added `fcmToken TEXT` column to `users` |
| 4 → 5 | Created `pending_alarms` table |
| 5 → 6 | Added `ngoMetadata TEXT` column to `users` |
| 6 → 7 | Added `ngoAddress TEXT` column to `users` |

**Functions:**
- `getInstance(context: Context): AppDatabase` — thread-safe singleton (double-check lock)

**DAOs exposed:** `driveDao()`, `applicationDao()`, `userDao()`, `pendingAlarmDao()`

**Callers:** `MainViewModel`, `BootReceiver`, `DriveReminderReceiver`

---

#### `TypeConverters.kt`

| Field | Detail |
|---|---|
| **File** | `services/local/TypeConverters.kt` |
| **Purpose** | Room type converters for enum serialisation. |

**Converters:**

| Function | Direction |
|---|---|
| `fromUserRole(role: UserRole): String` | Enum → String |
| `toUserRole(value: String): UserRole` | String → Enum |
| `fromDriveStatus(status: DriveStatus): String` | Enum → String |
| `toDriveStatus(value: String): DriveStatus` | String → Enum |
| `fromApplicationStatus(status: ApplicationStatus): String` | Enum → String |
| `toApplicationStatus(value: String): ApplicationStatus` | String → Enum |

---

#### `DriveDao.kt`

| Field | Detail |
|---|---|
| **File** | `services/local/dao/DriveDao.kt` |
| **Purpose** | Room DAO for the `drives` table. |

**Functions:**

| Function | Description |
|---|---|
| `getAllDrives(): List<Drive>` | Returns all drives |
| `getDriveById(driveId: String): Drive?` | Returns single drive or null |
| `insertDrives(drives: List<Drive>)` | Upserts a batch (REPLACE conflict strategy) |
| `insertDrive(drive: Drive)` | Upserts a single drive |
| `clearDrives()` | Deletes all rows |

**Callers:** `MainViewModel`

---

#### `ApplicationDao.kt`

| Field | Detail |
|---|---|
| **File** | `services/local/dao/ApplicationDao.kt` |
| **Purpose** | Room DAO for the `applications` table. |

**Functions:**

| Function | Description |
|---|---|
| `getApplicationsByVolunteer(volunteerId: String): List<Application>` | Volunteer's own applications |
| `getApplicationsByDrive(driveId: String): List<Application>` | Applications for a drive |
| `insertApplications(applications: List<Application>)` | Upserts a batch |
| `insertApplication(application: Application)` | Upserts a single application |
| `deleteApplication(applicationId: String)` | Deletes by ID |
| `clearApplications()` | Deletes all rows |
| `updateStatus(applicationId: String, status: ApplicationStatus)` | Updates status field |

**Callers:** `MainViewModel`

---

#### `UserDao.kt`

| Field | Detail |
|---|---|
| **File** | `services/local/dao/UserDao.kt` |
| **Purpose** | Room DAO for the `users` table (single-user local cache). |

**Functions:**

| Function | Description |
|---|---|
| `getUser(): User?` | Returns the single cached user |
| `insertUser(user: User)` | Upserts the user (REPLACE) |
| `clearUser()` | Deletes all rows |

**Callers:** `MainViewModel`, `AuthViewModel`

---

#### `PendingAlarmDao.kt`

| Field | Detail |
|---|---|
| **File** | `services/local/dao/PendingAlarmDao.kt` |
| **Purpose** | Room DAO for the `pending_alarms` table. Survives reboots for alarm restoration. |

**Functions:**

| Function | Description |
|---|---|
| `insertAlarm(alarm: PendingAlarm)` | Upserts an alarm record |
| `deleteAlarm(alarmId: String)` | Deletes a single alarm by ID |
| `getAllAlarms(): List<PendingAlarm>` | Returns all pending alarms |
| `deleteAlarmsForDrive(driveId: String)` | Deletes all alarms for a drive |
| `deleteAlarmsByApplication(applicationId: String)` | Deletes alarms linked to an application |

**Callers:** `MainViewModel`, `BootReceiver`, `DriveReminderReceiver`

---

### 5.2 Remote — Firebase

---

#### `FirebaseService.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/firebase/FirebaseService.kt` |
| **Purpose** | Singleton holder for Firebase SDK instances and collection name constants. |

**Properties:**
- `auth: FirebaseAuth` — Firebase Auth instance
- `firestore: FirebaseFirestore` — Firestore instance
- `storage: FirebaseStorage` — Firebase Storage instance
- `currentUser: FirebaseUser?` — currently authenticated user
- `currentUserId: String?` — UID of current user

**Constants:** `USERS_COLLECTION = "users"`, `DRIVES_COLLECTION = "drives"`, `APPLICATIONS_COLLECTION = "applications"`

**Functions:**
- `isLoggedIn(): Boolean` — true if a Firebase user is authenticated

**Callers:** All Firebase service classes, `AuthViewModel`

---

#### `UserService.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/firebase/UserService.kt` |
| **Purpose** | Firestore + Firebase Auth CRUD for user accounts. |

**Functions:**

| Function | Description |
|---|---|
| `registerUser(uid, email, name, role, phoneNumber, bio, profileImageUrl, ngoName, ngoDescription): Result<User>` | Writes new user doc to Firestore |
| `loginUser(email: String, password: String): Result<User>` | Firebase Auth email/password sign-in; reads Firestore doc |
| `getCurrentUser(): Result<User>` | Reads current user's Firestore doc |
| `updateUser(user: User): Result<Unit>` | Updates Firestore user doc |
| `updateProfileImage(uid: String, imageUrl: String): Result<Unit>` | Sets `profileImageUrl` field |
| `logoutUser()` | Firebase Auth sign-out |
| `sendPasswordReset(email: String): Result<Unit>` | Sends password reset email |
| `checkGoogleUser(idToken: String): Result<User?>` | Checks if Google UID already exists in Firestore |
| `registerGoogleUser(uid, email, name, role, ngoName, ngoDescription): Result<User>` | Creates Firestore doc for Google-authenticated user |

**Callers:** `AuthViewModel`

---

#### `DriveService.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/firebase/DriveService.kt` |
| **Purpose** | Firestore CRUD for the `drives` collection. |

**Functions:**

| Function | Description |
|---|---|
| `createDrive(drive: Drive): Result<Unit>` | Writes drive document to Firestore |
| `getDriveById(driveId: String): Result<Drive>` | Fetches single drive |
| `getAllActiveDrives(): Result<List<Drive>>` | Queries `status == ACTIVE` drives |
| `getDrivesByNgo(ngoId: String): Result<List<Drive>>` | Queries by `ngoId` field |
| `updateDrive(drive: Drive): Result<Unit>` | Overwrites drive document |
| `updateDriveStatus(driveId: String, status: DriveStatus): Result<Unit>` | Updates only `status` field |
| `deleteDrive(driveId: String): Result<Unit>` | Deletes drive document |
| `incrementVolunteerCount(driveId: String): Result<Unit>` | Atomically increments `currentVolunteers` |
| `decrementVolunteerCount(driveId: String): Result<Unit>` | Atomically decrements `currentVolunteers` |

**Callers:** `MainViewModel`

---

#### `ApplicationService.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/firebase/ApplicationService.kt` |
| **Purpose** | Firestore CRUD for the `applications` collection. |

**Functions:**

| Function | Description |
|---|---|
| `applyToDrive(driveId, driveTitle, volunteerId, volunteerName, message): Result<Application>` | Creates application document |
| `getApplicationsByVolunteer(volunteerId: String): Result<List<Application>>` | Queries by `volunteerId` |
| `getApplicationsByDrive(driveId: String): Result<List<Application>>` | Queries by `driveId` |
| `updateApplicationStatus(applicationId: String, status: ApplicationStatus): Result<Unit>` | Updates `status` field |
| `withdrawApplication(applicationId: String): Result<Unit>` | Sets status to `WITHDRAWN` |
| `hasApplied(driveId: String, volunteerId: String): Result<Boolean>` | Checks if application exists |

**Callers:** `MainViewModel`

---

#### `NotificationService.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/firebase/NotificationService.kt` |
| **Purpose** | Firestore CRUD for the `notifications` collection. Supports real-time listener. |

**Functions:**

| Function | Description |
|---|---|
| `sendNotification(notification: AppNotification): Result<Unit>` | Writes notification document to Firestore |
| `listenForNotifications(uid: String, callback: (List<AppNotification>) -> Unit): ListenerRegistration` | Attaches real-time Firestore snapshot listener for a user's notifications |
| `markAsRead(notificationId: String): Result<Unit>` | Sets `read = true` on a document |

**Callers:** `MainViewModel` (`startNotificationListener`, `stopNotificationListener`, `markNotificationRead`); `DriveReminderReceiver`

---

#### `StorageService.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/firebase/StorageService.kt` |
| **Purpose** | Firebase Storage operations for profile images and drive banners. |

**Functions:**

| Function | Description |
|---|---|
| `uploadProfileImage(uid: String, imageUri: Uri): Result<String>` | Uploads to `profile_images/{uid}`, returns download URL |
| `uploadDriveBanner(driveId: String, imageUri: Uri): Result<String>` | Uploads to `drive_banners/{driveId}`, returns download URL |
| `deleteImage(imageUrl: String): Result<Unit>` | Deletes by storage URL |

**Callers:** `MainViewModel` (`saveUserProfile`, `uploadDriveBanner`)

---

### 5.3 Remote — APIs

---

#### `RetrofitClient.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/RetrofitClient.kt` |
| **Purpose** | Lazy singleton Retrofit instances for all external APIs. Adds custom headers where required. |

**Singletons:**

| Property | Base URL | Notes |
|---|---|---|
| `quotableApi: QuotableApi` | `https://zenquotes.io/api/` | — |
| `weatherApi: WeatherApi` | `https://api.open-meteo.com/` | — |
| `geocodingApi: GeocodingApi` | `https://nominatim.openstreetmap.org/` | Adds `User-Agent: VolunteerLink/1.0` |
| `globalGivingApi: GlobalGivingApi` | `https://api.globalgiving.org/` | Adds `Accept: application/json` |

---

#### `QuotableApi.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/api/QuotableApi.kt` |
| **Purpose** | Fetches a random inspirational quote from ZenQuotes. |

**Endpoints:**

| Function | Method | Path | Returns |
|---|---|---|---|
| `getRandomQuote()` | GET | `random` | `List<Quote>` |

**Callers:** `MainViewModel.refreshQuote`

---

#### `WeatherApi.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/api/WeatherApi.kt` |
| **Purpose** | Fetches weather forecast data from Open-Meteo. |

**Endpoints:**

| Function | Method | Path | Parameters |
|---|---|---|---|
| `getWeather(latitude, longitude, current, timezone)` | GET | `v1/forecast` | Returns `WeatherResponse` |

**Callers:** `MainViewModel.loadDriveWeatherAndDistance`

---

#### `GeocodingApi.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/api/GeocodingApi.kt` |
| **Purpose** | Geocodes a human-readable address to lat/lon via Nominatim. |

**Endpoints:**

| Function | Method | Path | Parameters |
|---|---|---|---|
| `geocode(address: String, format: String, limit: Int)` | GET | `search` | Returns `List<GeocodingResponse>` |

**Callers:** `MainViewModel` (geocodes drive locations for `driveCoordinatesState`, weather, and distance)

---

#### `GlobalGivingApi.kt`

| Field | Detail |
|---|---|
| **File** | `services/remote/api/GlobalGivingApi.kt` |
| **Purpose** | Searches GlobalGiving for NGO projects by theme or keyword. |

**Endpoints:**

| Function | Method | Path | Returns |
|---|---|---|---|
| `getProjectsByTheme(theme: String, apiKey: String)` | GET | `api/public/projectservice/themes/{theme}/projects/active` | `ProjectSearchResponse` |
| `searchProjects(apiKey: String, keyword: String)` | GET | `api/public/services/search/projects` | `SearchProjectsResponse` |

**Callers:** `MainViewModel.searchGlobalGivingByCategory`, `MainViewModel.searchNgoModal`

---

### 5.4 Background / System

---

#### `LocationUpdateWorker.kt`

| Field | Detail |
|---|---|
| **File** | `services/LocationUpdateWorker.kt` |
| **Purpose** | `CoroutineWorker` that advances the `LocationSimulator` by one CSV row every 15 minutes. |

**Functions:**

| Function | Description |
|---|---|
| `doWork(): Result` | Calls `LocationSimulator.initIfNeeded(applicationContext)` then `LocationSimulator.advance()`; returns `Result.success()` |

**Callers:** `MainActivity.onCreate` (enqueued as `PeriodicWorkRequest` with 15-minute interval, `KEEP` policy)

---

#### `AlarmScheduler.kt`

| Field | Detail |
|---|---|
| **File** | `services/AlarmScheduler.kt` |
| **Purpose** | Schedules and cancels `AlarmManager` exact alarms for drive reminders. |

**Functions:**

| Function | Description |
|---|---|
| `schedule(context: Context, alarm: PendingAlarm)` | Creates `PendingIntent` targeting `DriveReminderReceiver` with all alarm extras; calls `setExactAndAllowWhileIdle` (falls back to `setAndAllowWhileIdle` if exact alarm permission denied) |
| `cancel(context: Context, alarmId: String)` | Retrieves same `PendingIntent` and calls `AlarmManager.cancel` |
| `calculate24HrBeforeMs(dateStr: String): Long` | **⚠️ TEST OVERRIDE ACTIVE:** currently returns `System.currentTimeMillis() + 30_000L` (30-second delay for testing). Must be restored to parse `dateStr` and return `driveDate - 24 hours` before submission. |

**Callers:** `MainViewModel` (`applyToDrive`, `withdrawApplication`), `BootReceiver`

---

#### `BootReceiver.kt`

| Field | Detail |
|---|---|
| **File** | `services/BootReceiver.kt` |
| **Purpose** | `BroadcastReceiver` that re-schedules all pending alarms after device reboot. |

**Functions:**

| Function | Description |
|---|---|
| `onReceive(context, intent)` | Filters for `ACTION_BOOT_COMPLETED`; reads all `PendingAlarm` rows from Room; re-schedules alarms with future `triggerTimeMs` via `AlarmScheduler.schedule`; deletes stale alarms |

**Registered in:** `AndroidManifest.xml` with `RECEIVE_BOOT_COMPLETED` permission

---

#### `DriveReminderReceiver.kt`

| Field | Detail |
|---|---|
| **File** | `services/DriveReminderReceiver.kt` |
| **Purpose** | `BroadcastReceiver` that fires when an alarm triggers. Posts a system notification and writes an `AppNotification` to Firestore. |

**Functions:**

| Function | Description |
|---|---|
| `onReceive(context, intent)` | Extracts `alarmId`, `driveId`, `driveName`, `recipientUid`, `recipientRole` from intent extras; calls `NotificationHelper.showNotification`; on IO dispatcher: calls `NotificationService.sendNotification` and `PendingAlarmDao.deleteAlarm` |

**Registered in:** `AndroidManifest.xml` as exported receiver for alarm intents

---

## 6. ViewModels

### `AuthViewModel.kt`

| Field | Detail |
|---|---|
| **File** | `services/viewmodel/AuthViewModel.kt` |
| **Extends** | `ViewModel` |

**Sealed class `AuthState`:**

| State | Fields |
|---|---|
| `Loading` | — |
| `LoggedIn` | `user: User` |
| `LoggedOut` | — |
| `Error` | `message: String` |

**Data class `PendingGoogleUser`:**

| Field | Type |
|---|---|
| `uid` | `String` |
| `email` | `String` |
| `name` | `String` |

**StateFlows:**

| StateFlow | Type | Description |
|---|---|---|
| `authState` | `StateFlow<AuthState>` | Current authentication state |
| `pendingGoogleUser` | `StateFlow<PendingGoogleUser?>` | Populated when Google Sign-In completes but registration is needed |

**Public Functions:**

| Function | Description |
|---|---|
| `login(email, password)` | Firebase Auth email/password sign-in via `UserService.loginUser`; emits `LoggedIn` or `Error` |
| `register(email, password, name, role, phoneNumber, bio, profileImageUrl, ngoName, ngoDescription)` | Creates Auth user + Firestore doc; emits `LoggedIn` |
| `signOut()` | Signs out; emits `LoggedOut` |
| `clearError()` | Resets `authState` to `LoggedOut` |
| `updateCurrentUser(user: User)` | Updates `authState` with new user data (used after profile save) |
| `clearPendingGoogleUser()` | Clears `pendingGoogleUser` |
| `signInWithGoogle(idToken: String, context: Context)` | Checks if Google user exists; if yes emits `LoggedIn`; if no populates `pendingGoogleUser` |
| `registerFcmToken(uid: String)` | Gets current FCM token and saves to Firestore user doc |
| `completeGoogleRegistration(name, role, ngoName, ngoDescription)` | Creates Firestore doc for pending Google user; emits `LoggedIn` |

**Private Functions:**

| Function | Description |
|---|---|
| `checkAuthState()` | On init: checks `FirebaseService.currentUser`; loads from Firestore; emits `LoggedIn` or `LoggedOut` |

**Observed by:** `AppNavigation`, `LoginScreen`, `RegisterScreen`, `HomeScreen`, `DriveDetailScreen`, `MyApplicationsScreen`, `ProfileScreen`, `EditVolunteerProfileScreen`, `NgoDashboardScreen`, `CreateDriveScreen`, `ManageDrivesScreen`, `NgoProfileScreen`, `EditNgoProfileScreen`, `MainActivity`

---

### `MainViewModel.kt`

| Field | Detail |
|---|---|
| **File** | `services/viewmodel/MainViewModel.kt` |
| **Extends** | `AndroidViewModel` |

**StateFlows:**

| StateFlow | Type | Description |
|---|---|---|
| `notifications` | `StateFlow<List<AppNotification>>` | All notifications for current user |
| `unreadCount` | `StateFlow<Int>` | Count of `read == false` notifications |
| `ngoDrives` | `StateFlow<List<Drive>>` | Drives owned by the logged-in NGO |
| `ngoApplications` | `StateFlow<List<Application>>` | Applications for NGO's drives |
| `isLoading` | `StateFlow<Boolean>` | Global loading flag |
| `errorMessage` | `StateFlow<String?>` | Error message for `AppToast` |
| `successMessage` | `StateFlow<String?>` | Success message for `AppToast` |
| `updatedUser` | `StateFlow<User?>` | Emitted after successful profile save |
| `currentUser` | `StateFlow<User?>` | Currently logged-in user |
| `allActiveDrives` | `StateFlow<List<Drive>>` | All ACTIVE drives (volunteer home + map) |
| `quote` | `StateFlow<Quote?>` | Current daily quote |
| `volunteerApplications` | `StateFlow<List<Application>>` | Applications by the logged-in volunteer |
| `searchResults` | `StateFlow<List<Drive>>` | Results from drive search (deprecated SearchScreen) |
| `isRefreshing` | `StateFlow<Boolean>` | Pull-to-refresh indicator |
| `profileUpdateSuccess` | `StateFlow<Boolean>` | Triggers navigation back after profile save |
| `ngoSearchResults` | `StateFlow<List<GlobalGivingProject>>` | GlobalGiving results for category search |
| `driveWeather` | `StateFlow<WeatherResponse?>` | Weather for the selected drive's date |
| `driveDistance` | `StateFlow<Double?>` | Distance in km from simulated location to drive |
| `ngoModalResults` | `StateFlow<List<GlobalGivingProject>>` | GlobalGiving results for NGO search modal |
| `ngoModalLoading` | `StateFlow<Boolean>` | Loading flag for NGO modal |
| `ngoModalError` | `StateFlow<String?>` | Error for NGO modal |
| `driveCoordinatesState` | `StateFlow<Map<String, Pair<Double, Double>>>` | Map of driveId → (lat, lon) geocoded from drive address |
| `contextRankedDrives` | `StateFlow<List<Drive>>` | Drives re-ranked by `ContextEngine` |

**Public Functions:**

| Function | Description |
|---|---|
| `loadNgoDashboard(ngoId: String)` | Loads NGO's drives + all applications; caches to Room |
| `createDrive(drive: Drive)` | Writes to Firestore; caches to Room; triggers notifications |
| `updateDriveStatus(driveId: String, status: DriveStatus)` | Updates Firestore + Room |
| `deleteDrive(driveId: String)` | Deletes from Firestore + Room |
| `closeDrive(driveId: String)` | Sets status to CLOSED in Firestore + Room; sends notifications to approved applicants |
| `expirePassedDrives(drives: List<Drive>)` | Closes drives whose date is in the past |
| `updateApplicationStatus(applicationId: String, status: ApplicationStatus)` | Updates Firestore + Room; sends notification to volunteer |
| `uploadDriveBanner(imageUri: Uri, context: Context, onResult: (String?) -> Unit)` | Uploads via Cloudinary (unsigned preset) on `Dispatchers.IO`; returns URL via callback |
| `updateDrive(drive: Drive)` | Overwrites drive in Firestore + Room |
| `loadVolunteerHome(uid: String)` | Loads active drives + volunteer applications; geocodes drives; runs `ContextEngine` ranking |
| `refreshVolunteerHome(uid: String)` | Pull-to-refresh version of `loadVolunteerHome` |
| `refreshNgoDashboard(ngoId: String)` | Pull-to-refresh version of `loadNgoDashboard` |
| `refreshDriveApplications(driveId: String)` | Pull-to-refresh for `DriveApplicationsScreen` |
| `saveUserProfile(user: User)` | Updates Firestore + Room; emits `profileUpdateSuccess` |
| `loadDriveApplications(driveId: String)` | Loads applications for one drive (non-refreshing) |
| `searchDrives(query: String, category: String)` | Filters `allActiveDrives` by title/NGO + category |
| `searchGlobalGivingByCategory(category: String)` | Calls `GlobalGivingApi.getProjectsByTheme` |
| `withdrawApplication(applicationId: String, driveId: String)` | Sets application to WITHDRAWN; decrements drive volunteer count; cancels alarm; sends notification |
| `applyToDrive(driveId, driveTitle, volunteerId, volunteerName, message)` | Creates application; increments drive count; schedules 24hr alarm; sends notification to NGO |
| `refreshDrives()` | Re-fetches all active drives |
| `refreshMyApplications(uid: String)` | Re-fetches volunteer's applications |
| `refreshCurrentUser(uid: String)` | Reloads user from Firestore + Room |
| `refreshNgoDrives(ngoId: String)` | Re-fetches NGO drives |
| `refreshApplicationsForDrive(driveId: String)` | Re-fetches applications for a drive |
| `loadDriveWeatherAndDistance(drive: Drive)` | Geocodes drive address; fetches Open-Meteo weather; calculates Haversine distance from simulated location |
| `clearDriveWeather()` | Resets `driveWeather` and `driveDistance` to null |
| `searchNgoModal(query: String)` | Calls `GlobalGivingApi.searchProjects` for NGO search modal |
| `clearNgoModal()` | Resets `ngoModalResults`, `ngoModalLoading`, `ngoModalError` |
| `updateContextRanking(uid: String)` | Runs `ContextEngine.rankDrives` with current location + applications |
| `clearMessages()` | Sets `errorMessage` and `successMessage` to null |
| `clearProfileUpdateSuccess()` | Sets `profileUpdateSuccess` to false |
| `refreshQuote()` | Calls `QuotableApi.getRandomQuote`; emits to `quote` |
| `startNotificationListener(uid: String)` | Attaches Firestore real-time listener via `NotificationService` |
| `stopNotificationListener()` | Removes Firestore listener registration |
| `markNotificationRead(notificationId: String)` | Marks notification read in Firestore; updates local list |

---

## 7. Navigation

All routes are sealed class objects in `components/common/AppNavigation.kt`.

| Route Object | Path | Bottom Nav | Role Access | Composable |
|---|---|---|---|---|
| `Screen.Login` | `login` | No | Any | `LoginScreen` |
| `Screen.Register` | `register` | No | Any | `RegisterScreen` |
| `Screen.VolunteerHome` | `volunteer_home` | Yes (Volunteer) | VOLUNTEER | `HomeScreen` |
| `Screen.DriveDetail` | `drive_detail/{driveId}` | No | VOLUNTEER | `DriveDetailScreen` |
| `Screen.Search` | `search` | No | VOLUNTEER | `SearchScreen` *(no composable registered — deprecated)* |
| `Screen.MyApplications` | `my_applications` | Yes (Volunteer) | VOLUNTEER | `MyApplicationsScreen` |
| `Screen.VolunteerProfile` | `volunteer_profile` | Yes (Volunteer) | VOLUNTEER | `ProfileScreen` |
| `Screen.EditVolunteerProfile` | `edit_volunteer_profile` | No | VOLUNTEER | `EditVolunteerProfileScreen` |
| `Screen.NgoDashboard` | `ngo_dashboard` | Yes (NGO) | NGO | `NgoDashboardScreen` |
| `Screen.CreateDrive` | `create_drive` | Yes (NGO) | NGO | `CreateDriveScreen` |
| `Screen.ManageDrives` | `manage_drives` | Yes (NGO) | NGO | `ManageDrivesScreen` |
| `Screen.NgoProfile` | `ngo_profile` | Yes (NGO) | NGO | `NgoProfileScreen` |
| `Screen.NgoApplications` | `ngo_applications/{driveId}` | No | NGO | `DriveApplicationsScreen` |
| `Screen.DriveConfirmation` | `drive_confirmation` | No | NGO | `DriveConfirmationScreen` |
| `Screen.EditDrive` | `edit_drive/{driveId}` | No | NGO | `EditDriveScreen` |
| `Screen.EditNgoProfile` | `edit_ngo_profile` | No | NGO | `EditNgoProfileScreen` |
| `Screen.AboutUs` | `about_us` | No | Any | `AboutUsScreen` |
| `Screen.ContactUs` | `contact_us` | No | Any | `ContactUsScreen` |
| `Screen.TermsConditions` | `terms_conditions` | No | Any | `TermsConditionsScreen` |
| `Screen.Notifications` | `notifications` | No | Any | `NotificationsScreen` |
| `Screen.DriveMap` | `drive_map` | Yes (Volunteer) | VOLUNTEER | `MapScreen` |

**Bottom nav sets (defined in `AppNavigation`):**

- `volunteerTopRoutes`: `volunteer_home`, `my_applications`, `volunteer_profile`, `drive_map`
- `ngoTopRoutes`: `ngo_dashboard`, `create_drive`, `manage_drives`, `ngo_profile`

**Auth-state navigation (handled by `LaunchedEffect` in `AppNavigation`):**

- `AuthState.LoggedIn` + `VOLUNTEER` → navigate to `volunteer_home`, clear back stack
- `AuthState.LoggedIn` + `NGO` → navigate to `ngo_dashboard`, clear back stack
- `AuthState.LoggedOut` + `pendingGoogleUser != null` → navigate to `register`, clear back stack
- `AuthState.LoggedOut` → navigate to `login`, clear back stack

---

## 8. Context-Awareness

The context-awareness subsystem ranks volunteer drives by combining sensory (GPS proximity) and non-sensory (time-of-day, application history) signals. It consists of three components:

---

### `LocationSimulator.kt`

| Field | Detail |
|---|---|
| **File** | `services/LocationSimulator.kt` |
| **Type** | `object` (singleton) |
| **Purpose** | Replays real GPS trace from `assets/geolife_melbourne.csv` to simulate volunteer movement. Emits the current simulated position as a `StateFlow`. |

**Inner class:**

```
data class SimulatedLocation(
    latitude: Double,
    longitude: Double,
    altitude: Double,
    date: String,
    time: String
)
```

**StateFlows:**

| StateFlow | Type | Description |
|---|---|---|
| `currentLocation` | `StateFlow<SimulatedLocation?>` | Currently active simulated GPS position (public) |

**Functions:**

| Function | Description |
|---|---|
| `loadFromAssets(context: Context)` | Reads `geolife_melbourne.csv` from assets; parses each row into `SimulatedLocation`; stores in internal list |
| `advance()` | Increments internal index; emits next `SimulatedLocation` to `currentLocation` |
| `getCurrentLocation(): SimulatedLocation?` | Returns current value without advancing |
| `initIfNeeded(context: Context)` | Calls `loadFromAssets` if list is empty; emits first point to `currentLocation` |

**Interconnections:**
- Called by `LocationUpdateWorker.doWork()` every 15 min
- Called by `MainActivity.onCreate` for initial load
- Observed by `MapScreen`, `HomeScreen`, `MainViewModel.loadVolunteerHome`, `MainViewModel.loadDriveWeatherAndDistance`, `MainViewModel.updateContextRanking`

---

### `ContextEngine.kt`

| Field | Detail |
|---|---|
| **File** | `services/ContextEngine.kt` |
| **Type** | `object` (singleton) |
| **Purpose** | Scores and re-ranks a list of drives using a multi-factor scoring model combining proximity, time-of-day, category preference from history, and spots urgency. |

**Functions:**

| Function | Description |
|---|---|
| `distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double` | Haversine great-circle distance formula; returns distance in kilometres |
| `getTimePeriod(): String` | Returns `"morning"` (6–12), `"afternoon"` (12–17), `"evening"` (17–21), or `"night"` (21–6) based on current system hour |
| `getPreferredCategories(applications: List<Application>): List<String>` | Groups applications by drive category; returns top 3 most-applied-to categories |
| `rankDrives(drives: List<Drive>, location: SimulatedLocation?, applications: List<Application>, driveCoordinates: Map<String, Pair<Double, Double>>): List<Drive>` | Scores each drive and returns sorted descending list |

**Scoring model (per drive, max ~90 points):**

| Signal | Max Points | Logic |
|---|---|---|
| Proximity | 40 | 40 if < 5 km; 25 if < 15 km; 10 if < 30 km; 0 otherwise |
| Time-of-day | 5–20 | 20 if drive category matches time period (e.g., morning → "Health"); otherwise 5 |
| Category preference | 20 | 20 if category is in top-3 history categories; 10 if category appears in history at all; 0 otherwise |
| Spots urgency | 10 | 10 if ≥ 80% full; 5 if ≥ 50% full; 0 otherwise |

**Interconnections:**
- Called by `MainViewModel.loadVolunteerHome` and `MainViewModel.updateContextRanking`
- Uses `LocationSimulator.currentLocation` value (passed in as parameter)
- Uses `MainViewModel.driveCoordinatesState` (passed in as parameter)
- Output flows into `MainViewModel.contextRankedDrives` → observed by `HomeScreen`

---

### `LocationUpdateWorker.kt`

| Field | Detail |
|---|---|
| **File** | `services/LocationUpdateWorker.kt` |
| **Type** | `CoroutineWorker` |
| **Purpose** | WorkManager periodic job that advances the GPS simulator and implicitly triggers context re-ranking on the next UI observation. |

**Functions:**

| Function | Description |
|---|---|
| `doWork(): Result` | Calls `LocationSimulator.initIfNeeded(applicationContext)` to ensure CSV is loaded; calls `LocationSimulator.advance()` to move to the next GPS point; returns `Result.success()` |

**Schedule:** Enqueued in `MainActivity.onCreate` as `PeriodicWorkRequest` with a 15-minute interval, `ExistingPeriodicWorkPolicy.KEEP` (only one instance runs at a time).

**Interconnections:**
- Input: `LocationSimulator` (mutates internal state)
- Output: `LocationSimulator.currentLocation` StateFlow update → propagates to all collectors (`MapScreen`, `MainViewModel`)

---

## 9. Task Allocation Table

| Screen / Feature | File(s) | Complexity | Assigned To | Status |
|---|---|---|---|---|
| Login Screen | `screens/common/LoginScreen.kt`, `services/viewmodel/AuthViewModel.kt` | Medium | | |
| Register Screen | `screens/common/RegisterScreen.kt`, `services/viewmodel/AuthViewModel.kt` | Medium | | |
| Volunteer Home Screen | `screens/volunteer/VolunteerHomeScreen.kt` | Medium | | |
| Drive Detail Screen | `screens/volunteer/DriveDetailScreen.kt` | High | | |
| Drive Map Screen | `screens/volunteer/MapScreen.kt` | High | | |
| My Applications Screen | `screens/volunteer/MyApplicationsScreen.kt` | Medium | | |
| Volunteer Profile Screen | `screens/volunteer/ProfileScreen.kt` | Low | | |
| Edit Volunteer Profile | `screens/volunteer/EditVolunteerProfileScreen.kt` | Low | | |
| Search Screen (Deprecated) | `screens/volunteer/SearchScreen.kt` | Low | | |
| NGO Dashboard Screen | `screens/ngo/NgoHomeScreen.kt` | Medium | | |
| Create Drive Wizard | `screens/ngo/CreateDriveScreen.kt` | High | | |
| Manage Drives Screen | `screens/ngo/ManageDrivesScreen.kt` | Medium | | |
| Drive Applications Screen | `screens/ngo/DriveApplicationsScreen.kt` | Medium | | |
| Edit Drive Screen | `screens/ngo/EditDriveScreen.kt` | Medium | | |
| NGO Profile Screen | `screens/ngo/NgoProfileScreen.kt` | Low | | |
| Edit NGO Profile | `screens/ngo/EditNgoProfileScreen.kt` | Low | | |
| Drive Confirmation Screen | `screens/ngo/DriveConfirmationScreen.kt` | Low | | |
| Notifications Screen | `screens/common/NotificationsScreen.kt` | Medium | | |
| About Us Screen | `screens/company/AboutUsScreen.kt` | Low | | |
| Contact Us Screen | `screens/company/ContactUsScreen.kt` | Low | | |
| Terms & Conditions Screen | `screens/company/TermsConditionsScreen.kt` | Low | | |
| AppNavigation + Route Definitions | `components/common/AppNavigation.kt` | High | | |
| AppToast Component | `components/common/AppToast.kt` | Low | | |
| AppLoader Component | `components/common/AppLoader.kt` | Low | | |
| ProfileHeaderCard Component | `components/common/ProfileHeaderCard.kt` | Medium | | |
| LottieEmptyState Component | `components/common/LottieEmptyState.kt` | Low | | |
| NotificationHelper Component | `components/common/NotificationHelper.kt` | Low | | |
| DriveCard Component | `components/volunteer/DriveCard.kt` | Low | | |
| QuoteCard Component | `components/volunteer/QuoteCard.kt` | Low | | |
| WeatherCard Component | `components/volunteer/WeatherCard.kt` | Low | | |
| VolunteerNavBar Component | `components/volunteer/VolunteerNavBar.kt` | Low | | |
| DriveManageCard Component | `components/ngo/DriveManageCard.kt` | Medium | | |
| NgoNavBar Component | `components/ngo/NgoNavBar.kt` | Low | | |
| Drive Data Model + Room | `datamodels/Drive.kt`, `services/local/dao/DriveDao.kt` | Low | | |
| Application Data Model + Room | `datamodels/Application.kt`, `services/local/dao/ApplicationDao.kt` | Low | | |
| User Data Model + Room | `datamodels/User.kt`, `services/local/dao/UserDao.kt` | Low | | |
| AppNotification Data Model | `datamodels/AppNotification.kt` | Low | | |
| PendingAlarm Data Model + Room | `datamodels/PendingAlarm.kt`, `services/local/dao/PendingAlarmDao.kt` | Low | | |
| Room Database + Migrations | `services/local/AppDatabase.kt`, `services/local/TypeConverters.kt` | Medium | | |
| Firebase Auth + User Service | `services/remote/firebase/UserService.kt`, `services/viewmodel/AuthViewModel.kt` | High | | |
| Firebase Drive Service | `services/remote/firebase/DriveService.kt` | Medium | | |
| Firebase Application Service | `services/remote/firebase/ApplicationService.kt` | Medium | | |
| Firebase Notification Service | `services/remote/firebase/NotificationService.kt` | Medium | | |
| Firebase Storage Service | `services/remote/firebase/StorageService.kt` | Low | | |
| Quotable / ZenQuotes API | `services/remote/api/QuotableApi.kt`, `datamodels/Quote.kt` | Low | | |
| Weather API (Open-Meteo) | `services/remote/api/WeatherApi.kt`, `datamodels/WeatherResponse.kt` | Low | | |
| Geocoding API (Nominatim) | `services/remote/api/GeocodingApi.kt`, `datamodels/GeocodingResponse.kt` | Low | | |
| GlobalGiving API | `services/remote/api/GlobalGivingApi.kt`, `datamodels/NgoSearchResponse.kt` | Medium | | |
| MainViewModel | `services/viewmodel/MainViewModel.kt` | High | | |
| LocationSimulator | `services/LocationSimulator.kt` | High | | |
| ContextEngine | `services/ContextEngine.kt` | High | | |
| LocationUpdateWorker (WorkManager) | `services/LocationUpdateWorker.kt` | Medium | | |
| AlarmScheduler + 24hr Reminder | `services/AlarmScheduler.kt` | High | | |
| BootReceiver | `services/BootReceiver.kt` | Medium | | |
| DriveReminderReceiver | `services/DriveReminderReceiver.kt` | Medium | | |
| Theme + Colour System | `ui/Theme.kt`, `ui/Color.kt`, `ui/Type.kt` | Medium | | |
| MainActivity | `MainActivity.kt` | Medium | | |
