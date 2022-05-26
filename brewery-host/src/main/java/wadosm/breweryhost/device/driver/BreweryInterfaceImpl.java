package wadosm.breweryhost.device.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.DigiPort;

import javax.annotation.PostConstruct;

@Service
@Log4j2
@RequiredArgsConstructor
public class BreweryInterfaceImpl implements BreweryInterface {

    private final int PWM_POWER_CORELATION = 0x0a;

    private final DigiPort digiPort;

    @Getter
    @AllArgsConstructor
    /*
     *  +------+-----+----------+------+---+OrangePiH3+---+------+----------+-----+------+
     *  | GPIO | wPi |   Name   | Mode | V | Physical | V | Mode | Name     | wPi | GPIO |
     *  +------+-----+----------+------+---+----++----+---+------+----------+-----+------+
     *  |      |     |     3.3V |      |   |  1 || 2  |   |      | 5V       |     |      |
     *  |   12 |   0 |    SDA.0 | ALT2 | 0 |  3 || 4  |   |      | 5V       |     |      |
     *  |   11 |   1 |    SCL.0 | ALT2 | 0 |  5 || 6  |   |      | GND      |     |      |
     *  |    6 |  #2 |      PA6 |  OUT | 0 |  7 || 8  | 0 | ALT3 | TXD.3    | 3   | 13   |
     *  |      |     |      GND |      |   |  9 || 10 | 0 | ALT3 | RXD.3    | 4   | 14   |
     *  |    1 |  #5 |    RXD.2 |  OUT | 0 | 11 || 12 | 1 | IN   | PD14     |#6   | 110  |
     *  |    0 |  #7 |    TXD.2 |  OUT | 1 | 13 || 14 |   |      | GND      |     |      |
     *  |    3 |  #8 |    CTS.2 |  OUT | 0 | 15 || 16 | 0 | OFF  | PC04     | 9   | 68   |
     *  |      |     |     3.3V |      |   | 17 || 18 | 0 | OFF  | PC07     |#10  | 71   |
     *  |   64 |  11 |   MOSI.0 | ALT3 | 0 | 19 || 20 |   |      | GND      |     |      |
     *  |   65 |  12 |   MISO.0 | ALT3 | 0 | 21 || 22 | 0 | OUT  | RTS.2    |#13  | 2    |
     *  |   66 |  14 |   SCLK.0 | ALT3 | 0 | 23 || 24 | 0 | ALT3 | CE.0     |#15  | 67   |
     *  |      |     |      GND |      |   | 25 || 26 | 0 | OFF  | PA21     |#16  | 21   |
     *  |   19 | #17 |    SDA.1 |  OFF | 0 | 27 || 28 | 0 | OFF  | SCL.1    |#18  | 18   |
     *  |    7 | #19 |     PA07 |  OUT | 0 | 29 || 30 |   |      | GND      |     |      |
     *  |    8 | #20 |     PA08 |  OUT | 0 | 31 || 32 | 0 | OFF  | RTS.1    |#21  | 200  |
     *  |    9 | #22 |     PA09 |  OFF | 0 | 33 || 34 |   |      | GND      |     |      |
     *  |   10 | #23 |     PA10 |  OUT | 0 | 35 || 36 | 0 | OFF  | CTS.1    |#24  | 201  |
     *  |   20 | #25 |     PA20 |  OUT | 0 | 37 || 38 | 0 | OFF  | TXD.1    |#26  | 198  |
     *  |      |     |      GND |      |   | 39 || 40 | 0 | OFF  | RXD.1    |#27  | 199  |
     *  +------+-----+----------+------+---+----++----+---+------+----------+-----+------+
     *  | GPIO | wPi |   Name   | Mode | V | Physical | V | Mode | Name     | wPi | GPIO |
     *  +------+-----+----------+------+---+OrangePiH3+---+------+----------+-----+------+
     */
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
        ORANGE(21),
        ALIVE(10);

        public final int pinNumber;
    }

    @PostConstruct
    public void initDriver() {
        digiPort.pinMode(Pin.POWER.pinNumber, 1);
        digiPort.pinMode(Pin.MOTOR_1.pinNumber, 1);
        digiPort.pinMode(Pin.MOTOR_2.pinNumber, 1);
        digiPort.pinMode(Pin.MOTOR_3.pinNumber, 1);

        digiPort.pinMode(Pin.BLUE.pinNumber, 1);
        digiPort.pinMode(Pin.ALARM.pinNumber, 1);
        digiPort.pinMode(Pin.WHITE.pinNumber, 1);
        digiPort.pinMode(Pin.ORANGE.pinNumber, 1);
        digiPort.pinMode(Pin.ALIVE.pinNumber, 1);

        digiPort.softPwmCreate(Pin.MAINS_1.pinNumber, 0, toPwmPower(0xff));
        digiPort.softPwmCreate(Pin.MAINS_2.pinNumber, 0, toPwmPower(0xff));

        digiPort.displayInit(0, Pin.SPI1_CLK.pinNumber, Pin.SPI1_DIO.pinNumber);
        digiPort.displayInit(1, Pin.SPI2_CLK.pinNumber, Pin.SPI2_DIO.pinNumber);

        digiPort.setBrightness(0, 7, true);
        digiPort.setBrightness(1, 7, true);

        powerEnable(true);
    }

    private int toPwmPower(int rawPower) {
        return PWM_POWER_CORELATION * rawPower;
    }

    private int fromPwmPower(int rawPower) {
        return rawPower / PWM_POWER_CORELATION;
    }

    private int boolToInt(boolean enable) {
        return enable ? 1 : 0;
    }

    @Override
    public void powerEnable(boolean enable) {
        digiPort.digitalWrite(Pin.POWER.pinNumber, boolToInt(enable));
    }

    @Override
    public void setAlarm(boolean alarmEnabled) {
        digiPort.digitalWrite(Pin.ALARM.pinNumber, boolToInt(alarmEnabled));
    }

    @Override
    public void motorEnable(int motorNumber, boolean enable) {
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

    @Override
    public void setMainsPower(int mainsNumber, int power) {
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

    @Override
    public void displayClear(int channel) {
        digiPort.clear(channel);
    }

    @Override
    public void displayShowNumberDecEx(int channel, int num, int dots, boolean leadingZero, int length, int pos) {
        digiPort.showNumberDecEx(
                channel, num, dots, leadingZero, length, pos
        );
    }

    @Override
    public BreweryState readDriverInterfaceState() {
        Boolean power = responseToBoolean(digiPort.digitalRead(Pin.POWER.pinNumber));
        Boolean motor1 = responseToBoolean(digiPort.digitalRead(Pin.MOTOR_1.pinNumber));
        Boolean motor2 = responseToBoolean(digiPort.digitalRead(Pin.MOTOR_2.pinNumber));
        Boolean motor3 = responseToBoolean(digiPort.digitalRead(Pin.MOTOR_3.pinNumber));
        Integer mains1 = fromPwmPower(digiPort.softPwmRead(Pin.MAINS_1.pinNumber));
        Integer mains2 = fromPwmPower(digiPort.softPwmRead(Pin.MAINS_2.pinNumber));

        return new BreweryState(power, motor1, motor2, motor3, mains1, mains2);

    }

    @Override
    public void heartbeat(boolean state) {
        digiPort.digitalWrite(Pin.ALIVE.pinNumber, boolToInt(state));
    }

    private boolean responseToBoolean(Object response) {
        return ((Integer) response) != 0;
    }
}
