# ReConnect

This project now uses modular backend ports so the same UI can run with different data providers.

## Current backend implementation

- `privateLocal` flavor uses Room-backed repositories and local attachment storage.
- `playstoreSupabase` flavor is wired as a flavor target and can receive backend implementations later.

## Architecture notes

- Contracts live under `app/src/main/java/dev/pranav/reconnect/data/port`.
- Local implementations live under `app/src/main/java/dev/pranav/reconnect/data/local`.
- Room schema is generated to `app/schemas` through KSP.
- App initialization uses `AppContainer.init(...)` in `MainActivity`.

## Build

```bash
./gradlew :app:assemblePrivateLocalDebug
```

