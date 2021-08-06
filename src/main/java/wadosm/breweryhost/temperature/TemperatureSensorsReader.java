package wadosm.breweryhost.temperature;

import java.util.List;

public interface TemperatureSensorsReader {

    List<TemperatureSensor> readSensors();

}
