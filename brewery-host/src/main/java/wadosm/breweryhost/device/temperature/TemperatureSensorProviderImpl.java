package wadosm.breweryhost.device.temperature;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.temperature.model.RawTemperatureSensor;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class TemperatureSensorProviderImpl implements TemperatureSensorProvider {

    private List<RawTemperatureSensor> rawTemperatureSensors = new ArrayList<>();

    private final TemperatureSensorsReader temperatureSensorsReader;

    private final AtomicBoolean ready = new AtomicBoolean(true);

    public TemperatureSensorProviderImpl(TemperatureSensorsReader temperatureSensorsReader) {
        this.temperatureSensorsReader = temperatureSensorsReader;
    }

    @Scheduled(fixedDelay = 100)
    @Async
    @Override
    public void readPeriodicallySensors() {
        if (ready.get()) {
            ready.set(false);
            rawTemperatureSensors = temperatureSensorsReader.readSensors();
            ready.set(true);
        }
    }

    @Override
    public List<TemperatureSensor> getTemperatureSensors() {
        return rawTemperatureSensors
                .stream().map(TemperatureSensor::fromRaw)
                .collect(Collectors.toList());
    }

    @Override
    public RawTemperatureSensor getRawTemperatureSensor(String sensorId) {
        return rawTemperatureSensors.stream().filter(
                x -> x.getSensorId().equals(sensorId)
        ).findFirst().orElse(null);
    }
}
