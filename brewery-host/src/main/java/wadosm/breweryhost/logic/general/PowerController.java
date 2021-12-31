package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wadosm.breweryhost.device.system.SystemServices;

import javax.annotation.PreDestroy;

@RestController
@RequestMapping("/power")
@Log4j2
public class PowerController {

    private final SystemServices systemServices;

    private final PowerService powerService;

    public PowerController(PowerService powerService, SystemServices systemServices) {
        this.systemServices = systemServices;
        this.powerService = powerService;

        systemServices.heartBeat(true);
    }

    @PostMapping("/powerOff")
    public void powerOff() {
        powerService.powerOff();
    }

    @PostMapping("/restart")
    public void restart() {
        powerService.restart();
    }

    @PreDestroy
    public void destroy() {
        systemServices.heartBeat(false);
    }
}
