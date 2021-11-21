package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.device.externalinterface.FakeExternalInterface;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

import static org.assertj.core.api.Assertions.assertThat;

class BrewingControllerTest {

    @Test
    void received_some_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_getBrewingState)
                        .build()
        );

        // then
        assertThat(fakeExternalInterface.responseDTO.getCommandId()).isEqualTo(123);

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().isEnabled()).isFalse();
        assertThat(response.getBrewingState().getCurrentTemperature()).isEqualTo(123.0f);
        assertThat(response.getBrewingState().getDestinationTemperature()).isNull();
        assertThat(response.getBrewingState().getMaxPower()).isNull();
        assertThat(response.getBrewingState().getPowerTemperatureCorrelation()).isNull();
        assertThat(response.getBrewingState().getTimeElapsed()).isNull();
        assertThat(response.getBrewingState().isMotorEnabled()).isFalse();
        assertThat(response.getBrewingState().isTemperatureAlarm()).isFalse();
    }

    @Test
    void received_setDestinationTemperature_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_setDestinationTemperature)
                        .floatValue(987.0f)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.destinationTemperature).isEqualTo(987.0f);

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().getDestinationTemperature()).isEqualTo(987.0f);
    }

    @Test
    void received_setMaxPower_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_setMaxPower)
                        .intValue(66)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.maxPower).isEqualTo(66);

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().getMaxPower()).isEqualTo(66);
    }

    @Test
    void received_setPowerTemperatureCorrelation_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_setPowerTemperatureCorrelation)
                        .floatValue(55.0f)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.powerTemperatureCorrelation).isEqualTo(55f);

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().getPowerTemperatureCorrelation()).isEqualTo(55f);
    }

    @Test
    void received_enableTemperatureAlarm_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_enableTemperatureAlarm)
                        .enable(true)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.temperatureAlarm).isTrue();

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().isTemperatureAlarm()).isTrue();
    }

    @Test
    void received_enable_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_enable)
                        .enable(true)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.enabled).isTrue();

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().isEnabled()).isTrue();
    }

    @Test
    void received_setTimer_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_setTimer)
                        .intValue(15 * 60)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.timer).isEqualTo(15 * 60);

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().getTimeElapsed()).isEqualTo(15 * 60);
    }

    @Test
    void received_removeTimer_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        fakeBrewingService.timer = 0;

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_removeTimer)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.timer).isNull();

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().getTimeElapsed()).isNull();
    }

    @Test
    void received_motorEnable_command() {
        // given
        FakeBrewingService fakeBrewingService = new FakeBrewingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        BrewingController brewingController = new BrewingController(fakeBrewingService, fakeExternalInterface);

        // when
        brewingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Brewing_motorEnable)
                        .enable(true)
                        .build()
        );

        // then
        assertThat(fakeBrewingService.motorEnabled).isTrue();

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(BrewingController.BrewingStatusResponse.class);
        BrewingController.BrewingStatusResponse response =
                (BrewingController.BrewingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getBrewingState().isMotorEnabled()).isTrue();
    }

    private static class FakeBrewingService implements BrewingService {

        boolean enabled;

        Float destinationTemperature;

        boolean temperatureAlarm;

        Integer maxPower;

        Float powerTemperatureCorrelation;

        Integer timer;

        boolean motorEnabled;

        @Override
        public void enable(boolean enable) {
            enabled = enable;
        }

        @Override
        public void setDestinationTemperature(Float temperature) {
            destinationTemperature = temperature;
        }

        @Override
        public void enableTemperatureAlarm(boolean enable) {
            temperatureAlarm = enable;
        }

        @Override
        public void setMaxPower(Integer powerInPercents) {
            maxPower = powerInPercents;
        }

        @Override
        public void setPowerTemperatureCorrelation(Float percentagesPerDegree) {
            powerTemperatureCorrelation = percentagesPerDegree;
        }

        @Override
        public void setTimer(int seconds) {
            timer = seconds;
        }

        @Override
        public void removeTimer() {
            timer = null;
        }

        @Override
        public void motorEnable(boolean enable) {
            motorEnabled = enable;
        }

        @Override
        public BrewingState getBrewingState() {
            return new BrewingState(enabled, 123.0f, destinationTemperature, maxPower, powerTemperatureCorrelation,
                    timer, motorEnabled, temperatureAlarm, 70);
        }
    }
}