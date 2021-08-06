package wadosm.breweryhost.externalinterface;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.externalinterface.dto.BreweryStatusDTO;
import wadosm.breweryhost.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.serial.SerialPortFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class ExternalInterfaceImpl implements ExternalInterface {

    Set<CommandListener> commandListeners = new HashSet();

    private final ObjectMapper objectMapper;

    private final SerialPortFile serialPortFile;

    public ExternalInterfaceImpl(SerialPortFile serialPortFile, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.serialPortFile = serialPortFile;
        this.serialPortFile.addDataListener(event -> onReceivedData(event.getReceivedData()));
    }

    private void onReceivedData(byte[] data) {
        for (CommandListener commandListener : commandListeners) {
            try {
                CommandDTO commandDTO = objectMapper.readValue(data, CommandDTO.class);
                commandListener.commandReceived(commandDTO);
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
    }

    @Override
    public void removeCommandListener(CommandListener commandListener) {
        commandListeners.remove(commandListener);
    }

    @Override
    public void sendBreweryStatus(BreweryStatusDTO breweryStatusDTO) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(breweryStatusDTO);
            serialPortFile.writeBytes(data);
        } catch (JsonProcessingException e) {
        }
    }

}
