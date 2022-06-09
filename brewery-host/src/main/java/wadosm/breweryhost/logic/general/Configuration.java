package wadosm.breweryhost.logic.general;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    private Map<String, List<Float>> temperatureCalibration;

    private Map<String, List<Float>> temperatureCalibrationMeasurements;

    private String brewingSensorId;

    private Integer brewingMotorNumber;

    public ConfigurationBuilder toBuilder() {
        return builder()
                .temperatureCalibration(getTemperatureCalibration())
                .temperatureCalibrationMeasurements(getTemperatureCalibrationMeasurements())
                .brewingSensorId(getBrewingSensorId())
                .brewingMotorNumber(getBrewingMotorNumber());
    }
}
