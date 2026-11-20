# textDark Mobile (Android)

Мобильная версия textDark: стеклянный интерфейс (blur), круглые кнопки, два режима
(Программирование / Блокнот), вкладки, .txtd, таблицы, картинки, поиск и замена, терминал Python.

## Сборка APK без компьютера с Android Studio (GitHub Actions)
1. Создайте новый репозиторий на GitHub, загрузите в него содержимое этой папки (включая `.github`).
2. Вкладка **Actions** → **Build APK** → **Run workflow**.
3. Через 3–5 минут откройте завершённый запуск → **Artifacts** → `textDark-apk` → скачайте и распакуйте.
4. Перенесите `app-release.apk` на телефон и откройте. Разрешите установку из неизвестных источников.

## Сборка в Android Studio
1. Установите Android Studio (последняя версия), **File → Open** → папка проекта.
2. Дождитесь Gradle Sync (SDK Platform 36 установится по запросу).
3. **Build → Build Bundle(s)/APK(s) → Build APK(s)**.
4. APK: `app/build/outputs/apk/release/app-release.apk` (или `debug/` для debug-сборки).

## Сборка из терминала
Нужны JDK 17, Android SDK (ANDROID_HOME) и Gradle 8.11+:
    gradle assembleRelease

## Совместимость .txtd
Ключ и формат взяты из main.py textDark 3.3: `TEXTDARK\x02 | nonce(12) | AES-256-GCM`, AAD = заголовок.
Внутри — обычный текст или `TEXTDARK_RICH_HTML_V1` + HTML (режим «Блокнот»: форматирование, таблицы, картинки).
Файлы открываются и сохраняются в обе стороны (проверено перекрёстным тестом Python ↔ JS).
Если в ПК-версии поменяется ключ, замените `TXTD_KEY_HEX` в `app/src/main/assets/index.html`.

## Ограничения
- Python в терминале грузится из интернета (Pyodide) при первом запуске; кнопки «Стоп» нет.
- Блюр: полноценный `backdrop-filter` работает в WebView на Android 9+.
