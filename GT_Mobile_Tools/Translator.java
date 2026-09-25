import java.io.IOException;

public final class Translator implements Runnable {
    private static final int MAX_TEXT = 5000;
    private static int generation;

    private final int id;
    private final String text;
    private final String from;
    private final String to;

    private Translator(int id, String text, String from, String to) {
        this.id = id;
        this.text = text;
        this.from = from;
        this.to = to;
    }

    public static synchronized void start(String text, String from, String to) {
        new Thread(new Translator(++generation, text, from, to)).start();
    }

    private static synchronized boolean isCurrent(int id) {
        return id == generation;
    }

    public void run() {
        String source = "uk".equals(from) ? TextUtils.ukrainianI(text) : text;
        if (source.length() > MAX_TEXT) {
            source = source.substring(0, MAX_TEXT);
        }
        String result;
        try {
            byte[] payload = ServerConnection.pack(new String[] {from, to, source});
            result = ServerConnection.text(ServerConnection.request(ServerConnection.TRANSLATE, payload));
        } catch (ServerException e) {
            result = e.getMessage();
        } catch (IOException e) {
            result = L10n.get(L10n.SERVER_ERROR);
        }
        if (isCurrent(id)) {
            MainScreen.resultText = result;
            MainScreen.refresh();
        }
    }
}
