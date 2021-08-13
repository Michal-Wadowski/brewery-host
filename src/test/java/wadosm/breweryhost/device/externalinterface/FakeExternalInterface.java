package wadosm.breweryhost.device.externalinterface;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import wadosm.breweryhost.BeanConfiguration;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;

public class FakeExternalInterface implements ExternalInterface {

    public ResponseDTO responseDTO;

    private final ObjectMapper objectMapper;

    public FakeExternalInterface() {
        objectMapper = new BeanConfiguration().objectMapper();
    }

    @Override
    public void addCommandListener(CommandListener commandListener) {

    }

    @Override
    public void removeCommandListener(CommandListener commandListener) {

    }

    @Override
    public void sendResponse(ResponseDTO responseDTO) {
        this.responseDTO = responseDTO;
    }
}
