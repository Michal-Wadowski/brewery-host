package wadosm.breweryhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.SocketWrapperImpl;
import wadosm.breweryhost.device.externalinterface.DriverSessionImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Log4j2
@SpringBootTest
public class ConnectionPoolTest {

    @Autowired
    DriverInterface driverInterface;

    @Autowired
    ObjectMapper objectMapper;

    class DriverConnector {
        public void tryConnect() {
            try {
                log.info("Try to connect");
                Socket socket = new Socket("localhost", 1111);

                log.info("connected");

                SocketWrapperImpl socketWrapper = new SocketWrapperImpl(socket);
                DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);
                driverInterface.setSession(driverSession);

                while (driverSession.isConnected()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }

                driverInterface.removeSession();

            } catch (IOException ignored) {

            }
        }
    }

    @Test
    @Disabled
    void connectToDriver() {
        DriverConnector driverConnector = new DriverConnector();

        while (true) {
            driverConnector.tryConnect();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
