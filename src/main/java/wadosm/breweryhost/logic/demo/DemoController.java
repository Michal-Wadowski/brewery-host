package wadosm.breweryhost.logic.demo;

public interface DemoController {

    void doReboot();

    void doPowerOff();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void playSound(int period);

    void setMainsPower(int mainsNumber, int power);

//    BreweryStatusDTO readBreweryStatus();
}
