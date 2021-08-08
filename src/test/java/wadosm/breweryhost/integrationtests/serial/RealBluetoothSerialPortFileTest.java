package wadosm.breweryhost.integrationtests.serial;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wadosm.breweryhost.serial.BluetoothSerialPortFile;


@Disabled
@SpringBootTest
class RealBluetoothSerialPortFileTest {

    @Autowired
    BluetoothSerialPortFile serialPortFile;

    @Test
    void real_bluetooth_echo_test() throws InterruptedException {
        serialPortFile.addDataListener(event -> {
            String received = new String(event.getReceivedData());
            System.out.println("received: " + received);
            serialPortFile.writeBytes(("echo: " + received).getBytes());
        });

        Thread.sleep(60 * 1000);
    }

}