# Plan: Lean Supabase Integration for 'playstoreSupabase'

Migrate the application to use a Lean Data Access strategy. The `playstoreSupabase` flavor will
communicate directly with Supabase, while the `privateLocal` flavor retains offline Room storage.
Heavy "Repository" and "DTO" patterns will be replaced with direct Data Stores.

## 1. Domain Model Updates

Refactor `Contact` and `PastMoment` to support serialization and the required schema changes.

- **Files**:
    - `data/model/Contact.kt`
    - `data/model/PastMoment.kt`
- **Changes**:
    - Add `@Serializable` to models to allow direct Supabase usage.
    - Update `PastMoment` to support `contactIds: List<String>` (replacing single `contactId`).
    - Update `PastMoment` to replace `dateLabel` string with `dateEpochMs: Long`.

## 2. Abstraction Layer (Lean Interfaces)

Define simple interfaces for data access, removing the complex Repository pattern.

- **Files**:
    - `data/port/ContactStore.kt` (Renamed from ContactRepository)
    - `data/port/MomentStore.kt` (Renamed from MomentRepository)

## 3. Supabase Implementation ('playstoreSupabase')

Implement the stores directly using the Supabase Client.

- **Files**:
    - `data/remote/SupabaseContactStore.kt`
    - `data/remote/SupabaseMomentStore.kt`
- **Logic**:
    - `SupabaseContactStore`: Direct `client.postgrest["contacts"]` calls. Handles `user_id`
      implicitly via Auth (RLS).
    - `SupabaseMomentStore`: Handles `moments` insert/select. Handles the `moment_contacts` junction
      table for the many-to-many relationship.
    - **No DTOs**: Use the domain models directly with `@SerialName` if needed.

## 4. Local Implementation ('privateLocal')

Adapt the existing Room implementation to the new `Store` interfaces.

- **Files**:
    - `data/local/RoomContactStore.kt`
    - `data/local/RoomMomentStore.kt`
    - `data/local/db/MomentEntity.kt` (Update schema to match domain model changes: Store
      `contactIds` as JSON string)
    - `data/local/db/RoomConverters.kt` (Add `List<String>` converter)
- **Logic**:
    - Wraps the DAOs to map `Entity <-> Domain Model`.

## 5. Dependency Injection

Update `AppContainer` to provide the correct implementation based on the flavor.

- **Files**:
    - `data/port/AppContainer.kt`
- **Logic**:
    - Check `BuildConfig.FLAVOR`.
    - If `playstoreSupabase`: Instantiate `SupabaseContactStore`.
    - If `privateLocal`: Instantiate `RoomContactStore`.

## 6. UI Refactoring

Update ViewModels to use the new Store interfaces and updated Models.

- **Files**:
    - `ui/home/HomeViewModel.kt`
    - `ui/detail/PersonDetailViewModel.kt`
    - `ui/journey/JourneyViewModel.kt`
    - `ui/detail/LogMomentSheet.kt` (Update for multiple contact selection)

## 7. Cleanup

Remove redundant "Architecture" files.

- **Delete**:
    - `data/repository/*`
    - Old wrapper implementations if they exist.

