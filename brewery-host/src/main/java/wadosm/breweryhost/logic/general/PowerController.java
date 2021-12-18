package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterfaceImpl;
import wadosm.breweryhost.device.externalinterface.CommandListener;
import wadosm.breweryhost.device.externalinterface.Session;
import wadosm.breweryhost.device.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.device.system.SystemServices;

import javax.annotation.PreDestroy;

@Service
@Log4j2
public class PowerController implements CommandListener {

    private final SystemServices systemServices;

    public PowerController(PowerService powerService, SystemServices systemServices) {
        this.systemServices = systemServices;

        systemServices.heartBeat(true);
    }

    @Override
    public void commandReceived(CommandDTO commandDTO, Session session) {
        PowerService powerService = new PowerServiceImpl(
                new DriverInterfaceImpl(session),
                systemServices
        );

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
