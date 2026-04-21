# Keep Screen On While Tuning — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use gor-mobile-subagent-driven-development (recommended) or gor-mobile-executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Держать экран включённым, пока `TunerViewState.isListening == true` — то есть пока пользователь настраивает инструмент.

**Architecture:** Один `DisposableEffect` в `TunerScreen`, завязанный на `viewState.isListening`. Ставит/снимает `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` на окне Activity — официально рекомендованный Google способ. Никаких изменений во ViewModel, DI, доменном/data-слое, манифесте, разрешениях.

**Tech Stack:** Kotlin, Jetpack Compose, Android `Window.addFlags / clearFlags`.

**Spec:** `docs/superpowers/specs/2026-04-21-keep-screen-on-design.md`

---

## File Structure

| Действие | Путь | Ответственность |
|---|---|---|
| Modify | `app/src/main/java/dev/gorban/zentuner/feature/tuner/presentation/screen/TunerScreen.kt` | Добавить `DisposableEffect(viewState.isListening)`, управляющий флагом окна |

Новых файлов не создаётся. Архитектурно изменение попадает в `presentation/screen/` — это единственное место в feature, где уже живут side-effects, связывающие Compose с платформой (разрешения, навигация). Оконный флаг — того же класса side-effect, так что слой корректный.

---

## Task 1: Добавить управление `FLAG_KEEP_SCREEN_ON` в TunerScreen

**Files:**
- Modify: `app/src/main/java/dev/gorban/zentuner/feature/tuner/presentation/screen/TunerScreen.kt`

**Context (как сейчас выглядит файл, строки важные для задачи):**

```kotlin
@Composable
fun TunerScreen() {
    val viewModel: TunerViewModel = koinViewModel()
    val viewState by viewModel.viewStates().collectAsStateWithLifecycle()
    val viewAction by viewModel.viewActions().collectAsStateWithLifecycle(null)
    val context = LocalContext.current
    // ...
    TunerView(
        viewState = viewState,
        eventHandler = viewModel::obtainEvent
    )
}
```

`context = LocalContext.current` уже есть — используем его. `viewState.isListening` уже есть в `TunerViewState`.

---

- [ ] **Step 1: Добавить импорты**

Открой `app/src/main/java/dev/gorban/zentuner/feature/tuner/presentation/screen/TunerScreen.kt`. В блок импортов добавь три строки (сохранив алфавитный порядок):

```kotlin
import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
```

`LaunchedEffect` уже импортирован — `DisposableEffect` импортируем рядом.

---

- [ ] **Step 2: Вставить `DisposableEffect` внутрь `TunerScreen`**

Найди внутри функции `TunerScreen()` блок `LaunchedEffect(viewAction) { ... }` (строки ~52–60). Сразу **после** этого блока, до `if (viewState.showSettingsDialog)`, вставь:

```kotlin
    DisposableEffect(viewState.isListening) {
        val window = (context as? Activity)?.window
        if (viewState.isListening) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
```

**Почему именно так:**
- Ключ `viewState.isListening` — effect перезапускается при смене состояния: на `true` ставит флаг, на `false` `onDispose` снимает.
- `(context as? Activity)` — безопасное приведение. Для `@Preview` `context` может не быть Activity; `?.` даст `null` и превью не сломается.
- `onDispose { clearFlags }` — гарантированно снимает флаг, когда экран уходит с композиции (уход пользователя, смерть экрана).

---

- [ ] **Step 3: Проверить, что файл собирается**

Из **корня worktree** (`.worktrees/keep-screen-on`) запусти:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`. Если падает — типичные причины: забытый импорт, неправильное место вставки (внутри другого `@Composable`, не `TunerScreen`), опечатка в имени флага.

---

- [ ] **Step 4: Собрать debug APK**

```bash
./gradlew :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`. Путь до APK: `app/build/outputs/apk/debug/app-debug.apk`.

---

- [ ] **Step 5: Ручная проверка на устройстве через skill `claude-in-mobile`**

Использовать **только** skill `claude-in-mobile` — он сам ставит APK (по глобальному правилу, adb/xcodebuild вручную не трогаем).

Шаги проверки:

1. Установить свежий debug APK на подключённое устройство через skill.
2. В системных настройках устройства поставить короткий screen timeout — например, 15 секунд.
3. Запустить приложение ZenTuner.
4. Нажать кнопку Play (запуск тюнера). Дать разрешение на микрофон, если ещё не выдано.
5. **Подождать 30 секунд, не касаясь экрана.** Ожидаемо: экран остаётся включённым, тюнер показывает амплитуду/ноту.
6. Нажать кнопку Pause. **Подождать 30 секунд.** Ожидаемо: экран гаснет по системному таймауту.
7. Разбудить экран, нажать Play снова. **Подождать 30 секунд.** Ожидаемо: экран снова не гаснет.
8. Нажать Play, свернуть приложение кнопкой Home. **Подождать 30 секунд.** Ожидаемо: экран гаснет (при фоне система сама перестаёт уважать флаг).
9. Вернуться в приложение (оно уже в фоне, recent apps) — состояние может оказаться либо на Play, либо на Pause (зависит от того, как ViewModel переживает фон). Если снова Play — экран опять не должен гаснуть.

**Если что-то из шагов 5, 7, 8 не совпало с ожидаемым — не коммитить, вернуться к Step 2, перечитать код.**

---

- [ ] **Step 6: Коммит**

Из worktree:

```bash
git add app/src/main/java/dev/gorban/zentuner/feature/tuner/presentation/screen/TunerScreen.kt
git commit -m "feat(tuner): keep screen on while tuner is listening"
```

---

## Task 2: Верификация через skill `gor-mobile-verification-before-completion`

**Files:** нет модификаций кода — это только проверочный шаг.

- [ ] **Step 1: Запусти skill `gor-mobile-verification-before-completion`**

Skill потребует пруфов перед тем, как заявить, что задача закрыта. Минимальный набор пруфов здесь:

1. `./gradlew :app:compileDebugKotlin` — вывод `BUILD SUCCESSFUL`.
2. `./gradlew :app:assembleDebug` — вывод `BUILD SUCCESSFUL`.
3. Лог ручной проверки из Task 1 Step 5 — все девять пунктов совпали с ожидаемым.
4. `git log --oneline feature/keep-screen-on ^main` — показывает ровно один feat-коммит из Task 1 Step 6 (плюс коммиты со spec и планом).

Если все четыре пункта — зелёные, можно переходить к `gor-mobile-finishing-a-development-branch`.

---

## Self-Review (checklist для автора плана)

1. **Spec coverage:**
   - «Держать экран включённым, пока `isListening == true`» → Task 1 Step 2.
   - «На паузе — снимать флаг» → `onDispose` в Step 2 + ручная проверка Step 5.6–5.7.
   - «При уходе в фон — система сама, но `onDispose` страхует» → Step 5.8.
   - «Никаких изменений в манифесте / ViewModel / DI» → зафиксировано в File Structure, таблица пуста кроме одной строки.
   - «Юнит-тестов не пишем» → раздел Testing в спеке это оговорил; план соответствует.
2. **Placeholder scan:** плейсхолдеров/TBD/TODO/«add appropriate X» нет.
3. **Type consistency:** `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`, `Activity`, `DisposableEffect` — используются одинаково на всём плане.
