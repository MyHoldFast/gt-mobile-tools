# GT Mobile Tools 4.3

[English](#english) | [Русский](#русский)

## English

A J2ME translator: translate text, listen to it and dictate it by voice. The app talks to its own small server, which does the actual work.

On S60 touch phones the interface now scales to the screen, runs in full screen and the language list scrolls with your finger, so everything is readable and easy to tap.

**What's inside**

- `dist/` - the built JAR, just install it on your phone.
- `GT_Mobile_Tools/` - sources and resources. To compile, run this in that folder:

  ```
  javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath cldcapi11.jar:midpapi20.jar:jsr75.jar:mmapi.jar -d classes *.java
  ```

  Then preverify the classes and pack them into a JAR together with the resources and `META-INF/MANIFEST.MF`.
- `server/gt_server.py` - the server. Needs `requests`, `gemini_webapi` and `ffmpeg`, start it with `python3 gt_server.py`, it listens on port 8090.

**Voice input and Gemini**

Speech recognition goes through Gemini (the `gemini_webapi` library), so the server needs the `__Secure-1PSID` and `__Secure-1PSIDTS` cookies from a signed-in gemini.google.com session (browser DevTools - Application - Cookies). Put them into `gt_server.py` instead of `YOUR_1PSID` / `YOUR_1PSIDTS`, or start the server like this:

```
GEMINI_1PSID=... GEMINI_1PSIDTS=... python3 gt_server.py
```

Translation and text-to-speech don't need cookies.

**Server address**

By default the app connects to `179.254.142.53:8090`. If you run your own server, change it in the app: Menu - Settings - Server.

## Русский

J2ME-переводчик: переводит текст, озвучивает его и понимает голосовой ввод. Всю работу делает небольшой сервер, к которому подключается приложение.

На сенсорных S60 интерфейс теперь подстраивается под экран, работает в полноэкранном режиме, а список языков листается пальцем - всё крупно и легко попадать.

**Что в архиве**

- `dist/` - собранный JAR, просто ставишь на телефон.
- `GT_Mobile_Tools/` - исходники и ресурсы. Собираются из этой папки:

  ```
  javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath cldcapi11.jar:midpapi20.jar:jsr75.jar:mmapi.jar -d classes *.java
  ```

  Потом preverify и упаковка в JAR вместе с ресурсами и `META-INF/MANIFEST.MF`.
- `server/gt_server.py` - сервер. Нужны `requests`, `gemini_webapi` и `ffmpeg`, запуск `python3 gt_server.py`, слушает порт 8090.

**Голосовой ввод и Gemini**

Распознавание речи идёт через Gemini (библиотека `gemini_webapi`), поэтому серверу нужны куки `__Secure-1PSID` и `__Secure-1PSIDTS` из залогиненной сессии gemini.google.com (DevTools браузера - Application - Cookies). Вписываешь их в `gt_server.py` вместо `YOUR_1PSID` / `YOUR_1PSIDTS` или запускаешь сервер так:

```
GEMINI_1PSID=... GEMINI_1PSIDTS=... python3 gt_server.py
```

Для перевода и озвучки куки не нужны.

**Адрес сервера**

По умолчанию приложение подключается к `179.254.142.53:8090`. Если поднимаешь свой сервер, адрес меняется в приложении: Меню - Настройки - Сервер.
