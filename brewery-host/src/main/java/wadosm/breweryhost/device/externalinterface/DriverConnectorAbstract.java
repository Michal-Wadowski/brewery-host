package wadosm.breweryhost.device.externalinterface;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import wadosm.breweryhost.device.SocketWrapperImpl;
import wadosm.breweryhost.device.driver.DriverInterface;

import java.io.IOException;
import java.net.Socket;

@Log4j2
abstract class DriverConnectorAbstract implements DriverConnector {

    private final DriverInterface driverInterface;
    private final ObjectMapper objectMapper;

    public DriverConnectorAbstract(DriverInterface driverInterface, ObjectMapper objectMapper) {
        this.driverInterface = driverInterface;
        this.objectMapper = objectMapper;
    }

    @Override
    public void tryConnect() {
//        try {
//            Socket socket = new Socket("localhost", 1111);

//            log.info("Connected to driver");

//            SocketWrapperImpl socketWrapper = new SocketWrapperImpl(socket);
//            DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);
//            driverInterface.setSession(driverSession);
            driverInterface.init();

//            while (driverSession.isConnected()) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ignored) {
//                }
//            }

//            log.info("Disconnected from driver");

//            driverInterface.removeSession();

//        } catch (IOException ignored) {
//
//        }
    }
}
