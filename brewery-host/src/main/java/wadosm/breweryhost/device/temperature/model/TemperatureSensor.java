package wadosm.breweryhost.device.temperature.model;

import lombok.*;

@AllArgsConstructor
@Getter
@ToString
@Builder
@EqualsAndHashCode
public class TemperatureSensor {

    private String sensorId;

    private Float temperature;

    public static TemperatureSensor fromRaw(RawTemperatureSensor rawTemperatureSensor) {
        if (rawTemperatureSensor == null) {
            return null;
        }
        return TemperatureSensor.builder()
                .sensorId(rawTemperatureSensor.getSensorId())
                .temperature(rawTemperatureSensor.getRawTemperature() / 1000.0f)
                .build();
    }
}
