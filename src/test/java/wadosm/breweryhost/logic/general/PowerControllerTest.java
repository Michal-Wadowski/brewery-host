package wadosm.breweryhost.logic.general;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.device.externalinterface.FakeExternalInterface;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

import static org.assertj.core.api.Assertions.assertThat;

class PowerControllerTest {

    @Test
    void receive_restart_command() {
        // given
        FakePowerService fakePowerService = new FakePowerService();
        PowerController powerController = new PowerController(new FakeExternalInterface(), fakePowerService);

        // when
        powerController.commandReceived(
                CommandDTO.builder()
                        .command(CommandDTO.Command.Power_restart)
                        .build()
        );

        // then
        assertThat(fakePowerService.powerOff).isFalse();
        assertThat(fakePowerService.restart).isTrue();
    }

    @Test
    void receive_powerOff_command() {
        // given
        FakePowerService fakePowerService = new FakePowerService();
        PowerController powerController = new PowerController(new FakeExternalInterface(), fakePowerService);

        // when
        powerController.commandReceived(
                CommandDTO.builder()
                        .command(CommandDTO.Command.Power_powerOff)
                        .build()
        );

        // then
        assertThat(fakePowerService.powerOff).isTrue();
        assertThat(fakePowerService.restart).isFalse();
    }

    @Test
    void ignore_other_command() {
        // given
        FakePowerService fakePowerService = new FakePowerService();
        PowerController powerController = new PowerController(new FakeExternalInterface(), fakePowerService);

        // when
        powerController.commandReceived(
                CommandDTO.builder()
                        .command(CommandDTO.Command.Brewing_enable)
                        .build()
        );

        // then
        assertThat(fakePowerService.powerOff).isFalse();
        assertThat(fakePowerService.restart).isFalse();
    }

    private static class FakePowerService implements PowerService {

        boolean powerOff;

        boolean restart;

        @Override
        public void powerOff() {
            powerOff = true;
        }

        @Override
        public void restart() {
            restart = true;
        }
    }
}