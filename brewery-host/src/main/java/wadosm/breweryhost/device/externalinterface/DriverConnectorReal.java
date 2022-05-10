package wadosm.breweryhost.device.externalinterface;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterface;

@Service
//@Profile("!demo")
public class DriverConnectorReal extends DriverConnectorAbstract {

    public DriverConnectorReal(DriverInterface driverInterface,
            ObjectMapper objectMapper) {
        super(driverInterface, objectMapper);

        Thread powerTask = new TryConnectTask(this);
        powerTask.start();
    }

    private static class TryConnectTask extends Thread {

        private final DriverConnector driverConnector;

        private TryConnectTask(DriverConnector driverConnector) {
            super("TryConnectTask-Thread");
            this.driverConnector = driverConnector;
        }

        public void run() {
            tryEnablePower();
        }

        private void tryEnablePower() {
//            while (true) {
                driverConnector.tryConnect();

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ignored) {
//                }
//            }
        }
    }
}
