package wadosm.breweryhost.controller.brewing;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BrewingStatus {

    private final boolean powerEnabled;

    private final Float currentTemperature;

    private final Float dstTemperature;

    private final Float maxPower;

    private final Float powerTemperatureCorrelation;

    private final Integer timeElapsed;

    private final boolean motorEnabled;
}
