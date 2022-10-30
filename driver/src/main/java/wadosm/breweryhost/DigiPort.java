package wadosm.breweryhost;

public interface DigiPort {
    void pinMode(int pin, int mode);

    void softPwmCreate(int pin, int initialValue, int pwmRange);

    void digitalWrite(int pin, int value);

    void softPwmStop(int pin);

    void softPwmWrite(int pin, int value);

    int digitalRead(int pin);

    int softPwmRead(int pin);

    void displayInit(int channel, int pinClk, int pinDIO);

    void setBrightness(int channel, int brightness, boolean on);

    void setSegments(int channel, byte[] segments, int length, int pos);

    void clear(int channel);

    void showNumberDec(int channel, int num, boolean leading_zero, int length, int pos);

    void showNumberDecEx(int channel, int num, int dots, boolean leading_zero, int length, int pos);

    void showNumberHexEx(int channel, int num, int dots, boolean leading_zero, int length, int pos);
}
