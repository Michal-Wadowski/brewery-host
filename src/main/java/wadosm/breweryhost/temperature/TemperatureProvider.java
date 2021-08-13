package wadosm.breweryhost.temperature;

import java.util.List;

public interface TemperatureProvider {

    List<TemperatureSensor> getTemperatureSensors();

    Integer getSensorTemperature(String sensorId);

}
