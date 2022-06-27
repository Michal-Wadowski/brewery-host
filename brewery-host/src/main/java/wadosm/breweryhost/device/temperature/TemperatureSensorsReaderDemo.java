package wadosm.breweryhost.device.temperature;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
    public List<TemperatureSensor> readSensors() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }
        int temperature = (int) ((Math.cos(((Instant.now().getNano()) / 100000000.0 * 2 * Math.PI)) * 50 + 50) * 1000);

        return List.of(
                new TemperatureSensor(fermentingTemperatureSensorId, temperature),
                new TemperatureSensor(brewingTemperatureSensorId, temperature)
        );
    }
}
