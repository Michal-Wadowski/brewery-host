package wadosm.breweryhost.device.externalinterface;

import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

public interface CommandListener {
    void commandReceived(CommandDTO commandDTO, Session session);
}
