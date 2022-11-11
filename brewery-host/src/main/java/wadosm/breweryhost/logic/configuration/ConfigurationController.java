package wadosm.breweryhost.logic.configuration;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import wadosm.breweryhost.device.temperature.TemperatureSensorProvider;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.brewing.BrewingService;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    private final BrewingService brewingService;

    private final TemperatureSensorProvider temperatureSensorProvider;
    private final ConfigProvider configProvider;

    public ConfigurationController(BrewingService brewingService, TemperatureSensorProvider temperatureSensorProvider, ConfigProvider configProvider) {
        this.brewingService = brewingService;
        this.temperatureSensorProvider = temperatureSensorProvider;
        this.configProvider = configProvider;
    }

    @GetMapping("/getSensorsConfiguration")
    public Configuration.SensorsConfiguration getSensorsConfiguration() {
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
        Configuration configuration = configProvider.loadConfiguration();
        Configuration.SensorsConfiguration sensorsConfiguration = configuration.getSensorsConfiguration();

        List<String> showSensorIds =
                sensorsConfiguration.getShowBrewingSensorIds().stream()
                        .filter(x -> !x.equals(showSensorDto.sensorId))
                        .collect(Collectors.toList());
        if (showSensorDto.show) {
            showSensorIds.add(showSensorDto.sensorId);
        }
        sensorsConfiguration.setShowBrewingSensorIds(showSensorIds);

        configProvider.saveConfiguration(
                configuration.toBuilder().sensorsConfiguration(sensorsConfiguration).build()
        );
    }

    @Data
    static class UseSensorDto {
        private final String sensorId;
        private final Boolean use;
    }

    @PostMapping("/useSensor")
    public void useSensor(@RequestBody UseSensorDto useSensorDto) {
        Configuration configuration = configProvider.loadConfiguration();
        Configuration.SensorsConfiguration sensorsConfiguration = configuration.getSensorsConfiguration();

        List<String> showSensorIds =
                sensorsConfiguration.getUseBrewingSensorIds().stream()
                        .filter(x -> !x.equals(useSensorDto.sensorId))
                        .collect(Collectors.toList());
        if (useSensorDto.use) {
            showSensorIds.add(useSensorDto.sensorId);
        }
        sensorsConfiguration.setUseBrewingSensorIds(showSensorIds);

        configProvider.saveConfiguration(
                configuration.toBuilder().sensorsConfiguration(sensorsConfiguration).build()
        );
    }

    @Data
    static class CalibrateTemperatureDto {
        private final Integer side;
        private final Float value;
    }

    @PostMapping("/calibrateTemperature")
    public void calibrateTemperature(@RequestBody CalibrateTemperatureDto calibrateTemperatureDto) {
        brewingService.calibrateTemperature(calibrateTemperatureDto.getSide(), calibrateTemperatureDto.getValue());
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
