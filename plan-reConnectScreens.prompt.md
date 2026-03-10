# ReConnect — Full Implementation Plan

Implement the ReConnect app: 4 screens (Onboarding, Contact Picker, Home Dashboard, Person Detail) with Material 3 Expressive design, contacts permission flow, and Clean Architecture.

---

## Current State

- **Scaffold app** with `NavigationSuiteScaffold`, 3 placeholder tabs (Home/Favorites/Profile), and a "Hello Android" greeting.
- **Dependencies already synced** (Compose BOM `2026.02.01`, Kotlin `2.3.10`, Coil 3, Material Icons Extended, ViewModel Compose, Navigation Suite).
- **Theme** is default purple — needs complete overhaul to warm amber/navy palette from mockups.
- **No data layer**, no ViewModels, no real screens yet.

## Design Tokens (from Mockup Analysis)

### Colors
| Token                | Hex         | Usage                                      |
|----------------------|-------------|---------------------------------------------|
| `GoldPrimary`        | `#D4A843`   | Buttons, "ReConnect" title, accents         |
| `GoldDark`           | `#B8922F`   | Subtitle text, pressed states               |
| `GoldLight`          | `#E8C96A`   | Highlights                                  |
| `NavyDark`           | `#1C2B3A`   | Bottom nav background, dark text            |
| `NavyMedium`         | `#2C3E50`   | Secondary dark                              |
| `CreamBackground`    | `#FFF9F0`   | App background                              |
| `CreamLight`         | `#FFFDF8`   | Surface/cards                               |
| `AmberCardStart`     | `#FFF3D6`   | Birthday card gradient start                |
| `AmberCardEnd`       | `#FFE4A8`   | Birthday card gradient end                  |
| `BlueCard`           | `#D6E4F5`   | Catch-up / timeline cards                   |
| `ActiveGreen`        | `#4CAF50`   | Online status dot                           |
| `CoralLabel`         | `#D4724A`   | "BIRTHDAY BASH" label                       |
| `CharcoalText`       | `#1A1A2E`   | Primary body text                           |
| `CardYellowLight`    | `#FFF8E1`   | "Next Talk" bento card                      |

### Typography
- **Serif** (`FontFamily.Serif`) → Display, Headline, and large Title roles (bold, 20–40sp).
- **Sans-serif** (`FontFamily.Default`) → Body, Label, small Title roles.
- Display Large: Serif Bold 40sp (e.g., "Sarah Jenkins", "Upcoming Connections").
- Headline Medium: Serif Bold 22sp (e.g., "Find your inner circle", "Eleanor Vance").
- Body Large: Sans 16sp (descriptions, notes).
- Label Large: Sans SemiBold 14sp, 0.5sp tracking (uppercase section headers like "BIRTHDAY BASH", "QUICK CATCH-UPS").

### Shapes
- Large containers/cards: `RoundedCornerShape(28.dp)`
- Medium cards: `RoundedCornerShape(20.dp)`
- Small chips/buttons: `RoundedCornerShape(12.dp)`
- Avatars/FAB: `CircleShape`

---

## Architecture

```
dev.pranav.reconnect/
├── MainActivity.kt                    # Entry point, routing, NavigationSuiteScaffold
├── data/
│   ├── model/
│   │   ├── Contact.kt                 # Contact data class
│   │   ├── ReconnectInterval.kt       # Enum: WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
│   │   ├── UpcomingEvent.kt           # Sealed class: Birthday, CatchUp, TimelineReminder
│   │   └── PastMoment.kt             # Data class + MomentCategory enum
│   └── repository/
│       └── ContactRepository.kt       # Device contacts reader + sample data provider
├── ui/
│   ├── theme/
│   │   ├── Color.kt                   # Overhaul → ReConnect palette
│   │   ├── Type.kt                    # Overhaul → Serif/Sans dual-font
│   │   └── Theme.kt                   # Overhaul → custom lightColorScheme, Shapes
│   ├── navigation/
│   │   └── AppRoute.kt               # Sealed class for app routes
│   ├── onboarding/
│   │   └── OnboardingScreen.kt        # Permission rationale screen
│   ├── picker/
│   │   ├── ContactPickerScreen.kt     # Select important contacts + set intervals
│   │   └── ContactPickerViewModel.kt  # Loads device contacts, manages selection state
│   ├── home/
│   │   ├── HomeScreen.kt             # Dashboard with event cards + quick catch-ups
│   │   └── HomeViewModel.kt          # Exposes HomeUiState
│   └── detail/
│       ├── PersonDetailScreen.kt      # Contact profile, next talk, past moments
│       └── PersonDetailViewModel.kt   # Exposes PersonDetailUiState
```

### Navigation Strategy
- **No `navigation-compose` dependency** — `NavigationSuiteScaffold` handles the adaptive bottom nav chrome.
- Routing is managed via a mutable state (`currentRoute: AppRoute`) in `MainActivity`/`ReConnectApp`.
- A `backStack: SnapshotStateList<AppRoute>` supports `BackHandler` for back navigation.
- `NavigationSuiteScaffold` is only shown when `currentRoute` is a main-app route (Home, Circle, Events, Settings), hidden during Onboarding and Contact Picker.
- Person Detail is pushed onto the backstack from Home; back pops to Home.

### State Management
- Each screen has an immutable `UiState` data class.
- ViewModels expose `StateFlow<UiState>` collected via `collectAsState()`.
- State hoisting: screens are stateless composables receiving state + event lambdas.

---

## Step-by-Step Implementation

### Step 1 — AndroidManifest: Add Contacts Permission

**File:** `app/src/main/AndroidManifest.xml`

Add `<uses-permission android:name="android.permission.READ_CONTACTS"/>` before the `<application>` tag.

---

### Step 2 — Theme Overhaul

#### 2a — `Color.kt`
Replace the purple palette entirely with the ReConnect tokens listed above. Define all colors as top-level `val` properties.

#### 2b — `Type.kt`
Define `SerifFontFamily = FontFamily.Serif` and `SansFontFamily = FontFamily.Default`. Build a full `Typography(...)` mapping:
- `displayLarge` / `displayMedium` / `displaySmall` → Serif Bold, 40/34/28sp
- `headlineLarge` / `headlineMedium` / `headlineSmall` → Serif Bold/SemiBold, 26/22/20sp
- `titleLarge` / `titleMedium` / `titleSmall` → Sans SemiBold/Medium, 18/16/14sp
- `bodyLarge` / `bodyMedium` / `bodySmall` → Sans Normal, 16/14/12sp
- `labelLarge` / `labelMedium` / `labelSmall` → Sans SemiBold/Medium, 14/12/10sp with 0.5sp tracking

#### 2c — `Theme.kt`
- Remove `dynamicColor` and dark theme logic — single `lightColorScheme` using ReConnect tokens.
- Wire `primary = GoldPrimary`, `onPrimary = White`, `secondary = NavyDark`, `background = CreamBackground`, `surface = CreamLight`, etc.
- Add `ReConnectShapes` with `RoundedCornerShape` values (12/20/28/32dp).
- Simplify `ReConnectTheme` to: `MaterialTheme(colorScheme, typography, shapes, content)`.

---

### Step 3 — Data Models

#### 3a — `Contact.kt`
```kotlin
data class Contact(
    val id: String,
    val name: String,
    val title: String = "",
    val relationship: String = "",
    val photoUri: String? = null,
    val phoneNumber: String = "",
    val isActive: Boolean = false,
    val isImportant: Boolean = false,
    val reconnectInterval: ReconnectInterval = ReconnectInterval.MONTHLY
)
```

#### 3b — `ReconnectInterval.kt`
```kotlin
enum class ReconnectInterval(val label: String, val days: Int) {
    WEEKLY("Weekly", 7),
    BIWEEKLY("Biweekly", 14),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}
```

#### 3c — `UpcomingEvent.kt`
Sealed class with three subtypes:
- `Birthday` — `contactName`, `contactId`, `age: Int`, `day: Int`, `month: String`, `note: String`
- `CatchUp` — `contactName`, `contactId`, `day: Int`, `dayOfWeek: String`
- `TimelineReminder` — `contactName`, `contactId`, `duration: String`, `actionLabel: String`

#### 3d — `PastMoment.kt`
```kotlin
enum class MomentCategory { DINING, ART, OUTDOORS, GENERAL }

data class PastMoment(
    val id: String,
    val title: String,
    val description: String,
    val dateLabel: String,
    val category: MomentCategory,
    val imageUris: List<String> = emptyList()
)
```

---

### Step 4 — Repository

**File:** `data/repository/ContactRepository.kt`

- `getDeviceContacts(contentResolver: ContentResolver): List<Contact>` — queries `ContactsContract.CommonDataKinds.Phone`, deduplicates by contact ID, returns sorted by display name.
- `getSampleContacts(): List<Contact>` — hardcoded Eleanor Vance, Sarah Jenkins, Mark, David Chen, Elena Rodriguez, Mom.
- `getSampleUpcomingEvents(): List<UpcomingEvent>` — Birthday Bash (Sarah, March 12, age 28), CatchUp (Mark, 15 Wednesday), TimelineReminder (Mom, 3 months).
- `getSampleQuickCatchUps(): List<Pair<Contact, String>>` — David Chen ("Last spoke: 2 weeks ago"), Elena Rodriguez ("Suggested: Afternoon tea").
- `getSamplePastMoments(): List<PastMoment>` — Dinner at Rosso's, Gallery Opening (with image URIs), Park Morning Walk.

---

### Step 5 — Navigation Route Definitions

**File:** `ui/navigation/AppRoute.kt`

```kotlin
sealed interface AppRoute {
    data object Onboarding : AppRoute
    data object ContactPicker : AppRoute
    data object Home : AppRoute
    data class PersonDetail(val contactId: String) : AppRoute
}
```

---

### Step 6 — Onboarding Screen

**File:** `ui/onboarding/OnboardingScreen.kt`

**Layout** (from mockup — top to bottom, centered):
1. **Top bar row**: `IconButton("×")` (close/skip) on left, `Text("ReConnect")` in `GoldPrimary` serif centered.
2. **Hero image**: Large `Box` with `RoundedCornerShape(28.dp)`, cream background, placeholder `Icon(Icons.Default.Group)` centered (or a bundled drawable). At bottom-right corner, a small `Surface(CircleShape)` with `Icon(Icons.Default.GroupAdd)` in gold.
3. **Heading**: `Text("Find your\ninner circle")` — `displaySmall` serif bold, centered.
4. **Subtitle**: `Text("Sync your contacts to see who's already here and start building your community today.")` — `bodyLarge`, centered, `onSurfaceVariant` color.
5. **Primary CTA**: `Button("Sync Friends →")` — gold filled, full-width (with horizontal padding), `RoundedCornerShape(28.dp)`. `onClick` triggers `permissionLauncher.launch(READ_CONTACTS)`.
6. **Secondary CTA**: `TextButton("NOT NOW")` — uppercase, `onSurfaceVariant`.
7. **Privacy footer**: `Row` with `Icon(Icons.Default.Lock, size=14)` + `Text("Your contacts are encrypted and never shared.")` in `bodySmall`, `MediumGray`.

**Behavior**:
- `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` → on granted: navigate to `ContactPicker`. On denied: stay on screen (or navigate to Home with empty state if "NOT NOW" was pressed).
- "×" and "NOT NOW" both navigate to `Home` (skipping picker).
- Background: vertical gradient from `CreamBackground` top to `White` bottom.

---

### Step 7 — Contact Picker Screen (New)

**File:** `ui/picker/ContactPickerViewModel.kt`

```kotlin
data class ContactPickerUiState(
    val contacts: List<Contact> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val intervals: Map<String, ReconnectInterval> = emptyMap(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)
```

- `loadContacts(contentResolver)` — calls `ContactRepository.getDeviceContacts()`, sets `isLoading = false`.
- `toggleContact(id: String)` — adds/removes from `selectedIds`; when added, defaults interval to `MONTHLY` in `intervals` map.
- `setInterval(id: String, interval: ReconnectInterval)` — updates `intervals[id]`.
- `updateSearch(query: String)` — filters displayed contacts.
- `getSelectedContacts(): List<Contact>` — returns contacts with their chosen intervals applied.

**File:** `ui/picker/ContactPickerScreen.kt`

**Layout**:
1. **Top bar**: `Text("Choose Your Circle")` title (serif headlineMedium), `TextButton("Skip")` as trailing action.
2. **Subtitle**: `Text("Select the people who matter most and how often you'd like to reconnect.")` — `bodyMedium`, padded.
3. **Search bar**: `OutlinedTextField` with `Icons.Default.Search` leading icon, `RoundedCornerShape(28.dp)`, full width.
4. **Contact list** (`LazyColumn`):
   - Each item: `Row` with circular avatar (`AsyncImage` or `Icon(Icons.Default.Person)` fallback), `Column(name, phoneNumber)`, trailing `Checkbox`.
   - When checked, an `AnimatedVisibility` block expands below the row showing a `SingleChoiceSegmentedButtonRow` with interval options: Weekly | Monthly | Quarterly | Yearly. Default = Monthly.
   - Selected rows get a subtle `primaryContainer` background tint.
5. **Bottom bar**: `Surface` with `Button("Continue · ${selectedCount} selected")`, enabled when `selectedCount > 0`, full-width, gold filled.

**Behavior**:
- On screen enter, `viewModel.loadContacts(context.contentResolver)` is called via `LaunchedEffect`.
- "Continue" navigates to `Home`.
- "Skip" navigates to `Home` with no selected contacts.
- Show `CircularProgressIndicator` while `isLoading`.

---

### Step 8 — Home Dashboard Screen

**File:** `ui/home/HomeViewModel.kt`

```kotlin
data class HomeUiState(
    val upcomingEvents: List<UpcomingEvent> = emptyList(),
    val quickCatchUps: List<Pair<Contact, String>> = emptyList()
)
```

- Loads from `ContactRepository.getSampleUpcomingEvents()` and `getSampleQuickCatchUps()`.

**File:** `ui/home/HomeScreen.kt`

**Layout** (vertically scrolling `LazyColumn`):

1. **Top app bar** (custom `Row`, not `TopAppBar`):
   - Left: Gold chain-link icon (`Icons.Default.Link` or similar) + `Text("ReConnect")` in `GoldPrimary` serif titleLarge.
   - Right: `IconButton(Icons.Default.Search)` + circular avatar `Box` (40dp, `CircleShape`, placeholder icon).

2. **Section header**: `Text("Upcoming\nConnections")` — `displayMedium` serif bold, `CharcoalText`, padding 24dp horizontal.

3. **Birthday Bash Card** (full-width, 28dp corner radius, amber gradient background `Brush.verticalGradient(AmberCardStart, AmberCardEnd)`):
   - `Text("BIRTHDAY BASH")` — `labelLarge`, `CoralLabel`, uppercase.
   - `Text("Sarah\nJenkins")` — `displayLarge` serif bold.
   - `Text("Turning 28. Don't forget the lilies!")` — `bodyMedium`.
   - Bottom row: Left = `Text("12")` huge (64sp serif, 0.15f alpha) + `Text("MARCH")` bold uppercase below it. Right = `Button("Send Wish")` with sparkle icon, `RoundedCornerShape(24.dp)`, gold-dark background.
   - Card height: ~320dp, padding 24dp.

4. **Catch-up Card** (full-width, `BlueCard` background, 28dp corners):
   - `Icon(Icons.Default.Checklist)` small.
   - `Text("Catch up with Mark")` — `headlineMedium` serif bold.
   - `Text("15")` — large serif (48sp) in `GoldPrimary`.
   - `Text("WEDNESDAY")` — `labelLarge`, `GoldPrimary`, uppercase.
   - Card height: ~200dp.

5. **Timeline Reminder Card** (`BlueCard` background, 28dp corners):
   - `Icon(Icons.Default.History)` small.
   - `Text("3 Months Since Mom")` — `headlineMedium` serif bold.
   - `Button("CALL NOW")` — outlined, full-width, `GoldPrimary` text and border.

6. **Quick Catch-ups section header**: `Row` with `Text("QUICK CATCH-UPS")` in `labelLarge` uppercase spaced + `TextButton("View all")` in `GoldPrimary`.

7. **Quick Catch-up rows** (for each contact):
   - `Row`: circular avatar (48dp) + `Column(name = titleMedium bold, subtitle = bodySmall gray)` + `IconButton(Icons.Default.EditNote)` + `IconButton(Icons.Default.Send)`.
   - `ElevatedCard` with `RoundedCornerShape(20.dp)`, full width.

8. **FAB**: `FloatingActionButton(onClick = {})` in `GoldPrimary`, `Icon(Icons.Default.Add)`, positioned bottom-end.

**Scroll animation**: Apply a `graphicsLayer` modifier to each event card item that calculates scale/alpha based on the item's scroll offset relative to the viewport center — items scale from 0.92→1.0 and alpha from 0.7→1.0 as they approach center. This mimics `TransformingLazyColumn` morphing for phone layouts.

---

### Step 9 — Person Detail Screen

**File:** `ui/detail/PersonDetailViewModel.kt`

```kotlin
data class PersonDetailUiState(
    val contact: Contact? = null,
    val nextTalkDescription: String = "",
    val nextTalkDate: String = "",
    val toDiscuss: String = "",
    val pastMoments: List<PastMoment> = emptyList()
)
```

- Loads contact by ID from `ContactRepository.getSampleContacts()`.
- Loads sample past moments and next-talk data.

**File:** `ui/detail/PersonDetailScreen.kt`

**Layout** (vertically scrolling `Column` inside a `Scaffold`):

1. **Top app bar**: `IconButton(Icons.AutoMirrored.Default.ArrowBack)` on left (navigates back), `Text("ReConnect")` center, `IconButton(Icons.Default.MoreVert)` right.

2. **Profile header** (centered column):
   - `Box`: Circular `AsyncImage` avatar (120dp, `CircleShape` clip, `border(3.dp, Color(0xFFE0D0B8), CircleShape)`). Green dot overlay: `Box(16dp, CircleShape, ActiveGreen)` at bottom-end with white border.
   - `Text("Eleanor Vance")` — `headlineLarge` serif bold.
   - `Text("Creative Director • Close Friend")` — `titleSmall`, `GoldDark`.

3. **Action buttons row**: `Button("Message", icon = Icons.Default.ChatBubbleOutline)` gold filled + `OutlinedButton("Call", icon = Icons.Default.Phone)` outlined. Both `RoundedCornerShape(28.dp)`, equal weight.

4. **"Next Talk" section**:
   - `Text("Next Talk")` — `headlineMedium` serif bold, start-aligned.
   - Bento card (`CardYellowLight` background, `RoundedCornerShape(28.dp)`, padding 20dp):
     - `Text("TO DISCUSS")` — `labelLarge`, `GoldDark`, uppercase, letter-spaced.
     - `Text("Coffee at The Daily Grind. Recent travel plans and that new book recommendation!")` — `titleLarge` serif.
     - Small gold dot divider (8dp circle, centered).
     - `Row`: `Icon(Icons.Default.CalendarToday, GoldDark)` + `Text("Friday, 10:00 AM")` — `bodyLarge`.
     - `Button("Add to Calendar")` — `NavyDark` background, white text, full-width, `RoundedCornerShape(28.dp)`, with `Icons.Default.CalendarMonth` leading icon.

5. **"Past Moments" section**:
   - `Text("Past Moments")` — `headlineMedium` serif bold.
   - Vertical timeline using a `Column`. Each moment is a `Row`:
     - **Left axis** (40dp wide): Category icon in a small `Surface(CircleShape)` with a tinted background. Icons: `DINING` → fork/knife (`Icons.Default.Restaurant`), `ART` → palette (`Icons.Default.Palette`), `OUTDOORS` → park (`Icons.Default.Park`). Below icon: a vertical `Divider` line (2dp, `MediumGray`, connecting to next moment).
     - **Right content** (flex):
       - `Text(dateLabel)` — `labelMedium`, `MediumGray`, uppercase.
       - `Text(title)` — `titleLarge` serif semi-bold.
       - If `imageUris` is non-empty: `LazyRow` of rounded image thumbnails (100×80dp, `RoundedCornerShape(12.dp)`), using placeholder `Box` with colored backgrounds.
       - `Text(description)` — `bodyMedium`, `onSurfaceVariant`.
     - Bottom spacing: 24dp between moments.

6. **Bottom nav**: Same as Home — HOME, PEOPLE (selected), HISTORY, SETTINGS in `NavyDark`.

---

### Step 10 — Wire Everything in `MainActivity.kt`

**Replace** the current `ReConnectApp()` composable:

1. Define `var currentRoute by rememberSaveable { mutableStateOf<AppRoute>(AppRoute.Onboarding) }` and a `backStack` list.
   - Use a simple `SharedPreferences` check for `"onboarding_complete"` to decide start route.

2. For Onboarding and ContactPicker routes: render the screen composable directly (no bottom nav).

3. For Home and PersonDetail routes: wrap in `NavigationSuiteScaffold` with updated `AppDestinations`:
   ```kotlin
   enum class AppDestinations(val label: String, val icon: ImageVector) {
       HOME("Home", Icons.Default.Home),
       CIRCLE("Circle", Icons.Default.People),
       EVENTS("Events", Icons.Default.CalendarMonth),
       SETTINGS("Settings", Icons.Default.Settings)
   }
   ```
   - Inside the scaffold content, switch on `currentRoute`:
     - `AppRoute.Home` → `HomeScreen(onContactClick = { id -> navigate to PersonDetail(id) })`
     - `AppRoute.PersonDetail(id)` → `PersonDetailScreen(contactId = id, onBack = { pop backstack })`

4. Add `BackHandler` when on PersonDetail to pop back to Home.

5. Remove old `Greeting`, `GreetingPreview`, old `AppDestinations` enum, and unused drawable imports.

---

## File Change Summary

| Action   | File                                                    |
|----------|---------------------------------------------------------|
| Modify   | `AndroidManifest.xml` — add `READ_CONTACTS` permission |
| Modify   | `ui/theme/Color.kt` — replace palette                  |
| Modify   | `ui/theme/Type.kt` — dual-font typography              |
| Modify   | `ui/theme/Theme.kt` — custom scheme + shapes           |
| Create   | `data/model/Contact.kt`                                |
| Create   | `data/model/ReconnectInterval.kt`                      |
| Create   | `data/model/UpcomingEvent.kt`                           |
| Create   | `data/model/PastMoment.kt`                              |
| Create   | `data/repository/ContactRepository.kt`                  |
| Create   | `ui/navigation/AppRoute.kt`                             |
| Create   | `ui/onboarding/OnboardingScreen.kt`                     |
| Create   | `ui/picker/ContactPickerScreen.kt`                      |
| Create   | `ui/picker/ContactPickerViewModel.kt`                   |
| Create   | `ui/home/HomeScreen.kt`                                 |
| Create   | `ui/home/HomeViewModel.kt`                              |
| Create   | `ui/detail/PersonDetailScreen.kt`                       |
| Create   | `ui/detail/PersonDetailViewModel.kt`                    |
| Modify   | `MainActivity.kt` — full rewrite of `ReConnectApp()`   |

## Notes

- **TransformingLazyColumn** is a Wear OS-only component (`androidx.wear.compose.foundation.lazy`). For the phone app, use a standard `LazyColumn` with `graphicsLayer { scaleX; scaleY; alpha }` modifiers keyed on each item's scroll offset to replicate the scaling/morphing motion from the mockups.
- **No `navigation-compose` needed** — routing is state-driven with `NavigationSuiteScaffold` handling the bottom nav shell.
- **Placeholder images**: Use `Icon` composables with Material icons as avatar/photo placeholders. `AsyncImage` (Coil) is wired for `photoUri` from device contacts but falls back to icons when URI is null.
- **Persistence deferred**: Contact selections and intervals are in-memory for now. Room can be added later for persisting selected contacts, notes, and auto-save functionality.

