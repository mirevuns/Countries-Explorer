# Countries Explorer

**Автор:** Грибовский Илья Игоревич

Android-приложение для просмотра стран через REST Countries API.

**Ветки:** `feature/homework-6` (ДЗ 5-6), `feature/final-project` (финал)

**Стек:** Kotlin, Compose, Coroutines, Retrofit, Room, DataStore, Hilt, WorkManager

## Запуск

JDK 17, Windows, PowerShell:

```text
.\gradlew.bat assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Для instrumented-тестов нужен эмулятор или телефон.

Ключ API v5 в `local.properties`:

```text
REST_COUNTRIES_API_KEY=YOUR_KEY
```

Регистрация: https://restcountries.com/sign-up

Перед первым запуском финальной ветки лучше удалить старую версию приложения (Room v6).

---

## ДЗ 5

Список и детали стран, поиск, избранное в Room.

- Тап по сердцу сохраняет страну в БД, после перезапуска избранное на месте
- Обработка загрузки, ошибок сети, пустого списка и поиска
- Архитектура: API и Room - Repository - ViewModel - Compose UI

**Тесты:** 21 юнит, 7 instrumented (`MainActivityComposeTest`, `NavigationComposeInstrumentedTest`, `FavoritesRoomInstrumentedTest` и др.)

**Ветка:** см. историю до `feature/homework-6`

---

## ДЗ 6

Flow и DataStore поверх ДЗ 5.

- В списке: `combine`, `merge`, `debounce`, `flatMapLatest`, `stateIn`, refresh через `MutableSharedFlow`
- Настройки списка (сортировка) в DataStore
- Избранное по-прежнему в Room, обновляется через Flow
- Локальный UI: подсказка поиска на `mutableStateOf`, остальное - `StateFlow`

**Тесты:** 22 юнит, 6 instrumented (добавлены проверки refresh без сети и FilterChips)

**Ветка:** `feature/homework-6`, PR #3

---

## Финальный проект

Offline-first: страны кэшируются в Room, личные данные и синхронизация сохраняются локально.

**Что есть в приложении**

- Кэш стран с TTL, баннеры офлайн и устаревших данных
- Настройки: автообновление, Wi-Fi only, ручная синхронизация и предзагрузка
- Нижняя навигация: Страны, Коллекции, История, Настройки
- Профили: история, заметки, журнал, коллекции и избранное отдельно для каждого
- Заметки на экране детали, журнал просмотров, пользовательские коллекции
- WorkManager для фонового обновления кэша

**Room v6:** `cached_countries`, `cache_metadata`, `visit_history`, `country_notes`, `profiles`, `journal_entries`, `collections`, `collection_countries`, `favorites`

**Как проверить**

1. Синхронизация или предзагрузка, потом авиарежим - список и детали из кэша
2. Открыть страну - она появится в Истории и Журнале, заметка сохранится после перезапуска
3. Второй профиль - свои данные, не смешиваются с первым
4. Коллекция - добавить страну с экрана детали

**Тесты:** 28 юнит, 11 instrumented (включая настройки, историю, заметки, WorkManager)

**Ветка:** `feature/final-project`, PR #4

---

## Скриншоты (ДЗ 5)

![Загрузка](screenshots/loading.png)
![Ошибка](screenshots/error.png)
![Список](screenshots/list.png)
![Пусто](screenshots/empty.png)
![Детали](screenshots/detail.png)
![Избранное](screenshots/favorites.png)
