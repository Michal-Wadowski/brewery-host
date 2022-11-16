package wadosm.breweryhost.logic.brewing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.temperature.TemperatureSensorProvider;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.brewing.model.SensorsConfiguration;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TemperatureProvider {

    private final ConfigProvider configProvider;
    private final TemperatureSensorProvider temperatureSensorProvider;
    private final CalibrationProvider calibrationProvider;

    Double getSelectedAverageTemperatures() {
        Configuration configuration = getConfiguration();
        SensorsConfiguration sensorsConfiguration = configuration.getSensorsConfiguration();
        double usedTemperature = sensorsConfiguration.getUseBrewingSensorIds().stream()
                .map(shownSensorId -> getCalibratedSensor(shownSensorId, sensorsConfiguration))
                .filter(Objects::nonNull)
                .mapToDouble(TemperatureSensor::getTemperature)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(usedTemperature)) {
            return null;
        } else {
            return Math.round(usedTemperature * 100) / 100.0;
        }
    }

    private TemperatureSensor getCalibratedSensor(String shownSensorId, SensorsConfiguration sensorsConfiguration) {
        return calibrationProvider.correctTemperature(getUncalibrated(shownSensorId, sensorsConfiguration));
    }

    private Configuration getConfiguration() {
        return configProvider.loadConfiguration();
    }

    List<TemperatureSensor> getAllTemperatures() {
        SensorsConfiguration sensorsConfiguration = getConfiguration().getSensorsConfiguration();
        List<String> showBrewingSensorIds = sensorsConfiguration.getShowBrewingSensorIds();
        List<String> useBrewingSensorIds = sensorsConfiguration.getUseBrewingSensorIds();

        List<TemperatureSensor> result = showBrewingSensorIds.stream()
                .map(sensorId -> getCalibratedSensor(sensorId, sensorsConfiguration))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean usedMoreThanOneSensors = useBrewingSensorIds.size() > 1;
        boolean usedButNotShown = !new HashSet<>(showBrewingSensorIds).containsAll(useBrewingSensorIds);
        if (usedButNotShown || usedMoreThanOneSensors) {
            Double usedTemperature = getSelectedAverageTemperatures();
            if (usedTemperature != null) {
                result.add(TemperatureSensor.builder()
                        .sensorId("#used")
                        .temperature(usedTemperature)
                        .build()
                );
            }
        }

        return result;
    }

    private TemperatureSensor getUncalibrated(String sensorId, SensorsConfiguration sensorsConfiguration) {
        return TemperatureSensor.fromRaw(
                temperatureSensorProvider.getRawTemperatureSensor(sensorId),
                sensorsConfiguration
        );
    }
}
