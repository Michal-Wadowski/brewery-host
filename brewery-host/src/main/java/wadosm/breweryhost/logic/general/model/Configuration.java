package wadosm.breweryhost.logic.general.model;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Jacksonized
@Builder
@Value
public class Configuration {

    @Builder.Default
    @With @NonNull Map<String, List<Float>> temperatureCalibration = Map.of();
    @Builder.Default
    @With @NonNull Map<String, List<Float>> temperatureCalibrationMeasurements = Map.of();
    @Builder.Default
    @With @NonNull Integer brewingMotorNumber = 1;
    @Builder.Default
    @With @NonNull SensorsConfiguration sensorsConfiguration = SensorsConfiguration.builder().build();

    @Jacksonized
    @Builder
    @Value
    public static class SensorsConfiguration {
        @Builder.Default
        @With @NonNull List<String> useBrewingSensorIds = List.of();
        @Builder.Default
        @With @NonNull List<String> showBrewingSensorIds = List.of();
    }
}
