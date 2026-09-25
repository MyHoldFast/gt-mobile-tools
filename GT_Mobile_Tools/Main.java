import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

public final class Main extends MIDlet {
    public static Main instance;
    public static MainScreen mainScreen;
    public static LanguageScreen languageScreen;
    private static Display display;

    public Main() {
        instance = this;
    }

    protected void startApp() {
        if (display != null) {
            return;
        }
        display = Display.getDisplay(this);
        Settings.load(this);
        L10n.setEnglish(Settings.english);
        mainScreen = new MainScreen();
        languageScreen = new LanguageScreen();
        show(mainScreen);
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
        SpeechPlayer.stop();
        ServerConnection.close();
    }

    public static void show(Displayable screen) {
        display.setCurrent(screen);
    }

    public static void showMain() {
        show(mainScreen);
    }

    public static String version() {
        String version = instance.getAppProperty("MIDlet-Version");
        return version != null ? version : "";
    }

    public static void exit() {
        instance.destroyApp(true);
        instance.notifyDestroyed();
    }
}
