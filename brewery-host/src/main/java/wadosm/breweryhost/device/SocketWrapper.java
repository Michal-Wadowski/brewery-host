package wadosm.breweryhost.device;

public interface SocketWrapper {

    String read();

    void write(String data);

    boolean isClosed();
}
