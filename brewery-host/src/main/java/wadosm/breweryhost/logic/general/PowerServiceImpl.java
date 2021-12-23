package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.DriverInterfaceState;
import wadosm.breweryhost.device.system.SystemServices;

@Service
@Log4j2
public class PowerServiceImpl implements PowerService {

    private final DriverInterface driverInterface;

    private final SystemServices systemServices;

    public PowerServiceImpl(DriverInterface driverInterface, SystemServices systemServices) {
        this.driverInterface = driverInterface;
        this.systemServices = systemServices;

        Thread powerTask = new EnablePowerTask(driverInterface);
        powerTask.start();
    }

    private static class EnablePowerTask extends Thread {

        private final DriverInterface driverInterface;

        private EnablePowerTask(DriverInterface driverInterface) {
            super("PowerServiceImpl-Thread");
            this.driverInterface = driverInterface;
        }

        public void run() {
            tryEnablePower();
        }

        private void tryEnablePower() {
            while (true) {
                DriverInterfaceState interfaceState = driverInterface.readDriverInterfaceState();
                if (driverInterface.isReady() && interfaceState != null && interfaceState.getPower()) {
                    break;
                }

                driverInterface.powerEnable(true);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
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
