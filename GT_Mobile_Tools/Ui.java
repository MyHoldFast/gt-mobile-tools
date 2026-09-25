import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class Ui {
    public static final int TOP_LEFT = Graphics.TOP | Graphics.LEFT;
    public static final int TOP_CENTER = Graphics.TOP | Graphics.HCENTER;
    public static final int TOP_RIGHT = Graphics.TOP | Graphics.RIGHT;

    public static final int BACKGROUND = 0xD9EED0;
    public static final int GREEN = 0x217000;
    public static final int FOCUS = 0xFF7000;
    public static final int WHITE = 0xFFFFFF;
    public static final int BOX_FILL = 0xA4CA94;
    public static final int MENU_FILL = 0xA3E283;
    public static final int DIALOG = 0xF4F4F4;
    public static final int DIALOG_BORDER = 0x808080;
    public static final int DIALOG_TEXT = 0x646464;
    public static final int SELECTED_OPTION = 0xFF0000;
    public static final int LIST_TEXT = 0x306125;
    public static final int SCROLL = 0x799E6E;
    public static final int SCROLL_ACTIVE = 0x99C58B;
    public static final int SCROLL_CORNER = 0xA0BF96;
    public static final int SCROLL_CORNER_ACTIVE = 0xBDDDB2;

    public static int SCALE = 100;
    public static int BAR = 24;
    public static int ROW = 22;
    private static int screenWidth;
    private static int screenHeight;
    private static Font plainFont;
    private static Font boldFont;

    public static final int SOFT_LEFT = 100;
    public static final int SOFT_RIGHT = 101;
    public static final int STAR = 102;
    public static final int POUND = 103;

    public static int action(Canvas canvas, int key) {
        switch (key) {
            case -6: return SOFT_LEFT;
            case -7: return SOFT_RIGHT;
            case Canvas.KEY_STAR: return STAR;
            case Canvas.KEY_POUND: return POUND;
            case Canvas.KEY_NUM2: return Canvas.UP;
            case Canvas.KEY_NUM8: return Canvas.DOWN;
            case Canvas.KEY_NUM4: return Canvas.LEFT;
            case Canvas.KEY_NUM6: return Canvas.RIGHT;
            case Canvas.KEY_NUM5: return Canvas.FIRE;
        }
        try {
            return canvas.getGameAction(key);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static void setScreen(int width, int height, boolean touch) {
        if (width == screenWidth && height == screenHeight) {
            return;
        }
        screenWidth = width;
        screenHeight = height;
        SCALE = Math.max(100, Math.min(width, height) * 100 / 240);
        if (touch) {
            SCALE = SCALE * 4 / 3;
        }
        BAR = px(24);
        ROW = px(22);
        plainFont = pick(Font.STYLE_PLAIN, ROW - 1);
        boldFont = SCALE >= 130 ? pick(Font.STYLE_BOLD, ROW - 4) : Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
    }

    public static int px(int value) {
        return value * SCALE / 100;
    }

    public static Font font() {
        return plainFont;
    }

    public static Font boldFont() {
        return boldFont;
    }

    private static Font pick(int style, int maxHeight) {
        int[] sizes = SCALE >= 130 ? new int[] {Font.SIZE_LARGE, Font.SIZE_MEDIUM} : new int[] {Font.SIZE_MEDIUM};
        for (int i = 0; i < sizes.length; i++) {
            Font font = Font.getFont(Font.FACE_PROPORTIONAL, style, sizes[i]);
            if (font.getHeight() <= maxHeight) {
                return font;
            }
        }
        return Font.getFont(Font.FACE_PROPORTIONAL, style, Font.SIZE_SMALL);
    }

    public static int mix(int a, int b, int weight) {
        return blend(a, b, weight * 256 / 100) & 0xFFFFFF;
    }

    public static Image fit(Image source) {
        return SCALE == 100 ? source : scale(source, px(source.getWidth()), px(source.getHeight()));
    }

    public static Image image(String path) {
        try {
            return Image.createImage(path);
        } catch (IOException e) {
            return Image.createImage(1, 1);
        }
    }

    public static Image strip(Image tile, int width, int height, int cornerColor) {
        if (tile.getHeight() != height) {
            tile = scale(tile, tile.getWidth(), height);
        }
        Image result = Image.createImage(width, height);
        Graphics g = result.getGraphics();
        for (int x = 0; x < width; x += tile.getWidth()) {
            g.drawImage(tile, x, 0, TOP_LEFT);
        }
        g.setColor(cornerColor);
        g.fillRect(0, 0, 1, 1);
        g.fillRect(width - 1, 0, 1, 1);
        g.fillRect(0, height - 1, 1, 1);
        g.fillRect(width - 1, height - 1, 1, 1);
        return result;
    }

    public static Image scale(Image source, int width, int height) {
        int sw = source.getWidth();
        int sh = source.getHeight();
        if (sw == width && sh == height) {
            return source;
        }
        int[] src = new int[sw * sh];
        source.getRGB(src, 0, sw, 0, 0, sw, sh);
        int[] dst = new int[width * height];
        int stepX = width > 1 ? ((sw - 1) << 16) / (width - 1) : 0;
        int stepY = height > 1 ? ((sh - 1) << 16) / (height - 1) : 0;
        for (int y = 0; y < height; y++) {
            int fy = y * stepY;
            int y0 = fy >> 16;
            int y1 = Math.min(y0 + 1, sh - 1);
            int wy = (fy >> 8) & 0xFF;
            for (int x = 0; x < width; x++) {
                int fx = x * stepX;
                int x0 = fx >> 16;
                int x1 = Math.min(x0 + 1, sw - 1);
                int wx = (fx >> 8) & 0xFF;
                dst[y * width + x] = blend(
                    blend(src[y0 * sw + x0], src[y0 * sw + x1], wx),
                    blend(src[y1 * sw + x0], src[y1 * sw + x1], wx),
                    wy);
            }
        }
        return Image.createRGBImage(dst, width, height, true);
    }

    private static int blend(int a, int b, int weight) {
        if (weight == 0 || a == b) {
            return a;
        }
        int inverse = 256 - weight;
        int result = 0;
        for (int shift = 0; shift < 32; shift += 8) {
            int ca = (a >>> shift) & 0xFF;
            int cb = (b >>> shift) & 0xFF;
            result |= ((ca * inverse + cb * weight) >> 8) << shift;
        }
        return result;
    }

    public static String truncate(Font font, String text, int maxWidth) {
        if (font.stringWidth(text) <= maxWidth) {
            return text;
        }
        int limit = maxWidth - font.stringWidth("...");
        int width = 0;
        int i = 0;
        while (i < text.length() && width + font.charWidth(text.charAt(i)) <= limit) {
            width += font.charWidth(text.charAt(i));
            i++;
        }
        return text.substring(0, i) + "...";
    }

    public static String truncateLeft(Font font, String text, int maxWidth) {
        if (font.stringWidth(text) <= maxWidth) {
            return text;
        }
        int limit = maxWidth - font.stringWidth("...");
        int width = 0;
        int i = text.length();
        while (i > 0 && width + font.charWidth(text.charAt(i - 1)) <= limit) {
            i--;
            width += font.charWidth(text.charAt(i));
        }
        return "..." + text.substring(i);
    }

    public static void drawWrapped(Graphics g, String text, int x, int y, int maxWidth, int maxHeight) {
        Font font = g.getFont();
        int lineHeight = font.getHeight();
        int ellipsis = font.stringWidth("...");
        int length = text.length();
        int start = 0;
        while (start < length && maxHeight >= lineHeight) {
            boolean last = maxHeight < lineHeight * 2;
            int limit = last ? maxWidth - ellipsis : maxWidth;
            int width = 0;
            int end = start;
            int space = -1;
            while (end < length) {
                char c = text.charAt(end);
                if (c == '\n') {
                    break;
                }
                int w = font.charWidth(c);
                if (width + w > limit && end > start) {
                    break;
                }
                if (c == ' ') {
                    space = end;
                }
                width += w;
                end++;
            }
            int next = end;
            if (end < length) {
                char c = text.charAt(end);
                if (c == '\n' || c == ' ') {
                    next = end + 1;
                } else if (space > start && !last) {
                    end = space;
                    next = space + 1;
                }
            }
            while (next < length && text.charAt(next) == ' ') {
                next++;
            }
            String line = text.substring(start, end);
            if (last && next < length) {
                g.drawString(line + "...", x, y, TOP_LEFT);
                return;
            }
            g.drawString(line, x, y, TOP_LEFT);
            start = next;
            y += lineHeight;
            maxHeight -= lineHeight;
        }
    }

    public static void drawDialog(Graphics g, Image shadow, int width, int height) {
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        g.drawImage(shadow, 0, 0, TOP_LEFT);
        g.setColor(DIALOG);
        g.fillRect(x + 1, y + 1, width, height);
        g.setColor(DIALOG_BORDER);
        g.drawRoundRect(x, y, width + 1, height + 1, 4, 4);
    }
}
