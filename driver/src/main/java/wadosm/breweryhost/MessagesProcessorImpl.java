package wadosm.breweryhost;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.externalinterface.dto.RequestDTO;

@Component
@RequiredArgsConstructor
public class MessagesProcessorImpl implements MessagesProcessor {

    private final InternalRest internalRest;
    private final ObjectMapper objectMapper;

    @Override
    public String processMessage(String message) {
        try {
            RequestDTO requestDTO = objectMapper.readValue(message, RequestDTO.class);
            String[] parts = requestDTO.getCommand().split("\\.");

            String controller = Character.toLowerCase(parts[0].charAt(0)) + parts[0].substring(1);
            String method = Character.toLowerCase(parts[1].charAt(0)) + parts[1].substring(1);
            String url = "/" + controller + "/" + method;

            CommandDTO commandDTO = new CommandDTO();
            commandDTO.setCommandId(requestDTO.getCommandId());
            commandDTO.setIntValue(requestDTO.getIntValue());
            commandDTO.setFloatValue(requestDTO.getFloatValue());
            commandDTO.setNumber(requestDTO.getNumber());
            commandDTO.setEnable(requestDTO.getEnable());

            return internalRest.post(url, commandDTO);
        } catch (Exception ignored) {
            return null;
        }
    }

}
