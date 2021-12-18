package wadosm.breweryhost.device.externalinterface;

import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.logic.DeviceCommand;
import wadosm.breweryhost.logic.DeviceResponse;

import java.util.List;

public interface Session {

    void sendResponse(ResponseDTO responseDTO);

    DeviceResponse sendCommand(DeviceCommand command);

    List<DeviceResponse> sendCommands(List<DeviceCommand> commandList);

}
