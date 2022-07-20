package wadosm.breweryhost.device.temperature;

import wadosm.breweryhost.device.temperature.model.RawTemperatureSensor;

import java.util.List;

public interface TemperatureSensorsReader {

    List<RawTemperatureSensor> readSensors();

}
