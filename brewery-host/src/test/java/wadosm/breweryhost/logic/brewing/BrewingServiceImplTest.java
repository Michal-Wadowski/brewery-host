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
import static org.mockito.Mockito.*;

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
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();
        if (currCalib != null) {
            configProvider.saveConfiguration(
                    Configuration.builder()
                            .temperatureCalibration(Map.of("aabbcc", currCalib))
                            .brewingSensorId("aabbcc")
                            .build()
            );
        }
        BrewingServiceImpl brewingService = new BrewingServiceImpl(
                breweryInterface, temperatureProvider, configProvider
        );

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
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .brewingSensorId("aabbcc")
                        .temperatureCalibrationMeasurements(initialConfig)
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        temperatureProvider.setCurrTemperature(10000);

        // when
        brewingService.calibrateTemperature(side, 20.0f);

        // then
        assertThat(configProvider.loadConfiguration().getTemperatureCalibrationMeasurements())
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
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .brewingSensorId("aabbcc")
                        .temperatureCalibrationMeasurements(initialConfig)
                        .temperatureCalibration(initialCalibration)
                        .build()
        );
        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        temperatureProvider.setCurrTemperature(50000);

        // when
        brewingService.calibrateTemperature(side, 40.0f);

        // then
        MapAssert<String, List<Float>> assertion =
                assertThat(configProvider.loadConfiguration().getTemperatureCalibration())
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
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .brewingSensorId("aabbcc")
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        temperatureProvider.setCurrTemperature(50000);

        // when
        brewingService.calibrateTemperature(0, 40.0f);

        // then
        assertThat(configProvider.isConfigUpdated()).isTrue();
    }

    @Test
    void shouldNotFailOnEmptyConfiguration() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        // when/then
        brewingService.processStep();
    }

    @Test
    void processStep_when_no_thermometer_configured() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .brewingMotorNumber(1)
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        // when
        brewingService.enable(true);

        // then
        verify(digiPort, atLeastOnce()).clear(0);
    }

    @Test
    void motor_enable_when_no_thermometer() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .brewingMotorNumber(1)
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        // when
        brewingService.enable(true);
        brewingService.motorEnable(true);

        // then
        verify(digiPort, atLeastOnce()).digitalWrite(BreweryInterfaceImpl.Pin.MOTOR_1.pinNumber, 1);
    }

    @Test
    void processStep_when_no_motor_configured() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        // when
        brewingService.enable(true);
        brewingService.motorEnable(true);

        // then
        verify(digiPort, atLeastOnce()).clear(0);
    }

    @Test
    void should_use_configuration_as_thermometer_id_source() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        TemperatureProvider temperatureProvider = mock(TemperatureProvider.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);

        when(temperatureProvider.getSensorTemperature("aabbcc"))
                .thenReturn(12350);

        when(configProvider.loadConfiguration()).thenReturn(
                Configuration.builder()
                        .brewingSensorId("aabbcc")
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface, temperatureProvider,
                configProvider);

        // when
        BrewingState brewingState = brewingService.getBrewingState();

        // then
        assertThat(brewingState.getCurrentTemperature()).isEqualTo(12.35f);
    }

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

        private Configuration configuration;

        @Getter
        private boolean configUpdated = false;

        public FakeConfigProvider(Configuration configuration) {
            this.configuration = configuration;
        }

        public FakeConfigProvider() {
            this.configuration = new Configuration();
        }

        @Override
        public Configuration loadConfiguration() {
            return configuration;
        }

        @Override
        public void saveConfiguration(Configuration configuration) {
            this.configuration = configuration;
            configUpdated = true;
        }

    }
}