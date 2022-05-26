package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wadosm.breweryhost.device.system.SystemServices;

@RestController
@RequestMapping("/power")
@Log4j2
public class PowerController {

    private final SystemServices systemServices;

    public PowerController(SystemServices systemServices) {
        this.systemServices = systemServices;
    }

    @PostMapping("/powerOff")
    public void powerOff() {
        systemServices.doPowerOff();
    }

    @PostMapping("/restart")
    public void restart() {
        systemServices.doReboot();
    }
}
