package wadosm.breweryhost.logic.brewing.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Jacksonized
@Builder
@Value
public class SensorsConfiguration {
    @Builder.Default
    @With
    @NonNull List<String> useBrewingSensorIds = List.of();
    @Builder.Default
    @With
    @NonNull List<String> showBrewingSensorIds = List.of();
    @With
    @Builder.Default
    @NonNull Map<String, String> sensorNames = Map.of();
    @Builder.Default
    @With
    @NonNull Map<String, SensorCalibration> calibrationMeasurements = Map.of();

    @Jacksonized
    @Builder
    @Value
    public static class SensorCalibration {
        Double lowMeasured;
        Double lowDesired;
        Double highMeasured;
        Double highDesired;
    }
}
