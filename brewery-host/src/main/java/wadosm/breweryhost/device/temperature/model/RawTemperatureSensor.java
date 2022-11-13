package wadosm.breweryhost.device.temperature.model;

import lombok.Value;

@Value
public class RawTemperatureSensor {
    String sensorId;
    Integer rawTemperature;
}
