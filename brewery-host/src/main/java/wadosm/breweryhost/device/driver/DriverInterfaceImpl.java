package wadosm.breweryhost.device.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wadosm.breweryhost.device.externalinterface.Session;
import wadosm.breweryhost.logic.DeviceCommand;
import wadosm.breweryhost.logic.DeviceResponse;

import java.util.Arrays;
import java.util.List;

public class DriverInterfaceImpl implements DriverInterface {

    @Getter
    @AllArgsConstructor
    enum Pin {
        // current setup
        SOUND(2),
        POWER(7),
        MAINS_1(5),
        MAINS_2(13),

        MOTOR_1(19),
        MOTOR_2(8),
        MOTOR_3(20),

        // SPI1
        SPI1_CLK(23),
        SPI1_DIO(25),

        // SPI2 (not connected yet)
        SPI2_CLK(17),
        SPI2_DIO(22),

        // w1-gpio
        PARAM_W1(6),

        // i2c
        I2C_1(24),
        I2C_2(26),
        I2C_3(27),

        BLUE(15),
        ALARM(16),
        WHITE(18),
        ORANGE(21);

        private final int pinNumber;
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

    private Session session;

    public void setSession(Session session) {
        this.session = session;
        init();
    }

    @Override
    public void init() {
        if (session != null) {
            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.POWER, 1)));
            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.MOTOR_1, 1)));
            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.MOTOR_2, 1)));
            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.MOTOR_3, 1)));

            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.BLUE, 1)));
            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.ALARM, 1)));
            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.WHITE, 1)));
            session.sendCommand(new DeviceCommand("pinMode", Arrays.asList(Pin.ORANGE, 1)));

            session.sendCommand(new DeviceCommand("softPwmCreate", Arrays.asList(Pin.MAINS_1, 0, 0x0a * 0xff)));
            session.sendCommand(new DeviceCommand("softPwmCreate", Arrays.asList(Pin.MAINS_2, 0, 0x0a * 0xff)));

            session.sendCommand(new DeviceCommand("displayInit", Arrays.asList(0, Pin.SPI1_CLK, Pin.SPI1_DIO)));
            session.sendCommand(new DeviceCommand("displayInit", Arrays.asList(1, Pin.SPI2_CLK, Pin.SPI2_DIO)));
        }
    }

    private int boolToInt(boolean enable) {
        return enable ? 1 : 0;
    }

    @Override
    public void powerEnable(boolean enable) {
        if (session != null) {
            session.sendCommand(new DeviceCommand("digitalWrite", Arrays.asList(Pin.POWER, boolToInt(enable))));
        }
    }

    @Override
    public void motorEnable(int motorNumber, boolean enable) {
        if (session != null) {
            Pin pin = null;
            if (motorNumber == 1) {
                pin = Pin.MOTOR_1;
            } else if (motorNumber == 2) {
                pin = Pin.MOTOR_2;
            } else if (motorNumber == 3) {
                pin = Pin.MOTOR_3;
            }
            if (pin != null) {
                session.sendCommand(new DeviceCommand("digitalWrite", Arrays.asList(pin, boolToInt(enable))));
            }
        }
    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        if (session != null) {
            Pin pin = null;
            if (mainsNumber == 1) {
                pin = Pin.MAINS_1;
            } else if (mainsNumber == 2) {
                pin = Pin.MAINS_2;
            }

            if (pin != null) {
                session.sendCommand(new DeviceCommand("softPwmWrite", Arrays.asList(pin, power)));
            }
        }
    }

    @Override
    public DriverInterfaceState readDriverInterfaceState() {
        if (session != null) {
            List<DeviceResponse> deviceResponses = session.sendCommands(Arrays.asList(
                    new DeviceCommand("digitalRead", Arrays.asList(Pin.POWER)),
                    new DeviceCommand("digitalRead", Arrays.asList(Pin.MOTOR_1)),
                    new DeviceCommand("digitalRead", Arrays.asList(Pin.MOTOR_2)),
                    new DeviceCommand("digitalRead", Arrays.asList(Pin.MOTOR_3)),
                    new DeviceCommand("digitalRead", Arrays.asList(Pin.MAINS_1)),
                    new DeviceCommand("digitalRead", Arrays.asList(Pin.MAINS_2))
            ));

            Boolean power = null;
            Boolean motor1 = null;
            Boolean motor2 = null;
            Boolean motor3 = null;
            Integer mains1 = null;
            Integer mains2 = null;

            for (DeviceResponse response : deviceResponses) {
                if (response.getFunction().equals("digitalRead")) {
                    if ((Integer) response.getArguments().get(0) == Pin.POWER.pinNumber) {
                        power = (Boolean) response.getResponse();
                    }

                    if ((Integer) response.getArguments().get(0) == Pin.MOTOR_1.pinNumber) {
                        motor1 = (Boolean) response.getResponse();
                    }

                    if ((Integer) response.getArguments().get(0) == Pin.MOTOR_2.pinNumber) {
                        motor2 = (Boolean) response.getResponse();
                    }

                    if ((Integer) response.getArguments().get(0) == Pin.MOTOR_3.pinNumber) {
                        motor3 = (Boolean) response.getResponse();
                    }

                    if ((Integer) response.getArguments().get(0) == Pin.MAINS_1.pinNumber) {
                        mains1 = (Integer) response.getResponse();
                    }

                    if ((Integer) response.getArguments().get(0) == Pin.MAINS_2.pinNumber) {
                        mains2 = (Integer) response.getResponse();
                    }
                }
            }

            return new DriverInterfaceState(power, motor1, motor2, motor3, null, mains1, mains2);
        } else {
            return new DriverInterfaceState(null, null, null, null, null, null, null);
        }
    }
}
