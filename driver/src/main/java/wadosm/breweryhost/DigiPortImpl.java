package wadosm.breweryhost;

import org.springframework.core.env.Environment;

import java.util.Arrays;

/*
  Don't change any signature or name, it's used by driver JNI.
 */
public class DigiPortImpl implements DigiPort {

    private final DriverLoader driverLoader;

    public DigiPortImpl(DriverLoader driverLoader) {
        this.driverLoader = driverLoader;
    }

    @Override
    public native void pinMode(int pin, int mode);

    @Override
    public native void softPwmCreate(int pin, int initialValue, int pwmRange);

    @Override
    public native void digitalWrite(int pin, int value);

    @Override
    public native void softPwmStop(int pin);

    @Override
    public native void softPwmWrite(int pin, int value);

    @Override
    public native int digitalRead(int pin);

    @Override
    public native int softPwmRead(int pin);

    @Override
    public native void displayInit(int channel, int pinClk, int pinDIO);

    @Override
    public native void setBrightness(int channel, int brightness, boolean on);

    @Override
    public native void setSegments(int channel, byte[] segments, int length, int pos);

    @Override
    public native void clear(int channel);

    @Override
    public native void showNumberDec(int channel, int num, boolean leading_zero, int length, int pos);

    @Override
    public native void showNumberDecEx(int channel, int num, int dots, boolean leading_zero, int length, int pos);

    @Override
    public native void showNumberHexEx(int channel, int num, int dots, boolean leading_zero, int length, int pos);

    @Override
    public native void debugEnable(boolean enable);
}
