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
    Float destinationTemperature;
    @With
    Integer maxPower;
    @With
    Float powerTemperatureCorrelation;
    @With
    boolean motorEnabled;
    @With
    boolean temperatureAlarmEnabled;
}