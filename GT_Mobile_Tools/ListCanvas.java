import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class ListCanvas extends Canvas implements Runnable {
    private static final int FRAME_MS = 20;
    private static final int TOUCH_SLOP = 8;
    private static final int SCROLL_ZONE = 20;
    private static final int MIN_FLING = 150;
    private static final int MAX_FLING = 5000;
    private static final int STOP_SPEED = 20;
    private static final int REST_MS = 80;

    protected int selectedRow;
    protected int selectedColumn;

    private int scrollY;
    private int width = -1;
    private int height;
    private int columnWidth;
    private int viewTop;
    private int viewHeight;
    private int thumbHeight;
    private int thumbY;
    private Image header;
    private Image highlight;

    private boolean thumbDrag;
    private boolean touching;
    private boolean moved;
    private int pressY;
    private int lastY;
    private int dragOffset;
    private int pressRow = -1;
    private int pressColumn;
    private long lastTime;
    private int velocity;
    private volatile int flingId;
    private int runId;
    private volatile boolean flinging;

    protected ListCanvas() {
        setFullScreenMode(true);
    }

    protected abstract int columns();

    protected abstract int rows();

    protected abstract boolean hasCell(int row, int column);

    protected abstract Image icon(int row, int column);

    protected abstract String text(int row, int column);

    protected abstract String title();

    protected abstract String leftLabel();

    protected abstract String centerLabel();

    protected abstract void onSelect();

    protected abstract void onLeft();

    protected abstract void onBack();

    protected void onLayout() {
    }

    protected int width() {
        return width;
    }

    protected void resetSelection() {
        stopFling();
        selectedRow = 0;
        selectedColumn = 0;
        scrollY = 0;
    }

    protected void reveal() {
        layout();
        int top = selectedRow * Ui.ROW;
        if (top < scrollY) {
            scrollY = top;
        } else if (top + Ui.ROW > scrollY + viewHeight) {
            scrollY = top + Ui.ROW - viewHeight;
        }
        clampScroll();
    }

    protected void center() {
        layout();
        stopFling();
        scrollY = selectedRow * Ui.ROW - (viewHeight - Ui.ROW) / 2;
        clampScroll();
    }

    private void layout() {
        if (width == getWidth() && height == getHeight()) {
            return;
        }
        width = getWidth();
        height = getHeight();
        Ui.setScreen(width, height, hasPointerEvents());
        onLayout();
        columnWidth = (width - Ui.px(6)) / columns();
        viewTop = Ui.BAR + Ui.px(2);
        viewHeight = height - 2 * viewTop;
        thumbHeight = Math.max(Ui.px(20), viewHeight / 3);
        header = Ui.scale(Ui.image("/res/head.png"), width, Ui.BAR);
        highlight = Ui.strip(Ui.image("/res/el.png"), columnWidth, Ui.ROW, Ui.BACKGROUND);
        clampScroll();
    }

    private int maxScroll() {
        return Math.max(0, rows() * Ui.ROW - viewHeight);
    }

    private boolean clampScroll() {
        int max = maxScroll();
        if (scrollY < 0) {
            scrollY = 0;
            return true;
        }
        if (scrollY > max) {
            scrollY = max;
            return true;
        }
        return false;
    }

    private int rowAt(int y) {
        int row = (y - viewTop + scrollY) / Ui.ROW;
        return row >= 0 && row < rows() ? row : -1;
    }

    private int columnAt(int x, int row) {
        int column = Math.min(x / Math.max(1, columnWidth), columns() - 1);
        return row >= 0 && hasCell(row, column) ? column : 0;
    }

    private void fixColumn() {
        if (!hasCell(selectedRow, selectedColumn)) {
            selectedColumn = 0;
        }
    }

    protected void paint(Graphics g) {
        layout();
        Font font = Ui.font();
        g.setFont(font);
        int fontHeight = font.getHeight();
        g.setColor(Ui.BACKGROUND);
        g.fillRect(0, 0, width, height);

        int count = rows();
        boolean pressing = touching && !moved && pressRow >= 0;
        int markRow = pressing ? pressRow : selectedRow;
        int markColumn = pressing ? pressColumn : selectedColumn;
        int textX = Ui.px(19);
        g.setClip(0, viewTop, width, viewHeight);
        for (int row = scrollY / Ui.ROW; row < count; row++) {
            int y = viewTop + row * Ui.ROW - scrollY;
            if (y >= viewTop + viewHeight) {
                break;
            }
            for (int column = 0; column < columns(); column++) {
                if (!hasCell(row, column)) {
                    continue;
                }
                int x = columnWidth * column;
                if (row == markRow && column == markColumn) {
                    g.drawImage(highlight, x + 1, y, Ui.TOP_LEFT);
                    g.setColor(Ui.WHITE);
                } else {
                    g.setColor(Ui.LIST_TEXT);
                }
                Image icon = icon(row, column);
                if (icon != null) {
                    g.drawImage(icon, x + Ui.px(2), y + (Ui.ROW - icon.getHeight()) / 2, Ui.TOP_LEFT);
                }
                g.drawString(Ui.truncate(font, text(row, column), columnWidth - textX - 1), x + textX, y + (Ui.ROW - fontHeight) / 2, Ui.TOP_LEFT);
            }
        }
        g.setClip(0, 0, width, height);

        g.drawImage(header, 0, 0, Ui.TOP_LEFT);
        g.drawImage(header, 0, height - Ui.BAR, Ui.TOP_LEFT);
        g.setColor(Ui.WHITE);
        g.drawString(Ui.truncateLeft(font, title(), width - Ui.px(4)), width / 2, (Ui.BAR - fontHeight) / 2, Ui.TOP_CENTER);
        int footerY = height - Ui.BAR + (Ui.BAR - fontHeight) / 2;
        String left = leftLabel();
        if (left != null) {
            g.drawString(left, Ui.px(2), footerY, Ui.TOP_LEFT);
        }
        g.drawString(centerLabel(), width / 2, footerY, Ui.TOP_CENTER);
        g.drawString(L10n.get(L10n.BACK), width - Ui.px(2), footerY, Ui.TOP_RIGHT);

        int max = maxScroll();
        if (max > 0) {
            int barWidth = Ui.px(3);
            int barX = width - barWidth - 1;
            thumbY = viewTop + (viewHeight - thumbHeight) * scrollY / max;
            g.setColor(thumbDrag ? Ui.SCROLL_ACTIVE : Ui.SCROLL);
            g.fillRect(barX, thumbY, barWidth, thumbHeight);
            g.setColor(thumbDrag ? Ui.SCROLL_CORNER_ACTIVE : Ui.SCROLL_CORNER);
            g.fillRect(barX, thumbY, 1, 1);
            g.fillRect(barX + barWidth - 1, thumbY, 1, 1);
            g.fillRect(barX, thumbY + thumbHeight - 1, 1, 1);
            g.fillRect(barX + barWidth - 1, thumbY + thumbHeight - 1, 1, 1);
        }
    }

    protected void keyPressed(int key) {
        stopFling();
        int count = rows();
        switch (Ui.action(this, key)) {
            case Canvas.UP:
                selectedRow = selectedRow > 0 ? selectedRow - 1 : count - 1;
                fixColumn();
                reveal();
                break;
            case Canvas.DOWN:
                selectedRow = selectedRow < count - 1 ? selectedRow + 1 : 0;
                fixColumn();
                reveal();
                break;
            case Canvas.LEFT:
                selectedColumn = 0;
                break;
            case Canvas.RIGHT:
                if (hasCell(selectedRow, 1)) {
                    selectedColumn = 1;
                }
                break;
            case Canvas.FIRE:
                onSelect();
                break;
            case Ui.SOFT_LEFT:
                onLeft();
                break;
            case Ui.SOFT_RIGHT:
                onBack();
                break;
        }
        repaint();
    }

    protected void keyRepeated(int key) {
        int action = Ui.action(this, key);
        if (action == Canvas.UP || action == Canvas.DOWN) {
            keyPressed(key);
        }
    }

    protected void pointerPressed(int x, int y) {
        layout();
        boolean wasFlinging = stopFling();
        if (y >= height - Ui.BAR) {
            if (x >= width * 2 / 3) {
                onBack();
            } else if (x > width / 3) {
                onSelect();
            } else {
                onLeft();
            }
            repaint();
            return;
        }
        if (y < viewTop) {
            return;
        }
        if (maxScroll() > 0 && x >= width - Ui.px(SCROLL_ZONE)) {
            thumbDrag = true;
            dragOffset = y - thumbY;
            repaint();
            return;
        }
        touching = true;
        moved = false;
        pressY = y;
        lastY = y;
        lastTime = System.currentTimeMillis();
        velocity = 0;
        pressRow = wasFlinging ? -1 : rowAt(y);
        pressColumn = columnAt(x, pressRow);
        repaint();
    }

    protected void pointerDragged(int x, int y) {
        if (thumbDrag) {
            int track = viewHeight - thumbHeight;
            if (track > 0) {
                scrollY = (y - dragOffset - viewTop) * maxScroll() / track;
                clampScroll();
                repaint();
            }
            return;
        }
        if (!touching || !moved && Math.abs(y - pressY) <= Ui.px(TOUCH_SLOP)) {
            return;
        }
        moved = true;
        long now = System.currentTimeMillis();
        int dy = lastY - y;
        scrollY += dy;
        clampScroll();
        int dt = (int) (now - lastTime);
        if (dt > 0) {
            velocity = (velocity + 3 * (dy * 1000 / dt)) / 4;
        }
        lastY = y;
        lastTime = now;
        repaint();
    }

    protected void pointerReleased(int x, int y) {
        if (thumbDrag) {
            thumbDrag = false;
            repaint();
            return;
        }
        if (!touching) {
            return;
        }
        touching = false;
        if (!moved) {
            int row = pressRow;
            pressRow = -1;
            if (row >= 0 && hasCell(row, pressColumn)) {
                selectedRow = row;
                selectedColumn = pressColumn;
                onSelect();
            }
            repaint();
            return;
        }
        if (System.currentTimeMillis() - lastTime < REST_MS && Math.abs(velocity) > Ui.px(MIN_FLING)) {
            startFling(Math.max(-Ui.px(MAX_FLING), Math.min(Ui.px(MAX_FLING), velocity)));
        }
        repaint();
    }

    protected void hideNotify() {
        stopFling();
        touching = false;
        thumbDrag = false;
    }

    private boolean stopFling() {
        boolean was = flinging;
        flinging = false;
        flingId++;
        return was;
    }

    private void startFling(int speed) {
        velocity = speed;
        flinging = true;
        runId = ++flingId;
        new Thread(this).start();
    }

    public void run() {
        int id = runId;
        long last = System.currentTimeMillis();
        int remainder = 0;
        while (id == flingId) {
            try {
                Thread.sleep(FRAME_MS);
            } catch (InterruptedException e) {
                break;
            }
            long now = System.currentTimeMillis();
            int dt = Math.min((int) (now - last), 100);
            last = now;
            int distance = velocity * dt + remainder;
            scrollY += distance / 1000;
            remainder = distance % 1000;
            velocity -= velocity * dt * 3 / 1000;
            boolean edge = clampScroll();
            if (id != flingId) {
                break;
            }
            repaint();
            if (edge || Math.abs(velocity) < Ui.px(STOP_SPEED)) {
                flinging = false;
                break;
            }
        }
    }
}
