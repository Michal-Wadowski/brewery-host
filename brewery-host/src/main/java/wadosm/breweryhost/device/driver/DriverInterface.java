package wadosm.breweryhost.device.driver;

import wadosm.breweryhost.device.externalinterface.DriverSession;
import wadosm.breweryhost.logic.DeviceCommand;

import java.util.List;

public interface DriverInterface {

    void setSession(DriverSession driverSession);

    void removeSession();

    void init();

    boolean isReady();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void setMainsPower(int mainsNumber, int power);

    DriverInterfaceState readDriverInterfaceState();

    void setAlarm(boolean alarmEnabled);

    void displayClear(int channel);

    void displayShowNumberDecEx(int channel, int num, int dots, boolean leadingZero, int length, int pos);
}
