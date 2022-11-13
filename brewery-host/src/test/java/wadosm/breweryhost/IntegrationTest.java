package wadosm.breweryhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import wadosm.breweryhost.device.driver.BreweryInterface;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
public class IntegrationTest {

    @Autowired
    BreweryInterface breweryInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${configuration.file}")
    private String calibrationFile;

    @Value("${brewing.temperature_sensor.id}")
    private String brewingTemperatureSensorId;

    @Value("${fermenting.temperature_sensor.id}")
    private String fermentingTemperatureSensorId;

    @Test
    void contextLoads() {
        assertThat(calibrationFile).isNotEmpty();
        assertThat(brewingTemperatureSensorId).isNotEmpty();
        assertThat(fermentingTemperatureSensorId).isNotEmpty();
    }


}
