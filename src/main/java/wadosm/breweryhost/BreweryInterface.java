package wadosm.breweryhost;

public interface BreweryInterface {

    void doReboot();

    void doPowerOff();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void playSound(int period);

    void setMainsPower(int mainsNumber, int power);

    BreweryStatus readBreweryStatus();
}
