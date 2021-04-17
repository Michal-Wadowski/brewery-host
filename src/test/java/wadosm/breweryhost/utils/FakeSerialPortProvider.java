package wadosm.breweryhost.utils;

import java.io.File;
import java.io.IOException;

public class FakeSerialPortProvider {

    public static final String HOST_PORT_DEVICE = "/tmp/com1";
    public static final String CLIENT_PORT_DEVICE = "/tmp/com2";
    
    private Process process;
    
    public void createSerialPorts() {
        try {
            process = Runtime.getRuntime().exec(
                    String.format("socat PTY,raw,echo=0,link=%s PTY,raw,echo=0,link=%s",
                            HOST_PORT_DEVICE, CLIENT_PORT_DEVICE
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 100; i++) {
            File hostFile = new File(HOST_PORT_DEVICE);
            File clientFile = new File(CLIENT_PORT_DEVICE);

            if (hostFile.exists() && clientFile.exists()) {
                break;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void releaseSerialPorts() {
        process.destroy();
    }
    
}
