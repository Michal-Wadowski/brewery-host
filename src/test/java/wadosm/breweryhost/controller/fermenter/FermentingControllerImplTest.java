package wadosm.breweryhost.controller.fermenter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wadosm.breweryhost.BeanConfiguration;
import wadosm.breweryhost.driver.DriverInterface;
import wadosm.breweryhost.driver.DriverInterfaceState;
import wadosm.breweryhost.driver.FakeDriverInterface;
import wadosm.breweryhost.externalinterface.ExternalInterface;
import wadosm.breweryhost.externalinterface.ExternalInterfaceImpl;
import wadosm.breweryhost.externalinterface.FakeSerialPortFile;
import wadosm.breweryhost.temperature.FakeTemperatureProvider;

import static org.assertj.core.api.Assertions.assertThat;

class FermentingControllerImplTest {

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
        FermentingControllerImpl controller = new FermentingControllerImpl(
                temperatureProvider, driverInterface, getExternalInterface()
        );
        controller.setMotorNumber(2);

        controller.enable(enabled);
        controller.setDestinationTemperature(destTemperature);

        // when
        controller.processStep();

        // then
        assertThat(driverInterface.motorEnabled[1]).isEqualTo(motorEnabled);
        assertThat(driverInterface.locked).isFalse();
    }

    private ExternalInterface getExternalInterface() {
        FakeSerialPortFile serialPortFile = new FakeSerialPortFile();
        BeanConfiguration beanConfiguration = new BeanConfiguration();
        return new ExternalInterfaceImpl(
                    serialPortFile, beanConfiguration.objectMapper()
            );
    }

    @ParameterizedTest
    @CsvSource({
            "15.0, 18000, true, false",
            "15.0, 18000, false, false",
            "15.0, 14000, true, true",
            "15.0, 14000, false, true",
    })
    void processing_step(
            Float destTemperature, Integer currTemperature, boolean motorBefore, boolean motorAfter
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        temperatureProvider.currentTemperature = currTemperature;

        FakeDriverInterface driverInterface = new FakeDriverInterface();
        driverInterface.motorEnabled[1] = motorBefore;

        FermentingControllerImpl controller = new FermentingControllerImpl(
                temperatureProvider, driverInterface, getExternalInterface()
        );
        controller.setMotorNumber(2);

        controller.enable(true);
        controller.setDestinationTemperature(destTemperature);

        // when
        controller.processStep();

        // then
        assertThat(driverInterface.motorEnabled[1]).isEqualTo(motorAfter);
        assertThat(driverInterface.locked).isFalse();
    }
}