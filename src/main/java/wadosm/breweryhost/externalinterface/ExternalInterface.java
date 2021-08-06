package wadosm.breweryhost.externalinterface;

import wadosm.breweryhost.externalinterface.dto.BreweryStatusDTO;

public interface ExternalInterface {

    void addCommandListener(CommandListener commandListener);

    void removeCommandListener(CommandListener commandListener);

    void sendBreweryStatus(BreweryStatusDTO breweryStatusDTO);
}
