package wadosm.breweryhost.device.externalinterface;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import wadosm.breweryhost.device.SocketWrapper;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.logic.DeviceCommand;
import wadosm.breweryhost.logic.DeviceResponse;

import java.io.Closeable;
import java.util.*;

@Log4j2
public class DriverSessionImpl implements DriverSession {

    private final SocketWrapper socketWrapper;
    private final ObjectMapper objectMapper;

    public DriverSessionImpl(SocketWrapper socketWrapper, ObjectMapper objectMapper) {
        this.socketWrapper = socketWrapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isConnected() {
        return !socketWrapper.isClosed();
    }

    @Override
    public synchronized void sendResponse(ResponseDTO responseDTO) {
        try {
            Map<String, Object> container = new HashMap<>();
            container.put("responseDto", responseDTO);
            String data = objectMapper.writeValueAsString(container);
            socketWrapper.write(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized DeviceResponse sendCommand(DeviceCommand command) {
        List<DeviceResponse> deviceResponses = sendCommands(List.of(command));
        if (deviceResponses.size() > 0) {
            return deviceResponses.get(0);
        }
        return null;
    }

    @Override
    public synchronized List<DeviceResponse> sendCommands(List<DeviceCommand> commandList) {
        try {
            Map<String, List<DeviceCommand>> container = new HashMap<>();
            List<DeviceCommand> commands = new ArrayList<>(commandList);
            container.put("commands", commands);
            String data = objectMapper.writeValueAsString(container);

            socketWrapper.write(data);

            String response = socketWrapper.read();
            if (response != null) {
                DeviceResponse[] deviceResponses = objectMapper.readValue(response, DeviceResponse[].class);

                if (deviceResponses.length > 0) {
                    return Arrays.asList(deviceResponses);
                }
            }

        } catch (JsonProcessingException ignored) {
        }

        return List.of();
    }
}
