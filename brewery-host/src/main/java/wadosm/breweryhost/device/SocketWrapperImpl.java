package wadosm.breweryhost.device;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketWrapperImpl implements SocketWrapper {

    private final Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean closed = false;

    public SocketWrapperImpl(Socket socket) {
        this.socket = socket;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            closed = true;
        }
    }

    @Override
    public synchronized String read() {
        if (isClosed()) {
            return null;
        }

        try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            close();
        }
        return null;
    }

    @Override
    public synchronized void write(String data) {
        if (isClosed()) {
            return;
        } else {
            try {
                outputStream.write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                close();
            }
        }
    }

    private synchronized void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        } finally {
            closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}
