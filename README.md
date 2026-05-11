# Countries Explorer (Android)

## Описание

Android-приложение для просмотра стран мира. REST Countries API. Экран избранного с сохранением в Room (данные переживают перезапуск приложения).

## Стек и архитектура

- Kotlin + Jetpack Compose + Coroutines
- Retrofit + OkHttp + Gson
- DI: Hilt
- БД: Room (таблица favorites)
- Архитектура: data (API, local) - Repository - ViewModel - UI

## API (REST Countries)

- Base URL: https://restcountries.com/
- List: GET /v3.1/region/{region} (Africa, Americas, Asia, Europe, Oceania, Antarctic)
- Search: GET /v3.1/name/{name}
- Detail: GET /v3.1/alpha/{code}
- API ключ не требуется

## Room

Таблица: **favorites** (поля: code, name, country)

Избранное сохраняется между перезапусками приложения.

Проверка: добавить страну в избранное - закрыть приложение - запустить - избранное на месте.

## ДЗ 5

## PR 

Pull request с рабочим кодом и описанием по требованиям курса.

## Сколько сделано юнит-тестов: 16 

(`app/src/test`), интеграционных 7 (`app/src/androidTest`). В `test/` - JVM, ViewModel, репозиторий + MockWebServer, фейковый DAO, без Hilt/Compose/Navigation. В `androidTest/` - Hilt, Compose, навигация, Room in-memory, мок-сервер через тестовый модуль.

## Юнит (16) 

`CountriesListViewModelTest` (6), `CountryDetailViewModelTest` (3), `FavoritesSharedViewModelTest` (2), `CountriesRepositoryTest` (2), `FavoriteEntityTest` (1), `CountryCodeHelperTest` (2).

## Интеграция (7) 

`MainActivityComposeTest` (2), `NavigationComposeInstrumentedTest` (3), `FavoritesRoomInstrumentedTest` (2).

## Сценарии 

Загрузка списка и деталей; ошибка сети - повтор - успех; пустой список и пустой поиск - `Empty`; debounce поиска; фильтр избранного через `combine` без лишнего `getAllCountries`; избранное и Room (дубликат по коду, Flow после insert); репозиторий + MockWebServer; UI: список - детали, ошибка детали - «Повторить», подсказка поиска (`mutableStateOf`).

## Flow в тестах 

для `CountriesListViewModel.uiState` проверяются цепочки `Loading`/`Success`/`Empty`/`Error` и смена данных при фильтрах; для `favoriteEntries` - Turbine (`[]` - список с записью); для `favorites` и Room `Flow` - обновление после insert/toggle. Где `SharingStarted.WhileSubscribed`, в тестах держится активный коллектор.

## Запуск 

`gradlew.bat :app:testDebugUnitTest`, `gradlew.bat :app:connectedDebugAndroidTest` (эмулятор или устройство).

## ДЗ 6

В списке стран: `combine`, `merge`, `debounce`, `distinctUntilChanged`, `flatMapLatest`, `MutableSharedFlow`, `stateIn` + `WhileSubscribed`; настройки списка - DataStore (`ListPreferencesRepository`); избранное - Room. Локальный UI на экране списка: **`mutableStateOf`** (подсказка по поиску), остальное - `StateFlow` + `collectAsState()`.

## Сборка (JDK 17)

Windows: `gradlew.bat assembleDebug`

## Скриншоты 

![Загрузка](screenshots/loading.png)
![Ошибка загрузки](screenshots/error.png)
![Список стран](screenshots/list.png)
![Ничего не найдено](screenshots/empty.png)
![Детали страны](screenshots/detail.png)
![Избранное](screenshots/favorites.png)
