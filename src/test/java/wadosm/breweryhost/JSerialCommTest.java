package wadosm.breweryhost;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wadosm.breweryhost.utils.FakeSerialPortProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Integer.min;
import static org.assertj.core.api.Assertions.assertThat;
import static wadosm.breweryhost.utils.FakeSerialPortProvider.CLIENT_PORT_DEVICE;
import static wadosm.breweryhost.utils.FakeSerialPortProvider.HOST_PORT_DEVICE;

class JSerialCommTest {

    FakeSerialPortProvider fakeSerialPortProvider = new FakeSerialPortProvider();

    @BeforeEach
    public void beforeAll() {
        fakeSerialPortProvider.createSerialPorts();
    }

    @AfterEach
    public void afterAll() {
        fakeSerialPortProvider.releaseSerialPorts();
    }

    @Test
    public void should_simple_transfer_data() throws InterruptedException {
        // given
        byte[] testData = "testData".getBytes();
        byte[] readBuffer = new byte[testData.length];

        SerialPort hostPort = SerialPort.getCommPort(HOST_PORT_DEVICE);
        SerialPort clientPort = SerialPort.getCommPort(CLIENT_PORT_DEVICE);

        hostPort.openPort();
        clientPort.openPort();

        // when
        clientPort.writeBytes(testData, testData.length);
        Thread.sleep(100);
        hostPort.readBytes(readBuffer, testData.length);

        // then
        assertThat(readBuffer).isEqualTo(testData);
    }

    @Test
    public void should_transfer_chunk_data() throws InterruptedException {

        // given
        byte[] testData = "testData\n".getBytes();
        final byte[] readBuffer = new byte[testData.length];
        AtomicBoolean dataIsReady = new AtomicBoolean(false);

        SerialPort hostPort = SerialPort.getCommPort(HOST_PORT_DEVICE);
        SerialPort clientPort = SerialPort.getCommPort(CLIENT_PORT_DEVICE);

        hostPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        hostPort.addDataListener(new CommonSerialPortMessageListener(event -> {
            byte[] dataRead = event.getReceivedData();
            System.arraycopy(dataRead, 0, readBuffer, 0, min(dataRead.length, testData.length));

            dataIsReady.set(true);
        }));

        hostPort.openPort();
        clientPort.openPort();

        // when
        clientPort.writeBytes(testData, testData.length);

        for (int i = 0; i < 100; i++) {
            if (dataIsReady.get()) {
                break;
            }
            Thread.sleep(10);
        }

        // then
        assertThat(readBuffer).isEqualTo(testData);
    }

    @Test
    public void should_transfer_chunk_data_with_begin_and_end_padding() throws InterruptedException {

        // given
        byte[] testData = "padding#testData\npadding".getBytes();
        byte[] expectedData = "testData\n".getBytes();
        final byte[] readBuffer = new byte[expectedData.length];
        AtomicBoolean dataIsReady = new AtomicBoolean(false);

        SerialPort hostPort = SerialPort.getCommPort(HOST_PORT_DEVICE);
        SerialPort clientPort = SerialPort.getCommPort(CLIENT_PORT_DEVICE);

        hostPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        hostPort.addDataListener(new CommonSerialPortMessageListener(event -> {
            byte[] dataRead = event.getReceivedData();
            int copyLength = min(dataRead.length, expectedData.length);
            System.arraycopy(dataRead, dataRead.length - expectedData.length, readBuffer, 0, copyLength);

            dataIsReady.set(true);
        }));

        hostPort.openPort();
        clientPort.openPort();

        // when
        clientPort.writeBytes(testData, testData.length);

        for (int i = 0; i < 100; i++) {
            if (dataIsReady.get()) {
                break;
            }
            Thread.sleep(10);
        }

        // then
        assertThat(readBuffer).isEqualTo(expectedData);
    }

    private interface SerialEventHandler {
        void serialEvent(SerialPortEvent event);
    }

    private static class CommonSerialPortMessageListener implements SerialPortMessageListener {

        SerialEventHandler serialEventHandler;
        byte[] delimiter;

        public CommonSerialPortMessageListener(SerialEventHandler serialEventHandler) {
            this(serialEventHandler, new byte[]{(byte) '\n'});
        }

        public CommonSerialPortMessageListener(SerialEventHandler serialEventHandler, byte[] delimiter) {
            this.serialEventHandler = serialEventHandler;
            this.delimiter = delimiter;
        }

        @Override
        public byte[] getMessageDelimiter() {
            return delimiter;
        }

        @Override
        public boolean delimiterIndicatesEndOfMessage() {
            return true;
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            serialEventHandler.serialEvent(event);
        }
    }

}