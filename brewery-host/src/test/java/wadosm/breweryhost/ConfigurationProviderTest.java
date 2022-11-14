package wadosm.breweryhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import wadosm.breweryhost.logic.general.ConfigProviderImpl;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
public class ConfigurationProviderTest {

    @Autowired
    private ConfigProviderImpl configProvider;

    @Autowired
    private ObjectMapper mapper;

    @Value("${configuration.file}")
    private String calibrationFile;

    @BeforeEach
    void setUp() {
        new File(calibrationFile).delete();
    }

    @AfterEach
    void tearDown() {
        new File(calibrationFile).delete();
    }

    @Test
    void calibrationFileNameExists() {
        // given/when
        String calibrationFile = configProvider.getConfigurationFile();

        // then
        assertThat(calibrationFile).isNotEmpty();

        assertThat(configProvider.loadConfiguration()).isNotNull();
    }

    @Test
    void calibrationConfigHasNotEmptyCollectionsAndMaps() {
        // given/when
        Configuration configuration = configProvider.loadConfiguration();

        // then
        assertThat(configuration).isNotNull();
        assertThat(configuration.getSensorsConfiguration()).isNotNull();
        assertThat(configuration.getSensorsConfiguration().getShowBrewingSensorIds()).isNotNull();
        assertThat(configuration.getSensorsConfiguration().getUseBrewingSensorIds()).isNotNull();
        assertThat(configuration.getSensorsConfiguration().getCalibrationMeasurements()).isNotNull();
    }

    @Test
    void configLoadsFromFile() throws IOException {
        // given
        String exampleConfig = "{\"brewingMotorNumber\":123, \"alarmMaxTime\":\"PT5S\"}";

        BufferedWriter writer = new BufferedWriter(new FileWriter(calibrationFile));
        writer.append(exampleConfig);
        writer.close();

        // when
        Configuration configuration = configProvider.loadConfiguration();

        // then
        assertThat(configuration)
                .isNotNull()
                .extracting("brewingMotorNumber", "alarmMaxTime")
                .contains(123, Duration.ofSeconds(5));
    }

    @Test
    void configSaveToFile() throws IOException {
        // given
        var configuration = configProvider.loadConfiguration().withBrewingMotorNumber(123).withAlarmMaxTime(Duration.ofSeconds(5));

        // when
        configProvider.saveConfiguration(configuration);

        // then
        Configuration storedConfig = mapper.readValue(new File(calibrationFile), Configuration.class);
        assertThat(storedConfig)
                .isNotNull()
                .extracting("brewingMotorNumber", "alarmMaxTime")
                .contains(123, Duration.ofSeconds(5));
    }
}
