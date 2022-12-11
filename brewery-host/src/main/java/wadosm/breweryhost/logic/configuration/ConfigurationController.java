package wadosm.breweryhost.logic.configuration;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import wadosm.breweryhost.device.temperature.TemperatureSensorProvider;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.brewing.model.SensorsConfiguration;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final TemperatureSensorProvider temperatureSensorProvider;
    private final ConfigProvider configProvider;

    public ConfigurationController(TemperatureSensorProvider temperatureSensorProvider, ConfigProvider configProvider) {
        this.temperatureSensorProvider = temperatureSensorProvider;
        this.configProvider = configProvider;
    }

    @GetMapping("/getSensorsConfiguration")
    public SensorsConfiguration getSensorsConfiguration() {
        Configuration configuration = configProvider.loadConfiguration();
        return configuration.getSensorsConfiguration();
    }

    @Data
    static class ShowSensorDto {
        private String sensorId;
        private Boolean show;
    }

    @PostMapping("/showSensor")
    public void showSensor(@RequestBody ShowSensorDto showSensorDto) {
        SensorsConfiguration sensorsConfiguration = configProvider.loadConfiguration().getSensorsConfiguration();

        List<String> showSensorIds =
                sensorsConfiguration.getShowBrewingSensorIds().stream()
                        .filter(x -> !x.equals(showSensorDto.sensorId))
                        .collect(Collectors.toList());
        if (showSensorDto.show) {
            showSensorIds.add(showSensorDto.sensorId);
        }

        configProvider.updateAndSaveConfiguration(configuration -> configuration
                .withSensorsConfiguration(configuration
                        .getSensorsConfiguration()
                        .withShowBrewingSensorIds(showSensorIds)
                )
        );
    }

    @Data
    static class UseSensorDto {
        private final String sensorId;
        private final Boolean use;
    }

    @PostMapping("/useSensor")
    public void useSensor(@RequestBody UseSensorDto useSensorDto) {
        SensorsConfiguration sensorsConfiguration = configProvider.loadConfiguration().getSensorsConfiguration();

        List<String> showSensorIds =
                sensorsConfiguration.getUseBrewingSensorIds().stream()
                        .filter(x -> !x.equals(useSensorDto.sensorId))
                        .collect(Collectors.toList());
        if (useSensorDto.use) {
            showSensorIds.add(useSensorDto.sensorId);
        }

        configProvider.updateAndSaveConfiguration(configuration -> configuration
                .withSensorsConfiguration(configuration
                        .getSensorsConfiguration()
                        .withUseBrewingSensorIds(showSensorIds)
                )
        );
    }

    @GetMapping("/getTemperatureSensors")
    public List<TemperatureSensor> getTemperatureSensors() {
        return temperatureSensorProvider.getTemperatureSensors();
    }

    @GetMapping("/manualConfig")
    public Configuration manualConfig() {
        return configProvider.loadConfiguration();
    }

    @PostMapping("/manualConfig")
    public void manualConfig(@Valid @RequestBody Configuration configuration) {
        configProvider.saveConfiguration(configuration);
    }
}
