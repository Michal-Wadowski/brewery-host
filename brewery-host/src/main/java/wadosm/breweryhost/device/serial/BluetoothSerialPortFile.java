package wadosm.breweryhost.device.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class BluetoothSerialPortFile implements SerialPortFile {

    private final SerialPort serialPort;

    private final List<SerialPortEventListener> listeners = new ArrayList<>();

    public BluetoothSerialPortFile() {
        serialPort = SerialPort.getCommPort(getSerialPortDevice());

        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        serialPort.addDataListener(new CommonSerialPortMessageListener(this));
    }

    protected String getSerialPortDevice() {
        return "/dev/rfcomm0";
    }

    @Override
    public void addDataListener(SerialPortEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void writeBytes(byte[] data) {
        serialPort.writeBytes(data, data.length);
    }

    @Scheduled(fixedRate = 1000)
    public void periodicallyCheckSerial() {
        boolean portOpened = serialPort.openPort();

        if (portOpened && serialPort.bytesAvailable() > 0) {
            log.warn("Serial buffer overflow, clearing");
            byte[] buffer = new byte[serialPort.bytesAvailable()];
            serialPort.readBytes(buffer, serialPort.bytesAvailable());
            serialPort.closePort();
        }

    }

    private static class CommonSerialPortMessageListener implements SerialPortMessageListener {

        BluetoothSerialPortFile serialPortFile;

        public CommonSerialPortMessageListener(BluetoothSerialPortFile serialPortFile) {
            this.serialPortFile = serialPortFile;
        }

        @Override
        public byte[] getMessageDelimiter() {
            return new byte[]{};
        }

        @Override
        public boolean delimiterIndicatesEndOfMessage() {
            return false;
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            this.serialPortFile.serialEvent(event);
        }
    }

    private void serialEvent(SerialPortEvent event) {
        for (SerialPortEventListener listener : listeners) {
            listener.serialEvent(event);
        }
    }
}