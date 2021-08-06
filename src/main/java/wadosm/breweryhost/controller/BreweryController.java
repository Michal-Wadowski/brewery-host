package wadosm.breweryhost.controller;

import wadosm.breweryhost.externalinterface.dto.BreweryStatusDTO;

public interface BreweryController {

    void doReboot();

    void doPowerOff();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void playSound(int period);

    void setMainsPower(int mainsNumber, int power);

    BreweryStatusDTO readBreweryStatus();
}
