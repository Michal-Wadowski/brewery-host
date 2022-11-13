package wadosm.breweryhost.device.temperature.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import wadosm.breweryhost.logic.general.model.Configuration;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Builder
@Value
public class TemperatureSensor {

    @NotNull String sensorId;
    @With
    @NonNull Double temperature;
    String name;
    @Builder.Default
    boolean used = false;

    public static TemperatureSensor fromRaw(RawTemperatureSensor rawTemperatureSensor, Configuration.SensorsConfiguration sensorsConfiguration) {
        if (rawTemperatureSensor == null) {
            return null;
        }
        Map<String, String> sensorNames = sensorsConfiguration.getSensorNames();
        return TemperatureSensor.builder()
                .sensorId(rawTemperatureSensor.getSensorId())
                .temperature(rawTemperatureSensor.getRawTemperature() / 1000.0)
                .name(sensorNames.getOrDefault(rawTemperatureSensor.getSensorId(), null))
                .used(sensorsConfiguration.getUseBrewingSensorIds().contains(rawTemperatureSensor.getSensorId()))
                .build();
    }
}
