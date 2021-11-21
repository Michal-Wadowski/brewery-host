package wadosm.breweryhost.device.temperature;

import java.util.List;

public interface TemperatureSensorsReader {

    List<TemperatureSensor> readSensors();

}
