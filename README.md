# KurlClient

A cross-platform HTTP client built with **Kotlin Multiplatform** and **Compose Multiplatform**, targeting Android, iOS, and Desktop (JVM). Think of it as a native Postman alternative that runs everywhere from a single shared codebase.

---

## Features

- **HTTP Request Playground** — compose requests with URL, method (GET, POST, PUT, PATCH, DELETE, etc.), headers, query params, and raw body
- **Response Viewer** — view response body (with syntax-highlighted JSON viewer), headers, status code, response time, and size
- **Network Info** — inspect TLS protocol, cipher suite, certificate details, HTTP version, and local/remote addresses
- **Collections** — save requests into a nested folder tree; load saved requests back into the playground
- **cURL Import/Export** — paste a `curl` command to populate the playground, or copy the current request as a `curl` command
- **Adaptive Layout** — compact (mobile) bottom-nav layout and expanded (tablet/desktop) side-by-side layout auto-selected based on window width
- **Unsaved Changes Guard** — warns before discarding in-progress edits when switching requests

---

## High-Level Design

```
┌─────────────────────────────────────────────────────┐
│                    Platform Entry Points             │
│   MainActivity (Android) │ main() (JVM) │ iOS VC    │
└───────────────────────────┬─────────────────────────┘
                            │
                    ┌───────▼────────┐
                    │   composeApp   │  Theme, App root,
                    │   (commonMain) │  DI wiring (Koin)
                    └───────┬────────┘
                            │
              ┌─────────────▼──────────────┐
              │      feature-workspace      │
              │  WorkSpaceScreen            │
              │  ├── CompactScaffold        │  Adaptive
              │  └── ExpandedScaffold       │  layouts
              │       ├── PlaygroundScreen  │
              │       │   ├── RequestPanel  │
              │       │   │   ├── UrlBar    │
              │       │   │   ├── Headers   │
              │       │   │   ├── Params    │
              │       │   │   ├── Body      │
              │       │   │   └── Auth (WIP)│
              │       │   └── ResponsePanel │
              │       │       ├── Body      │
              │       │       ├── Headers   │
              │       │       └── NetworkInfo│
              │       └── CollectionsScreen  │
              │           └── FolderTree    │
              └─────────────┬──────────────┘
                            │ MVI (State / Event / Effect)
              ┌─────────────▼──────────────┐
              │    ViewModels (Koin)        │
              │  WorkspaceViewModel         │
              │  PlaygroundScreenModel      │
              │  CollectionsViewModel       │
              └──────┬──────────┬──────────┘
                     │          │
          ┌──────────▼──┐  ┌────▼───────────┐
          │  kurl-core  │  │   kurl-store    │
          │  KurlEngine │  │  SQLDelight DB  │
          │  (Ktor HTTP)│  │  CollectionFolder│
          │  KurlRequest│  │  SavedRequest   │
          │  KurlResponse│ └─────────────────┘
          └─────────────┘
                     │
          ┌──────────▼──────────────────────┐
          │      ui-json-viewer             │
          │  JsonParser → JNode tree        │
          │  JsonViewer (syntax coloring)   │
          └─────────────────────────────────┘
```

### Modules

| Module | Role |
|---|---|
| `androidApp` | Android application entry point (`MainActivity`, `KurlClientApp`) |
| `iosApp` | Xcode project / iOS entry point |
| `composeApp` | App shell — theme, root `App` composable, Koin DI setup |
| `feature-workspace` | All UI screens: workspace, playground, collections; ViewModels; cURL import/export |
| `kurl-core` | Platform-agnostic HTTP engine (`KurlEngine` / Ktor), request/response models, network info extraction |
| `kurl-store` | SQLDelight database — collections and saved requests persistence |
| `presentation-base` | MVI base (`MviViewModel`), adaptive window width utilities |
| `ui-json-viewer` | Standalone JSON tree parser and syntax-colored viewer composable |

### Key Technologies

| Area | Library / Tool |
|---|---|
| UI | Compose Multiplatform 1.10.2 |
| Language | Kotlin 2.3.10 |
| DI | Koin |
| State management | `MviViewModel` (androidx.lifecycle + StateFlow) |
| HTTP | Ktor (platform-specific engines per target) |
| Persistence | SQLDelight 2.0.2 |
| Build | Gradle (KMP convention plugins) |
| Static analysis | Detekt |

---

## Project Structure

```
KurlClient/
├── androidApp/          # Android entry point
├── iosApp/              # Xcode project / iOS entry point
├── composeApp/          # App shell & theme
│   └── src/
│       ├── commonMain/  # Shared app root, DI
│       ├── androidMain/ # Android-specific wiring
│       ├── jvmMain/     # Desktop main()
│       └── iosMain/     # iOS MainViewController
├── feature-workspace/   # All screens and ViewModels
├── kurl-core/           # HTTP engine & models
├── kurl-store/          # SQLDelight DB (collections)
├── presentation-base/   # MVI base classes
└── ui-json-viewer/      # JSON viewer composable
```

---

## Build & Run

### Android

```shell
# macOS / Linux
./gradlew :androidApp:assembleDebug

# Windows
.\gradlew.bat :androidApp:assembleDebug
```

### Desktop (JVM)

```shell
# macOS / Linux
./gradlew :composeApp:run

# Windows
.\gradlew.bat :composeApp:run
```

### iOS

Open the [`/iosApp`](./iosApp) directory in Xcode and run from there, or use the run widget in your IDE.

---

## Architecture: MVI

Each screen follows a strict MVI contract defined in `presentation-base`:

```
UiEvent  ──▶  ViewModel  ──▶  UiState  (rendered by Composable)
                    └──────▶  UiEffect (one-shot: navigation, snackbars)
```

ViewModels are provided via Koin and observed with `collectAsStateWithLifecycle()`.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) and [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).