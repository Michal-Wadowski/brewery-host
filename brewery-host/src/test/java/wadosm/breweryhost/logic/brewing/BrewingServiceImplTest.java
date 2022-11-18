package wadosm.breweryhost.logic.brewing;

import lombok.Getter;
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
import wadosm.breweryhost.logic.brewing.model.BrewingSnapshotState;
import wadosm.breweryhost.logic.brewing.model.SensorsConfiguration;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BrewingServiceImplTest {

    public static Stream<Arguments> should_handle_configuration_variants() {
        return Stream.of(
                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(TemperatureSensor.builder().sensorId("aabbcc").temperature(50.12).used(true).build())
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .showBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66100)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50100)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("aabbcc").temperature(50.1).used(true).build(),
                                TemperatureSensor.builder().sensorId("ddeeff").temperature(66.1).used(true).build(),
                                TemperatureSensor.builder().sensorId("#used").temperature(58.1).build()
                        )
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),
                        new FakeTemperatureSensorProvider(),
                        List.of()
                ),

                Arguments.of(
                        Configuration.builder().build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of()
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("ddeeff"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("ddeeff").temperature(66.12).build(),
                                TemperatureSensor.builder().sensorId("#used").temperature(50.12).build()
                        )
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .showBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("aabbcc").temperature(50.12).used(true).build(),
                                TemperatureSensor.builder().sensorId("ddeeff").temperature(66.12).build()
                        )
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),
                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 66120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .findFirst().get(),
                        List.of(
                                TemperatureSensor.builder().sensorId("#used").temperature(50.12).build()
                        )
                )
        );
    }

    public static Stream<Arguments> should_handle_used_sensor_variants() {
        return Stream.of(
                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 99120)))
                                .findFirst().get(),

                        99.0,
                        true
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 99120)))
                                .findFirst().get(),

                        30.0,
                        false
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc"))
                                        .build()
                        ).build(),

                        new FakeTemperatureSensorProvider(),

                        99.0,
                        false
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 50120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 99120)))
                                .findFirst().get(),

                        99.0,
                        false
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("ddeeff"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 99120)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 30120)))
                                .findFirst().get(),

                        99.0,
                        true
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 20000)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 60000)))
                                .findFirst().get(),

                        41.0,
                        true
                ),

                Arguments.of(
                        Configuration.builder().sensorsConfiguration(
                                SensorsConfiguration.builder()
                                        .useBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                        .build()
                        ).build(),

                        Stream.of(new FakeTemperatureSensorProvider())
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("aabbcc", 20000)))
                                .peek(x -> x.setCurrTemperatureSensor(new RawTemperatureSensor("ddeeff", 60000)))
                                .findFirst().get(),

                        39.0,
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void should_handle_configuration_variants(Configuration configuration,
                                              TemperatureSensorProvider sensorProvider,
                                              List<TemperatureSensor> expectedTemperatureSensors) {
        // given
        ConfigProvider configProvider = new FakeConfigProvider();
        configProvider.saveConfiguration(configuration);

        BrewingServiceImpl brewingService = new BrewingServiceImpl(
                mock(BreweryInterface.class),
                configProvider,
                new BrewingSettingsProviderImpl(new FakeConfigProvider()),
                getTemperatureProvider(sensorProvider, configProvider),
                mock(MainsPowerProvider.class),
                mock(TimeProvider.class)
        );

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getReadings().getCurrentTemperature()).
                isEqualTo(expectedTemperatureSensors);
    }

    private static TemperatureProvider getTemperatureProvider(TemperatureSensorProvider sensorProvider, ConfigProvider configProvider) {
        return new TemperatureProvider(configProvider, sensorProvider, new CalibrationProvider(configProvider));
    }

    @ParameterizedTest
    @MethodSource
    void should_handle_used_sensor_variants(Configuration configuration,
                                            TemperatureSensorProvider sensorProvider,
                                            Double destinationTemperature,
                                            Boolean power) {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeConfigProvider configProvider = new FakeConfigProvider();
        configProvider.saveConfiguration(configuration);

        BrewingSettingsProviderImpl brewingSettingsProvider = new BrewingSettingsProviderImpl(new FakeConfigProvider());
        BrewingServiceImpl brewingService = new BrewingServiceImpl(
                breweryInterface, configProvider, brewingSettingsProvider,
                getTemperatureProvider(sensorProvider, configProvider),
                new MainsPowerProvider(brewingSettingsProvider, breweryInterface),
                mock(TimeProvider.class)
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

    @Test
    void shouldNotFailOnEmptyConfiguration() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = getBrewingService(breweryInterface, temperatureProvider, configProvider);

        // when/then
        brewingService.processStep();
    }

    @Test
    void shouldUseDefaultMotorPin() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        FakeTemperatureSensorProvider temperatureProvider = new FakeTemperatureSensorProvider();
        FakeConfigProvider configProvider = new FakeConfigProvider();

        BrewingServiceImpl brewingService = getBrewingService(breweryInterface, temperatureProvider, configProvider);

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

        BrewingServiceImpl brewingService = getBrewingService(breweryInterface, temperatureProvider, configProvider);

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

        BrewingServiceImpl brewingService = getBrewingService(breweryInterface, temperatureProvider, configProvider);

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

        BrewingServiceImpl brewingService = getBrewingService(breweryInterface, temperatureProvider, configProvider);

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
                        .sensorsConfiguration(SensorsConfiguration.builder()
                                .showBrewingSensorIds(List.of("aabbcc"))
                                .useBrewingSensorIds(List.of("aabbcc"))
                                .build())
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider,
                new BrewingSettingsProviderImpl(new FakeConfigProvider()),
                getTemperatureProvider(temperatureSensorProvider, configProvider),
                mock(MainsPowerProvider.class),
                mock(TimeProvider.class)
        );

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getReadings().getCurrentTemperature()).isEqualTo(
                List.of(TemperatureSensor.builder().sensorId("aabbcc").temperature(12.35).used(true).build())
        );
    }

    @Test
    void should_use_thermometer_name() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        TemperatureSensorProvider temperatureSensorProvider = mock(TemperatureSensorProvider.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);

        when(temperatureSensorProvider.getRawTemperatureSensor("aabbcc"))
                .thenReturn(new RawTemperatureSensor("aabbcc", 12350));
        when(temperatureSensorProvider.getRawTemperatureSensor("ddeeff"))
                .thenReturn(new RawTemperatureSensor("ddeeff", 11110));

        when(configProvider.loadConfiguration()).thenReturn(
                Configuration.builder()
                        .sensorsConfiguration(SensorsConfiguration.builder()
                                .showBrewingSensorIds(List.of("aabbcc", "ddeeff"))
                                .useBrewingSensorIds(List.of("aabbcc"))
                                .sensorNames(Map.of("aabbcc", "foo", "ddeeff", "bar"))
                                .build())
                        .build()
        );

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider,
                new BrewingSettingsProviderImpl(new FakeConfigProvider()),
                getTemperatureProvider(temperatureSensorProvider, configProvider),
                mock(MainsPowerProvider.class),
                mock(TimeProvider.class)
        );

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getReadings().getCurrentTemperature()).contains(
                TemperatureSensor.builder().sensorId("aabbcc").temperature(12.35).name("foo").used(true).build(),
                TemperatureSensor.builder().sensorId("ddeeff").temperature(11.11).name("bar").used(false).build()
        );
    }

    @Test
    void test_valid_powerTemperatureCorrelation_from_getBrewingSnapshotState() {
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        TemperatureSensorProvider sensorProvider = mock(TemperatureSensorProvider.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder().build());

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider,
                new BrewingSettingsProviderImpl(new FakeConfigProvider()),
                getTemperatureProvider(sensorProvider, configProvider),
                mock(MainsPowerProvider.class),
                mock(TimeProvider.class)
        );

        brewingService.setPowerTemperatureCorrelation(123.45);

        // when
        BrewingSnapshotState brewingSnapshotState = brewingService.getBrewingSnapshotState();

        // then
        assertThat(brewingSnapshotState.getSettings().getPowerTemperatureCorrelation()).isEqualTo(123.45);
    }

    @Test
    void test_keep_valid_powerTemperatureCorrelation_while_processStep() {
        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        ConfigProvider configProvider = new FakeConfigProvider();

        TemperatureProvider temperatureProvider = mock(TemperatureProvider.class);
        when(temperatureProvider.getSelectedAverageTemperatures()).thenReturn(50.0);

        BrewingSettingsProvider brewingSettingsProvider = new BrewingSettingsProviderImpl(new FakeConfigProvider());
        brewingSettingsProvider.setEnabled(true);
        brewingSettingsProvider.setDestinationTemperature(100.0);
        brewingSettingsProvider.setMaxPower(70);
        MainsPowerProvider mainsPowerProvider = new MainsPowerProvider(brewingSettingsProvider, breweryInterface);

        BrewingServiceImpl brewingService = new BrewingServiceImpl(breweryInterface,
                configProvider, brewingSettingsProvider, temperatureProvider, mainsPowerProvider, mock(TimeProvider.class)
        );

        // when
        brewingService.setPowerTemperatureCorrelation(1.0);

        // then
        verify(breweryInterface).setMainsPower(1, 0x7f);
        verify(breweryInterface).setMainsPower(2, 0x7f);
    }

    private static BrewingServiceImpl getBrewingService(BreweryInterface breweryInterface, FakeTemperatureSensorProvider temperatureProvider, FakeConfigProvider configProvider) {
        return new BrewingServiceImpl(
                breweryInterface, configProvider, new BrewingSettingsProviderImpl(new FakeConfigProvider()),
                getTemperatureProvider(temperatureProvider, configProvider),
                mock(MainsPowerProvider.class), mock(TimeProvider.class)
        );
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
            this.configuration = configuration;
        }

        public FakeConfigProvider() {
            this.configuration = Configuration.builder().build();
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

        @Override
        public void updateAndSaveConfiguration(Function<Configuration, Configuration> updateConfiguration) {
            saveConfiguration(updateConfiguration.apply(configuration));
        }
    }
}