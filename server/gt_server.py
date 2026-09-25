import os
import socketserver
import struct
import subprocess
import tempfile

import requests

HOST = "0.0.0.0"
PORT = int(os.environ.get("GT_PORT", "8090"))
HF_TOKEN = os.environ.get("HF_TOKEN", "YOUR_HF_TOKEN")
HF_STT_URL = "https://router.huggingface.co/hf-inference/models/openai/whisper-large-v3-turbo"
TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single"
TTS_URL = "https://translate.google.com/translate_tts"
USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36"

IDLE_TIMEOUT = 600
MAX_FRAME = 2 * 1024 * 1024
TTS_CHUNK = 200

TRANSLATE, SPEAK, RECOGNIZE = 1, 2, 3

http = requests.Session()
http.headers["User-Agent"] = USER_AGENT


def java_utf(text):
    text = "".join(
        c if ord(c) < 0x10000 else chr(0xD7C0 + (ord(c) >> 10)) + chr(0xDC00 + (ord(c) & 0x3FF))
        for c in text
    )
    data = text.encode("utf-8", "surrogatepass").replace(b"\x00", b"\xc0\x80")
    while len(data) > 0xFFFF:
        text = text[: len(text) * 0xFFFF // len(data) - 1]
        data = text.encode("utf-8", "surrogatepass").replace(b"\x00", b"\xc0\x80")
    return struct.pack(">H", len(data)) + data


def read_utf(buf, pos):
    (size,) = struct.unpack_from(">H", buf, pos)
    pos += 2
    raw = buf[pos : pos + size].replace(b"\xc0\x80", b"\x00")
    text = raw.decode("utf-8", "surrogatepass").encode("utf-16", "surrogatepass").decode("utf-16", "replace")
    return text, pos + size


def read_strings(buf, count):
    values, pos = [], 0
    for _ in range(count):
        value, pos = read_utf(buf, pos)
        values.append(value)
    return values


def google_translate(source, target, text):
    resp = http.post(
        TRANSLATE_URL,
        data={"client": "gtx", "sl": source, "tl": target, "dt": "t", "q": text},
        timeout=30,
    )
    resp.raise_for_status()
    data = resp.json()
    translation = "".join(part[0] for part in data[0] or [] if part and part[0])
    detected = data[2] if len(data) > 2 and isinstance(data[2], str) else None
    return translation, detected


def split_text(text, limit):
    chunks, current = [], ""
    for word in text.split():
        while len(word) > limit:
            if current:
                chunks.append(current)
                current = ""
            chunks.append(word[:limit])
            word = word[limit:]
        if current and len(current) + 1 + len(word) > limit:
            chunks.append(current)
            current = word
        else:
            current = f"{current} {word}" if current else word
    if current:
        chunks.append(current)
    return chunks


def text_to_speech(lang, text):
    if lang == "auto":
        lang = google_translate("auto", "en", text)[1] or "en"
    audio = b""
    for chunk in split_text(text, TTS_CHUNK):
        resp = http.get(
            TTS_URL,
            params={"ie": "UTF-8", "q": chunk, "tl": lang, "client": "tw-ob"},
            timeout=30,
        )
        resp.raise_for_status()
        audio += resp.content
    return audio


def speech_to_text(audio):
    with tempfile.TemporaryDirectory() as tmp:
        src = os.path.join(tmp, "in")
        dst = os.path.join(tmp, "out.wav")
        with open(src, "wb") as f:
            f.write(audio)
        subprocess.run(
            ["ffmpeg", "-i", src, "-vn", "-y", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1", "-f", "wav", dst],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        if not os.path.exists(dst):
            raise ValueError("Audio conversion failed")
        with open(dst, "rb") as f:
            wav = f.read()
    resp = http.post(
        HF_STT_URL,
        headers={"Authorization": f"Bearer {HF_TOKEN}", "Content-Type": "audio/wav"},
        data=wav,
        timeout=60,
    )
    resp.raise_for_status()
    return resp.json().get("text", "").strip()


def dispatch(command, payload):
    if command == TRANSLATE:
        source, target, text = read_strings(payload, 3)
        return java_utf(google_translate(source, target, text)[0])
    if command == SPEAK:
        lang, text = read_strings(payload, 2)
        return text_to_speech(lang, text)
    if command == RECOGNIZE:
        return java_utf(speech_to_text(payload))
    raise ValueError(f"Unknown command {command}")


def read_exact(stream, size):
    data = stream.read(size)
    return data if len(data) == size else None


class Handler(socketserver.StreamRequestHandler):
    timeout = IDLE_TIMEOUT

    def handle(self):
        try:
            while True:
                header = read_exact(self.rfile, 5)
                if header is None:
                    return
                command, size = struct.unpack(">BI", header)
                if size > MAX_FRAME:
                    return
                payload = read_exact(self.rfile, size)
                if payload is None:
                    return
                try:
                    status, body = 0, dispatch(command, payload)
                except requests.HTTPError as e:
                    status, body = 1, java_utf(f"HTTP {e.response.status_code}")
                except Exception as e:
                    status, body = 1, java_utf(str(e)[:200] or type(e).__name__)
                self.wfile.write(struct.pack(">BI", status, len(body)) + body)
                self.wfile.flush()
        except OSError:
            return


class Server(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True


if __name__ == "__main__":
    with Server((HOST, PORT), Handler) as server:
        server.serve_forever()
