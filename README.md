# ZenTuner

A minimalist chromatic tuner for Android with a retro-inspired UI featuring an arc gauge and segmented note display.

## Features

- Real-time pitch detection using autocorrelation algorithm
- Arc gauge indicator with spring animation
- Seven-segment style note display
- Adjustable microphone gain threshold via rotary knob
- Supports full chromatic range (C0 - B8)
- Standard guitar tuning detection (E2, A2, D3, G3, B3, E4)

## Architecture

The project follows a **Feature Slice Architecture** with clean separation of concerns:

```
core/
  BaseSharedViewModel.kt        # Base ViewModel with State/Action/Event pattern
  UseCase.kt                    # UseCase interface

feature/tuner/
  presentation/
    screen/TunerScreen.kt       # Thin adapter (ViewModel -> View)
    view/                       # Pure UI composables
    viewmodel/                  # ViewModel, ViewState, ViewEvent, ViewAction
  domain/
    model/                      # Note, PitchResult, SymbolSegment
    repository/                 # ITunerRepository interface
    usecase/                    # ObservePitch, StartTuner, StopTuner
  data/
    datasource/                 # AudioRecorder, PitchDetector
    repository/                 # TunerRepository implementation
  di/                           # Koin DI module
```

### Key patterns

- **BaseSharedViewModel<State, Action, Event>** with `obtainEvent()` for unidirectional data flow
- **UseCase** interface with `suspend fun execute(params): Result<T>`
- **Koin** for dependency injection
- **Jetpack Compose** for UI with `@Preview` support

## Tech stack

- Kotlin
- Jetpack Compose (Material 3)
- Koin (DI)
- Kotlin Coroutines & Flow
- JUnit 4 + MockK (testing)

## Requirements

- Android SDK 24+ (Android 7.0)
- Microphone permission (requested at runtime)

## Build & Run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Tests

```bash
./gradlew test
```

## License

All rights reserved.
