# GT Mobile Tools 4.3

[English](#english) | [Русский](#русский)

## English

J2ME translator: text translation, text-to-speech, speech recognition.

- `dist/` - the built JAR, just install it on your phone.
- `GT_Mobile_Tools/` - sources and resources. Compile from this folder:

  ```
  javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath cldcapi11.jar:midpapi20.jar:jsr75.jar:mmapi.jar -d classes *.java
  ```

  Then preverify and pack into a JAR together with the resources and `META-INF/MANIFEST.MF`.
- `server/gt_server.py` - the app's server, run with `python3 gt_server.py` (requires `requests` and `ffmpeg`, port 8090).

**Hugging Face token.** `gt_server.py` contains the placeholder `YOUR_HF_TOKEN` - replace it with your own token (https://huggingface.co/settings/tokens) or pass it via `HF_TOKEN=...`. Speech recognition needs it, translation and text-to-speech work without it.

**Server address** defaults to `94.159.98.141:8090` and can be changed in the app: Menu → Settings → Server.

## Русский

J2ME-переводчик: перевод, озвучка, распознавание речи.

- `dist/` - собранный JAR, просто ставишь на телефон.
- `GT_Mobile_Tools/` - исходники и ресурсы. Компилируется из этой папки:

  ```
  javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath cldcapi11.jar:midpapi20.jar:jsr75.jar:mmapi.jar -d classes *.java
  ```

  Затем preverify и упаковка в JAR вместе с ресурсами и `META-INF/MANIFEST.MF`.
- `server/gt_server.py` - сервер для приложения, запуск: `python3 gt_server.py` (нужны `requests` и `ffmpeg`, порт 8090).

**Токен Hugging Face.** В `gt_server.py` вместо токена стоит `YOUR_HF_TOKEN` - замени на свой (https://huggingface.co/settings/tokens) или передай через `HF_TOKEN=...`. Он нужен для распознавания речи, перевод и озвучка работают и без него.

**Адрес сервера** по умолчанию `94.159.98.141:8090`, меняется в приложении: Меню → Настройки → Сервер.
