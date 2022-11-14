package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalibrationProviderTest {

    static Stream<Configuration> should_keep_temperature_unchanged_when_no_or_incomplete_calibration() {
        return Stream.of(
                Configuration.builder().build(),

                Configuration.builder().sensorsConfiguration(
                        Configuration.SensorsConfiguration.builder()
                                .calibrationMeasurements(Map.of("sensorId", Configuration.SensorsConfiguration.SensorCalibration.builder().build()))
                                .build()
                ).build(),

                Configuration.builder().sensorsConfiguration(
                        Configuration.SensorsConfiguration.builder()
                                .calibrationMeasurements(Map.of("sensorId", Configuration.SensorsConfiguration.SensorCalibration.builder()
                                        .lowMeasured(11.0)
                                        .lowDesired(10.0)
                                        .build()
                                ))
                                .build()
                ).build(),

                Configuration.builder().sensorsConfiguration(
                        Configuration.SensorsConfiguration.builder()
                                .calibrationMeasurements(Map.of("sensorId", Configuration.SensorsConfiguration.SensorCalibration.builder()
                                        .lowMeasured(11.0)
                                        .lowDesired(10.0)
                                        .highDesired(20.0)
                                        .build()
                                ))
                                .build()
                ).build(),

                Configuration.builder().sensorsConfiguration(
                        Configuration.SensorsConfiguration.builder()
                                .calibrationMeasurements(Map.of("other-sensorId", Configuration.SensorsConfiguration.SensorCalibration.builder()
                                        .lowMeasured(11.0)
                                        .lowDesired(10.0)
                                        .highMeasured(19.0)
                                        .highDesired(20.0)
                                        .build()
                                ))
                                .build()
                ).build()
        );
    }

    @ParameterizedTest
    @MethodSource
    void should_keep_temperature_unchanged_when_no_or_incomplete_calibration(Configuration configuration) {
        // given
        ConfigProvider configProvider = mock(ConfigProvider.class);
        CalibrationProvider calibrationProvider = new CalibrationProvider(configProvider);
        when(configProvider.loadConfiguration()).thenReturn(configuration);

        // when
        TemperatureSensor example = TemperatureSensor
                .builder()
                .sensorId("sensorId")
                .name("name")
                .used(true)
                .temperature(123.0)
                .build();
        TemperatureSensor temperatureSensor = calibrationProvider.correctTemperature(example);

        // then
        assertThat(temperatureSensor).isEqualTo(example);
    }


    static Stream<Arguments> should_calibrate_temperature() {
        Configuration identityConfiguration = Configuration.builder().sensorsConfiguration(
                Configuration.SensorsConfiguration.builder()
                        .calibrationMeasurements(Map.of("sensorId", Configuration.SensorsConfiguration.SensorCalibration.builder()
                                .lowMeasured(10.0)
                                .lowDesired(10.0)
                                .highMeasured(20.0)
                                .highDesired(20.0)
                                .build()
                        ))
                        .build()
        ).build();
        Configuration sampleConfiguration = Configuration.builder().sensorsConfiguration(
                Configuration.SensorsConfiguration.builder()
                        .calibrationMeasurements(Map.of("sensorId", Configuration.SensorsConfiguration.SensorCalibration.builder()
                                .lowMeasured(10.0)
                                .lowDesired(20.0)
                                .highMeasured(50.0)
                                .highDesired(40.0)
                                .build()
                        ))
                        .build()
        ).build();
        return Stream.of(
                Arguments.of(
                        identityConfiguration,
                        10.0,
                        10.0
                ),

                Arguments.of(
                        sampleConfiguration,
                        10.0,
                        20.0
                ),

                Arguments.of(
                        sampleConfiguration,
                        50.0,
                        40.0
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void should_calibrate_temperature(Configuration configuration, Double measured, Double desired) {
        // given
        ConfigProvider configProvider = mock(ConfigProvider.class);
        CalibrationProvider calibrationProvider = new CalibrationProvider(configProvider);
        when(configProvider.loadConfiguration()).thenReturn(configuration);

        // when
        TemperatureSensor example = TemperatureSensor
                .builder()
                .sensorId("sensorId")
                .name("name")
                .used(true)
                .temperature(measured)
                .build();
        TemperatureSensor temperatureSensor = calibrationProvider.correctTemperature(example);

        // then
        assertThat(temperatureSensor).isEqualTo(TemperatureSensor
                .builder()
                .sensorId("sensorId")
                .name("name")
                .used(true)
                .temperature(desired)
                .build()
        );
    }

}