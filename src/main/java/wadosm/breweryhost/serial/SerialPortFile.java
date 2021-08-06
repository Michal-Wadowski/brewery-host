package wadosm.breweryhost.serial;

public interface SerialPortFile {

    void addDataListener(SerialPortEventListener listener);

    void writeBytes(byte[] data);
}
