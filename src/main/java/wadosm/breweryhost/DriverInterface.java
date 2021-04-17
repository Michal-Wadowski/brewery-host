package wadosm.breweryhost;

public interface DriverInterface {

    boolean canUpdate();

    boolean lock();

    void unlock();

    boolean isLocked();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void playSound(int period);

    void setMainsPower(int mainsNumber, int power);

    BreweryStatus readBreweryStatus();

}
