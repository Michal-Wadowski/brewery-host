package wadosm.breweryhost.logic.brewing;

import lombok.Getter;
import lombok.Setter;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wadosm.breweryhost.DigiPort;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.device.driver.BreweryInterfaceImpl;
import wadosm.breweryhost.device.temperature.TemperatureProvider;
import wadosm.breweryhost.device.temperature.TemperatureSensor;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BrewingServiceImplTest {

    private static Stream<Arguments> should_get_calibrated_temperature_with_calibration() {
        return Stream.of(
                Arguments.of(null, 50500, 50.5f),
                Arguments.of(List.of(0.0f, 0.0f), 50500, 50.5f),
                Arguments.of(List.of(0.0f, 10.0f), 50500, 60.5f),
                Arguments.of(List.of(0.1f, 0.0f), 0, 0.0f),
                Arguments.of(List.of(0.2f, 0.0f), 50500, 60.6f),
                Arguments.of(List.of(0.2f, 10.0f), 50500, 70.6f),
                Arguments.of(List.of(0.0f, 10.0f), 0, 10.0f),
                Arguments.of(List.of(-0.1f, -10.0f), 50500, 35.45f)
        );
    }

    private static Stream<Arguments> temperatureCalibrationMeasurements() {
        return Stream.of(
                Arguments.of(
                        Map.of("aabbcc", Arrays.asList(null, null, -1f, -2f)),
                        0,
                        List.of(10.0f, 20.0f, -1f, -2f)
                ),

                Arguments.of(
                        Map.of("aabbcc", Arrays.asList(-1f, -2f, null, null)),
                        1,
                        List.of(-1f, -2f, 10.0f, 20.0f)
                ),

                Arguments.of(
                        null,
                        0,
                        Arrays.asList(10.0f, 20.0f, null, null)
                ),

                Arguments.of(
                        null,
                        1,
                        Arrays.asList(null, null, 10.0f, 20.0f)
                )
        );
    }

    private static Stream<Arguments> calibrateTemperature() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        0,
                        null
                ),

                Arguments.of(
                        Map.of("aabbcc", Arrays.asList(null, null, null, null)),
                        Map.of("aabbcc", Arrays.asList(-1f, -2f)),
                        0,
                        null
                ),

                Arguments.of(
                        Map.of("aabbcc", Arrays.asList(10f, 20f, null, null)),
                        Map.of("aabbcc", Arrays.asList(-1f, -2f)),
                        1,
                        Arrays.asList(0.5f, 15f)
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void should_get_calibrated_temperature_with_calibration(List<Float> currCalib, Integer sensorValue,
                                                            Float expectedTemperature) {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();
        if (currCalib != null) {
            configProvider.setConfiguration(
                    Configuration.builder().temperatureCalibration(Map.of("aabbcc", currCalib)).build());
        }
        BrewingServiceImpl brewingService = new BrewingServiceImpl(
                breweryInterface, temperatureProvider, configProvider
        );
        brewingService.setBrewingTemperatureSensor("aabbcc");

        temperatureProvider.setCurrTemperature(sensorValue);

        // when
        BrewingState brewingState = brewingService.getBrewingState();

        // then
        assertThat(brewingState.getCurrentTemperature()).isEqualTo(expectedTemperature);
    }

    @ParameterizedTest
    @MethodSource
    void temperatureCalibrationMeasurements(Map<String, List<Float>> initialConfig, Integer side,
                                            List<Float> expectedMeasurements) {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        configProvider.getConfiguration().setTemperatureCalibrationMeasurements(initialConfig);

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);
        brewingService.setBrewingTemperatureSensor("aabbcc");

        temperatureProvider.setCurrTemperature(10000);

        // when
        brewingService.calibrateTemperature(side, 20.0f);

        // then
        assertThat(configProvider.getConfiguration().getTemperatureCalibrationMeasurements())
                .isNotNull()
                .containsKey("aabbcc")
                .extracting("aabbcc").asList()
                .hasSize(4)
                .containsSequence(expectedMeasurements);
    }

    @ParameterizedTest
    @MethodSource
    void calibrateTemperature(Map<String, List<Float>> initialConfig, Map<String, List<Float>> initialCalibration,
                              Integer side, List<Float> expectedCalibration) {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        configProvider.getConfiguration().setTemperatureCalibration(initialCalibration);
        configProvider.getConfiguration().setTemperatureCalibrationMeasurements(initialConfig);

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);
        brewingService.setBrewingTemperatureSensor("aabbcc");

        temperatureProvider.setCurrTemperature(50000);

        // when
        brewingService.calibrateTemperature(side, 40.0f);

        // then
        MapAssert<String, List<Float>> assertion =
                assertThat(configProvider.getConfiguration().getTemperatureCalibration())
                        .isNotNull();

        if (expectedCalibration == null) {
            assertion.doesNotContainKey("aabbcc");
        } else {
            assertion.containsKey("aabbcc")
                    .extracting("aabbcc").asList()
                    .hasSize(2)
                    .containsSequence(expectedCalibration);
        }
    }

    @Test
    void shouldUpdateCalibrationFileWhileCalibrating() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);
        brewingService.setBrewingTemperatureSensor("aabbcc");

        temperatureProvider.setCurrTemperature(50000);

        // when
        brewingService.calibrateTemperature(0, 40.0f);

        // then
        assertThat(configProvider.isConfigUpdated()).isTrue();
    }

//    @Test
//    void testEquation1() {
//        var x1 = 10f;
//        var t1 = 20f;
//        var x2 = 50f;
//        var t2 = 40f;
//
//        var a = (t2 - t1) / (x2 - x1);
//        var b = -a * x1 + t1;
//
//        assertThat(a * 10f + b).isEqualTo(20f);
//        assertThat(a * 50f + b).isEqualTo(40f);
//    }

    private static class FakeTemperatureProvider implements TemperatureProvider {

        @Setter
        private Integer currTemperature = null;

        @Override
        public void readPeriodicallySensors() {

        }

        @Override
        public List<TemperatureSensor> getTemperatureSensors() {
            return null;
        }

        @Override
        public Integer getSensorTemperature(String sensorId) {
            return currTemperature;
        }
    }

    private static class FakeConfigProvider implements ConfigProvider {

        private Configuration configuration = new Configuration();

        @Getter
        private boolean configUpdated = false;

        @Override
        public Configuration getConfiguration() {
            return configuration;
        }

        @Override
        public void setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            configUpdated = true;
        }

        @Override
        public List<Float> getTemperatureCalibrationOf(String brewingTemperatureSensor) {
            if (getConfiguration().getTemperatureCalibration() != null) {
                return getConfiguration().getTemperatureCalibration().get(brewingTemperatureSensor);
            }
            return null;
        }

    }
}