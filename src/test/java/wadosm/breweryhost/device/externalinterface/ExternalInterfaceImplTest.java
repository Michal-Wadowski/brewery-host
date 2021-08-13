package wadosm.breweryhost.device.externalinterface;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.junit.jupiter.api.Test;
import wadosm.breweryhost.BeanConfiguration;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalInterfaceImplTest {

    @Test
    void should_receive_command_from_json() {
        // given
        FakeSerialPortFile serialPortFile = new FakeSerialPortFile();

        SpyCommandListener commandListener = getExternalInterfaceCommandListener(serialPortFile);

        // when
        String commandJson = "{\"commandId\": 123, \"command\": \"Power.powerOff\"}";
        sendCommand(serialPortFile, commandJson);

        // then
        assertThat(commandListener.commandDTO).isNotNull();
        assertThat(commandListener.commandDTO.getCommandId()).isEqualTo(123);
        assertThat(commandListener.commandDTO.getCommand()).isEqualTo(CommandDTO.Command.Power_powerOff);
    }

    @Test
    void should_not_receive_command_when_malformed_json() {
        // given
        FakeSerialPortFile serialPortFile = new FakeSerialPortFile();

        SpyCommandListener commandListener = getExternalInterfaceCommandListener(serialPortFile);

        // when
        String commandJson = "{\"commandId\": 123, \"command";
        sendCommand(serialPortFile, commandJson);

        // then
        assertThat(commandListener.commandDTO).isNull();
    }

    private SpyCommandListener getExternalInterfaceCommandListener(FakeSerialPortFile serialPortFile) {
        BeanConfiguration beanConfiguration = new BeanConfiguration();
        ExternalInterface externalInterface = new ExternalInterfaceImpl(
                serialPortFile, beanConfiguration.objectMapper()
        );

        SpyCommandListener eventListener = new SpyCommandListener();
        externalInterface.addCommandListener(eventListener);
        return eventListener;
    }

    @Test
    void should_send_response_as_json() {
        // given
        FakeSerialPortFile serialPortFile = new FakeSerialPortFile();

        BeanConfiguration beanConfiguration = new BeanConfiguration();
        ExternalInterface externalInterface = new ExternalInterfaceImpl(
                serialPortFile, beanConfiguration.objectMapper()
        );

        // when
        CustomResponse breweryStatusDTO = new CustomResponse(123, 456L);
        externalInterface.sendResponse(breweryStatusDTO);

        // then
        assertThat(serialPortFile.dataWritten).isNotNull();
        assertThat(new String(serialPortFile.dataWritten)).contains("123").contains("456");
    }

    private static class CustomResponse extends ResponseDTO {

        public CustomResponse(Integer commandId, Long time) {
            super(commandId, time);
        }
    }

    private void sendCommand(FakeSerialPortFile serialPortFile, String commandJson) {
        SerialPortEvent event = new SerialPortEvent(
                SerialPort.getCommPort(""),
                -1,
                commandJson.getBytes()
        );
        serialPortFile.getListeners().get(0).serialEvent(event);
    }

    private static class SpyCommandListener implements CommandListener {

        CommandDTO commandDTO;

        @Override
        public void commandReceived(CommandDTO commandDTO) {
            this.commandDTO = commandDTO;
        }
    }

}