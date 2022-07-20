package wadosm.breweryhost.device.temperature.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class RawTemperatureSensor {

    private String sensorId;

    private Integer rawTemperature;

}
