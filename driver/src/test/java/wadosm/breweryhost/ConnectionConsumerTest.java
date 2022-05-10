package wadosm.breweryhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionConsumerTest {

    @Test
    void processMessage_powerOff_should_make_internal_rest() {
        // given
        FakeInternalRest internalRest = new FakeInternalRest();

        MessagesProcessorImpl messagesProcessor = new MessagesProcessorImpl(
                internalRest,
                new ObjectMapper()
        );

        // when
        messagesProcessor.processMessage("{\"command\":\"Power.powerOff\"}");

        // then
        assertThat(internalRest.getLastUrl())
                .isEqualTo("/power/powerOff");
    }

    @Test
    void processMessage_getBrewingState_should_make_internal_rest() {
        // given
        FakeInternalRest internalRest = new FakeInternalRest();

        MessagesProcessorImpl messagesProcessor = new MessagesProcessorImpl(
                internalRest,
                new ObjectMapper()
        );

        // when
        messagesProcessor.processMessage("{\"command\":\"Brewing.getBrewingState\",\"commandId\":12}");

        // then
        assertThat(internalRest.getLastUrl())
                .isEqualTo("/brewing/getBrewingState");
        assertThat(internalRest.getRequestBody())
                .isEqualTo(CommandDTO.builder().commandId(12).build());
    }

    @Test
    void processMessage_should_pass_response() {
        // given
        FakeInternalRest internalRest = new FakeInternalRest();

        MessagesProcessorImpl messagesProcessor = new MessagesProcessorImpl(
                internalRest,
                new ObjectMapper()
        );

        internalRest.setPreparedResponseBody("RESPONSE");

        // when
        String response = messagesProcessor.processMessage(
                "{\"command\":\"Brewing.getBrewingState\",\"commandId\":12}"
        );

        // then
        assertThat(response)
                .isEqualTo("RESPONSE");
    }
}