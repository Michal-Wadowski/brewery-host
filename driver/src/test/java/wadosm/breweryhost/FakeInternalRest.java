package wadosm.breweryhost;

import lombok.Getter;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

@Getter
class FakeInternalRest implements InternalRest {

    private String lastUrl;
    private CommandDTO lastRequestBody;
    private String preparedResponse;

    @Override
    public String post(String url, CommandDTO requestBody) {
        lastUrl = url;
        lastRequestBody = requestBody;
        return preparedResponse;
    }

    public CommandDTO getRequestBody() {
        return lastRequestBody;
    }

    public void setPreparedResponseBody(String response) {
        preparedResponse = response;
    }
}
