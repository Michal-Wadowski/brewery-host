package wadosm.breweryhost.logic.general.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    private Map<String, List<Float>> temperatureCalibration;

    private Map<String, List<Float>> temperatureCalibrationMeasurements;

    private Integer brewingMotorNumber;

    protected SensorsConfiguration sensorsConfiguration;

    public ConfigurationBuilder toBuilder() {
        return builder()
                .temperatureCalibration(getTemperatureCalibration())
                .temperatureCalibrationMeasurements(getTemperatureCalibrationMeasurements())
                .brewingMotorNumber(getBrewingMotorNumber())
                .sensorsConfiguration(getSensorsConfiguration());
    }

    public Map<String, List<Float>> getTemperatureCalibration() {
        return Objects.requireNonNullElseGet(temperatureCalibration, Map::of);
    }

    public Map<String, List<Float>> getTemperatureCalibrationMeasurements() {
        return Objects.requireNonNullElseGet(temperatureCalibrationMeasurements, Map::of);
    }

    public Integer getBrewingMotorNumber() {
        return Objects.requireNonNullElse(brewingMotorNumber, 1);
    }

    public SensorsConfiguration getSensorsConfiguration() {
        if (sensorsConfiguration == null) {
            return SensorsConfiguration.empty();
        }
        return sensorsConfiguration;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorsConfiguration {

        private List<String> useBrewingSensorIds;

        private List<String> showBrewingSensorIds;

        public List<String> getUseBrewingSensorIds() {
            return Objects.requireNonNullElseGet(useBrewingSensorIds, List::of);
        }

        public List<String> getShowBrewingSensorIds() {
            return Objects.requireNonNullElseGet(showBrewingSensorIds, List::of);
        }

        public static SensorsConfiguration empty() {
            return SensorsConfiguration.builder()
                    .useBrewingSensorIds(new LinkedList<>())
                    .showBrewingSensorIds(new LinkedList<>())
                    .build();
        }
    }
}
