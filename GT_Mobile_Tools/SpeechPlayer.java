import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

public final class SpeechPlayer implements Runnable {
    private static Player player;
    private static String cachedKey;
    private static byte[] cachedAudio;

    private final String text;
    private final String language;

    private SpeechPlayer(String text, String language) {
        this.text = text;
        this.language = language;
    }

    public static void speak(String text, String language) {
        new Thread(new SpeechPlayer(text, language)).start();
    }

    public static synchronized void stop() {
        if (player != null) {
            player.close();
            player = null;
        }
    }

    public void run() {
        try {
            String key = language + '\n' + text;
            if (!key.equals(cachedKey)) {
                String source = "uk".equals(language) ? TextUtils.ukrainianI(text) : text;
                byte[] payload = ServerConnection.pack(new String[] {language, source});
                cachedAudio = ServerConnection.request(ServerConnection.SPEAK, payload);
                cachedKey = key;
            }
            play(cachedAudio);
        } catch (ServerException e) {
            MainScreen.resultText = e.getMessage();
        } catch (IOException e) {
            MainScreen.resultText = L10n.get(L10n.SERVER_ERROR);
        } catch (Exception e) {
            MainScreen.resultText = e.toString();
        }
        MainScreen.setWaiting(false);
    }

    private static synchronized void play(byte[] audio) throws Exception {
        stop();
        player = Manager.createPlayer(new ByteArrayInputStream(audio), "audio/mpeg");
        player.realize();
        player.prefetch();
        player.start();
    }
}
