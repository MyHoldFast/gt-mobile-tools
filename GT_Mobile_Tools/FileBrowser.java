import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public final class FileBrowser extends ListCanvas implements Runnable, CommandListener {
    private static final String ROOT = "/";
    private static final String UP = "..";
    private static final int LIST = 0;
    private static final int OPEN = 1;
    private static final int SAVE = 2;

    private static final String[] ICONS = {"/res/up.png", "/res/disk.png", "/res/folder.png", "/res/file.png"};
    private static final int ICON_UP = 0;
    private static final int ICON_DISK = 1;
    private static final int ICON_FOLDER = 2;
    private static final int ICON_FILE = 3;
    private static final Image[] icons = new Image[ICONS.length];
    private static int iconScale;

    private final TextBox nameBox;
    private final Command ok;
    private final Command back;
    private Vector names = new Vector();
    private String path = ROOT;
    private boolean canSave;
    private boolean busy;
    private int task;
    private String argument;

    public FileBrowser() {
        ok = new Command(L10n.get(L10n.OK), Command.OK, 1);
        back = new Command(L10n.get(L10n.BACK), Command.BACK, 2);
        nameBox = new TextBox(L10n.get(L10n.ENTER_FILE_NAME), "", 64, TextField.ANY);
        nameBox.addCommand(ok);
        nameBox.addCommand(back);
        nameBox.setCommandListener(this);
        names.addElement(UP);
        Main.show(this);
        execute(LIST, ROOT);
    }

    private void execute(int task, String argument) {
        if (busy) {
            return;
        }
        busy = true;
        this.task = task;
        this.argument = argument;
        new Thread(this).start();
    }

    public void run() {
        try {
            if (task == LIST) {
                list(argument);
            } else if (task == OPEN) {
                open(argument);
            } else {
                save(argument);
            }
        } finally {
            busy = false;
        }
    }

    private void list(String target) {
        boolean root = ROOT.equals(target);
        Vector dirs = new Vector();
        Vector files = new Vector();
        FileConnection connection = null;
        try {
            Enumeration entries;
            if (root) {
                entries = FileSystemRegistry.listRoots();
            } else {
                connection = (FileConnection) Connector.open("file:///" + target, Connector.READ);
                entries = connection.list();
            }
            while (entries.hasMoreElements()) {
                String name = (String) entries.nextElement();
                if (name.endsWith("/")) {
                    dirs.addElement(name);
                } else if (name.toLowerCase().endsWith(".txt")) {
                    files.addElement(name);
                }
            }
        } catch (Exception e) {
            return;
        } finally {
            close(connection);
        }
        if (!root) {
            sort(dirs);
        }
        sort(files);
        Vector entries = new Vector();
        entries.addElement(UP);
        for (int i = 0; i < dirs.size(); i++) {
            entries.addElement(dirs.elementAt(i));
        }
        for (int i = 0; i < files.size(); i++) {
            entries.addElement(files.elementAt(i));
        }
        path = target;
        canSave = !root && MainScreen.resultText.trim().length() > 0;
        resetSelection();
        names = entries;
        repaint();
    }

    private void open(String file) {
        FileConnection connection = null;
        String text = "";
        try {
            connection = (FileConnection) Connector.open("file:///" + file, Connector.READ);
            InputStream in = connection.openInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[1024];
            int read;
            while ((read = in.read(chunk)) > 0) {
                buffer.write(chunk, 0, read);
            }
            in.close();
            text = TextUtils.decode(buffer.toByteArray());
        } catch (Exception e) {
        } finally {
            close(connection);
        }
        Main.showMain();
        if (text.trim().length() > 0) {
            MainScreen.sourceText = text;
            MainScreen.retranslate();
        }
    }

    private void save(String name) {
        FileConnection connection = null;
        try {
            connection = (FileConnection) Connector.open("file:///" + path + name + ".txt", Connector.READ_WRITE);
            if (!connection.exists()) {
                connection.create();
                OutputStream out = connection.openOutputStream();
                out.write(TextUtils.encode(MainScreen.resultText));
                out.close();
            }
        } catch (Exception e) {
        } finally {
            close(connection);
        }
        Main.showMain();
    }

    private static void close(FileConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
            }
        }
    }

    private static void sort(Vector items) {
        for (int i = 1; i < items.size(); i++) {
            String item = (String) items.elementAt(i);
            int j = i - 1;
            while (j >= 0 && ((String) items.elementAt(j)).compareTo(item) > 0) {
                items.setElementAt(items.elementAt(j), j + 1);
                j--;
            }
            items.setElementAt(item, j + 1);
        }
    }

    private static String parent(String dir) {
        int slash = dir.lastIndexOf('/', dir.length() - 2);
        return slash < 0 ? ROOT : dir.substring(0, slash + 1);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == back) {
            Main.show(this);
        } else if (command == ok && nameBox.getString().trim().length() > 0) {
            nameBox.setTitle(L10n.get(L10n.SAVING));
            execute(SAVE, nameBox.getString().trim());
        }
    }

    protected int columns() {
        return 1;
    }

    protected int rows() {
        return names.size();
    }

    protected boolean hasCell(int row, int column) {
        return column == 0;
    }

    protected Image icon(int row, int column) {
        if (iconScale != Ui.SCALE) {
            iconScale = Ui.SCALE;
            for (int i = 0; i < ICONS.length; i++) {
                icons[i] = Ui.fit(Ui.image(ICONS[i]));
            }
        }
        String name = entry(row);
        if (UP.equals(name)) {
            return icons[ICON_UP];
        }
        if (name.endsWith("/")) {
            return icons[ROOT.equals(path) ? ICON_DISK : ICON_FOLDER];
        }
        return icons[ICON_FILE];
    }

    protected String text(int row, int column) {
        return entry(row);
    }

    private String entry(int row) {
        Vector current = names;
        return row < current.size() ? (String) current.elementAt(row) : "";
    }

    protected String title() {
        return ROOT.equals(path) ? L10n.get(L10n.SELECT) : path;
    }

    protected String leftLabel() {
        return canSave ? L10n.get(L10n.SAVE) : null;
    }

    protected String centerLabel() {
        return L10n.get(L10n.OPEN);
    }

    protected void onSelect() {
        String name = entry(selectedRow);
        if (busy || name.length() == 0) {
            return;
        }
        boolean root = ROOT.equals(path);
        if (UP.equals(name)) {
            if (root) {
                Main.showMain();
            } else {
                execute(LIST, parent(path));
            }
        } else if (name.endsWith("/")) {
            execute(LIST, root ? name : path + name);
        } else {
            execute(OPEN, path + name);
        }
    }

    protected void onLeft() {
        if (canSave) {
            nameBox.setString("");
            Main.show(nameBox);
        }
    }

    protected void onBack() {
        Main.showMain();
    }
}
