package wadosm.breweryhost.logic.fermenting;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;

import java.time.Instant;

@RestController
@RequestMapping("/fermenting")
@Log4j2
public class FermentingController {

    private final FermentingService fermentingService;

    public FermentingController(FermentingService fermentingService) {
        this.fermentingService = fermentingService;
    }

    @PostMapping("/getFermentingState")
    public FermentingStatusResponse getFermentingState(@RequestBody CommandDTO commandDTO) {
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/setDestinationTemperature")
    public FermentingStatusResponse setDestinationTemperature(@RequestBody CommandDTO commandDTO) {
        Float value = commandDTO.getFloatValue();
        fermentingService.setDestinationTemperature(value);
        return sendStateResponse(commandDTO);
    }

    @PostMapping("/enable")
    public FermentingStatusResponse enable(@RequestBody CommandDTO commandDTO) {
        Boolean enable = commandDTO.getEnable();
        fermentingService.enable(enable);
        return sendStateResponse(commandDTO);
    }

    private FermentingStatusResponse sendStateResponse(CommandDTO commandDTO) {
        FermentingState fermentingState = fermentingService.getFermentingState();
        return new FermentingStatusResponse(
                commandDTO.getCommandId(), Instant.now().getEpochSecond(), fermentingState
        );
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
