package wadosm.breweryhost.logic.brewing;

import lombok.Setter;
import org.junit.jupiter.api.Test;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.DriverInterfaceImpl;
import wadosm.breweryhost.device.temperature.TemperatureProvider;
import wadosm.breweryhost.device.temperature.TemperatureSensor;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.Configuration;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BrewingServiceImplTest {

    @Test
    void getBrewingState_should_be_resilient_to_not_connected_driver() {
        //given
        DriverInterface driverInterface = new DriverInterfaceImpl();
        TemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        BrewingServiceImpl brewingService = new BrewingServiceImpl(driverInterface, temperatureProvider, new FakeConfigProvider());

        // when
        brewingService.getBrewingState();
    }

    @Test
    void processStep_should_be_resilient_to_not_connected_driver() {
        //given
        DriverInterface driverInterface = new DriverInterfaceImpl();
        TemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        BrewingServiceImpl brewingService = new BrewingServiceImpl(driverInterface, temperatureProvider, new FakeConfigProvider());
        brewingService.setMotorNumber(1);

        // when
        brewingService.processStep();
    }

    @Test
    void should_use_raw_temperature_without_calibration() {
        // given
        DriverInterface driverInterface = new DriverInterfaceImpl();
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        BrewingServiceImpl brewingService = new BrewingServiceImpl(driverInterface, temperatureProvider, new FakeConfigProvider());

        temperatureProvider.setCurrTemperature(50500);

        // when 1
        BrewingState brewingState = brewingService.getBrewingState();
        assertThat(brewingState.getCurrentTemperature()).isEqualTo(50.5f);
    }

    @Test
    void should_get_calibrated_temperature_with_calibration() {
        // given
        DriverInterface driverInterface = new DriverInterfaceImpl();
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        ConfigProvider configProvider = new FakeConfigProvider();
        BrewingServiceImpl brewingService = new BrewingServiceImpl(driverInterface, temperatureProvider,
                configProvider);

        temperatureProvider.setCurrTemperature(50500);

        // when 1
        BrewingState brewingState = brewingService.getBrewingState();
        assertThat(brewingState.getCurrentTemperature()).isEqualTo(50.5f);
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

        @Override
        public Configuration getConfiguration() {
            return Configuration.builder().thermometerCalibration(Map.of("aabbcc", List.of(0f, 25f, 50f, 75f))).build();
        }
    }
}