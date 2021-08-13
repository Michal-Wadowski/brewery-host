package wadosm.breweryhost.device.driver;

public class FakeDriverInterface implements DriverInterface {

    public Boolean[] motorEnabled = new Boolean[3];

    public Integer[] mainsPower = new Integer[2];

    public boolean locked;

    public int sound;

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
        sound = period;
    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        if (locked) {
            mainsPower[mainsNumber - 1] = power;
        }
    }

    @Override
    public DriverInterfaceState readDriverInterfaceState() {
        return new DriverInterfaceState(
                true, motorEnabled[0], motorEnabled[1], motorEnabled[2], sound, mainsPower[0], mainsPower[1]
        );
    }
}
