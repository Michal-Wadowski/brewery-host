package wadosm.breweryhost.driver;

import wadosm.breweryhost.driver.DriverInterface;
import wadosm.breweryhost.driver.DriverInterfaceState;

public class FakeDriverInterface implements DriverInterface {

    public Boolean[] motorEnabled = new Boolean[3];

    public Integer[] mainsPower = new Integer[2];

    public boolean locked;

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public boolean lock() {
        locked = true;
        return true;
    }

    @Override
    public void unlock() {
        locked = false;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void powerEnable(boolean enable) {

    }

    @Override
    public void motorEnable(int motorNumber, boolean enable) {
        if (locked) {
            motorEnabled[motorNumber - 1] = enable;
        }
    }

    @Override
    public void playSound(int period) {

    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        if (locked) {
            mainsPower[mainsNumber - 1] = power;
        }
    }

    @Override
    public DriverInterfaceState readDriverInterfaceState() {
        return new DriverInterfaceState(true, false, false, false, 0, 0, 0);
    }
}
