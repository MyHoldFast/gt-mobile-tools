import java.util.Random;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public final class MainScreen extends Canvas implements CommandListener {
    private static final String TITLE = "GT Mobile Tools";
    private static final String[] ABOUT_LINES = {"by HoldFast & JGDger", "Лан ты не урчи"};

    private static final int FOCUS_NONE = 0;
    private static final int FOCUS_SOURCE = 1;
    private static final int FOCUS_FROM = 2;
    private static final int FOCUS_TO = 3;
    private static final int FOCUS_RESULT = 4;
    private static final int FOCUS_SPEAK = 5;
    private static final int FOCUS_MIC = 6;

    private static final int MODE_MAIN = 0;
    private static final int MODE_MENU = 1;
    private static final int MODE_SETTINGS = 2;
    private static final int MODE_SPEAK = 3;
    private static final int MODE_RECORD = 4;
    private static final int MODE_ABOUT = 5;
    private static final int MODE_INTERFACE_LANGUAGE = 6;

    private static final int[] MENU_ITEMS = {L10n.FILE, L10n.SWAP, L10n.ABOUT, L10n.SETTINGS, L10n.EXIT};
    private static final int[] SETTINGS_ITEMS = {L10n.INTERFACE_LANGUAGE, L10n.SERVER};

    private static final int BUTTON_WIDTH = 38;
    private static final int PLAY = 0;
    private static final int MIC = 1;
    private static final int[][] BUTTON_COLORS = {
        {0x2E8B22, 0x57C94A, 0x1B5E14, 0x145010},
        {0xC62828, 0xF2503F, 0x8E1414, 0x6E0E0E}
    };
    private static final int DIALOG_WIDTH = 149;
    private static final int DIALOG_HEIGHT = 215;

    public static String sourceText = "";
    public static String resultText = "";
    public static int fromLanguage = 3;
    public static int toLanguage = 41;
    private static boolean waiting;

    private final Image headSource = Ui.image("/res/head.png");
    private final Image shadowSource = Ui.image("/res/shadow.png");
    private final Image rowTile = Ui.image("/res/el.png");
    private final Image softTile = Ui.image("/res/open.png");
    private final Random random = new Random();

    private Image head;
    private Image shadow;
    private Image arrow;
    private Image submenuOn;
    private Image submenuOff;
    private Image spinner;
    private Image microphone;
    private Image dialogButton;
    private Image flagRu;
    private Image flagEn;
    private Image softPressed;
    private Image fromBar;
    private Image toBar;
    private Image menuBar;
    private Image speakBar;

    private int width = -1;
    private int height;
    private int imageScale;
    private int buttonWidth;
    private int dialogWidth;
    private int dialogHeight;
    private int mode;
    private int focus = FOCUS_NONE;
    private int menuIndex;
    private int settingsIndex;
    private int speakIndex;
    private int interfaceIndex;
    private boolean recording;
    private boolean scribble;
    private int dragX;
    private int dragY;

    private int menuSoftWidth;
    private int sourceBoxHeight;
    private int resultBoxY;
    private int resultBoxHeight;
    private int languageBoxY;
    private int languageBoxHeight;
    private int fromBoxWidth;
    private int toBoxX;
    private int menuWidth;
    private int speakX;
    private int speakWidth;
    private int recordDialogWidth;

    private TextBox sourceBox;
    private TextBox resultBox;
    private TextBox serverBox;
    private Command ok;
    private Command back;

    public MainScreen() {
        setFullScreenMode(true);
        createTextBoxes();
        interfaceIndex = L10n.isEnglish() ? 1 : 0;
        mode = Settings.exists ? MODE_MAIN : MODE_INTERFACE_LANGUAGE;
    }

    public static void retranslate() {
        if (sourceText.trim().length() > 0) {
            resultText = L10n.get(L10n.LOADING);
            Translator.start(sourceText, L10n.CODES[fromLanguage], L10n.CODES[toLanguage]);
        }
        refresh();
    }

    public static void setWaiting(boolean value) {
        waiting = value;
        refresh();
    }

    public static void refresh() {
        Main.mainScreen.repaint();
    }

    private void createTextBoxes() {
        ok = new Command(L10n.get(L10n.OK), Command.OK, 1);
        back = new Command(L10n.get(L10n.BACK), Command.BACK, 2);
        sourceBox = new TextBox(L10n.get(L10n.ENTER_TEXT), "", 1024, TextField.ANY);
        resultBox = new TextBox(L10n.get(L10n.TRANSLATION_TITLE), "", 1024, TextField.ANY);
        serverBox = new TextBox(L10n.get(L10n.SERVER_ADDRESS), "", 64, TextField.ANY);
        sourceBox.addCommand(ok);
        sourceBox.addCommand(back);
        resultBox.addCommand(back);
        serverBox.addCommand(ok);
        serverBox.addCommand(back);
        sourceBox.setCommandListener(this);
        resultBox.setCommandListener(this);
        serverBox.setCommandListener(this);
    }

    private void showBox(TextBox box, String text) {
        box.setString("");
        int max = box.setMaxSize(Math.max(1024, text.length()));
        box.setString(text.length() > max ? text.substring(0, max) : text);
        Main.show(box);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == back) {
            Main.showMain();
        } else if (displayable == sourceBox) {
            sourceText = sourceBox.getString();
            Main.showMain();
            retranslate();
        } else if (displayable == serverBox) {
            Settings.setServer(serverBox.getString());
            ServerConnection.close();
            Main.showMain();
        }
    }

    private void setMode(int value) {
        mode = value;
        if (mode != MODE_ABOUT) {
            scribble = false;
        }
    }

    private void layout() {
        width = getWidth();
        height = getHeight();
        Ui.setScreen(width, height, hasPointerEvents());
        if (imageScale != Ui.SCALE) {
            imageScale = Ui.SCALE;
            buttonWidth = Ui.px(BUTTON_WIDTH);
            arrow = Ui.fit(Ui.image("/res/to.png"));
            submenuOn = Ui.fit(Ui.image("/res/cont.png"));
            submenuOff = Ui.fit(Ui.image("/res/contt.png"));
            spinner = Ui.fit(Ui.image("/res/wait.png"));
            microphone = Ui.fit(Ui.image("/res/micr.png"));
            dialogButton = Ui.fit(Ui.image("/res/button.png"));
            flagRu = Ui.fit(Ui.image("/rus.png"));
            flagEn = Ui.fit(Ui.image("/eng.png"));
            menuBar = null;
            speakBar = null;
            softPressed = null;
        }
        dialogWidth = Math.min(Ui.px(DIALOG_WIDTH), width);
        dialogHeight = Math.min(Ui.px(DIALOG_HEIGHT), height - 2 * Ui.BAR);
        head = Ui.scale(headSource, width, Ui.BAR);
        shadow = Ui.scale(shadowSource, width, height);
        scribble = false;
    }

    private void swapLanguages() {
        int language = fromLanguage;
        fromLanguage = toLanguage;
        toLanguage = language;
        retranslate();
    }

    private void activate(int target) {
        switch (target) {
            case FOCUS_SOURCE:
                showBox(sourceBox, sourceText);
                break;
            case FOCUS_RESULT:
                if (resultText.trim().length() > 0) {
                    showBox(resultBox, resultText);
                }
                break;
            case FOCUS_FROM:
                Main.languageScreen.open(true);
                break;
            case FOCUS_TO:
                Main.languageScreen.open(false);
                break;
            case FOCUS_SPEAK:
                setMode(MODE_SPEAK);
                break;
            case FOCUS_MIC:
                setMode(MODE_RECORD);
                break;
        }
    }

    private void tap(int target) {
        focus = target;
        activate(target);
    }

    private void menuAction(int index) {
        switch (MENU_ITEMS[index]) {
            case L10n.FILE:
                setMode(MODE_MAIN);
                new FileBrowser();
                break;
            case L10n.SWAP:
                setMode(MODE_MAIN);
                swapLanguages();
                break;
            case L10n.ABOUT:
                setMode(MODE_ABOUT);
                break;
            case L10n.SETTINGS:
                settingsIndex = 0;
                setMode(MODE_SETTINGS);
                break;
            case L10n.EXIT:
                Main.exit();
                break;
        }
    }

    private void settingsAction(int index) {
        switch (SETTINGS_ITEMS[index]) {
            case L10n.INTERFACE_LANGUAGE:
                interfaceIndex = L10n.isEnglish() ? 1 : 0;
                setMode(MODE_INTERFACE_LANGUAGE);
                break;
            case L10n.SERVER:
                setMode(MODE_MAIN);
                showBox(serverBox, Settings.serverAddress());
                break;
        }
    }

    private void applyInterfaceLanguage() {
        boolean english = interfaceIndex == 1;
        Settings.english = english;
        Settings.save();
        L10n.setEnglish(english);
        createTextBoxes();
        menuBar = null;
        speakBar = null;
        softPressed = null;
        setMode(MODE_MAIN);
    }

    private void speak() {
        setMode(MODE_MAIN);
        String text = speakIndex == 0 ? sourceText : resultText;
        int language = speakIndex == 0 ? fromLanguage : toLanguage;
        if (text.trim().length() > 0) {
            setWaiting(true);
            SpeechPlayer.speak(text, L10n.CODES[language]);
        }
    }

    private void toggleRecording() {
        if (!recording) {
            recording = true;
            VoiceRecorder.start();
        } else {
            recording = false;
            sourceText = L10n.get(L10n.LOADING);
            VoiceRecorder.finish();
            focus = FOCUS_NONE;
            setMode(MODE_MAIN);
        }
    }

    private void closeRecording() {
        if (recording) {
            recording = false;
            VoiceRecorder.cancel();
        }
        setMode(MODE_MAIN);
    }

    protected void keyPressed(int key) {
        int action = Ui.action(this, key);
        if (waiting) {
            if (action == Ui.SOFT_RIGHT) {
                setWaiting(false);
            }
            return;
        }
        switch (mode) {
            case MODE_MAIN:
                keyMain(action);
                break;
            case MODE_MENU:
                if (action == Ui.SOFT_LEFT || action == Ui.SOFT_RIGHT) {
                    setMode(MODE_MAIN);
                } else if (action == FIRE) {
                    menuAction(menuIndex);
                } else {
                    menuIndex = step(menuIndex, MENU_ITEMS.length, action);
                }
                break;
            case MODE_SETTINGS:
                if (action == Ui.SOFT_LEFT) {
                    setMode(MODE_MAIN);
                } else if (action == Ui.SOFT_RIGHT) {
                    setMode(MODE_MENU);
                } else if (action == FIRE) {
                    settingsAction(settingsIndex);
                } else {
                    settingsIndex = step(settingsIndex, SETTINGS_ITEMS.length, action);
                }
                break;
            case MODE_SPEAK:
                if (action == Ui.SOFT_LEFT || action == Ui.SOFT_RIGHT) {
                    setMode(MODE_MAIN);
                } else if (action == FIRE) {
                    speak();
                } else if (action == DOWN) {
                    speakIndex = 1;
                } else if (action == UP) {
                    speakIndex = 0;
                }
                break;
            case MODE_RECORD:
                if (action == Ui.SOFT_RIGHT) {
                    closeRecording();
                } else if (action == FIRE) {
                    toggleRecording();
                }
                break;
            case MODE_ABOUT:
                if (action == Ui.SOFT_LEFT || action == Ui.SOFT_RIGHT) {
                    setMode(MODE_MAIN);
                }
                break;
            case MODE_INTERFACE_LANGUAGE:
                if (action == FIRE) {
                    applyInterfaceLanguage();
                } else if (action == DOWN) {
                    interfaceIndex = 1;
                } else if (action == UP) {
                    interfaceIndex = 0;
                }
                break;
        }
        repaint();
    }

    private static int step(int index, int count, int action) {
        if (action == DOWN) {
            return (index + 1) % count;
        }
        if (action == UP) {
            return (index + count - 1) % count;
        }
        return index;
    }

    private void keyMain(int action) {
        switch (action) {
            case Ui.POUND:
                focus = FOCUS_MIC;
                setMode(MODE_RECORD);
                break;
            case Ui.STAR:
                focus = FOCUS_SPEAK;
                setMode(MODE_SPEAK);
                break;
            case Ui.SOFT_LEFT:
                setMode(MODE_MENU);
                break;
            case FIRE:
                if (focus == FOCUS_NONE) {
                    focus = FOCUS_SOURCE;
                } else {
                    activate(focus);
                }
                break;
            case RIGHT:
                if (focus == FOCUS_FROM) {
                    focus = FOCUS_TO;
                } else if (focus == FOCUS_SPEAK) {
                    focus = FOCUS_MIC;
                }
                break;
            case LEFT:
                if (focus == FOCUS_TO) {
                    focus = FOCUS_FROM;
                } else if (focus == FOCUS_MIC) {
                    focus = FOCUS_SPEAK;
                }
                break;
            case DOWN:
                if (focus < FOCUS_MIC) {
                    focus++;
                }
                break;
            case UP:
                focus = focus > FOCUS_SOURCE ? focus - 1 : FOCUS_SOURCE;
                break;
        }
    }

    protected void pointerPressed(int x, int y) {
        if (waiting) {
            return;
        }
        switch (mode) {
            case MODE_MAIN:
                pointerMain(x, y);
                break;
            case MODE_MENU:
            case MODE_SETTINGS:
                pointerMenu(x, y);
                break;
            case MODE_SPEAK:
                pointerSpeak(x, y);
                break;
            case MODE_RECORD:
                pointerRecord(x, y);
                break;
            case MODE_ABOUT:
                if (y > height - Ui.BAR && x < width / 3) {
                    setMode(MODE_MAIN);
                }
                break;
            case MODE_INTERFACE_LANGUAGE:
                pointerInterfaceLanguage(y);
                break;
        }
        repaint();
    }

    protected void pointerDragged(int x, int y) {
        if (mode == MODE_ABOUT && x > 0 && y > 0) {
            scribble = true;
            dragX = x;
            dragY = y;
            repaint();
        }
    }

    private void pointerMain(int x, int y) {
        int center = width / 2;
        if (y >= height - Ui.BAR) {
            if (x < menuSoftWidth) {
                setMode(MODE_MENU);
            } else if (x >= center - buttonWidth && x < center) {
                focus = FOCUS_SPEAK;
                setMode(MODE_SPEAK);
            } else if (x >= center && x < center + buttonWidth) {
                focus = FOCUS_MIC;
                setMode(MODE_RECORD);
            }
        } else if (y < Ui.BAR) {
            return;
        } else if (y < languageBoxY) {
            tap(FOCUS_SOURCE);
        } else if (y <= languageBoxY + languageBoxHeight) {
            if (x < 2 + fromBoxWidth) {
                tap(FOCUS_FROM);
            } else if (x > toBoxX) {
                tap(FOCUS_TO);
            } else {
                swapLanguages();
            }
        } else {
            tap(FOCUS_RESULT);
        }
    }

    private void pointerMenu(int x, int y) {
        int count = mode == MODE_MENU ? MENU_ITEMS.length : SETTINGS_ITEMS.length;
        int top = menuTop(count) + 2;
        if (x > menuWidth + Ui.px(8) || y < top || y >= top + count * Ui.ROW) {
            setMode(MODE_MAIN);
            return;
        }
        int index = (y - top) / Ui.ROW;
        if (mode == MODE_MENU) {
            menuIndex = index;
            menuAction(index);
        } else {
            settingsIndex = index;
            settingsAction(index);
        }
    }

    private void pointerSpeak(int x, int y) {
        int top = height - Ui.BAR - 2 * Ui.ROW;
        if (x < speakX - Ui.px(8) || x > speakX + speakWidth + Ui.px(8) || y < top || y >= height - Ui.BAR) {
            setMode(MODE_MAIN);
            return;
        }
        speakIndex = (y - top) / Ui.ROW;
        speak();
    }

    private void pointerRecord(int x, int y) {
        int left = (width - recordDialogWidth) / 2 + 1;
        int top = (height - dialogHeight) / 2 + 1;
        if (x > left && y > top && x < left + recordDialogWidth && y < top + dialogHeight) {
            toggleRecording();
        } else {
            closeRecording();
        }
    }

    private void pointerInterfaceLanguage(int y) {
        if (y >= height - Ui.BAR) {
            applyInterfaceLanguage();
            return;
        }
        if (y >= Ui.BAR) {
            interfaceIndex = y < height / 2 ? 0 : 1;
            applyInterfaceLanguage();
        }
    }

    protected void paint(Graphics g) {
        if (width != getWidth() || height != getHeight()) {
            layout();
        }
        Font font = Ui.font();
        g.setFont(font);
        int fontHeight = font.getHeight();
        menuSoftWidth = Math.max(font.stringWidth(L10n.get(L10n.MENU)) + Ui.px(10), width / 4);
        if (!scribble) {
            g.setColor(Ui.BACKGROUND);
            g.fillRect(0, 0, width, height);
        }
        g.drawImage(head, 0, 0, Ui.TOP_LEFT);
        g.drawImage(head, 0, height - Ui.BAR, Ui.TOP_LEFT);
        g.setColor(Ui.WHITE);
        g.drawString(Ui.truncateLeft(font, TITLE, width - Ui.px(4)), width / 2, (Ui.BAR - fontHeight) / 2, Ui.TOP_CENTER);

        if (mode == MODE_ABOUT) {
            paintAbout(g, font);
        } else if (mode == MODE_INTERFACE_LANGUAGE) {
            paintInterfaceLanguage(g, font);
        } else {
            paintTranslator(g, font);
        }
        if (waiting) {
            paintWaiting(g);
        }
    }

    private int ring() {
        return Math.max(2, Ui.px(2));
    }

    private void paintTextBox(Graphics g, int y, int h, boolean focused) {
        int w = width - 3;
        if (focused) {
            g.setColor(Ui.WHITE);
            g.fillRoundRect(1, y, w, h, 8, 8);
            g.setColor(Ui.FOCUS);
            for (int i = 0; i < ring(); i++) {
                g.drawRoundRect(1 + i, y + i, w - 2 * i, h - 2 * i, 8, 8);
            }
        } else {
            g.setColor(Ui.GREEN);
            g.drawRoundRect(2, y + 1, w - 2, h - 2, 5, 5);
            g.drawRoundRect(1, y, w, h, 8, 8);
        }
    }

    private Image paintLanguageBox(Graphics g, Image bar, int x, int w, boolean focused) {
        int h = languageBoxHeight;
        if (focused) {
            if (bar == null || bar.getWidth() != w || bar.getHeight() != h) {
                bar = Ui.strip(rowTile, w, h, Ui.BACKGROUND);
            }
            g.drawImage(bar, x, languageBoxY, Ui.TOP_LEFT);
            g.setColor(Ui.FOCUS);
            for (int i = 0; i < ring(); i++) {
                g.drawRoundRect(x + i, languageBoxY + i, w - 2 * i, h - 2 * i, 5, 5);
            }
        } else {
            g.setColor(Ui.BOX_FILL);
            g.fillRoundRect(x, languageBoxY, w, h, 5, 5);
            g.setColor(Ui.GREEN);
            g.drawRoundRect(x, languageBoxY, w, h, 5, 5);
        }
        return bar;
    }

    private void paintButton(Graphics g, int kind, int x, boolean focused, boolean active) {
        int inset = ring() + 1;
        int bx = x + inset;
        int by = height - Ui.BAR + inset;
        int bw = buttonWidth - 2 * inset;
        int bh = Ui.BAR - 2 * inset;
        int[] colors = BUTTON_COLORS[kind];
        if (focused || active) {
            g.setColor(Ui.FOCUS);
            for (int i = 1; i <= ring(); i++) {
                g.drawRoundRect(bx - i, by - i, bw + 2 * i, bh + 2 * i, bh + 2 * i, bh + 2 * i);
            }
        }
        int base = active ? colors[2] : focused ? colors[1] : colors[0];
        g.setColor(base);
        g.fillRoundRect(bx, by, bw, bh, bh, bh);
        g.setClip(bx, by, bw + 1, bh / 2);
        g.setColor(Ui.mix(base, Ui.WHITE, active ? 15 : 30));
        g.fillRoundRect(bx, by, bw, bh, bh, bh);
        g.setClip(0, 0, width, height);
        g.setColor(focused ? Ui.WHITE : colors[3]);
        g.drawRoundRect(bx, by, bw, bh, bh, bh);

        g.setColor(Ui.WHITE);
        int cx = bx + bw / 2;
        int cy = by + bh / 2;
        if (kind == PLAY) {
            int a = bh * 3 / 10;
            g.fillTriangle(cx - a * 3 / 4, cy - a, cx - a * 3 / 4, cy + a, cx + a, cy);
        } else {
            int line = Math.max(1, bh / 14);
            int capsuleW = Math.max(3, bh / 4);
            int capsuleH = bh * 2 / 5;
            int top = cy - bh * 7 / 20;
            g.fillRoundRect(cx - capsuleW / 2, top, capsuleW, capsuleH, capsuleW, capsuleW);
            int arcW = capsuleW + 2 * Math.max(2, bh / 9);
            int arcY = top + capsuleH / 4;
            for (int i = 0; i < line; i++) {
                g.drawArc(cx - arcW / 2 - i, arcY - i, arcW + 2 * i, capsuleH + 2 * i, 180, 180);
            }
            int stemTop = arcY + capsuleH + line - 1;
            int stemBottom = cy + bh * 7 / 20;
            g.fillRect(cx - line / 2, stemTop, line, stemBottom - stemTop);
            g.fillRect(cx - capsuleW / 2 - line, stemBottom - line + 1, capsuleW + 2 * line, line);
        }
    }

    private void paintTranslator(Graphics g, Font font) {
        int fontHeight = font.getHeight();
        int center = width / 2;
        int arrowWidth = arrow.getWidth();
        int footerY = height - Ui.BAR;
        int pad = Ui.px(5);

        paintButton(g, PLAY, center - buttonWidth, focus == FOCUS_SPEAK, mode == MODE_SPEAK || waiting);
        paintButton(g, MIC, center, focus == FOCUS_MIC, mode == MODE_RECORD);
        g.drawImage(arrow, center - arrowWidth / 2, height / 2 - arrow.getHeight() / 2, Ui.TOP_LEFT);

        languageBoxHeight = fontHeight + Ui.px(3);
        languageBoxY = height / 2 - languageBoxHeight / 2;
        fromBoxWidth = (width - arrowWidth) / 2 - 2;
        toBoxX = center - arrowWidth / 2 + arrowWidth + 1;
        int toBoxWidth = (width - arrowWidth) / 2 - 3;
        fromBar = paintLanguageBox(g, fromBar, 2, fromBoxWidth, focus == FOCUS_FROM);
        toBar = paintLanguageBox(g, toBar, toBoxX, toBoxWidth, focus == FOCUS_TO);

        sourceBoxHeight = languageBoxY - Ui.BAR - pad;
        paintTextBox(g, Ui.BAR + 1, sourceBoxHeight + 2, focus == FOCUS_SOURCE);

        resultBoxY = languageBoxY + languageBoxHeight + 3;
        resultBoxHeight = footerY - 3 - resultBoxY;
        paintTextBox(g, resultBoxY - 1, resultBoxHeight + 2, focus == FOCUS_RESULT);

        int softY = footerY + (Ui.BAR - fontHeight) / 2;
        String menu = L10n.get(L10n.MENU);
        boolean showMenu = center - buttonWidth > font.stringWidth(menu) + Ui.px(4);
        if ((mode == MODE_MENU || mode == MODE_SETTINGS) && showMenu) {
            if (softPressed == null) {
                softPressed = Ui.scale(softTile, font.stringWidth(menu) + Ui.px(3), Ui.ROW);
            }
            g.drawImage(softPressed, 0, footerY, Ui.TOP_LEFT);
        }
        g.setColor(Ui.WHITE);
        if (mode == MODE_SPEAK) {
            String cancel = L10n.get(L10n.CANCEL);
            if (font.stringWidth(cancel) > center - buttonWidth - Ui.px(4)) {
                cancel = L10n.get(L10n.CANCEL_SHORT);
            }
            g.drawString(cancel, width - Ui.px(2), softY, Ui.TOP_RIGHT);
        } else if (showMenu) {
            g.drawString(menu, Ui.px(2), softY, Ui.TOP_LEFT);
        }

        g.setColor(Ui.GREEN);
        Ui.drawWrapped(g, sourceText, pad, Ui.BAR + Ui.px(3), width - 2 * pad, sourceBoxHeight);
        Ui.drawWrapped(g, resultText, pad, resultBoxY + Ui.px(2), width - 2 * pad, resultBoxHeight);

        Font bold = Ui.boldFont();
        g.setFont(bold);
        int nameWidth = (width - arrowWidth) / 2 - 3;
        int nameY = languageBoxY + (languageBoxHeight - bold.getHeight()) / 2 + 1;
        String from = Ui.truncate(bold, L10n.language(fromLanguage), nameWidth);
        String to = Ui.truncate(bold, L10n.language(toLanguage), nameWidth);
        g.setColor(focus == FOCUS_FROM ? Ui.WHITE : Ui.GREEN);
        g.drawString(from, 2 + fromBoxWidth / 2, nameY, Ui.TOP_CENTER);
        g.setColor(focus == FOCUS_TO ? Ui.WHITE : Ui.GREEN);
        g.drawString(to, toBoxX + toBoxWidth / 2, nameY, Ui.TOP_CENTER);

        switch (mode) {
            case MODE_MENU:
                paintMenu(g, MENU_ITEMS, menuIndex);
                break;
            case MODE_SETTINGS:
                paintMenu(g, SETTINGS_ITEMS, settingsIndex);
                break;
            case MODE_SPEAK:
                paintSpeakPopup(g);
                break;
            case MODE_RECORD:
                paintRecord(g);
                break;
        }
    }

    private int menuTop(int count) {
        return height - Ui.BAR - (count * Ui.ROW + 2);
    }

    private static boolean hasSubmenu(int item) {
        return item == L10n.FILE || item == L10n.SETTINGS;
    }

    private void paintMenu(Graphics g, int[] items, int selected) {
        Font font = Ui.font();
        g.setFont(font);
        int arrowSpace = submenuOn.getWidth() + Ui.px(8);
        int w = 0;
        for (int i = 0; i < items.length; i++) {
            w = Math.max(w, font.stringWidth(L10n.get(items[i])) + (hasSubmenu(items[i]) ? arrowSpace : 0));
        }
        w += Ui.px(10);
        if (menuBar == null || menuWidth != w) {
            menuBar = Ui.strip(rowTile, w, Ui.ROW, Ui.MENU_FILL);
            menuWidth = w;
        }
        int top = menuTop(items.length);
        int boxHeight = items.length * Ui.ROW + 3;
        g.setColor(Ui.MENU_FILL);
        g.fillRoundRect(1, top, w + 3, boxHeight, 5, 5);
        g.drawImage(menuBar, 3, top + 2 + Ui.ROW * selected, Ui.TOP_LEFT);
        g.setColor(Ui.GREEN);
        g.drawRoundRect(1, top, w + 3, boxHeight, 5, 5);
        for (int i = 0; i < items.length; i++) {
            int y = top + 2 + Ui.ROW * i;
            g.setColor(i == selected ? Ui.WHITE : Ui.GREEN);
            g.drawString(L10n.get(items[i]), Ui.px(6), y + (Ui.ROW - font.getHeight()) / 2, Ui.TOP_LEFT);
            if (hasSubmenu(items[i])) {
                Image mark = i == selected ? submenuOn : submenuOff;
                g.drawImage(mark, w - mark.getWidth() - Ui.px(4), y + (Ui.ROW - mark.getHeight()) / 2, Ui.TOP_LEFT);
            }
        }
    }

    private void paintSpeakPopup(Graphics g) {
        Font font = Ui.font();
        g.setFont(font);
        int center = width / 2;
        int footerY = height - Ui.BAR;
        String original = L10n.get(L10n.ORIGINAL);
        String translation = L10n.get(L10n.TRANSLATION);
        int w = Math.max(font.stringWidth(original), font.stringWidth(translation)) + Ui.px(50);
        if (speakBar == null || speakWidth != w) {
            speakBar = Ui.strip(rowTile, w, Ui.ROW, Ui.BACKGROUND);
            speakWidth = w;
        }
        speakX = center - w / 2;
        int top = footerY - 2 * Ui.ROW - 2;
        g.setColor(Ui.MENU_FILL);
        g.fillRoundRect(speakX - 2, top, w + 3, 2 * Ui.ROW + 3, 5, 5);
        g.drawImage(speakBar, speakX, top + 2 + Ui.ROW * speakIndex, Ui.TOP_LEFT);
        g.setColor(Ui.GREEN);
        g.drawRoundRect(speakX - 2, top, w + 3, 2 * Ui.ROW + 3, 5, 5);
        int textOffset = (Ui.ROW - font.getHeight()) / 2;
        g.setColor(speakIndex == 0 ? Ui.WHITE : Ui.GREEN);
        g.drawString(original, center, top + 2 + textOffset, Ui.TOP_CENTER);
        g.setColor(speakIndex == 1 ? Ui.WHITE : Ui.GREEN);
        g.drawString(translation, center, top + 2 + Ui.ROW + textOffset, Ui.TOP_CENTER);
    }

    private void paintRecord(Graphics g) {
        Font font = Ui.font();
        g.setFont(font);
        String caption = L10n.get(recording ? L10n.SPEAK_NOW : L10n.PRESS_START);
        String label = L10n.get(recording ? L10n.STOP : L10n.START);
        recordDialogWidth = Math.max(dialogWidth, font.stringWidth(L10n.get(L10n.PRESS_START)) + Ui.px(10));
        Ui.drawDialog(g, shadow, recordDialogWidth, dialogHeight);
        g.drawImage(microphone, (width - microphone.getWidth()) / 2, (height - microphone.getHeight()) / 2, Ui.TOP_LEFT);
        int buttonY = (height + dialogHeight) / 2 - dialogButton.getHeight() - Ui.px(5);
        g.drawImage(dialogButton, (width - dialogButton.getWidth()) / 2, buttonY, Ui.TOP_LEFT);
        g.setColor(Ui.DIALOG_TEXT);
        g.drawString(caption, width / 2, (height - dialogHeight) / 2 + Ui.px(15), Ui.TOP_CENTER);
        g.setColor(Ui.WHITE);
        g.drawString(label, width / 2, buttonY + (dialogButton.getHeight() - font.getHeight()) / 2, Ui.TOP_CENTER);
    }

    private void paintWaiting(Graphics g) {
        Font font = Ui.font();
        g.setFont(font);
        String caption = L10n.get(L10n.LOADING);
        Ui.drawDialog(g, shadow, Math.max(dialogWidth, font.stringWidth(caption) + Ui.px(10)), dialogHeight);
        g.drawImage(spinner, (width - spinner.getWidth()) / 2, (height - spinner.getHeight()) / 2, Ui.TOP_LEFT);
        g.setColor(Ui.DIALOG_TEXT);
        g.drawString(caption, width / 2, height / 2 + spinner.getHeight() / 2 + Ui.px(20), Ui.TOP_CENTER);
    }

    private void paintAbout(Graphics g, Font font) {
        int fontHeight = font.getHeight();
        g.drawString(L10n.get(L10n.BACK), Ui.px(2), height - Ui.BAR + (Ui.BAR - fontHeight) / 2 + 1, Ui.TOP_LEFT);
        if (scribble) {
            g.setColor(55 + random.nextInt(200), 55 + random.nextInt(200), 55 + random.nextInt(200));
            g.drawLine(dragX, dragY, random.nextInt(width), random.nextInt(height));
        }
        g.setColor(Ui.GREEN);
        int y = height / 4 - fontHeight / 2;
        g.drawString(L10n.get(L10n.APP_NAME), width / 2, y, Ui.TOP_CENTER);
        g.drawString("v" + Main.version(), width / 2, y + fontHeight, Ui.TOP_CENTER);
        for (int i = 0; i < ABOUT_LINES.length; i++) {
            g.drawString(ABOUT_LINES[i], width / 2, y + (i + 2) * fontHeight, Ui.TOP_CENTER);
        }
    }

    private void paintInterfaceLanguage(Graphics g, Font font) {
        int gap = Ui.px(5);
        paintOption(g, font, flagRu, "Русский", height / 2 - gap - flagRu.getHeight(), interfaceIndex == 0);
        paintOption(g, font, flagEn, "English", height / 2 + gap, interfaceIndex == 1);
        g.setColor(Ui.WHITE);
        g.drawString("OK", width / 2, height - Ui.BAR + (Ui.BAR - font.getHeight()) / 2, Ui.TOP_CENTER);
    }

    private void paintOption(Graphics g, Font font, Image flag, String label, int y, boolean selected) {
        int x = (width - flag.getWidth() - font.stringWidth(label) - Ui.px(3)) / 2;
        g.drawImage(flag, x, y, Ui.TOP_LEFT);
        g.setColor(selected ? Ui.SELECTED_OPTION : Ui.GREEN);
        g.drawString(label, x + flag.getWidth() + Ui.px(3), y + (flag.getHeight() - font.getHeight()) / 2, Ui.TOP_LEFT);
    }
}
