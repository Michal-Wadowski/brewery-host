package wadosm.breweryhost.device.temperature;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@EnableAsync
public class TemperatureProviderImpl implements TemperatureProvider {

    private List<TemperatureSensor> temperatureSensors = new ArrayList<>();

    private final TemperatureSensorsReader temperatureSensorsReader;

    private final AtomicBoolean ready = new AtomicBoolean(true);

    public TemperatureProviderImpl(TemperatureSensorsReader temperatureSensorsReader) {
        this.temperatureSensorsReader = temperatureSensorsReader;
    }

    @Scheduled(fixedDelay = 100)
    @Async
    @Override
    public void readPeriodicallySensors() {
        if (ready.get()) {
            ready.set(false);
            temperatureSensors = temperatureSensorsReader.readSensors();
            ready.set(true);
        }
    }

    @Override
    public List<TemperatureSensor> getTemperatureSensors() {
        return temperatureSensors;
    }

    @Override
    public Integer getSensorTemperature(String sensorId) {
        Optional<TemperatureSensor> result = temperatureSensors.stream().filter(
                x -> x.getSensorId().equals(sensorId)
        ).findFirst();

        return result.map(TemperatureSensor::getTemperature).orElse(null);
    }
}
