package wadosm.breweryhost.externalinterface;

import wadosm.breweryhost.externalinterface.dto.ResponseDTO;

public interface ExternalInterface {

    void addCommandListener(CommandListener commandListener);

    void removeCommandListener(CommandListener commandListener);

    void sendResponse(ResponseDTO responseDTO);
}
