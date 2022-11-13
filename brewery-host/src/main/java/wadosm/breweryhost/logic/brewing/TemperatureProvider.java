package wadosm.breweryhost.logic.brewing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.temperature.TemperatureSensorProvider;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TemperatureProvider {

    private final ConfigProvider configProvider;
    private final TemperatureSensorProvider temperatureSensorProvider;

    Float getUsedTemperature() {
        Configuration.SensorsConfiguration sensorsConfiguration = getConfiguration().getSensorsConfiguration();
        double usedTemperature = sensorsConfiguration.getUseBrewingSensorIds().stream()
                .map(shownSensorId -> getCalibratedSensor(shownSensorId, sensorsConfiguration))
                .filter(Objects::nonNull)
                .mapToDouble(TemperatureSensor::getTemperature)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(usedTemperature)) {
            return null;
        } else {
            return (float) Math.round(usedTemperature * 100) / 100;
        }
    }

    private TemperatureSensor getCalibratedSensor(String shownSensorId, Configuration.SensorsConfiguration sensorsConfiguration) {
        TemperatureSensor uncalibratedSensor = getUncalibrated(shownSensorId, sensorsConfiguration);

        if (uncalibratedSensor != null) {
            Float uncalibratedTemperature = uncalibratedSensor.getTemperature();
            Map<String, List<Float>> temperatureCalibration = getConfiguration().getTemperatureCalibration();

            if (temperatureCalibration.containsKey(shownSensorId)) {
                List<Float> sensorCalibration = temperatureCalibration.get(shownSensorId);
                if (sensorCalibration.size() == 2) {
                    uncalibratedTemperature *= (1 + sensorCalibration.get(0));
                    uncalibratedTemperature += sensorCalibration.get(1);
                }
            }

            return uncalibratedSensor.withTemperature(Math.round(uncalibratedTemperature * 100) / 100.0f);
        }
        return null;
    }

    private Configuration getConfiguration() {
        return configProvider.loadConfiguration();
    }

    List<TemperatureSensor> getAllTemperatures() {
        Configuration.SensorsConfiguration sensorsConfiguration = getConfiguration().getSensorsConfiguration();
        List<String> showBrewingSensorIds = sensorsConfiguration.getShowBrewingSensorIds();
        List<String> useBrewingSensorIds = sensorsConfiguration.getUseBrewingSensorIds();

        List<TemperatureSensor> result = showBrewingSensorIds.stream()
                .map(sensorId -> getCalibratedSensor(sensorId, sensorsConfiguration))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean usedMoreThanOneSensors = useBrewingSensorIds.size() > 1;
        boolean usedButNotShown = !new HashSet<>(showBrewingSensorIds).containsAll(useBrewingSensorIds);
        if (usedButNotShown || usedMoreThanOneSensors) {
            Float usedTemperature = getUsedTemperature();
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

    private TemperatureSensor getUncalibrated(String sensorId, Configuration.SensorsConfiguration sensorsConfiguration) {
        return TemperatureSensor.fromRaw(
                temperatureSensorProvider.getRawTemperatureSensor(sensorId),
                sensorsConfiguration
        );
    }
}
