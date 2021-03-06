package wadosm.breweryhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.externalinterface.DriverConnector;
import wadosm.breweryhost.device.externalinterface.DriverConnectorDemo;

@Log4j2
@SpringBootTest
public class ConnectionPoolTest {

    @Autowired
    DriverInterface driverInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Disabled
    void connectToDriver() {
        DriverConnector driverConnector = new DriverConnectorDemo(driverInterface, objectMapper);

        while (true) {
            driverConnector.tryConnect();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
