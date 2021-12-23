package wadosm.breweryhost.logic.brewing;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.device.temperature.TemperatureProvider;

import java.time.Instant;

@RequestMapping("/brewing")
@Log4j2
public class BrewingController {

    private final BrewingService brewingService;

    public BrewingController(BrewingService brewingService) {
        this.brewingService = brewingService;
    }

    @PostMapping("/getBrewingState")
    public BrewingStatusResponse getBrewingState(@RequestBody CommandDTO commandDTO) {
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/setDestinationTemperature")
    public BrewingStatusResponse setDestinationTemperature(@RequestBody CommandDTO commandDTO) {
        Float value = commandDTO.getFloatValue();
        brewingService.setDestinationTemperature(value);
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/enable")
    public BrewingStatusResponse enable(@RequestBody CommandDTO commandDTO) {
        Boolean enable = commandDTO.getEnable();
        brewingService.enable(enable);
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/setMaxPower")
    public BrewingStatusResponse setMaxPower(@RequestBody CommandDTO commandDTO) {
        Integer maxPower = commandDTO.getIntValue();
        brewingService.setMaxPower(maxPower);
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/setPowerTemperatureCorrelation")
    public BrewingStatusResponse setPowerTemperatureCorrelation(@RequestBody CommandDTO commandDTO) {
        Float temperatureCorrelation = commandDTO.getFloatValue();
        brewingService.setPowerTemperatureCorrelation(temperatureCorrelation);
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/enableTemperatureAlarm")
    public BrewingStatusResponse enableTemperatureAlarm(@RequestBody CommandDTO commandDTO) {
        boolean enable = commandDTO.getEnable();
        brewingService.enableTemperatureAlarm(enable);
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/setTimer")
    public BrewingStatusResponse setTimer(@RequestBody CommandDTO commandDTO) {
        Integer time = commandDTO.getIntValue();
        brewingService.setTimer(time);
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/removeTimer")
    public BrewingStatusResponse removeTimer(@RequestBody CommandDTO commandDTO) {
        brewingService.removeTimer();
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/motorEnable")
    public BrewingStatusResponse motorEnable(@RequestBody CommandDTO commandDTO) {
        boolean enable = commandDTO.getEnable();
        brewingService.motorEnable(enable);
        return sendStateResponse(commandDTO);
    }

    private BrewingStatusResponse sendStateResponse(CommandDTO commandDTO) {
        BrewingState brewingState = brewingService.getBrewingState();
        return new BrewingStatusResponse(
                commandDTO.getCommandId(), Instant.now().getEpochSecond(), brewingState
        );
    }

    @Getter
    public static class BrewingStatusResponse extends ResponseDTO {

        private final BrewingState brewingState;

        public BrewingStatusResponse(Integer commandId, Long time, BrewingState brewingState) {
            super(commandId, time);
            this.brewingState = brewingState;
        }
    }
}
