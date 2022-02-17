package wadosm.breweryhost.logic.general;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class Configuration {
    private Map<String, List<Float>> thermometerCalibration;
}
