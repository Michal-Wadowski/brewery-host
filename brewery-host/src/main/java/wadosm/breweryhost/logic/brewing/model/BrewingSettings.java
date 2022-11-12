package wadosm.breweryhost.logic.brewing.model;

import lombok.*;

@Value
@Builder
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