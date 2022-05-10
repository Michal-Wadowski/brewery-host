package wadosm.breweryhost;

import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

public interface InternalRest {

    String post(String url, CommandDTO requestBody);

}
