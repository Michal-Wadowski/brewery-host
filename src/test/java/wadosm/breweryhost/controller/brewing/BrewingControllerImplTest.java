package wadosm.breweryhost.controller.brewing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wadosm.breweryhost.BeanConfiguration;
import wadosm.breweryhost.driver.FakeDriverInterface;
import wadosm.breweryhost.externalinterface.ExternalInterface;
import wadosm.breweryhost.externalinterface.ExternalInterfaceImpl;
import wadosm.breweryhost.externalinterface.FakeSerialPortFile;
import wadosm.breweryhost.temperature.FakeTemperatureProvider;

import static org.assertj.core.api.Assertions.assertThat;

class BrewingControllerImplTest {

    @ParameterizedTest
    @CsvSource({
            "true, , 30000, 0",
            "true, 70.0, , 0",
            "false, 70.0, 30000, 0"
    })
    void do_nothing_when_not_properly_setup(
            boolean enabled, Float destTemperature, Integer currTemperature, Integer currMainsPower
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        temperatureProvider.currentTemperature = currTemperature;


        FakeDriverInterface driverInterface = new FakeDriverInterface();
        BrewingControllerImpl controller = new BrewingControllerImpl(
                driverInterface,
                temperatureProvider,
                getExternalInterface()
        );

        controller.enable(enabled);
        controller.setDestinationTemperature(destTemperature);

        // when
        controller.processStep();

        // then
        assertThat(driverInterface.mainsPower[0]).isEqualTo(currMainsPower);
        assertThat(driverInterface.mainsPower[1]).isEqualTo(currMainsPower);
        assertThat(driverInterface.locked).isFalse();
    }


    @ParameterizedTest
    @CsvSource({
            "30.0, 70000, , , 0",
            "70.0, 30000, , , 255",
            "30.0, 70000, 100, , 0",
            "70.0, 30000, 100, , 255",
            "70.0, 30000, 0, , 0",
            "70.0, 30000, 50, , 0x7f",

            "70.0, 71000, 100, 1, 0",
            "70.0, 20000, 100, 1, 0x7f",

            "70.0, 65000, 100, 10, 0x7f",
            "70.0, 50000, 100, 10, 0xff",
            "70.0, 50000, 50, 10, 0x7f",
            "70.0, 75000, 100, 10, 0",
    })
    void processing_step(
            Float destTemperature, Integer currTemperature, Integer maxPower, Float temperatureCorrelation,
            Integer currMainsPower
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        temperatureProvider.currentTemperature = currTemperature;


        FakeDriverInterface driverInterface = new FakeDriverInterface();
        BrewingControllerImpl controller = new BrewingControllerImpl(
                driverInterface,
                temperatureProvider,
                getExternalInterface()
        );

        controller.enable(true);
        controller.setDestinationTemperature(destTemperature);
        controller.setMaxPower(maxPower);
        controller.setPowerTemperatureCorrelation(temperatureCorrelation);

        // when
        controller.processStep();

        // then
        assertThat(driverInterface.mainsPower[0]).isEqualTo(currMainsPower);
        assertThat(driverInterface.mainsPower[1]).isEqualTo(currMainsPower);
        assertThat(driverInterface.locked).isFalse();
    }

    private ExternalInterface getExternalInterface() {
        FakeSerialPortFile serialPortFile = new FakeSerialPortFile();
        BeanConfiguration beanConfiguration = new BeanConfiguration();
        return new ExternalInterfaceImpl(
                serialPortFile, beanConfiguration.objectMapper()
        );
    }
}