package wadosm.breweryhost.device.externalinterface;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterface;

//@Service
//@Profile("demo")
public class DriverConnectorDemo extends DriverConnectorAbstract {

    public DriverConnectorDemo(DriverInterface driverInterface,
            ObjectMapper objectMapper) {
        super(driverInterface, objectMapper);
    }
}
