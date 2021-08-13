package wadosm.breweryhost.logic.brewing;

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
public class BrewingController implements CommandListener {

    private final BrewingService brewingService;

    private final ExternalInterface externalInterface;

    public BrewingController(BrewingService brewingService, ExternalInterface externalInterface) {
        this.brewingService = brewingService;
        this.externalInterface = externalInterface;

        externalInterface.addCommandListener(this);
    }

    @Override
    public void commandReceived(CommandDTO commandDTO) {
        log.info(commandDTO);
        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_getBrewingState) {
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setDestinationTemperature) {
            Float value = commandDTO.getFloatValue();
            brewingService.setDestinationTemperature(value);
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_enable) {
            Boolean enable = commandDTO.getEnable();
            brewingService.enable(enable);
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setMaxPower) {
            Integer maxPower = commandDTO.getIntValue();
            brewingService.setMaxPower(maxPower);
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setPowerTemperatureCorrelation) {
            Float temperatureCorrelation = commandDTO.getFloatValue();
            brewingService.setPowerTemperatureCorrelation(temperatureCorrelation);
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_enableTemperatureAlarm) {
            boolean enable = commandDTO.getEnable();
            brewingService.enableTemperatureAlarm(enable);
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setTimer) {
            Integer time = commandDTO.getIntValue();
            brewingService.setTimer(time);
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_removeTimer) {
            brewingService.removeTimer();
            sendStateResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_motorEnable) {
            boolean enable = commandDTO.getEnable();
            brewingService.motorEnable(enable);
            sendStateResponse(commandDTO);
        }
    }

    private void sendStateResponse(CommandDTO commandDTO) {
        BrewingState brewingState = brewingService.getBrewingState();
        externalInterface.sendResponse( new BrewingStatusResponse(
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
