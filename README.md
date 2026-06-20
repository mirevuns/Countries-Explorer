# Countries Explorer — финальный проект

Android-приложение для просмотра стран через **REST Countries API**. Offline-first: данные кэшируются в Room, пользовательские заметки и история просмотров сохраняются локально, фоновая синхронизация — через WorkManager.

**Стек:** Kotlin, Jetpack Compose, Coroutines, Retrofit, Room, DataStore, Hilt, WorkManager.

**Ветка:** `feature/final-project`

### Запуск

```text
gradlew.bat assembleDebug
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:connectedDebugAndroidTest
```

API-ключ REST Countries v5 — в `local.properties`:

```text
REST_COUNTRIES_API_KEY=YOUR_KEY
```

Ключ: регистрация на https://restcountries.com/sign-up

---

## Что добавлено в финальной работе

- **Offline-first `CountriesRepository`** — чтение из Room-кэша, обновление с API, TTL (`CachePolicy`), баннеры офлайн и устаревших данных.
- **Экран «Настройки»** — TTL кэша, автообновление, синхронизация только по Wi‑Fi, предзагрузка; ручная синхронизация и очистка кэша; статус последней синхронизации.
- **Экран «Недавние»** — список просмотренных стран из `visit_history`.
- **Заметки на экране детали** — сохранение и удаление текста в `country_notes`.
- **WorkManager** — периодическое обновление кэша и разовая предзагрузка (`SyncScheduler`, Hilt `WorkerFactory`).
- **UI** — иконки «Настройки» и «Недавние» в топ-баре списка; информационные баннеры на списке и экране детали.

---

## Новые пользовательские данные

Добавлены таблицы Room (схема v4, миграция `3 → 4`):

| Таблица | Назначение |
|---------|------------|
| `cached_countries` | Офлайн-кэш списка стран (JSON `Country`) |
| `cache_metadata` | Время и статус последней полной синхронизации |
| `visit_history` | Недавно просмотренные страны (код, имя, время) |
| `country_notes` | Личные заметки пользователя к стране (по коду) |

Настройки приложения (TTL, автообновление, Wi‑Fi only, предзагрузка) хранятся в **DataStore** (`AppSettingsRepository`) — это конфигурация, не пользовательский контент.

---

## Новые сценарии

1. **Офлайн-режим** - после синхронизации отключить сеть: список и детали открываются из кэша; баннер «Офлайн-режим: показаны сохранённые данные» (на детали — «Офлайн: детали из локального кэша»).
2. **История и заметки** - открыть несколько стран → «Недавние»; на детали написать заметку → перезапуск приложения: история и заметка сохранены в Room.
3. **Синхронизация** - в настройках включить автообновление или нажать «Синхронизировать» / «Предзагрузить офлайн»; кэш обновляется с учётом ограничения Wi‑Fi only.
4. **Устаревший кэш** - при истечении TTL данные показываются с баннером «Данные могут быть устаревшими»; при наличии сети — обновление по запросу или в фоне.

---

## Offline-first: как устроено

Логика в `CountriesRepository` и `CachePolicy`:

1. **Нет сети** → вернуть данные из Room; если кэш пуст — ошибка.
2. **Сеть есть, кэш свежий** (TTL не истёк) → вернуть кэш без запроса к API.
3. **Сеть есть, кэш устарел или запрошено обновление** → запрос к REST Countries API, запись в `cached_countries`, обновление `cache_metadata`.
4. **Ошибка сети/API при непустом кэше** → вернуть кэш с флагом `isStale`; UI показывает баннер об устаревших данных.

Загрузка детали страны — по тому же принципу. При открытии детали запись добавляется в `visit_history`.

---

## Фоновая обработка (WorkManager)

| Компонент | Где используется | Зачем |
|-----------|------------------|-------|
| `CountryCacheSyncWorker` | Периодическая задача (каждые 6 ч) | Автообновление кэша при включённом автообновлении; учитывает Wi‑Fi only |
| `CountryPreloadWorker` | Разовая задача по кнопке «Предзагрузить офлайн» | Загрузка всех регионов в кэш для работы без сети |
| `SyncScheduler` | `CountriesApplication.onCreate`, экран настроек | Планирование и отмена периодической синхронизации, запуск предзагрузки |
| Hilt `WorkerFactory` | `CountriesApplication` | Внедрение зависимостей в workers |

WorkManager инициализируется в `CountriesApplication` (`Configuration.Provider`); автозапуск через manifest отключён.

---

## Тесты

**27 юнит-тестов** (`app/src/test`):

| Класс | Кол-во | Что проверяет |
|-------|--------|---------------|
| `CountriesListViewModelTest` | 7 | Состояния списка, фильтры, поиск |
| `CountryDetailViewModelTest` | 3 | Загрузка детали, ошибки |
| `FavoritesSharedViewModelTest` | 2 | Избранное |
| `CountriesRepositoryTest` | 6 | API + кэш, offline / stale / force refresh |
| `CachePolicyTest` | 3 | TTL кэша |
| `VisitHistoryRepositoryTest` | 1 | Запись истории просмотров |
| `CountryNoteRepositoryTest` | 2 | Сохранение и удаление заметок |
| `FavoriteEntityTest` | 1 | Маппинг избранного |
| `CountryCodeHelperTest` | 2 | URL флагов |

**11 instrumented-тестов** (`app/src/androidTest`, Hilt + Compose + Room in-memory + MockWebServer):

| Класс | Кол-во | Что проверяет |
|-------|--------|---------------|
| `MainActivityComposeTest` | 1 | Список стран, загрузка данных |
| `NavigationComposeInstrumentedTest` | 3 | Навигация список → детали, детали из кэша при ошибке сети, поиск |
| `FavoritesRoomInstrumentedTest` | 2 | Room: избранное, Flow |
| `FinalProjectComposeInstrumentedTest` | 3 | Настройки, «Недавние», заметка в Room |
| `CountryCacheWorkerInstrumentedTest` | 2 | `CountryPreloadWorker`, `CountryCacheSyncWorker` |
