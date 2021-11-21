package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.DriverInterfaceState;
import wadosm.breweryhost.device.system.SystemServices;

@Service
@Log4j2
public class PowerServiceImpl implements PowerService {

    private final SystemServices systemServices;

    private final DriverInterface driverInterface;

    public PowerServiceImpl(
            SystemServices systemServices, DriverInterface driverInterface
    ) {
        this.systemServices = systemServices;
        this.driverInterface = driverInterface;

        tryEnablePower(driverInterface);
    }

    private void tryEnablePower(DriverInterface driverInterface) {
        while (true) {
            DriverInterfaceState interfaceState = driverInterface.readDriverInterfaceState();
            if (interfaceState.getPower()) {
                break;
            }

            if (driverInterface.lock()) {
                driverInterface.powerEnable(true);
                driverInterface.unlock();
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
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
}
