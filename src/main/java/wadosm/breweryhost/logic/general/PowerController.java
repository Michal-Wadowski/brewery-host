package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.externalinterface.CommandListener;
import wadosm.breweryhost.device.externalinterface.ExternalInterface;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;

@Service
@Log4j2
public class PowerController implements CommandListener {

    private final PowerService powerService;

    public PowerController(
            ExternalInterface externalInterface,
            PowerService powerService
    ) {
        this.powerService = powerService;
        externalInterface.addCommandListener(this);
    }

    @Override
    public void commandReceived(CommandDTO commandDTO) {
        log.info(commandDTO);
        if (commandDTO.getCommand() == CommandDTO.Command.Power_powerOff) {
            powerService.powerOff();
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Power_restart) {
            powerService.restart();
        }
    }
}
