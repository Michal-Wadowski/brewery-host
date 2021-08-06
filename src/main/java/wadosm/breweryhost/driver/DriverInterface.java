package wadosm.breweryhost.driver;

public interface DriverInterface {

    boolean isReady();

    boolean lock();

    void unlock();

    boolean isLocked();

    void powerEnable(boolean enable);

    void motorEnable(int motorNumber, boolean enable);

    void playSound(int period);

    void setMainsPower(int mainsNumber, int power);

    DriverInterfaceState readDriverInterfaceState();
}
