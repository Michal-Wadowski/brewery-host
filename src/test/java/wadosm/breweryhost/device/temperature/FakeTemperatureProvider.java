package wadosm.breweryhost.device.temperature;

import java.util.List;

public class FakeTemperatureProvider implements TemperatureProvider {

    public Integer currentTemperature;

    @Override
    public void readPeriodicallySensors() {

    }

    @Override
    public List<TemperatureSensor> getTemperatureSensors() {
        return null;
    }

    @Override
    public Integer getSensorTemperature(String sensorId) {
        return currentTemperature;
    }
}
