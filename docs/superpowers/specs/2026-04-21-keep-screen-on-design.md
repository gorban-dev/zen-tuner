# Keep Screen On While Tuning

- **Date:** 2026-04-21
- **Branch:** `feature/keep-screen-on`
- **Worktree:** `.worktrees/keep-screen-on`

## Problem

Во время настройки инструмента экран гаснет по системному таймауту. Пользователь держит инструмент в руках, на экран не нажимает — и теряет визуальную обратную связь тюнера.

## Goal

Экран не должен гаснуть, пока тюнер активно слушает микрофон. Как только пользователь ставит тюнер на паузу или уходит с экрана — поведение возвращается к системному (экран гаснет по таймауту, батарея не расходуется зря).

## Scope

**In scope**
- Держать экран включённым ровно в промежутке, пока `TunerViewState.isListening == true`.

**Out of scope**
- Переключатель в настройках (в приложении один экран и нет раздела настроек).
- Разрешение `WAKE_LOCK` — оно не требуется для флага окна, см. доку Google.
- Любые изменения доменного слоя, DI, ViewModel, репозиториев.

## Approach

Официальная рекомендация Google ([developer.android.com/develop/background-work/background-tasks/awake/screen-on](https://developer.android.com/develop/background-work/background-tasks/awake/screen-on)) — ставить флаг `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` на окне Activity через `window.addFlags(...)`. WakeLock для этой задачи не используется: он нужен для фоновых сценариев, а не для «не гасить экран, пока пользователь на экране».

Ключевая деталь из доки: при уходе приложения в фон система автоматически перестаёт применять флаг — руками чистить его для этого случая не нужно. `clearFlags(...)` нужен только чтобы снять эффект, пока экран всё ещё видим (наш кейс — пауза внутри `TunerScreen`).

Реализация: `DisposableEffect(viewState.isListening)` в `TunerScreen`. При `isListening == true` добавляем флаг, в `onDispose` снимаем. Compose перезапустит effect при смене `isListening` — флаг снимется на паузе и поставится снова при старте. При уходе с экрана `onDispose` тоже отработает — экран будет отпущен мгновенно, не дожидаясь системной логики фона.

## Design

**Файл:** `app/src/main/java/dev/gorban/zentuner/feature/tuner/presentation/screen/TunerScreen.kt`

**Изменение:** один `DisposableEffect`, завязанный на `viewState.isListening`, внутри `TunerScreen`. Берёт `Window` из `LocalActivity` (или через `LocalContext` → `Activity`) и управляет флагом.

Псевдокод:

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

### Поведение

| Событие | Состояние `isListening` | Флаг окна | Результат |
|---|---|---|---|
| Старт настройки (кнопка Play) | `false → true` | ставится | Экран не гаснет |
| Пауза (кнопка Pause) | `true → false` | снимается в `onDispose` | Системный таймаут включается |
| Отказ в разрешении микрофона | остаётся `false` | не ставится | Ничего не меняется |
| Уход с экрана / закрытие приложения | экран уходит | снимается в `onDispose` | Баттл-ремень; система всё равно отпускает при фоне |
| Перезапуск процесса | state = default | `isListening = false` | Флаг не ставится |

### Что НЕ меняется

- `AndroidManifest.xml` — новых разрешений не требуется.
- `TunerViewModel` — нужное состояние `isListening` уже существует.
- `MainActivity`, DI, `TunerView`, доменный/data-слой.

### Архитектурное соответствие

Изменение лежит в `presentation/screen/`, где по правилам проекта (`CLAUDE.md` → Screen/View split) живут side-effects, связывающие Compose с платформой (разрешения, навигация). Оконный флаг — тот же класс side-effect, поэтому место корректное. `View`-слой остаётся чистым от ViewModel и от платформенных зависимостей.

## Error handling

- Если по какой-то причине `context` не является `Activity` (теоретически возможно для `LocalContext` в превью) — `as? Activity` даёт `null`, и `?.` просто ничего не делает. Превью не ломается.
- Флаг идемпотентен: повторный `addFlags` без промежуточного `clearFlags` не приводит к ошибке.

## Testing

Юнит-тесты не пишем: это side-effect окна Android, проверка через Robolectric ради одного `DisposableEffect` — оверкилл.

Ручная проверка через `claude-in-mobile`:
1. Собрать и поставить debug-APK на устройство.
2. В системных настройках устройства поставить короткий таймаут экрана (например, 15 секунд).
3. Запустить приложение → нажать Play → подождать дольше таймаута → экран горит, тюнер работает.
4. Нажать Pause → подождать дольше таймаута → экран гаснет по системному правилу.
5. Нажать Play снова → подождать → экран снова горит.
6. Нажать Play, свернуть приложение → дождаться системного таймаута → экран гаснет.

## Metadata

- **Worktree:** created (`A` — per overlay step 8.5).
- **Architecture rules:** Screen/View split соблюдён — side-effect в `presentation/screen/`, `View` остаётся чистым.
