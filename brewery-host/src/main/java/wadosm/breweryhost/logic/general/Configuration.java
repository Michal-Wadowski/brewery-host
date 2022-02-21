package wadosm.breweryhost.logic.general;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
    private Map<String, List<Float>> temperatureCalibration;
    private Map<String, List<Float>> temperatureCalibrationMeasurements;
}
