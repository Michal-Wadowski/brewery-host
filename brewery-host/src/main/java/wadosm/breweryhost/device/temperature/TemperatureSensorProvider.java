package wadosm.breweryhost.device.temperature;

import wadosm.breweryhost.device.temperature.model.RawTemperatureSensor;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;

import java.util.List;

public interface TemperatureSensorProvider {

    void readPeriodicallySensors();

    List<TemperatureSensor> getTemperatureSensors();

    RawTemperatureSensor getRawTemperatureSensor(String sensorId);

}
