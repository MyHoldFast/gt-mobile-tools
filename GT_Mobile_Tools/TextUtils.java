import java.io.UnsupportedEncodingException;

public final class TextUtils {
    private static final String CP1251 = "\u0402\u0403\u201A\u0453\u201E\u2026\u2020\u2021\u20AC\u2030\u0409\u2039\u040A\u040C\u040B\u040F\u0452\u2018\u2019\u201C\u201D\u2022\u2013\u2014\uFFFD\u2122\u0459\u203A\u045A\u045C\u045B\u045F\u00A0\u040E\u045E\u0408\u00A4\u0490\u00A6\u00A7\u0401\u00A9\u0404\u00AB\u00AC\u00AD\u00AE\u0407\u00B0\u00B1\u0406\u0456\u0491\u00B5\u00B6\u00B7\u0451\u2116\u0454\u00BB\u0458\u0405\u0455\u0457АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюя";

    public static String decode(byte[] data) {
        int start = data.length >= 3 && (data[0] & 0xFF) == 0xEF && (data[1] & 0xFF) == 0xBB && (data[2] & 0xFF) == 0xBF ? 3 : 0;
        String utf = decodeUtf8(data, start);
        return utf != null ? utf : decodeCp1251(data);
    }

    public static byte[] encode(String text) {
        try {
            return text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return text.getBytes();
        }
    }

    public static String ukrainianI(String text) {
        return text.replace('i', '\u0456').replace('I', '\u0406');
    }

    private static String decodeCp1251(byte[] data) {
        char[] chars = new char[data.length];
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;
            chars[i] = b < 0x80 ? (char) b : CP1251.charAt(b - 0x80);
        }
        return new String(chars);
    }

    private static String decodeUtf8(byte[] data, int start) {
        StringBuffer out = new StringBuffer(data.length);
        int i = start;
        while (i < data.length) {
            int b = data[i] & 0xFF;
            if (b < 0x80) {
                out.append((char) b);
                i++;
            } else if ((b & 0xE0) == 0xC0) {
                if (i + 1 >= data.length || (data[i + 1] & 0xC0) != 0x80) return null;
                out.append((char) ((b & 0x1F) << 6 | data[i + 1] & 0x3F));
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                if (i + 2 >= data.length || (data[i + 1] & 0xC0) != 0x80 || (data[i + 2] & 0xC0) != 0x80) return null;
                out.append((char) ((b & 0x0F) << 12 | (data[i + 1] & 0x3F) << 6 | data[i + 2] & 0x3F));
                i += 3;
            } else {
                return null;
            }
        }
        return out.toString();
    }
}
