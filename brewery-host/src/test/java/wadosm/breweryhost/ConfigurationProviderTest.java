package wadosm.breweryhost;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import wadosm.breweryhost.logic.general.ConfigProviderImpl;
import wadosm.breweryhost.logic.general.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
public class ConfigurationProviderTest {

    @Autowired
    private ConfigProviderImpl configProvider;

    @Autowired
    private ObjectMapper mapper;

    @Value("${calibration.file}")
    private String calibrationFile;

    @AfterEach
    void tearDown() {
        new File(calibrationFile).delete();
    }

    @Test
    void calibrationFileNameExists() {
        // given/when
        String calibrationFile = configProvider.getCalibrationFile();

        // then
        assertThat(calibrationFile).isNotEmpty();

        assertThat(configProvider.getConfiguration()).isNotNull();
    }

    @Test
    void calibrationConfigIsNotNull() {
        // given/when
        Configuration configuration = configProvider.getConfiguration();

        // then
        assertThat(configuration).isNotNull();
    }

    @Test
    void configLoadsFromFile() throws IOException {
        // given
        String exampleConfig = "{\"temperatureCalibration\":{\"sensor-1\":[1,2]}," +
                "\"temperatureCalibrationMeasurements\":{\"sensor-1\":[3,4,5,6]}}";

        BufferedWriter writer = new BufferedWriter(new FileWriter(calibrationFile));
        writer.append(exampleConfig);
        writer.close();

        // when
        Configuration configuration = configProvider.getConfiguration();

        // then
        assertThat(configuration)
                .isNotNull()
                .extracting("temperatureCalibration", "temperatureCalibrationMeasurements")
                .contains(Map.of("sensor-1", List.of(1f, 2f)), Map.of("sensor-1", List.of(3f, 4f, 5f, 6f)));
    }

    @Test
    void configSaveToFile() throws IOException {
        // given
        Configuration configuration = configProvider.getConfiguration();
        configuration.setTemperatureCalibrationMeasurements(Map.of(
                "example", Arrays.asList(null, null, 123f, 456f)
        ));

        // when
        configProvider.setConfiguration(configuration);

        // then
        Configuration storedConfig = mapper.readValue(new File(calibrationFile), Configuration.class);
    }
}
