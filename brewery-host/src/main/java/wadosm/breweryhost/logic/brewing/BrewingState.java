package wadosm.breweryhost.logic.brewing;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BrewingState {

    private final boolean enabled;

    private final Float currentTemperature;

    private final Float destinationTemperature;

    private final Integer maxPower;

    private final Float powerTemperatureCorrelation;

    private final Integer timeElapsed;

    private final boolean motorEnabled;

    private final boolean temperatureAlarm;

    private final Integer heatingPower;
}
