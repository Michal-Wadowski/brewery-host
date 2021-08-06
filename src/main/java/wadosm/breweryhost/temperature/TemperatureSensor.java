package wadosm.breweryhost.temperature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class TemperatureSensor {

    private String sensorId;

    private Integer temperature;

}
