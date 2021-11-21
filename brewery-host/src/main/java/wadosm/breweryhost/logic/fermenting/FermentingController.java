package wadosm.breweryhost.logic.fermenting;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.externalinterface.CommandListener;
import wadosm.breweryhost.device.externalinterface.ExternalInterface;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;

import java.time.Instant;

@Service
@Log4j2
public class FermentingController implements CommandListener {

    private final FermentingService fermentingService;

    private final ExternalInterface externalInterface;

    public FermentingController(
            FermentingService fermentingService, ExternalInterface externalInterface
    ) {
        this.fermentingService = fermentingService;
        this.externalInterface = externalInterface;

        externalInterface.addCommandListener(this);
    }

    @Override
    public void commandReceived(CommandDTO commandDTO) {
        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_getFermentingState) {
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_setDestinationTemperature) {
            Float value = commandDTO.getFloatValue();
            fermentingService.setDestinationTemperature(value);
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_enable) {
            Boolean enable = commandDTO.getEnable();
            fermentingService.enable(enable);
            sendStateResponse(commandDTO);
        }
    }

    private void sendStateResponse(CommandDTO commandDTO) {
        FermentingState fermentingState = fermentingService.getFermentingState();
        externalInterface.sendResponse( new FermentingStatusResponse(
                commandDTO.getCommandId(), Instant.now().getEpochSecond(), fermentingState
        ));
    }

    @Getter
    static class FermentingStatusResponse extends ResponseDTO {

        private final FermentingState fermentingState;

        public FermentingStatusResponse(Integer commandId, Long time, FermentingState fermentingState) {
            super(commandId, time);
            this.fermentingState = fermentingState;
        }
    }

}
