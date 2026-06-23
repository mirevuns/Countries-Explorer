# Countries Explorer

**Автор:** Грибовский Илья Игоревич

Android-приложение для просмотра стран через REST Countries API.

**Ветка:** `feature/homework-6` (ДЗ 5-6). Финальный проект - `feature/final-project`, PR #4.

**Стек:** Kotlin, Compose, Coroutines, Retrofit, Room, DataStore, Hilt

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

---

## ДЗ 5

Список и детали стран, поиск, избранное в Room.

- Тап по сердцу сохраняет страну в БД, после перезапуска избранное на месте
- Обработка загрузки, ошибок сети, пустого списка и поиска
- Архитектура: API и Room - Repository - ViewModel - Compose UI

**Тесты:** 21 юнит, 7 instrumented (`MainActivityComposeTest`, `NavigationComposeInstrumentedTest`, `FavoritesRoomInstrumentedTest` и др.)

---

## ДЗ 6

Flow и DataStore поверх ДЗ 5.

- В списке: `combine`, `merge`, `debounce`, `flatMapLatest`, `stateIn`, refresh через `MutableSharedFlow`
- Настройки списка (сортировка) в DataStore
- Избранное по-прежнему в Room, обновляется через Flow
- Локальный UI: подсказка поиска на `mutableStateOf`, остальное - `StateFlow`

**Тесты:** 22 юнит, 6 instrumented (refresh без сети, FilterChips)

**PR:** #3

---

## Скриншоты

![Загрузка](screenshots/loading.png)
![Ошибка](screenshots/error.png)
![Список](screenshots/list.png)
![Пусто](screenshots/empty.png)
![Детали](screenshots/detail.png)
![Избранное](screenshots/favorites.png)
