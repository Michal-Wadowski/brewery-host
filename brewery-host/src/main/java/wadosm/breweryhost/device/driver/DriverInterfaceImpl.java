package wadosm.breweryhost.device.driver;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.*;
import wadosm.breweryhost.device.externalinterface.DriverSession;

@Service
@Log4j2
public class DriverInterfaceImpl implements DriverInterface {

    private final int PWM_POWER_CORELATION = 0x0a;

    private final DriverEntry driverEntry = new DriverEntryImpl();
    private final DigiPort digiPort = new DigiPort(driverEntry);
    private final MessagesProcessor messagesProcessor;
    private final ConnectionConsumer connectionConsumer;

    public DriverInterfaceImpl(MessagesProcessor messagesProcessor) {
        this.messagesProcessor = messagesProcessor;
        connectionConsumer = new ConnectionConsumer(
                driverEntry, messagesProcessor
        );
    }

    @Getter
    @AllArgsConstructor
    public enum Pin {
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

        @JsonValue
        public int toValue() {
            return pinNumber;
        }
    }

//    private DriverSession driverSession;

    private boolean initialized = false;

    @Override
    public void setSession(DriverSession driverSession) {
//        this.driverSession = driverSession;
        init();
    }

    @Override
    public void removeSession() {
        setSession(null);
    }

    @Override
    public void init() {
        if (!initialized) {
            digiPort.pinMode(Pin.POWER.pinNumber, 1);
            digiPort.pinMode(Pin.MOTOR_1.pinNumber, 1);
            digiPort.pinMode(Pin.MOTOR_2.pinNumber, 1);
            digiPort.pinMode(Pin.MOTOR_3.pinNumber, 1);

            digiPort.pinMode(Pin.BLUE.pinNumber, 1);
            digiPort.pinMode(Pin.ALARM.pinNumber, 1);
            digiPort.pinMode(Pin.WHITE.pinNumber, 1);
            digiPort.pinMode(Pin.ORANGE.pinNumber, 1);

            digiPort.softPwmCreate(Pin.MAINS_1.pinNumber, 0, toPwmPower(0xff));
            digiPort.softPwmCreate(Pin.MAINS_2.pinNumber, 0, toPwmPower(0xff));

            digiPort.displayInit( 0, Pin.SPI1_CLK.pinNumber, Pin.SPI1_DIO.pinNumber);
            digiPort.displayInit( 1, Pin.SPI2_CLK.pinNumber, Pin.SPI2_DIO.pinNumber);

            digiPort.setBrightness(0, 7, true);
            digiPort.setBrightness(1, 7, true);

            initialized = true;

            Thread attachListener = new Thread() {
                public void run() {
                    connectionConsumer.attachListener();
                }
            };
            attachListener.start();
        }
    }

    private int toPwmPower(int rawPower) {
        return PWM_POWER_CORELATION * rawPower;
    }

    private int fromPwmPower(int rawPower) {
        return rawPower / PWM_POWER_CORELATION;
    }

    @Override
    public boolean isReady() {
        return initialized;
    }

    private int boolToInt(boolean enable) {
        return enable ? 1 : 0;
    }

    @Override
    public void powerEnable(boolean enable) {
        if (isReady()) {
            digiPort.digitalWrite(Pin.POWER.pinNumber, boolToInt(enable));
        }
    }

    @Override
    public void setAlarm(boolean alarmEnabled) {
        if (isReady()) {
            digiPort.digitalWrite(Pin.ALARM.pinNumber, boolToInt(alarmEnabled));
        }
    }

    @Override
    public void motorEnable(int motorNumber, boolean enable) {
        if (isReady()) {
            Pin pin = null;
            if (motorNumber == 1) {
                pin = Pin.MOTOR_1;
            } else if (motorNumber == 2) {
                pin = Pin.MOTOR_2;
            } else if (motorNumber == 3) {
                pin = Pin.MOTOR_3;
            }
            if (pin != null) {
                digiPort.digitalWrite(pin.pinNumber, boolToInt(enable));
            }
        }
    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        if (isReady()) {
            Pin pin = null;
            if (mainsNumber == 1) {
                pin = Pin.MAINS_1;
            } else if (mainsNumber == 2) {
                pin = Pin.MAINS_2;
            }

            if (pin != null) {
                digiPort.softPwmWrite(pin.pinNumber, toPwmPower(power));
            }
        }
    }

    @Override
    public void displayClear(int channel) {
        if (isReady()) {
            digiPort.clear(channel);
        }
    }

    @Override
    public void displayShowNumberDecEx(int channel, int num, int dots, boolean leadingZero, int length, int pos) {
        if (isReady()) {
            digiPort.showNumberDecEx(
                    channel, num, dots, leadingZero, length, pos
            );
        }
    }

    @Override
    public DriverInterfaceState readDriverInterfaceState() {
        if (isReady()) {

            Boolean power = responseToBoolean(digiPort.digitalRead(Pin.POWER.pinNumber));
            Boolean motor1 = responseToBoolean(digiPort.digitalRead(Pin.MOTOR_1.pinNumber));
            Boolean motor2 = responseToBoolean(digiPort.digitalRead(Pin.MOTOR_2.pinNumber));
            Boolean motor3 = responseToBoolean(digiPort.digitalRead(Pin.MOTOR_3.pinNumber));
            Integer mains1 = fromPwmPower(digiPort.softPwmRead(Pin.MAINS_1.pinNumber));
            Integer mains2 = fromPwmPower(digiPort.softPwmRead(Pin.MAINS_2.pinNumber));

            return new DriverInterfaceState(power, motor1, motor2, motor3, mains1, mains2);
        } else {
            return null;
        }
    }

    private boolean responseToBoolean(Object response) {
        return ((Integer) response) != 0;
    }
}
