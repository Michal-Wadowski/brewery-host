package wadosm.breweryhost.externalinterface.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wadosm.breweryhost.driver.DriverInterfaceState;
import wadosm.breweryhost.temperature.TemperatureSensor;

import java.util.List;

@Getter
@AllArgsConstructor
public class BreweryStatusDTO {

    private Integer commandId;

    private Long time;

    private DriverInterfaceState driverInterfaceState;

    private List<TemperatureSensor> temperatureSensors;

}
