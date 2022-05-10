package wadosm.breweryhost;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

@Component
@RequiredArgsConstructor
public class InternalRestImpl implements InternalRest {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String post(String url, CommandDTO requestBody) {
        HttpEntity<CommandDTO> request = new HttpEntity<>(requestBody);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "http://localhost:8080" + url, request, String.class
        );
        return responseEntity.getBody() + "\n";
    }

}
