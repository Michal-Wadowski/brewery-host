package wadosm.breweryhost.logic.general;

import lombok.*;

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

    public ConfigurationBuilder toBuilder() {
        return builder()
                .temperatureCalibration(getTemperatureCalibration())
                .temperatureCalibrationMeasurements(getTemperatureCalibrationMeasurements())
                .brewingSensorId(getBrewingSensorId());
    }
}
