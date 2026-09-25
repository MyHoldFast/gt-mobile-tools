import javax.microedition.lcdui.Image;

public final class LanguageScreen extends ListCanvas {
    private final Image[] sources = new Image[L10n.CODES.length];
    private final Image[] flags = new Image[L10n.CODES.length];
    private int flagScale;
    private int columnCount = 2;
    private boolean forSource;

    public LanguageScreen() {
        for (int i = 1; i < sources.length; i++) {
            sources[i] = Ui.image("/countries/" + L10n.CODES[i] + ".png");
        }
    }

    public void open(boolean source) {
        forSource = source;
        int index = source ? MainScreen.fromLanguage : MainScreen.toLanguage;
        select(index);
        center();
        Main.show(this);
    }

    protected void onLayout() {
        int index = index(selectedRow, selectedColumn);
        columnCount = Ui.SCALE > 100 && width() < Ui.px(300) ? 1 : 2;
        select(index);
    }

    private void select(int index) {
        selectedRow = index % rows();
        selectedColumn = index / rows();
    }

    private int index(int row, int column) {
        return row + column * rows();
    }

    protected int columns() {
        return columnCount;
    }

    protected int rows() {
        return (L10n.CODES.length + columnCount - 1) / columnCount;
    }

    protected boolean hasCell(int row, int column) {
        return index(row, column) < L10n.CODES.length;
    }

    protected Image icon(int row, int column) {
        if (flagScale != Ui.SCALE) {
            flagScale = Ui.SCALE;
            for (int i = 1; i < sources.length; i++) {
                flags[i] = Ui.fit(sources[i]);
            }
        }
        return flags[index(row, column)];
    }

    protected String text(int row, int column) {
        return L10n.language(index(row, column));
    }

    protected String title() {
        return L10n.get(L10n.SELECT_LANGUAGE);
    }

    protected String leftLabel() {
        return null;
    }

    protected String centerLabel() {
        return L10n.get(L10n.CHOOSE);
    }

    protected void onSelect() {
        int index = index(selectedRow, selectedColumn);
        if (forSource) {
            MainScreen.fromLanguage = index;
        } else {
            MainScreen.toLanguage = index;
        }
        Main.showMain();
        MainScreen.retranslate();
    }

    protected void onLeft() {
    }

    protected void onBack() {
        Main.showMain();
    }
}
