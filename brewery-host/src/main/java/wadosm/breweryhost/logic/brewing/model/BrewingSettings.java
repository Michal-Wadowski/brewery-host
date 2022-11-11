package wadosm.breweryhost.logic.brewing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BrewingSettings {
    private boolean enabled;
    private Float destinationTemperature;
    private Integer maxPower;
    private Float powerTemperatureCorrelation;
    private boolean motorEnabled;
    private boolean temperatureAlarmEnabled;
}