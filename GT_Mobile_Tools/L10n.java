public final class L10n {
    public static final int OK = 0;
    public static final int BACK = 1;
    public static final int ENTER_TEXT = 2;
    public static final int TRANSLATION_TITLE = 3;
    public static final int SWAP = 4;
    public static final int FILE = 5;
    public static final int ABOUT = 6;
    public static final int EXIT = 7;
    public static final int ORIGINAL = 8;
    public static final int TRANSLATION = 9;
    public static final int MENU = 10;
    public static final int CANCEL_SHORT = 11;
    public static final int CANCEL = 12;
    public static final int PRESS_START = 13;
    public static final int SPEAK_NOW = 14;
    public static final int STOP = 15;
    public static final int START = 16;
    public static final int APP_NAME = 17;
    public static final int LOADING = 18;
    public static final int SERVER_ERROR = 19;
    public static final int SELECT_LANGUAGE = 20;
    public static final int CHOOSE = 21;
    public static final int SAVING = 22;
    public static final int OPEN = 23;
    public static final int SAVE = 24;
    public static final int ENTER_FILE_NAME = 25;
    public static final int SELECT = 26;
    public static final int SETTINGS = 27;
    public static final int INTERFACE_LANGUAGE = 28;
    public static final int SERVER = 29;
    public static final int SERVER_ADDRESS = 30;

    private static final String[] RU = {
        "Ок",
        "Назад",
        "Введите текст для перевода:",
        "Перевод:",
        "Изм. напр. перевода",
        "Файл",
        "О программе",
        "Выход",
        "Оригинал",
        "Перевод",
        "Меню",
        "Отм.",
        "Отмена",
        "Нажмите старт",
        "Говорите",
        "Стоп",
        "Старт",
        "Переводчик Google",
        "Загрузка...",
        "Сервер недоступен. Попробуйте позднее",
        "Выберите язык",
        "Выбрать",
        "Сохранение...",
        "Открыть",
        "Сохр.",
        "Введите название для файла (без расширения):",
        "Выберите",
        "Настройки",
        "Язык интерфейса",
        "Сервер",
        "Адрес сервера (хост:порт):"
    };

    private static final String[] EN = {
        "Ok",
        "Back",
        "Enter the text:",
        "Translation:",
        "Change direction",
        "File",
        "About",
        "Exit",
        "Original",
        "Translation",
        "Menu",
        "Canc.",
        "Cancel",
        "Click Start",
        "Speak",
        "Stop",
        "Start",
        "Google Translate",
        "Loading...",
        "Server unavailable. Try again later",
        "Select a language",
        "Choose",
        "Saving...",
        "Open",
        "Save",
        "Enter the file name (without extension):",
        "Select",
        "Settings",
        "Interface language",
        "Server",
        "Server address (host:port):"
    };

    public static final String[] CODES = {
        "auto", "az", "sq", "en", "ar", "hy", "af", "eu", "be", "bg",
        "cy", "hu", "vi", "gl", "nl", "el", "ka", "da", "iw", "yi",
        "id", "ga", "is", "es", "it", "ca", "zh-CN", "ko", "ht", "la",
        "lv", "lt", "mk", "ms", "mt", "de", "no", "fa", "pl", "pt",
        "ro", "ru", "sr", "sk", "sl", "sw", "tl", "th", "tr", "uk",
        "ur", "fi", "fr", "hi", "hr", "cs", "sv", "et", "ja"
    };

    private static final String[] LANGUAGES_RU = {
        "Авто", "Азербайджанский", "Албанский", "Английский", "Арабский",
        "Армянский", "Африкаанс", "Баскский", "Белорусский", "Болгарский",
        "Валлийский", "Венгерский", "Вьетнамский", "Галисийский", "Голландский",
        "Греческий", "Грузинский", "Датский", "Иврит", "Идиш",
        "Индонезийский", "Ирландский", "Исландский", "Испанский", "Итальянский",
        "Каталанский", "Китайский", "Корейский", "Креольский (Гаити)", "Латынь",
        "Латышский", "Литовский", "Македонский", "Малайский", "Мальтийский",
        "Немецкий", "Норвежский", "Персидский", "Польский", "Португальский",
        "Румынский", "Русский", "Сербский", "Словацкий", "Словенский",
        "Суахили", "Тагальский", "Тайский", "Турецкий", "Украинский",
        "Урду", "Финский", "Французский", "Хинди", "Хорватский",
        "Чешский", "Шведский", "Эстонский", "Японский"
    };

    private static final String[] LANGUAGES_EN = {
        "Auto", "Azerbaijani", "Albanian", "English", "Arabic",
        "Armenian", "Afrikaans", "Basque", "Belarusian", "Bulgarian",
        "Welsh", "Hungarian", "Vietnamese", "Galician", "Dutch",
        "Greek", "Georgian", "Danish", "Hebrew", "Yiddish",
        "Indonesian", "Irish", "Icelandic", "Spanish", "Italian",
        "Catalan", "Chinese", "Korean", "Haitian Creole", "Latin",
        "Latvian", "Lithuanian", "Macedonian", "Malay", "Maltese",
        "German", "Norwegian", "Persian", "Polish", "Portuguese",
        "Romanian", "Russian", "Serbian", "Slovak", "Slovenian",
        "Swahili", "Tagalog", "Thai", "Turkish", "Ukrainian",
        "Urdu", "Finnish", "French", "Hindi", "Croatian",
        "Czech", "Swedish", "Estonian", "Japanese"
    };

    private static boolean english;

    public static void setEnglish(boolean value) {
        english = value;
    }

    public static boolean isEnglish() {
        return english;
    }

    public static String get(int id) {
        return english ? EN[id] : RU[id];
    }

    public static String language(int index) {
        return english ? LANGUAGES_EN[index] : LANGUAGES_RU[index];
    }
}
