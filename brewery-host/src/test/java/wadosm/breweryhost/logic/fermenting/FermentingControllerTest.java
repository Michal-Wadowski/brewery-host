package wadosm.breweryhost.logic.fermenting;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.device.externalinterface.FakeExternalInterface;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

import static org.assertj.core.api.Assertions.assertThat;

class FermentingControllerTest {

    @Test
    void received_some_command() {
        // given
        FakeFermentingService fakeFermentingService = new FakeFermentingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        FermentingController fermentingController = new FermentingController(fakeFermentingService, fakeExternalInterface);

        // when
        fermentingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Fermenting_enable)
                        .enable(true)
                        .build()
        );

        // then
        assertThat(fakeExternalInterface.responseDTO.getCommandId()).isEqualTo(123);

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(FermentingController.FermentingStatusResponse.class);
        FermentingController.FermentingStatusResponse response =
                (FermentingController.FermentingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getFermentingState().getCurrentTemperature()).isEqualTo(123.0f);
        assertThat(response.getFermentingState().isHeating()).isTrue();
    }

    @Test
    void command_received_enable() {
        // given
        FakeFermentingService fakeFermentingService = new FakeFermentingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        FermentingController fermentingController = new FermentingController(fakeFermentingService, fakeExternalInterface);

        // when
        fermentingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Fermenting_enable)
                        .enable(true)
                        .build()
        );

        // then
        assertThat(fakeFermentingService.enable).isTrue();

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(FermentingController.FermentingStatusResponse.class);
        FermentingController.FermentingStatusResponse response =
                (FermentingController.FermentingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getFermentingState().isEnabled()).isTrue();
    }

    @Test
    void command_received_setDestinationTemperature() {
        // given
        FakeFermentingService fakeFermentingService = new FakeFermentingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        FermentingController fermentingController = new FermentingController(fakeFermentingService, fakeExternalInterface);

        // when
        fermentingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Fermenting_setDestinationTemperature)
                        .floatValue(456.0f)
                        .build()
        );

        // then
        assertThat(fakeFermentingService.destTemperature).isEqualTo(456.0f);

        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(FermentingController.FermentingStatusResponse.class);
        FermentingController.FermentingStatusResponse response =
                (FermentingController.FermentingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getFermentingState().getDestinationTemperature()).isEqualTo(456.0f);
    }

    @Test
    void command_received_getFermentingStatus() {
        // given
        FakeFermentingService fakeFermentingService = new FakeFermentingService();
        FakeExternalInterface fakeExternalInterface = new FakeExternalInterface();
        FermentingController fermentingController = new FermentingController(fakeFermentingService, fakeExternalInterface);

        // when
        fermentingController.commandReceived(
                CommandDTO.builder()
                        .commandId(123)
                        .command(CommandDTO.Command.Fermenting_getFermentingState)
                        .build()
        );

        // then
        assertThat(fakeExternalInterface.responseDTO).isInstanceOf(FermentingController.FermentingStatusResponse.class);
        FermentingController.FermentingStatusResponse response =
                (FermentingController.FermentingStatusResponse) fakeExternalInterface.responseDTO;

        assertThat(response.getFermentingState().getDestinationTemperature()).isNull();
        assertThat(response.getFermentingState().isEnabled()).isFalse();
        assertThat(response.getFermentingState().getCurrentTemperature()).isEqualTo(123.0f);
        assertThat(response.getFermentingState().isHeating()).isTrue();
    }

    private static class FakeFermentingService implements FermentingService {

        boolean enable;

        Float destTemperature;

        @Override
        public void enable(boolean enable) {
            this.enable = enable;
        }

        @Override
        public void setDestinationTemperature(Float temperature) {
            destTemperature = temperature;
        }

        @Override
        public FermentingState getFermentingState() {
            return new FermentingState(enable, 123.0f, destTemperature, true);
        }

    }
}