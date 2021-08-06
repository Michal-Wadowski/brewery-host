package wadosm.breweryhost.externalinterface;

import wadosm.breweryhost.externalinterface.dto.CommandDTO;

public interface CommandListener {
    void commandReceived(CommandDTO commandDTO);
}
