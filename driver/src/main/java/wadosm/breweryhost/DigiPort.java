package wadosm.breweryhost;

public class DigiPort {

     private final DriverEntry driverEntry;

     public DigiPort(DriverEntry driverEntry) {
          this.driverEntry = driverEntry;
     }

     public native void pinMode(int pin, int mode);
     public native void softPwmCreate(int pin, int initialValue, int pwmRange);
     public native void digitalWrite(int pin, int value);
     public native void softPwmStop(int pin);
     public native void softPwmWrite(int pin, int value);

     public native int digitalRead(int pin);
     public native int softPwmRead(int pin);

     public native void displayInit(int channel, int pinClk, int pinDIO);
     public native void setBrightness(int channel, int brightness, boolean on);

     public native void setSegments(int channel, byte[] segments, int length, int pos);
     public native void clear(int channel);
     public native void showNumberDec(int channel, int num, boolean leading_zero, int length, int pos);
     public native void showNumberDecEx(int channel, int num, int dots, boolean leading_zero, int length, int pos);
     public native void showNumberHexEx(int channel, int num, int dots, boolean leading_zero, int length, int pos);

}
