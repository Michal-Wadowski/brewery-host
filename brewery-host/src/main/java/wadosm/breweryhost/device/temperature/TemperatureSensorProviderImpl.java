package wadosm.breweryhost.device.temperature;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.temperature.model.RawTemperatureSensor;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@EnableAsync
@RequiredArgsConstructor
public class TemperatureSensorProviderImpl implements TemperatureSensorProvider {


    private final TemperatureSensorsReader temperatureSensorsReader;
    private final ConfigProvider configProvider;

    private List<RawTemperatureSensor> rawTemperatureSensors = new ArrayList<>();
    private final AtomicBoolean ready = new AtomicBoolean(true);

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
        Configuration configuration = configProvider.loadConfiguration();
        return rawTemperatureSensors
                .stream().map(rawSensor -> TemperatureSensor.fromRaw(rawSensor, configuration.getSensorsConfiguration()))
                .collect(Collectors.toList());
    }

    @Override
    public RawTemperatureSensor getRawTemperatureSensor(String sensorId) {
        return rawTemperatureSensors.stream().filter(
                x -> x.getSensorId().equals(sensorId)
        ).findFirst().orElse(null);
    }
}
