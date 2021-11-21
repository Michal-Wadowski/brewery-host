package wadosm.breweryhost.device.externalinterface;

import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;

public interface ExternalInterface {

    void addCommandListener(CommandListener commandListener);

    void removeCommandListener(CommandListener commandListener);

    void sendResponse(ResponseDTO responseDTO);
}
