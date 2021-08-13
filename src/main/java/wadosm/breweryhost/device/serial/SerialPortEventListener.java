package wadosm.breweryhost.device.serial;

import com.fazecast.jSerialComm.SerialPortEvent;

public interface SerialPortEventListener {
    void serialEvent(SerialPortEvent event);
}
