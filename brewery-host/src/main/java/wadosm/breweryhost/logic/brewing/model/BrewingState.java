package wadosm.breweryhost.logic.brewing.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class BrewingState {

    private final boolean enabled;

    private final List<TemperatureSensor> currentTemperature;

    private final Float destinationTemperature;

    private final Integer maxPower;

    private final Float powerTemperatureCorrelation;

    private final boolean motorEnabled;

    private final boolean temperatureAlarm;

    private final Integer heatingPower;
}
