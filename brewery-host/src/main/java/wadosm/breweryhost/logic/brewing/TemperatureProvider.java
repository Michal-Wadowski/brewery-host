package wadosm.breweryhost.logic.brewing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.temperature.TemperatureSensorProvider;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TemperatureProvider {

    private final ConfigProvider configuration;
    private final TemperatureSensorProvider temperatureSensorProvider;

    Float getUsedTemperature() {
        double usedTemperature = getConfiguration().getSensorsConfiguration().getUseBrewingSensorIds().stream()
                .map(this::getCalibratedTemperature)
                .filter(Objects::nonNull)
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(usedTemperature)) {
            return null;
        } else {
            return (float) usedTemperature;
        }
    }

    private Float getCalibratedTemperature(String shownSensorId) {
        Float temperature = getUncalibratedTemperature(shownSensorId);
        Float calibratedTemperature = null;

        if (temperature != null) {
            Map<String, List<Float>> temperatureCalibration = getConfiguration().getTemperatureCalibration();

            // TODO: #2
            if (temperatureCalibration != null && temperatureCalibration.containsKey(shownSensorId)) {
                List<Float> sensorCalibration = temperatureCalibration.get(shownSensorId);
                if (sensorCalibration.size() == 2) {
                    temperature *= (1 + sensorCalibration.get(0));
                    temperature += sensorCalibration.get(1);
                }
            }

            calibratedTemperature = Math.round(temperature * 100) / 100.0f;
        }
        return calibratedTemperature;
    }

    private Configuration getConfiguration() {
        return configuration.loadConfiguration();
    }

    List<TemperatureSensor> getAllTemperatures() {
        Configuration.SensorsConfiguration sensorsConfiguration = getConfiguration().getSensorsConfiguration();
        List<String> showBrewingSensorIds = sensorsConfiguration.getShowBrewingSensorIds();
        List<String> useBrewingSensorIds = sensorsConfiguration.getUseBrewingSensorIds();

        List<TemperatureSensor> result =
                showBrewingSensorIds.stream().map(sensorId -> {
                            Float temperature = getCalibratedTemperature(sensorId);
                            if (temperature == null) {
                                return null;
                            }
                            return TemperatureSensor.builder()
                                    .sensorId(sensorId)
                                    .temperature(temperature)
                                    .build();
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        boolean usedSameAsShown = useBrewingSensorIds.equals(showBrewingSensorIds);
        boolean shownSingleSensor = showBrewingSensorIds.size() == 1;

        if (!usedSameAsShown || !shownSingleSensor) {
            Float usedTemperature = getUsedTemperature();
            if (usedTemperature != null) {
                result.add(TemperatureSensor.builder()
                        .sensorId("#use")
                        .temperature(usedTemperature)
                        .build()
                );
            }
        }

        return result;
    }

    private Float getUncalibratedTemperature(String sensorId) {
        // TODO: Update here after tests about use multiple sensors
        TemperatureSensor temperatureSensor = TemperatureSensor.fromRaw(
                temperatureSensorProvider.getRawTemperatureSensor(sensorId)
        );

        if (temperatureSensor != null) {
            return temperatureSensor.getTemperature();
        } else {
            return null;
        }
    }
}
