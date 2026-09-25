import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;

public final class Settings {
    public static final String DEFAULT_SERVER = "94.159.98.141";
    public static final int DEFAULT_PORT = 8090;
    private static final String STORE = "GTSettings";
    private static final String MANIFEST_KEY = "GT-Server";

    public static boolean exists;
    public static boolean english;
    private static String server = "";
    private static String defaultServer = DEFAULT_SERVER;

    public static void load(MIDlet midlet) {
        String fromManifest = midlet.getAppProperty(MANIFEST_KEY);
        if (fromManifest != null && fromManifest.trim().length() > 0) {
            defaultServer = fromManifest.trim();
        }
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(STORE, false);
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(store.getRecord(1)));
            english = in.readBoolean();
            server = in.readUTF();
            exists = true;
        } catch (Exception e) {
        } finally {
            close(store);
        }
    }

    public static void save() {
        RecordStore store = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(buffer);
            out.writeBoolean(english);
            out.writeUTF(server);
            byte[] data = buffer.toByteArray();
            store = RecordStore.openRecordStore(STORE, true);
            if (store.getNumRecords() == 0) {
                store.addRecord(data, 0, data.length);
            } else {
                store.setRecord(1, data, 0, data.length);
            }
            exists = true;
        } catch (Exception e) {
        } finally {
            close(store);
        }
    }

    public static String serverAddress() {
        return server.length() > 0 ? server : defaultServer;
    }

    public static void setServer(String address) {
        address = address.trim();
        server = address.equals(defaultServer) ? "" : address;
        save();
    }

    public static String socketUrl() {
        String address = serverAddress();
        if (address.indexOf(':') < 0) {
            address = address + ":" + DEFAULT_PORT;
        }
        return "socket://" + address;
    }

    private static void close(RecordStore store) {
        if (store != null) {
            try {
                store.closeRecordStore();
            } catch (Exception e) {
            }
        }
    }
}
