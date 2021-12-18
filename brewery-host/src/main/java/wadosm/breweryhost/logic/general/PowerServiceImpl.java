package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.DriverInterfaceState;
import wadosm.breweryhost.device.system.SystemServices;
import wadosm.breweryhost.logic.DeviceCommand;

import java.util.List;

@Service
@Log4j2
public class PowerServiceImpl implements PowerService {

    private final DriverInterface driverInterface;

    private final SystemServices systemServices;

    public PowerServiceImpl(DriverInterface driverInterface, SystemServices systemServices) {
        this.driverInterface = driverInterface;
        this.systemServices = systemServices;

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

            driverInterface.powerEnable(true);
        }
    }

    @Override
    public void powerOff() {
        driverInterface.powerEnable(false);
        systemServices.doPowerOff();
    }

    @Override
    public void restart() {
        driverInterface.powerEnable(false);
        systemServices.doReboot();
    }
}
