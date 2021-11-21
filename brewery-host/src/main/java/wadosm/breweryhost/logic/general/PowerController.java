package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.externalinterface.CommandListener;
import wadosm.breweryhost.device.externalinterface.ExternalInterface;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.system.SystemServices;

import javax.annotation.PreDestroy;

@Service
@Log4j2
public class PowerController implements CommandListener {

    private final PowerService powerService;

    private final SystemServices systemServices;

    public PowerController(
            ExternalInterface externalInterface,
            PowerService powerService,
            SystemServices systemServices
    ) {
        this.powerService = powerService;
        this.systemServices = systemServices;
        externalInterface.addCommandListener(this);

        systemServices.heartBeat(true);
    }

    @Override
    public void commandReceived(CommandDTO commandDTO) {
        if (commandDTO.getCommand() == CommandDTO.Command.Power_powerOff) {
            powerService.powerOff();
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Power_restart) {
            powerService.restart();
        }
    }

    @PreDestroy
    public void destroy() {
        systemServices.heartBeat(false);
    }
}
