# ZenTuner — CLAUDE.md

## Project overview

Android chromatic tuner app with retro-inspired UI (arc gauge, seven-segment note display). Real-time pitch detection via autocorrelation algorithm.

Package: `dev.gorban.zentuner`

## Build & test

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew test                  # Run unit tests (JUnit 4 + MockK)
./gradlew connectedAndroidTest  # Run instrumentation tests
```

Min SDK: 24, Target SDK: 36, Java 11.

## Architecture

**Feature Slice Architecture** with unidirectional data flow.

### Structure

```
core/
  BaseSharedViewModel.kt   — Base ViewModel<State, Action, Event> with obtainEvent()
  UseCase.kt               — interface UseCase<Params, Result> { suspend fun execute(params): Result }

feature/<name>/
  presentation/
    screen/   — Thin Screen composable: wires ViewModel to View, handles permissions/navigation
    view/     — Pure UI @Composable functions (no ViewModel deps, accept state + eventHandler)
    viewmodel/ — ViewModel, ViewState (data class), ViewEvent (sealed), ViewAction (sealed)
  domain/
    model/      — Domain models
    repository/ — Repository interfaces (I-prefix: ITunerRepository)
    usecase/    — UseCases (one per file, named <Verb><Noun>UseCase)
  data/
    datasource/ — Data sources (AudioRecorder, PitchDetector)
    repository/ — Repository implementations
  di/           — Koin module (one val <feature>Module per feature)
```

### Key patterns

- **BaseSharedViewModel<State, Action, Event>**: state via `viewState` property, one-shot actions via `viewAction`, events via `obtainEvent()`
- **Screen/View split**: Screen = thin adapter (gets ViewModel via `koinViewModel()`, collects state, passes to View). View = pure composable with `(viewState, eventHandler)` signature
- **UseCase**: `suspend fun execute(params): Result<T>`. Injected as `factory` in Koin
- **Repository**: interface in `domain/`, implementation in `data/`. Injected as `single` in Koin
- **DataSource**: injected as `single` in Koin

### Conventions

- Repository interfaces use `I` prefix (e.g. `ITunerRepository`)
- UseCase naming: `<Verb><Noun>UseCase` (e.g. `StartTunerUseCase`, `ObservePitchUseCase`)
- ViewState = `data class`, ViewEvent/ViewAction = `sealed class` or `sealed interface`
- One Koin module per feature: `val tunerModule = module { ... }`

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Koin 4.x (DI)
- Kotlin Coroutines & Flow
- JUnit 4 + MockK (testing)
- Gradle with version catalog (`gradle/libs.versions.toml`)

## Code style

- Kotlin, no Java source files
- Compose UI with `@Preview` support
- No Jetpack Navigation (single-screen app currently)
- Permissions handled in Screen composable via `rememberLauncherForActivityResult`
