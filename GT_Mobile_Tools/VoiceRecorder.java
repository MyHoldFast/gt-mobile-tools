import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

public final class VoiceRecorder implements Runnable {
    private static final int BEGIN = 0;
    private static final int FINISH = 1;
    private static final int CANCEL = 2;

    private static Player player;
    private static RecordControl control;
    private static ByteArrayOutputStream buffer;

    private final int task;

    private VoiceRecorder(int task) {
        this.task = task;
    }

    public static void start() {
        new Thread(new VoiceRecorder(BEGIN)).start();
    }

    public static void finish() {
        new Thread(new VoiceRecorder(FINISH)).start();
    }

    public static void cancel() {
        new Thread(new VoiceRecorder(CANCEL)).start();
    }

    public void run() {
        if (task == BEGIN) {
            begin();
            return;
        }
        byte[] audio = end();
        if (task == FINISH) {
            recognize(audio);
        }
    }

    private static synchronized void begin() {
        SpeechPlayer.stop();
        release();
        if (!open("capture://audio?encoding=amr")) {
            open("capture://audio");
        }
    }

    private static boolean open(String locator) {
        try {
            player = Manager.createPlayer(locator);
            player.realize();
            control = (RecordControl) player.getControl("RecordControl");
            buffer = new ByteArrayOutputStream();
            control.setRecordStream(buffer);
            control.startRecord();
            player.start();
            return true;
        } catch (Exception e) {
            release();
            return false;
        }
    }

    private static synchronized byte[] end() {
        try {
            if (control != null) {
                control.stopRecord();
                control.commit();
            }
        } catch (Exception e) {
        }
        byte[] audio = buffer != null ? buffer.toByteArray() : new byte[0];
        release();
        return audio;
    }

    private static void release() {
        if (player != null) {
            player.close();
        }
        player = null;
        control = null;
        buffer = null;
    }

    private static void recognize(byte[] audio) {
        String text = "";
        if (audio.length > 0) {
            try {
                text = ServerConnection.text(ServerConnection.request(ServerConnection.RECOGNIZE, audio));
            } catch (ServerException e) {
                MainScreen.resultText = e.getMessage();
            } catch (IOException e) {
                MainScreen.resultText = L10n.get(L10n.SERVER_ERROR);
            }
        }
        MainScreen.sourceText = text.trim();
        MainScreen.retranslate();
    }
}
