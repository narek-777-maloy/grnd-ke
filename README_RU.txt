ADMIN KEYBOARD — ОБЛАЧНАЯ СБОРКА APK

Для сборки нужен только браузер на телефоне. Компьютер и Android Studio не нужны.

1. Открой https://github.com и войди/создай аккаунт.
2. Создай новый репозиторий (New repository). Для простоты выбери Public.
3. В репозитории нажми Add file → Upload files и загрузи файл AdminKeyboard.zip из этого набора.
4. Создай файл workflow:
   Add file → Create new file
   Имя файла: .github/workflows/build.yml
   Вставь содержимое файла build.yml из папки .github/workflows этого набора и нажми Commit changes.
5. Открой вкладку Actions → Build AdminKeyboard APK → Run workflow.
   Если workflow автоматически запустился после добавления файлов, просто открой последний запуск.
6. Дождись зелёной галочки. Открой запуск → Artifacts → AdminKeyboard-debug-apk → скачай ZIP.
7. Распакуй скачанный ZIP на телефоне. Внутри будет app-debug.apk. Нажми на APK и установи.

Важно:
- Это debug APK для установки и проверки, не публикационная версия Play Store.
- При установке Android может попросить разрешить установку приложений из браузера/файлового менеджера.
- Не публикуй в репозитории личные данные или пароли. Исходный проект не содержит ключей подписи.
