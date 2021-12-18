package wadosm.breweryhost.device.driver;

import wadosm.breweryhost.logic.DeviceCommand;

import java.util.List;

public interface DriverInterface {

    void init();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void setMainsPower(int mainsNumber, int power);

    DriverInterfaceState readDriverInterfaceState();
}
