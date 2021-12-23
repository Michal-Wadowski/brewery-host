package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.DriverInterfaceImpl;
import wadosm.breweryhost.device.temperature.TemperatureProvider;
import wadosm.breweryhost.device.temperature.TemperatureSensor;

import java.util.List;

class BrewingServiceImplTest {

    @Test
    void getBrewingState_should_be_resilient_to_not_connected_driver() {
        //given
        DriverInterface driverInterface = new DriverInterfaceImpl();
        TemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        BrewingServiceImpl brewingService = new BrewingServiceImpl(driverInterface, temperatureProvider);

        // when
        brewingService.getBrewingState();
    }

    @Test
    void processStep_should_be_resilient_to_not_connected_driver() {
        //given
        DriverInterface driverInterface = new DriverInterfaceImpl();
        TemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        BrewingServiceImpl brewingService = new BrewingServiceImpl(driverInterface, temperatureProvider);
        brewingService.setMotorNumber(1);

        // when
        brewingService.processStep();
    }

    private static class FakeTemperatureProvider implements TemperatureProvider {
        @Override
        public void readPeriodicallySensors() {

        }

        @Override
        public List<TemperatureSensor> getTemperatureSensors() {
            return null;
        }

        @Override
        public Integer getSensorTemperature(String sensorId) {
            return null;
        }
    }
}