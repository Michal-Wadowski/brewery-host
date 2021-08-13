package wadosm.breweryhost.device.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import static wadosm.breweryhost.device.driver.DriverInterfaceImpl.Lock.CONTROLLER_LOCK;
import static wadosm.breweryhost.device.driver.DriverInterfaceImpl.Lock.IDLE;
import static wadosm.breweryhost.device.driver.DriverInterfaceImpl.Offsets.*;

@Component
public class DriverInterfaceImpl implements DriverInterface {

    private final int FILE_LENGTH = 9;

    @Getter
    @AllArgsConstructor
    enum Lock {
        NOT_INITIALIZED((byte) 0x00), IDLE((byte) 0x01), CONTROLLER_LOCK((byte) 0x02);

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

        public int getOffset(int number) {
            return getOffset() + number - 1;
        }

        private final int size;

        private final int count;
    }

    @AllArgsConstructor
    @Getter
    enum State {
        DISABLED((byte) 0x00), ENABLED((byte) 0x01);

        private final byte enabled;

        static State fromBoolean(boolean enabled) {
            return enabled ? ENABLED : DISABLED;
        }

        static boolean toBoolean(byte enabled) {
            return enabled != 0 ? true : false;
        }
    }

    private final DriverFile driverFile;

    public DriverInterfaceImpl(DriverFile driverFile) {
        this.driverFile = driverFile;
    }

    @Override
    public boolean isReady() {
        if (isNotValid()) return false;

        driverFile.seek(LOCK.getOffset());
        byte[] data = driverFile.read(LOCK.getSize());

        return data[0] == IDLE.getCode() || data[0] == CONTROLLER_LOCK.getCode();
    }

    private boolean isNotValid() {
        driverFile.open();
        return driverFile.length() != FILE_LENGTH;
    }

    @Override
    public boolean isLocked() {
        if (isNotValid()) return false;

        driverFile.seek(LOCK.getOffset());
        byte[] data = driverFile.read(LOCK.getSize());

        return data[0] == CONTROLLER_LOCK.getCode();
    }

    @Override
    public boolean lock() {
        if (isReady()) {
            driverFile.seek(LOCK.getOffset());
            driverFile.write(new byte[]{CONTROLLER_LOCK.getCode()});
            return true;
        }
        return false;
    }

    @Override
    public void unlock() {
        if (isReady()) {
            driverFile.seek(LOCK.getOffset());
            driverFile.write(new byte[]{IDLE.getCode()});
            driverFile.close();
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
                driverFile.seek(MOTOR.getOffset(motorNumber));
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
                data[0] = (byte) (period & 0xff);
                period /= 0x100;
                data[1] = (byte) (period & 0xff);
                driverFile.write(data);
            }
        }
    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        if (isLocked()) {
            if (mainsNumber >= 1 && mainsNumber <= MAINS.getCount()) {
                if (power >= 0 && power <= 0xff) {
                    driverFile.seek(MAINS.getOffset(mainsNumber));
                    byte[] data = new byte[1];
                    data[0] = (byte) (power & 0xff);
                    driverFile.write(data);
                }
            }
        }
    }

    @Override
    public DriverInterfaceState readDriverInterfaceState() {
        if (isReady()) {
            driverFile.seek(POWER.getOffset());
            boolean power = State.toBoolean(driverFile.read(POWER.getSize())[0]);

            driverFile.seek(MOTOR.getOffset(1));
            boolean motor1 = State.toBoolean(driverFile.read(MOTOR.getSize())[0]);

            driverFile.seek(MOTOR.getOffset(2));
            boolean motor2 = State.toBoolean(driverFile.read(MOTOR.getSize())[0]);

            driverFile.seek(MOTOR.getOffset(3));
            boolean motor3 = State.toBoolean(driverFile.read(MOTOR.getSize())[0]);

            driverFile.seek(SOUND.getOffset());
            byte[] rawSound = driverFile.read(SOUND.getSize());
            int sound = (rawSound[1] & 0xff) * 0x100 + (rawSound[0] & 0xff);

            driverFile.seek(MAINS.getOffset(1));
            int mains1 = driverFile.read(MAINS.getSize())[0] & 0xff;

            driverFile.seek(MAINS.getOffset(2));
            int mains2 = driverFile.read(MAINS.getSize())[0] & 0xff;

            driverFile.close();

            return new DriverInterfaceState(power, motor1, motor2, motor3, sound, mains1, mains2);
        }
        return null;
    }
}
