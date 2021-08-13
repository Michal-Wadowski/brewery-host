package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wadosm.breweryhost.device.driver.FakeDriverInterface;
import wadosm.breweryhost.device.externalinterface.FakeExternalInterface;
import wadosm.breweryhost.device.temperature.FakeTemperatureProvider;

import static org.assertj.core.api.Assertions.assertThat;

class BrewingServiceImplTest {

    @ParameterizedTest
    @CsvSource({
            "true, , 30000, 0",
            "true, 70.0, , 0",
            "false, 70.0, 30000, 0",
            "true, 70.0, , 0"
    })
    void do_nothing_when_not_properly_setup(
            boolean enabled, Float destTemperature, Integer currTemperature, Integer currMainsPower
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        temperatureProvider.currentTemperature = currTemperature;


        FakeDriverInterface driverInterface = new FakeDriverInterface();
        BrewingServiceImpl service = getBrewingService(temperatureProvider, driverInterface);

        // when
        service.enable(enabled);
        service.setDestinationTemperature(destTemperature);

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

            "70.0, , , , 0",
    })
    void mains_processing_step(
            Float destTemperature, Integer currTemperature, Integer maxPower, Float temperatureCorrelation,
            Integer currMainsPower
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();
        temperatureProvider.currentTemperature = currTemperature;


        FakeDriverInterface driverInterface = new FakeDriverInterface();
        BrewingServiceImpl service = getBrewingService(temperatureProvider, driverInterface);

        // when
        service.enable(true);
        service.setDestinationTemperature(destTemperature);
        service.setMaxPower(maxPower);
        service.setPowerTemperatureCorrelation(temperatureCorrelation);

        // then
        assertThat(driverInterface.mainsPower[0]).isEqualTo(currMainsPower);
        assertThat(driverInterface.mainsPower[1]).isEqualTo(currMainsPower);
        assertThat(driverInterface.locked).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, false, false",
            "true, false, true, false",
            "false, true, true, true",
            "false, true, false, false",
    })
    void motor_processing_step(
            boolean motorBefore, boolean enable, boolean motorAfter, boolean motorEnabled
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();

        FakeDriverInterface driverInterface = new FakeDriverInterface();

        driverInterface.motorEnabled[0] = motorBefore;

        BrewingServiceImpl service = getBrewingService(temperatureProvider, driverInterface);

        // when
        service.enable(enable);
        service.motorEnable(motorAfter);

        // then
        assertThat(driverInterface.motorEnabled[0]).isEqualTo(motorEnabled);
        assertThat(driverInterface.locked).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, 60.0, false",
            "true, false, 60.0, false",
            "false, true, 60.0, false",
            "true, true, 75.0, false",
            "true, true, 71.0, true",
            "true, true, 60.0, true"
    })
    void temperature_alarm(
            boolean enable, boolean temperatureAlarm, Float destTemperature, boolean alarmFired
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();

        FakeDriverInterface driverInterface = new FakeDriverInterface();

        BrewingServiceImpl service = getBrewingService(temperatureProvider, driverInterface);

        service.enable(enable);
        service.setDestinationTemperature(destTemperature);
        service.enableTemperatureAlarm(temperatureAlarm);

        temperatureProvider.currentTemperature = 71000;
        driverInterface.sound = 3;

        // when
        service.processStep();
        service.soundStep();

        // then
        if (alarmFired) {
            assertThat(driverInterface.sound).isNotZero();
        } else {
            assertThat(driverInterface.sound).isZero();
        }
        assertThat(driverInterface.locked).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "false, 75.0, true, 50, 100.0, true, 71.0",
            "true, , true, , , true, "
    })
    void getBrewingState(
            boolean enable, Float destinationTemperature, boolean temperatureAlarm, Integer maxPower,
            Float powerTemperatureCorrelation, boolean motorEnable, Float currentTemperature
    ) {
        // given
        FakeTemperatureProvider temperatureProvider = new FakeTemperatureProvider();

        FakeDriverInterface driverInterface = new FakeDriverInterface();

        BrewingServiceImpl service = getBrewingService(temperatureProvider, driverInterface);

        service.enable(enable);
        service.setDestinationTemperature(destinationTemperature);
        service.enableTemperatureAlarm(temperatureAlarm);
        service.setMaxPower(maxPower);
        service.setPowerTemperatureCorrelation(powerTemperatureCorrelation);
        service.motorEnable(motorEnable);

        if (currentTemperature != null) {
            temperatureProvider.currentTemperature = (int) (currentTemperature * 1000);
        }

        // when
        BrewingState brewingState = service.getBrewingState();

        // then
        assertThat(brewingState).isNotNull();

        assertThat(brewingState.isEnabled()).isEqualTo(enable);
        assertThat(brewingState.getDestinationTemperature()).isEqualTo(destinationTemperature);
        assertThat(brewingState.isTemperatureAlarm()).isEqualTo(temperatureAlarm);
        assertThat(brewingState.getMaxPower()).isEqualTo(maxPower);
        assertThat(brewingState.getPowerTemperatureCorrelation()).isEqualTo(powerTemperatureCorrelation);
        assertThat(brewingState.isMotorEnabled()).isEqualTo(motorEnable);

        assertThat(brewingState.getCurrentTemperature()).isEqualTo(currentTemperature);
    }

    private BrewingServiceImpl getBrewingService(FakeTemperatureProvider temperatureProvider,
                                                 FakeDriverInterface driverInterface) {
        BrewingServiceImpl service = new BrewingServiceImpl(
                driverInterface,
                temperatureProvider,
                new FakeExternalInterface()
        );
        service.setMotorNumber(1);
        service.setAlarmHi(2);
        service.setAlarmLo(1);
        return service;
    }
}