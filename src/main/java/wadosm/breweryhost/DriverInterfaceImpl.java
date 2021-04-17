package wadosm.breweryhost;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static wadosm.breweryhost.DriverInterfaceImpl.Lock.CONTROLLER_LOCK;
import static wadosm.breweryhost.DriverInterfaceImpl.Lock.IDLE;
import static wadosm.breweryhost.DriverInterfaceImpl.Offsets.*;

public class DriverInterfaceImpl implements DriverInterface {

    private final int FILE_LENGTH = 13;

    @Getter
    @AllArgsConstructor
    enum Lock {
        NOT_INITIALIZED((byte)0x00), IDLE((byte)0x01), CONTROLLER_LOCK((byte)0x02);

        private final byte code;
    }

    @Getter
    @AllArgsConstructor
    enum Offsets {
        LOCK(0, 1, 1),
        POWER(1, 1, 1),
        MOTOR(2, 1, 3),
        SOUND(5, 2, 1),
        MAINS(7, 1, 2);

        private final int offset;

        private final int size;

        private final int count;
    }

    @AllArgsConstructor
    @Getter
    enum State {
        DISABLED((byte)0x00), ENABLED((byte)0x01);

        private final byte enabled;

        static State fromBoolean(boolean enabled) {
            return enabled ? ENABLED : DISABLED;
        }
    }

    private final DriverFile driverFile;

    public DriverInterfaceImpl(DriverFile driverFile) {
        this.driverFile = driverFile;
    }

    @Override
    public boolean canUpdate() {
        if (isValid()) return false;

        driverFile.seek(LOCK.getOffset());
        byte[] data = driverFile.read(LOCK.getSize());

        return data[0] == IDLE.getCode() || data[0] == CONTROLLER_LOCK.getCode();
    }

    private boolean isValid() {
        if (driverFile.length() != FILE_LENGTH) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isLocked() {
        if (isValid()) return false;

        driverFile.seek(LOCK.getOffset());
        byte[] data = driverFile.read(LOCK.getSize());

        return data[0] == CONTROLLER_LOCK.getCode();
    }

    @Override
    public boolean lock() {
        if (canUpdate()) {
            driverFile.seek(LOCK.getOffset());
            driverFile.write(new byte[]{CONTROLLER_LOCK.getCode()});
            return true;
        }
        return false;
    }

    @Override
    public void unlock() {
        if (canUpdate()) {
            driverFile.seek(LOCK.getOffset());
            driverFile.write(new byte[]{IDLE.getCode()});
        }
    }

    @Override
    public void powerEnable(boolean enable) {
        if (isLocked()) {
            driverFile.seek(POWER.getOffset());
            driverFile.write(new byte[]{State.fromBoolean(enable).getEnabled()});
        }
    }

    @Override
    public void motorEnable(int motorNumber, boolean enable) {
        if (isLocked()) {
            if (motorNumber >= 1 && motorNumber <= MOTOR.getCount()) {
                driverFile.seek(MOTOR.getOffset() + motorNumber - 1);
                driverFile.write(new byte[]{State.fromBoolean(enable).getEnabled()});
            }
        }
    }

    @Override
    public void playSound(int period) {
        if (isLocked()) {
            if (period >= 0 && period <= 0xffff) {
                driverFile.seek(SOUND.getOffset());
                byte[] data = new byte[2];
                data[1] = (byte) (period & 0xff);
                period /= 0x100;
                data[0] = (byte) (period & 0xff);
                driverFile.write(data);
            }
        }
    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        if (isLocked()) {
            if (mainsNumber >= 1 && mainsNumber <= MAINS.getCount()) {
                if (power >= 0 && power <= 0xff) {
                    driverFile.seek(MAINS.getOffset() + mainsNumber - 1);
                    byte[] data = new byte[1];
                    data[0] = (byte) (power & 0xff);
                    driverFile.write(data);
                }
            }
        }
    }

    @Override
    public BreweryStatus readBreweryStatus() {
        return null;
    }
}
