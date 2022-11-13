package wadosm.breweryhost.logic.brewing.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class BrewingSettings {
    @With
    boolean enabled;
    @With
    Double destinationTemperature;
    @With
    Integer maxPower;
    @With
    Double powerTemperatureCorrelation;
    @With
    boolean motorEnabled;
    @With
    boolean temperatureAlarmEnabled;
}