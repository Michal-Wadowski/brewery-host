package wadosm.breweryhost.externalinterface;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.junit.jupiter.api.Test;
import wadosm.breweryhost.BeanConfiguration;
import wadosm.breweryhost.externalinterface.dto.BreweryStatusDTO;
import wadosm.breweryhost.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.serial.SerialPortEventListener;
import wadosm.breweryhost.serial.SerialPortFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalInterfaceImplTest {

    @Test
    void should_receive_command_from_json() {
        // given
        FakeSerialPortFile serialPortFile = new FakeSerialPortFile();

        SpyCommandListener commandListener = getExternalInterfaceCommandListener(serialPortFile);

        // when
        String commandJson = "{\"commandId\": 123, \"command\": \"Foo\"}";
        sendCommand(serialPortFile, commandJson);

        // then
        assertThat(commandListener.commandDTO).isNotNull();
        assertThat(commandListener.commandDTO.getCommandId()).isEqualTo(123);
        assertThat(commandListener.commandDTO.getCommand()).isEqualTo("Foo");
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
    void should_send_breweryStatusDTO_as_json() {
        // given
        FakeSerialPortFile serialPortFile = new FakeSerialPortFile();

        BeanConfiguration beanConfiguration = new BeanConfiguration();
        ExternalInterface externalInterface = new ExternalInterfaceImpl(
                serialPortFile, beanConfiguration.objectMapper()
        );

        // when
        BreweryStatusDTO breweryStatusDTO = new BreweryStatusDTO(123, 456L, null, null);
        externalInterface.sendBreweryStatus(breweryStatusDTO);

        // then
        assertThat(serialPortFile.dataWritten).isNotNull();
        assertThat(new String(serialPortFile.dataWritten)).contains("123").contains("456");
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

    private static class FakeSerialPortFile implements SerialPortFile {

        private final List<SerialPortEventListener> listeners = new ArrayList<>();

        byte[] dataWritten;

        public List<SerialPortEventListener> getListeners() {
            return listeners;
        }

        @Override
        public void addDataListener(SerialPortEventListener listener) {
            listeners.add(listener);
        }

        @Override
        public void writeBytes(byte[] data) {
            dataWritten = data;
        }
    }
}