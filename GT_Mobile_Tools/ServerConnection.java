import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public final class ServerConnection {
    public static final int TRANSLATE = 1;
    public static final int SPEAK = 2;
    public static final int RECOGNIZE = 3;

    private static final int MAX_RESPONSE = 4 * 1024 * 1024;
    private static final Object LOCK = new Object();

    private static StreamConnection connection;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static boolean stale;

    public static byte[] request(int command, byte[] payload) throws IOException, ServerException {
        synchronized (LOCK) {
            try {
                return exchange(command, payload);
            } catch (IOException e) {
                stale = true;
                return exchange(command, payload);
            }
        }
    }

    public static void close() {
        stale = true;
        shutdown(connection, in, out);
    }

    public static byte[] pack(String[] values) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(buffer);
        try {
            for (int i = 0; i < values.length; i++) {
                data.writeUTF(values[i]);
            }
        } catch (IOException e) {
        }
        return buffer.toByteArray();
    }

    public static String text(byte[] body) throws IOException {
        return new DataInputStream(new ByteArrayInputStream(body)).readUTF();
    }

    private static byte[] exchange(int command, byte[] payload) throws IOException, ServerException {
        if (stale || connection == null) {
            connect();
        }
        DataOutputStream o = out;
        DataInputStream i = in;
        o.writeByte(command);
        o.writeInt(payload.length);
        o.write(payload);
        o.flush();
        int status = i.readUnsignedByte();
        int length = i.readInt();
        if (length < 0 || length > MAX_RESPONSE) {
            throw new IOException("Bad frame");
        }
        byte[] body = new byte[length];
        i.readFully(body);
        if (status != 0) {
            throw new ServerException(text(body));
        }
        return body;
    }

    private static void connect() throws IOException {
        shutdown(connection, in, out);
        connection = null;
        stale = false;
        StreamConnection c = (StreamConnection) Connector.open(Settings.socketUrl(), Connector.READ_WRITE, true);
        in = c.openDataInputStream();
        out = c.openDataOutputStream();
        connection = c;
    }

    private static void shutdown(StreamConnection c, DataInputStream i, DataOutputStream o) {
        try {
            if (i != null) i.close();
        } catch (IOException e) {
        }
        try {
            if (o != null) o.close();
        } catch (IOException e) {
        }
        try {
            if (c != null) c.close();
        } catch (IOException e) {
        }
    }
}
