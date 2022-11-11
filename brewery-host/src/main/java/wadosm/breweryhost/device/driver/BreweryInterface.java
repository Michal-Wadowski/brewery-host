package wadosm.breweryhost.device.driver;

import wadosm.breweryhost.device.driver.model.BreweryRawState;

public interface BreweryInterface {

    void initDriver();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void setMainsPower(int mainsNumber, int power);

    BreweryRawState readDriverInterfaceState();

    void setAlarm(boolean alarmEnabled);

    void displayClear(int channel);

    void displayShowNumberDecEx(int channel, int num, int dots, boolean leadingZero, int length, int pos);

    void heartbeat(boolean state);
}
