package wadosm.breweryhost.logic.brewing;

import lombok.Getter;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wadosm.breweryhost.DigiPort;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.device.driver.BreweryInterfaceImpl;
import wadosm.breweryhost.device.temperature.TemperatureSensorProvider;
import wadosm.breweryhost.device.temperature.model.RawTemperatureSensor;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.brewing.model.BrewingSnapshotState;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BrewingServiceImplTest {

    private static Stream<Arguments> should_get_calibrated_temperature_with_calibration() {
        return Stream.of(
                Arguments.of(null, 50500,
                        List.of(new TemperatureSensor("aabbcc", 50.5f))),
                Arguments.of(List.of(0.0f, 0.0f), 50500,
                        List.of(new TemperatureSensor("aabbcc", 50.5f))),
                Arguments.of(List.of(0.0f, 10.0f), 50500,
                        List.of(new TemperatureSensor("aabbcc", 60.5f))),
                Arguments.of(List.of(0.1f, 0.0f), 0,
                        List.of(new TemperatureSensor("aabbcc", 0.0f))),
                Arguments.of(List.of(0.2f, 0.0f), 50500,
                        List.of(new TemperatureSensor("aabbcc", 60.6f))),
                Arguments.of(List.of(0.2f, 10.0f), 50500,
                        List.of(new TemperatureSensor("aabbcc", 70.6f))),
                Arguments.of(List.of(0.0f, 10.0f), 0,
                        List.of(new TemperatureSensor("aabbcc", 10.0f))),
                Arguments.of(List.of(-0.1f, -10.0f), 50500,
                        List.of(new TemperatureSensor("aabbcc", 35.45f)))
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

    public static Stream<Arguments> should_handle_configuration_variants() {
        return Stream.of(
                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(TemperatureSensor.builder().sensorId("aabbcc").temperature(50.12f).build())
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .showBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66100)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50100)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("aabbcc").temperature(50.1f).build(),
                                TemperatureSensor.builder().sensorId("ddeeff").temperature(66.1f).build(),
                                TemperatureSensor.builder().sensorId("#use").temperature(58.1f).build()
                        )
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),
                        new FakeTemperatureSensorProvider(),
                        List.of()
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(Configuration.SensorsConfiguration.empty()).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of()
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("ddeeff"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("ddeeff").temperature(66.12f).build(),
                                TemperatureSensor.builder().sensorId("#use").temperature(50.12f).build()
                        )
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("aabbcc").temperature(50.12f).build(),
                                TemperatureSensor.builder().sensorId("ddeeff").temperature(66.12f).build(),
                                TemperatureSensor.builder().sensorId("#use").temperature(50.12f).build()
                        )
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("#use").temperature(50.12f).build()
                        )
                )
        );
    }

    public static Stream<Arguments> should_handle_used_sensor_variants() {
        return Stream.of(
                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 99120)))
                                .findFirst().get(),

                        99f,
                        true
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 99120)))
                                .findFirst().get(),

                        30f,
                        false
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),

                        new FakeTemperatureSensorProvider(),

                        99f,
                        false
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 99120)))
                                .findFirst().get(),

                        99f,
                        false
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("ddeeff"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 99120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 30120)))
                                .findFirst().get(),

                        99f,
                        true
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 20000)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 60000)))
                                .findFirst().get(),

                        41f,
                        true
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                Configuration.SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 20000)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 60000)))
                                .findFirst().get(),

                        39f,
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void should_get_calibrated_temperature_with_calibration(List<Float> currCalib, Integer sensorValue,
                                                            List<TemperatureSensor> expectedTemperatureSensors) {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        Configuration.ConfigurationBuilder configurationBuilder = Configuration.builder()
                .sensorsConfiguration(Configuration.SensorsConfiguration.builder()
                        .showBrewingSensorIds(List.of("aabbcc"))
                        .build());

        if (currCalib != null) {
            configurationBuilder.temperatureCalibration(Map.of("aabbcc", currCalib));
        }

        configProvider.saveConfiguration(configurationBuilder.build());

        BrewingServiceImpl brewingService = new BrewingServiceImpl(
                breweryInterface, configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider),
                mock(MainsPowerProvider.class));

        temperatureProvider.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", sensorValue));

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getReadings().getCurrentTemperature()).
                isEqualTo(expectedTemperatureSensors);
    }

    private static BrewingSettingsProvider getMockedBrewingSettingsProvider() {
        BrewingSettingsProvider settingsProvider = mock(BrewingSettingsProvider.class);
        when(settingsProvider.getBrewingSettings()).thenReturn(new BrewingSettings());
        return settingsProvider;
    }

    @ParameterizedTest
    @MethodSource
    void should_handle_configuration_variants(Configuration configuration,
                                              TemperatureSensorProvider sensorProvider,
                                              List<TemperatureSensor> expectedTemperatureSensors) {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeConfigProvider configProvider = new FakeConfigProvider();
        configProvider.saveConfiguration(configuration);

        BrewingServiceImpl brewingService = new BrewingServiceImpl(
                breweryInterface, configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, sensorProvider), mock(MainsPowerProvider.class)
        );

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getReadings().getCurrentTemperature()).
                isEqualTo(expectedTemperatureSensors);
    }

    @ParameterizedTest
    @MethodSource
    void should_handle_used_sensor_variants(Configuration configuration,
                                            TemperatureSensorProvider sensorProvider,
                                            Float destinationTemperature,
                                            Boolean power) {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeConfigProvider configProvider = new FakeConfigProvider();
        configProvider.saveConfiguration(configuration);

        BrewingSettingsProvider brewingSettingsProvider = getMockedBrewingSettingsProvider();
        BrewingServiceImpl brewingService = new BrewingServiceImpl(
                breweryInterface, configProvider, brewingSettingsProvider,
                new TemperatureProvider(configProvider, sensorProvider),
                new MainsPowerProvider(brewingSettingsProvider, breweryInterface)
        );

        brewingService.setDestinationTemperature(destinationTemperature);

        // when
        brewingService.enable(true);

        // then
        if (power) {
            verify(breweryInterface).setMainsPower(eq(1), eq(255));
        } else {
            verify(breweryInterface, never()).setMainsPower(eq(1), eq(255));
        }
    }

    @Disabled
    @ParameterizedTest
    @MethodSource
    void temperatureCalibrationMeasurements(Map<String, List<Float>> initialConfig, Integer side,
                                            List<Float> expectedMeasurements) {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .sensorsConfiguration(Configuration.SensorsConfiguration.builder()
                                .useBrewingSensorIds(List.of("aabbcc"))
                                .build())
                        .temperatureCalibrationMeasurements(initialConfig)
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

        temperatureProvider.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 10000));

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

    @Disabled
    @ParameterizedTest
    @MethodSource
    void calibrateTemperature(Map<String, List<Float>> initialConfig, Map<String, List<Float>> initialCalibration,
                              Integer side, List<Float> expectedCalibration) {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .sensorsConfiguration(Configuration.SensorsConfiguration.builder()
                                .useBrewingSensorIds(List.of("aabbcc"))
                                .build())
                        .temperatureCalibrationMeasurements(initialConfig)
                        .temperatureCalibration(initialCalibration)
                        .build()
        );
        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

        temperatureProvider.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50000));

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
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .sensorsConfiguration(Configuration.SensorsConfiguration.builder()
                                .useBrewingSensorIds(List.of("aabbcc"))
                                .build())
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

        temperatureProvider.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50000));

        // when
        brewingService.calibrateTemperature(0, 40.0f);

        // then
        assertThat(configProvider.isConfigUpdated()).isTrue();
    }

    @Test
    void shouldNotFailOnEmptyConfiguration() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

        // when/then
        brewingService.processStep();
    }

    @Test
    void shouldUseDefaultMotorPin() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

        brewingService.motorEnable(true);
        brewingService.enable(true);

        // when
        brewingService.processStep();

        // then
        verify(breweryInterface, atLeastOnce()).motorEnable(eq(1), eq(true));
    }

    @Test
    void processStep_when_no_thermometer_configured() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterface breweryInterface = new BreweryInterfaceImpl(digiPort);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .brewingMotorNumber(1)
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

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
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider(
                Configuration.builder()
                        .brewingMotorNumber(1)
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

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
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureProvider), mock(MainsPowerProvider.class)
        );

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
        TemperatureSensorProvider temperatureSensorProvider = mock(TemperatureSensorProvider.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);

        when(temperatureSensorProvider.getRawTemperatureSensor("aabbcc"))
                .thenReturn(new RawTemperatureSensor("aabbcc", 12350));

        when(configProvider.loadConfiguration()).thenReturn(
                Configuration.builder()
                        .sensorsConfiguration(Configuration.SensorsConfiguration.builder()
                                .showBrewingSensorIds(List.of("aabbcc"))
                                .useBrewingSensorIds(List.of("aabbcc"))
                                .build())
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, temperatureSensorProvider), mock(MainsPowerProvider.class)
        );

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getReadings().getCurrentTemperature()).isEqualTo(
                List.of(new TemperatureSensor("aabbcc", 12.35f))
        );
    }

    @Test
    void test_valid_powerTemperatureCorrelation_from_getBrewingSnapshotState() {
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        TemperatureSensorProvider sensorProvider = mock(TemperatureSensorProvider.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(new Configuration());

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, getMockedBrewingSettingsProvider(), new TemperatureProvider(configProvider, sensorProvider), mock(MainsPowerProvider.class)
        );

        brewingService.setPowerTemperatureCorrelation(123.45f);

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getSettings().getPowerTemperatureCorrelation()).isEqualTo(123.45f);
    }

    @Test
    void test_keep_valid_powerTemperatureCorrelation_while_processStep() {
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(new Configuration());

        TemperatureProvider temperatureProvider = mock(TemperatureProvider.class);
        when(temperatureProvider.getUsedTemperature()).thenReturn(50.0f);

        BrewingSettingsProvider brewingSettingsProvider = getMockedBrewingSettingsProvider();
        brewingSettingsProvider.getBrewingSettings().setEnabled(true);
        brewingSettingsProvider.getBrewingSettings().setDestinationTemperature(100.0f);
        brewingSettingsProvider.getBrewingSettings().setMaxPower(70);

        MainsPowerProvider mainsPowerProvider = new MainsPowerProvider(brewingSettingsProvider, breweryInterface);

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, brewingSettingsProvider, temperatureProvider, mainsPowerProvider
        );

        // when
        brewingService.setPowerTemperatureCorrelation(1f);

        // then
        verify(breweryInterface).setMainsPower(1, 0x7f);
        verify(breweryInterface).setMainsPower(2, 0x7f);
    }

    private static class FakeTemperatureSensorProvider implements TemperatureSensorProvider {

        private final Map<String, RawTemperatureSensor> sensorsMap = new HashMap<>();

        @Override
        public void readPeriodicallySensors() {

        }

        @Override
        public List<TemperatureSensor> getTemperatureSensors() {
            return null;
        }

        @Override
        public RawTemperatureSensor getRawTemperatureSensor(String sensorId) {
            return sensorsMap.get(sensorId);
        }

        public void setCurrTemperatureSensor(RawTemperatureSensor sensor) {
            sensorsMap.put(sensor.getSensorId(), sensor);
        }
    }

    private static class FakeConfigProvider implements ConfigProvider {

        private Configuration configuration;

        @Getter
        private boolean configUpdated = false;

        public FakeConfigProvider(Configuration configuration) {
            this.configuration = initEmptyListsAndMapsInNeeded(configuration);
        }

        public FakeConfigProvider() {
            this.configuration = initEmptyListsAndMapsInNeeded(new Configuration());
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

        private Configuration initEmptyListsAndMapsInNeeded(Configuration configuration) {
            Configuration.ConfigurationBuilder builder = configuration.toBuilder();
            if (configuration.getSensorsConfiguration() == null) {
                builder.sensorsConfiguration(Configuration.SensorsConfiguration.empty());
            }
            if (configuration.getTemperatureCalibrationMeasurements() == null) {
                builder.temperatureCalibrationMeasurements(new HashMap<>());
            }
            if (configuration.getTemperatureCalibration() == null) {
                builder.temperatureCalibration(new HashMap<>());
            }
            return builder.build();
        }
    }
}