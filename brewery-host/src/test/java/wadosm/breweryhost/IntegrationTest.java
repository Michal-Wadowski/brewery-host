package wadosm.breweryhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.externalinterface.DriverConnector;
import wadosm.breweryhost.device.externalinterface.DriverConnectorDemo;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
public class IntegrationTest {

    @Autowired
    DriverInterface driverInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${calibration.file}")
    private String calibrationFile;

    @Value("${brewing.temperature_sensor.id}")
    private String brewingTemperatureSensorId;

    @Value("${fermenting.temperature_sensor.id}")
    private String fermentingTemperatureSensorId;

    @Test
    void contextLoads() throws IOException {
        assertThat(calibrationFile).isNotEmpty();
        assertThat(brewingTemperatureSensorId).isNotEmpty();
        assertThat(fermentingTemperatureSensorId).isNotEmpty();
    }

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
