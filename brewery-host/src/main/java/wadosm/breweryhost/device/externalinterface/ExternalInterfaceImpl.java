package wadosm.breweryhost.device.externalinterface;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.device.serial.SerialPortFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@Log4j2
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
                log.warn(e);
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
    public void sendResponse(ResponseDTO responseDTO) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(responseDTO);
            byte[] newData = new byte[data.length + 1];
            for (int i = 0; i < data.length; i++) {
                newData[i] = data[i];
            }
            newData[newData.length-1] = '\n';
            serialPortFile.writeBytes(newData);
        } catch (JsonProcessingException e) {
        }
    }

}
