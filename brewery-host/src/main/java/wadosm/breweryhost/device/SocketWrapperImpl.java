package wadosm.breweryhost.device;

import lombok.extern.log4j.Log4j2;
import wadosm.breweryhost.logic.general.PowerServiceImpl;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
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
    public String read() {
        if (isClosed()) {
            return null;
        }

        try {
            byte[] buffer = new byte[1024];
            int read = inputStream.read(buffer, 0, 1024);
            if (read > 0 ) {
                return new String(buffer, 0, read, StandardCharsets.UTF_8);
            } else {
                return null;
            }
        } catch (IOException e) {
            close();
        }
        return null;
    }

    @Override
    public void write(String data) {
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

    private void close() {
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
