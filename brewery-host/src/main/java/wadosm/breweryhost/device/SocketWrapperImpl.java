package wadosm.breweryhost.device;

import java.io.*;
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
            byte[] buffer = new byte[1024];
            int read = inputStream.read(buffer, 0, 1024);
            if (read > 0 ) {
                String s = new String(buffer, 0, read, StandardCharsets.UTF_8);
                return s;
            } else {
                return null;
            }
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
