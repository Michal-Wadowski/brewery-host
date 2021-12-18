package wadosm.breweryhost.logic.fermenting;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterfaceImpl;
import wadosm.breweryhost.device.externalinterface.CommandListener;
import wadosm.breweryhost.device.externalinterface.Session;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.device.temperature.TemperatureProvider;

import java.time.Instant;

@Service
@Log4j2
public class FermentingController implements CommandListener {

    private final TemperatureProvider temperatureProvider;

    public FermentingController(TemperatureProvider temperatureProvider) {
        this.temperatureProvider = temperatureProvider;
    }

    @Override
    public void commandReceived(CommandDTO commandDTO, Session session) {
        FermentingService fermentingService = new FermentingServiceImpl(
                new DriverInterfaceImpl(session), temperatureProvider
        );

        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_getFermentingState) {
            sendStateResponse(commandDTO, session, fermentingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_setDestinationTemperature) {
            Float value = commandDTO.getFloatValue();
            fermentingService.setDestinationTemperature(value);
            sendStateResponse(commandDTO, session, fermentingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_enable) {
            Boolean enable = commandDTO.getEnable();
            fermentingService.enable(enable);
            sendStateResponse(commandDTO, session, fermentingService);
        }
    }

    private void sendStateResponse(CommandDTO commandDTO, Session session, FermentingService fermentingService) {
        FermentingState fermentingState = fermentingService.getFermentingState();
        session.sendResponse( new FermentingStatusResponse(
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
