package wadosm.breweryhost.device.externalinterface;

import wadosm.breweryhost.device.serial.SerialPortEventListener;
import wadosm.breweryhost.device.serial.SerialPortFile;

import java.util.ArrayList;
import java.util.List;

public class FakeSerialPortFile implements SerialPortFile {

    private final List<SerialPortEventListener> listeners = new ArrayList<>();

    byte[] dataWritten;

    public List<SerialPortEventListener> getListeners() {
        return listeners;
    }

    @Override
    public void addDataListener(SerialPortEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void writeBytes(byte[] data) {
        dataWritten = data;
    }
}
