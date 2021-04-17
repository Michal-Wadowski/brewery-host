package wadosm.breweryhost;

import com.fazecast.jSerialComm.SerialPortDataListener;

public interface SerialPortFile {

    boolean addDataListener(SerialPortDataListener listener);

    int writeBytes(byte[] buffer, long bytesToWrite);

}
