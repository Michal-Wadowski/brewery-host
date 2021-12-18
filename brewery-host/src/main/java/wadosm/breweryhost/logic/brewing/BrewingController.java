package wadosm.breweryhost.logic.brewing;

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
public class BrewingController implements CommandListener {

    private final TemperatureProvider temperatureProvider;

    public BrewingController(TemperatureProvider temperatureProvider) {
        this.temperatureProvider = temperatureProvider;
    }

    @Override
    public void commandReceived(CommandDTO commandDTO, Session session) {
        BrewingService brewingService = new BrewingServiceImpl(
                new DriverInterfaceImpl(session), temperatureProvider
        );

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_getBrewingState) {
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setDestinationTemperature) {
            Float value = commandDTO.getFloatValue();
            brewingService.setDestinationTemperature(value);
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_enable) {
            Boolean enable = commandDTO.getEnable();
            brewingService.enable(enable);
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setMaxPower) {
            Integer maxPower = commandDTO.getIntValue();
            brewingService.setMaxPower(maxPower);
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setPowerTemperatureCorrelation) {
            Float temperatureCorrelation = commandDTO.getFloatValue();
            brewingService.setPowerTemperatureCorrelation(temperatureCorrelation);
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_enableTemperatureAlarm) {
            boolean enable = commandDTO.getEnable();
            brewingService.enableTemperatureAlarm(enable);
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setTimer) {
            Integer time = commandDTO.getIntValue();
            brewingService.setTimer(time);
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_removeTimer) {
            brewingService.removeTimer();
            sendStateResponse(commandDTO, session, brewingService);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_motorEnable) {
            boolean enable = commandDTO.getEnable();
            brewingService.motorEnable(enable);
            sendStateResponse(commandDTO, session, brewingService);
        }
    }

    private void sendStateResponse(CommandDTO commandDTO, Session session, BrewingService brewingService) {
        BrewingState brewingState = brewingService.getBrewingState();
        session.sendResponse( new BrewingStatusResponse(
                commandDTO.getCommandId(), Instant.now().getEpochSecond(), brewingState
        ));
    }

    @Getter
    static class BrewingStatusResponse extends ResponseDTO {

        private final BrewingState brewingState;

        public BrewingStatusResponse(Integer commandId, Long time, BrewingState brewingState) {
            super(commandId, time);
            this.brewingState = brewingState;
        }
    }
}
