package wadosm.breweryhost.logic.fermenting;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wadosm.breweryhost.device.driver.FakeDriverInterface;
import wadosm.breweryhost.device.externalinterface.FakeExternalInterface;
import wadosm.breweryhost.device.temperature.FakeTemperatureProvider;

import static org.assertj.core.api.Assertions.assertThat;

class FermentingServiceImplTest {

    @ParameterizedTest
    @CsvSource({
            "true, , 14000, false",
            "true, 15.0, , false",
            "false, 15.0, 14000, false",
    })
    void do_nothing_when_not_properly_setup(
            boolean enabled, Float destTemperature, Integer currTemperature, boolean motorEnabled
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        temperatureProvider.currentTemperature = currTemperature;


        FakeDriverInterface driverInterface = new FakeDriverInterface();
        FermentingServiceImpl service = new FermentingServiceImpl(
                temperatureProvider, driverInterface, new FakeExternalInterface()
        );
        service.setMotorNumber(2);

        // when
        service.enable(enabled);
        service.setDestinationTemperature(destTemperature);

        // then
        assertThat(driverInterface.motorEnabled[1]).isEqualTo(motorEnabled);
        assertThat(driverInterface.locked).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "15.0, 18000, true, false",
            "15.0, 18000, false, false",
            "15.0, 14000, true, true",
            "15.0, 14000, false, true",
            ", 14000, false, false",
            "15.0, , false, false",
    })
    void processing_step(
            Float destTemperature, Integer currTemperature, boolean motorBefore, boolean motorAfter
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        temperatureProvider.currentTemperature = currTemperature;

        FakeDriverInterface driverInterface = new FakeDriverInterface();
        driverInterface.motorEnabled[1] = motorBefore;

        FermentingServiceImpl service = new FermentingServiceImpl(
                temperatureProvider, driverInterface, new FakeExternalInterface()
        );
        service.setMotorNumber(2);

        // when
        service.enable(true);
        service.setDestinationTemperature(destTemperature);

        // then
        assertThat(driverInterface.motorEnabled[1]).isEqualTo(motorAfter);
        assertThat(driverInterface.locked).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "13.0, 20.0, true, true",
            "13.0, 20.0, false, false",
            "20.0, 20.0, true, false",
            "25.0, 20.0, true, false",
            "25.0, , true, false",
            ", , true, false",
    })
    void getFermentingState(Float currentTemperature, Float destTemperature, boolean enable, boolean heating) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        if (currentTemperature != null) {
            temperatureProvider.currentTemperature = (int) (currentTemperature * 1000);
        }

        FakeDriverInterface driverInterface = new FakeDriverInterface();

        FermentingServiceImpl service = new FermentingServiceImpl(
                temperatureProvider, driverInterface, new FakeExternalInterface()
        );
        service.setMotorNumber(2);

        service.setDestinationTemperature(destTemperature);
        service.enable(enable);

        // when
        FermentingState fermentingState = service.getFermentingState();

        // then
        assertThat(fermentingState).isNotNull();
        assertThat(fermentingState.isEnabled()).isEqualTo(enable);
        assertThat(fermentingState.isHeating()).isEqualTo(heating);
        assertThat(fermentingState.getDestinationTemperature()).isEqualTo(destTemperature);
        assertThat(fermentingState.getCurrentTemperature()).isEqualTo(currentTemperature);
    }
}