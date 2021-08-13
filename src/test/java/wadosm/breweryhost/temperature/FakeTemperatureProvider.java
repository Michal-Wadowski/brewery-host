package wadosm.breweryhost.temperature;

import wadosm.breweryhost.temperature.TemperatureProvider;
import wadosm.breweryhost.temperature.TemperatureSensor;

import java.util.List;

public class FakeTemperatureProvider implements TemperatureProvider {

    public Integer currentTemperature;

    @Override
    public List<TemperatureSensor> getTemperatureSensors() {
        return null;
    }

    @Override
    public Integer getSensorTemperature(String sensorId) {
        return currentTemperature;
    }
}
