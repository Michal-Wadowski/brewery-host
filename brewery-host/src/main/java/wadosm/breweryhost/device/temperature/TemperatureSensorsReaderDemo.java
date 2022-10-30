package wadosm.breweryhost.device.temperature;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.temperature.model.RawTemperatureSensor;

import java.time.Instant;
import java.util.List;

@Component
@Profile("local")
public class TemperatureSensorsReaderDemo implements TemperatureSensorsReader {

    @Value("${fermenting.temperature_sensor.id}")
    @Getter
    @Setter
    private String fermentingTemperatureSensorId;

    @Value("${brewing.temperature_sensor.id}")
    @Getter
    @Setter
    private String brewingTemperatureSensorId;

    @Override
    public List<RawTemperatureSensor> readSensors() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }

        long time = Instant.now().getEpochSecond() % 60;

        int temperature1 =
                (int) ((Math.cos((time / 60.0 * 2 * Math.PI)) * 50 + 50) * 1000);
        int temperature2 =
                (int) ((Math.cos((time / 60.0 * 1.7 * Math.PI)) * 50 + 50) * 1000);

        return List.of(
                new RawTemperatureSensor(fermentingTemperatureSensorId, temperature1),
                new RawTemperatureSensor(brewingTemperatureSensorId, temperature2)
        );
    }
}
