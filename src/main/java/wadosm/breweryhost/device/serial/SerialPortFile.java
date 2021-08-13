package wadosm.breweryhost.device.serial;

public interface SerialPortFile {

    void addDataListener(SerialPortEventListener listener);

    void writeBytes(byte[] data);
}
