package wadosm.breweryhost.controller.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.driver.DriverInterface;
import wadosm.breweryhost.driver.DriverInterfaceState;
import wadosm.breweryhost.externalinterface.CommandListener;
import wadosm.breweryhost.externalinterface.ExternalInterface;
import wadosm.breweryhost.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.system.SystemServices;

@Service
@Log4j2
public class PowerControllerImpl implements PowerController, CommandListener {

    private final SystemServices systemServices;

    private final DriverInterface driverInterface;

    public PowerControllerImpl(
            SystemServices systemServices, ExternalInterface externalInterface, DriverInterface driverInterface
    ) {
        this.systemServices = systemServices;
        this.driverInterface = driverInterface;

        externalInterface.addCommandListener(this);

        tryEnablePower(driverInterface);
    }

    private void tryEnablePower(DriverInterface driverInterface) {
        while (true) {
            DriverInterfaceState interfaceState = driverInterface.readDriverInterfaceState();
            if (interfaceState.getPower()) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

            if (driverInterface.lock()) {
                driverInterface.powerEnable(true);
                driverInterface.unlock();
            }
        }
    }

    @Override
    public void powerOff() {
        if (driverInterface.lock()) {
            driverInterface.powerEnable(false);
            driverInterface.unlock();
        }
        systemServices.doPowerOff();
    }

    @Override
    public void restart() {
        if (driverInterface.lock()) {
            driverInterface.powerEnable(false);
            driverInterface.unlock();
        }
        systemServices.doReboot();
    }

    @Override
    public void commandReceived(CommandDTO commandDTO) {
        log.info(commandDTO);
        if (commandDTO.getCommand() == CommandDTO.Command.Power_powerOff) {
            powerOff();
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Power_restart) {
            restart();
        }
    }
}
