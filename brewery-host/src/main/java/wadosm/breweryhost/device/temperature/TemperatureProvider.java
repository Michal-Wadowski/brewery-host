package wadosm.breweryhost.device.temperature;

import java.util.List;

public interface TemperatureProvider {

    void readPeriodicallySensors();

    List<TemperatureSensor> getTemperatureSensors();

    Integer getSensorTemperature(String sensorId);

}
